package dotnet54.classifiers.tschief.splitters.rise;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.classifiers.tschief.splitters.dev.FastRandomTreeSplitterV3;
import dotnet54.tscore.data.*;
import dotnet54.tscore.exceptions.MultivariateDataNotSupportedException;
import dotnet54.tscore.exceptions.NotImplementedException;
import dotnet54.util.Util;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.math3.util.Pair;
import org.jtransforms.fft.DoubleFFT_1D;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class RISESplitterV2 extends NodeSplitter{

	public final int MAX_ITERATIONS = 1000;

	public TSChiefOptions.RiseFeatures filterType;
	public TSChiefOptions.IntervalSectionMethod intervalSamplingMethod;
//	public FastRandomTreeSplitterV1 binarySplitter;
	public FastRandomTreeSplitterV3 binarySplitter;
	public transient DoubleFFT_1D fft; // TODO BUG issue with topk clone
	public int numFeatures;
	public int[] interval = new int[3]; // start, end, length
	public int minIntervalLength;
	public int maxLag;
	protected Random rand;

	public RISESplitterV2(TSChiefNode node){
		super(node);
		splitterType = SplitterType.RIFSplitterV2;
		splitterClass = getClassName();
		if (node!=null){
			this.node.result.rif_count++;
			this.rand = options.getRand();
		}else{
			rand = new Random();
		}
		this.intervalSamplingMethod = TSChiefOptions.IntervalSectionMethod.StartThenEnd;

		//	pick a random filter by default
		TSChiefOptions.RiseFeatures[] filters = options.riseEnabledTransforms.toArray(new TSChiefOptions.RiseFeatures[0]);
		this.filterType = filters[rand.nextInt(filters.length)];

	}

	public RISESplitterV2 clone(){
		// TODO CHECK AGAIN
		RISESplitterV2 deepCopy = this.options.jsonHelper.gson.fromJson(this.options.jsonHelper.gson.toJson(this), RISESplitterV2.class);

		// handle transient fields
		deepCopy.node = this.node;
		deepCopy.options = this.options;

		// handle transient field fft
		deepCopy.fft = new DoubleFFT_1D(interval[2]);;

		// handle random -> create a new RNG
		deepCopy.binarySplitter.rand = options.getRand();

		return deepCopy;
	}

	@Override
	public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();

		if (filterType.equals(TSChiefOptions.RiseFeatures.ACF)) {
			this.node.result.rif_acf_count++;
		}else if (filterType.equals(TSChiefOptions.RiseFeatures.PACF)) {
			this.node.result.rif_pacf_count++;
		}else if (filterType.equals(TSChiefOptions.RiseFeatures.ARMA)) {
			this.node.result.rif_arma_count++;
		}else if (filterType.equals(TSChiefOptions.RiseFeatures.PS)) {
			this.node.result.rif_ps_count++;
		}else if (filterType.equals(TSChiefOptions.RiseFeatures.DFT)) {
			this.node.result.rif_dft_count++;
		}else{
			throw new IllegalArgumentException("Unsupported rise filter type");
		}

		int length  = nodeTrainData.length();
			
		int i = 0;
		while (interval[2] < numFeatures && i < MAX_ITERATIONS){
			if (intervalSamplingMethod.equals(TSChiefOptions.IntervalSectionMethod.StartThenEnd)){
				interval = Util.getRandomIntervalSamplingLengthFirst(rand, options.rise_min_interval, length, length);
			}else{
				throw new NotImplementedException();
			}
			i++;
		}
		
		fft = new DoubleFFT_1D(interval[2]);

		// TODO check maxLag
//		maxLag=(interval[2])/4;
//		if(maxLag>100)
//			maxLag=100;
//		if(maxLag< options.rise_min_interval)
//			maxLag=(interval[2]);
		this.maxLag = interval[2];

		Dataset fullTrainSet = options.getTrainingSet();
		//TODO already set by the caller?
		trainIndices.setDataset(fullTrainSet);
		
//		binarySplitter = new FastRandomTreeSplitterV1(rand);
		binarySplitter = new FastRandomTreeSplitterV3(rand);
		Dataset transformedNodeTrainData = transform(nodeTrainData);
		this.node.result.num_intervals++; // assumes each transform uses 1 interval

		if (options.rise_feature_selection == TSChiefOptions.FeatureSelectionMethod.ConstantInt) {
			binarySplitter.setNumFeatures(Math.min(transformedNodeTrainData.length(), numFeatures));
		}

		NodeSplitterResult result = binarySplitter.fitByIndices(transformedNodeTrainData, trainIndices);

		// TODO handle nulls better
		if (result == null){
			return null;
		}
		result.splitter = this;
		if (result.splitIndices == null) {
			return null; //cant find a sensible split point for this data.
		}



		//TODO slow -- optimize this
		for (int k : result.splitIndices.keys()) {
			TIntArrayList ind = result.splitIndices.get(k);
			Dataset temp = new MTSDataset(ind.size());
			for (i = 0; i < ind.size(); i++) {
				temp.add(fullTrainSet.getSeries(ind.get(i)));
			}
			result.splits.putIfAbsent(k, temp);
		}


		if (TSChiefNode.has_empty_split(result.splits)) {
//			throw new Exception("empty splits found! check for bugs");
		}

		// TODO may fail for empty splits - add a check
		result.weightedGini = node.weighted_gini(nodeTrainData.size(), result.splits);
		weightedGini = result.weightedGini;
		this.node.weightedGiniPerSplitter.add(new Pair<>(this.getClassName(), weightedGini));
//		this.node.meanWeightedGini.increment(weightedGini);
//		this.node.sdtvWeightedGini.increment(weightedGini);

		//time is measured separately for split function from train function as it can be called separately -- to prevent double counting
		this.node.tree.result.rif_time += (System.nanoTime() - startTime);
		this.isFitted = true;
		return result;
	}

	@Override
	public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		// TODO dont redo this if calling from fit
		Dataset transformedTrainData = transform(nodeTrainData);

		NodeSplitterResult result = binarySplitter.splitByIndices(transformedTrainData, trainIndices);
		result.splitter = this;

		Dataset fullTrainSet = options.getTrainingSet();
		for (int k : result.splitIndices.keys()) {
			TIntArrayList ind = result.splitIndices.get(k);
			Dataset temp = new MTSDataset(ind.size());
			for (int i = 0; i < ind.size(); i++) {
				temp.add(fullTrainSet.getSeries(ind.get(i)));
			}
			result.splits.putIfAbsent(k, temp);
		}

		result.weightedGini = node.weighted_gini(nodeTrainData.size(), result.splits);
		weightedGini = result.weightedGini;
		this.node.weightedGiniPerSplitter.add(new Pair<>(this.getClassName(), weightedGini));
//		this.node.meanWeightedGini.increment(weightedGini);
//		this.node.sdtvWeightedGini.increment(weightedGini);

		//time is measured separately for split function from train function as it can be called separately -- to prevent double counting 
		this.node.tree.result.rif_time += (System.nanoTime() - startTime);
		return result;
	}
	
	@Override
	public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
		TimeSeries transformed_query;

		if(options.rise_transform_level == TSChiefOptions.TransformLevel.Node) {
			transformed_query = transformSeries(query);
		}else {
//			transformed_query = this.node.tree.treeLevelRiseTransformedTestData.getSeries(queryIndex);
			throw new NotImplementedException();
		}		
				
		return binarySplitter.predict(transformed_query, testData, queryIndex);
	}

	@Override
	public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public NodeSplitterResult splitByIndices(Dataset allTrainData, DatasetIndex trainIndices) throws Exception {
		throw new NotImplementedException();
	}

	public double gini(List<Integer> labels) {

		Map<Integer, Integer> class_map = new HashMap<Integer, Integer>();
		Integer key;
		
		for (int i = 0; i < labels.size(); i++) {
			key = labels.get(i);
			
			if (class_map.containsKey(key)) {
				class_map.put(key, class_map.get(key) + 1);
			}else {
				class_map.put(key, 1);
			}
		}
		
		double sum = 0;
		double p;
		int total_size = labels.size();
		
		for (Entry<Integer, Integer> entry: class_map.entrySet()) {
			p = (double)entry.getValue() / total_size;
			sum += p * p;
		}
		
		return 1 - sum;
	}

	private Dataset transform(Dataset train) throws Exception {

		if (train.isMultivariate()){
			throw new MultivariateDataNotSupportedException();
		}
		int datsetSize = train.size();
		Dataset output = new MTSDataset(datsetSize);

		for (int s = 0; s < datsetSize; s++) {
			//TODO using only dimension 1
			double[] series = train.getSeries(s).data()[0];

			//shifaz - check if this copy is necessary? can modify convert function to work on a given range
			double[] tmp = new double[interval[2]];
			System.arraycopy(series, interval[0], tmp, 0, interval[2]);
			double[] newseries = convert(tmp, filterType, maxLag);
			TimeSeries ts = new MTimeSeries(newseries, train.getClass(s));
//            ts.original_series = train.getSeries(s);
//            ts.transformed_series = true;
			output.add(ts);
		}

		return output;
	}

	private TimeSeries transformSeries(TimeSeries series) {
		//NOTE using only dimension 1
		double[] tmp = new double[interval[2]];
		System.arraycopy(series.data()[0], interval[0], tmp, 0, interval[2]);
		double[] newData = convert(tmp, filterType, maxLag);
		TimeSeries ts = new MTimeSeries(newData, series.label());
		return ts;
	}

	private double[] convert(double[] data, TSChiefOptions.RiseFeatures filter, int maxLag) {
		double[] out = null;

		if (filter == TSChiefOptions.RiseFeatures.ACF) {
			out = ACF.transformArray(data, maxLag);

			for (int i = 0; i < out.length; i++) {
				if (out[i] == Double.NaN) {
					out[i] = 0;
				}
			}

		}else if (filter == TSChiefOptions.RiseFeatures.PACF) {

			out = PACF.transformArray(data, maxLag);

			for (int i = 0; i < out.length; i++) {
				if (out[i] == Double.NaN) {
					out[i] = 0;
				}
			}

		}else if (filter == TSChiefOptions.RiseFeatures.ARMA) {

			double[] acf = ACF.transformArray(data, maxLag);
			double[] pacf = PACF.transformArray(acf, maxLag);
			double[] arma = ARMA.transformArray(data, acf, pacf, maxLag, false);

			int len =  arma.length;

			out = new double[len];

			System.arraycopy(arma, 0, out, 0, arma.length);

			for (int i = 0; i < out.length; i++) {
				if (out[i] == Double.NaN) {
					out[i] = 0;
				}
			}

		}else if (filter == TSChiefOptions.RiseFeatures.ACF_PACF_ARMA) {

			double[] acf = ACF.transformArray(data, maxLag);
			double[] pacf = PACF.transformArray(acf, maxLag);
			double[] arma = ARMA.transformArray(data, acf, pacf, maxLag, false);

			int len = acf.length + pacf.length + arma.length;

			out = new double[len];

			System.arraycopy(acf, 0, out, 0, acf.length);
			System.arraycopy(pacf, 0, out, acf.length, pacf.length);
			System.arraycopy(arma, 0, out, acf.length + pacf.length, arma.length);

			for (int i = 0; i < out.length; i++) {
				if (out[i] == Double.NaN) {
					out[i] = 0;
				}
			}

		}else if (filter == TSChiefOptions.RiseFeatures.DFT) {

			out = transformSpectral(data, TSChiefOptions.RiseFeatures.DFT);

		}else if (filter == TSChiefOptions.RiseFeatures.PS) {

			out = transformSpectral(data, TSChiefOptions.RiseFeatures.PS);

		}else if (filter == TSChiefOptions.RiseFeatures.ACF_PACF_ARMA_PS_combined) {

			double[] acf = ACF.transformArray(data, maxLag);
			double[] pacf = PACF.transformArray(acf, maxLag);
			double[] arma = ARMA.transformArray(data, acf, pacf, maxLag, false);

			double[] spectral = transformSpectral(data, TSChiefOptions.RiseFeatures.PS);

			int len = acf.length + pacf.length + arma.length + spectral.length;

			out = new double[numFeatures * 4];	//TODO check

			//new changes in v1.1.17 adding num_attribs_per_component
			System.arraycopy(acf, 0, out, 0, Math.min(numFeatures, acf.length));
			System.arraycopy(pacf, 0, out, acf.length, Math.min(numFeatures,pacf.length));
			System.arraycopy(arma, 0, out, acf.length + pacf.length, Math.min(numFeatures,arma.length));
			System.arraycopy(spectral, 0, out, acf.length + pacf.length + arma.length, Math.min(numFeatures,spectral.length));

			for (int i = 0; i < out.length; i++) {
				if (out[i] == Double.NaN) {
					out[i] = 0;
				}
			}
		}else if (filter == TSChiefOptions.RiseFeatures.ACF_PACF_ARMA_DFT) {

			double[] acf = ACF.transformArray(data, maxLag);
			double[] pacf = PACF.transformArray(acf, maxLag);
			double[] arma = ARMA.transformArray(data, acf, pacf, maxLag, false);

			double[] spectral = transformSpectral(data, TSChiefOptions.RiseFeatures.DFT);

			int len = acf.length + pacf.length + arma.length + spectral.length;

			out = new double[len];

			System.arraycopy(acf, 0, out, 0, acf.length);
			System.arraycopy(pacf, 0, out, acf.length, pacf.length);
			System.arraycopy(arma, 0, out, acf.length + pacf.length, arma.length);
			System.arraycopy(spectral, 0, out, acf.length + pacf.length + arma.length, spectral.length);

			for (int i = 0; i < out.length; i++) {
				if (out[i] == Double.NaN) {
					out[i] = 0;
				}
			}
		}



		return out;
	}

	//just a temp function -- move this code to a filter
	private double[] transformSpectral(double[] input, TSChiefOptions.RiseFeatures subFilter) {
		//extract this code to a clean PS filter in future, for now, just keeping here to prevent unnecessarily creating lots of objects
		//

		if (subFilter == TSChiefOptions.RiseFeatures.DFT) {
			double[] dft = new double[2 * input.length];
			System.arraycopy(input, 0, dft, 0, input.length);
			//        this.fft.realForward(dft);
			fft.complexForward(dft);
			//        ps[1] = 0; // DC-coefficient imaginary part

			return dft;

		}else{ // use PS
			double[] dft = new double[2 * input.length];
			System.arraycopy(input, 0, dft, 0, input.length);
			//        this.fft.realForward(dft);
			fft.complexForward(dft);
			//        ps[1] = 0; // DC-coefficient imaginary part

			//TODO using PS only because HiveCOTE uses it, we could just use DFT here, right?

			double[] ps = new double[input.length];

			for(int j=0;j<input.length;j++){
				ps[j] = dft[j*2] * dft[j*2] + dft[j*2+1] * dft[j*2+1];
			}

			return ps;
		}

	}

	private static DecimalFormat df = new DecimalFormat("#.####");
	public String toString() {
		if (this.binarySplitter != null && interval != null) {
			return "RISEV2[" + this.filterType
					+ ", m=" + numFeatures
					+ ", a=" + binarySplitter.bestAttribute
					+ ", t=" + df.format(binarySplitter.bestThreshold)
					+ ", wg=" + df.format(binarySplitter.bestWGini)
					+ ", int=[" + interval[0] + "-" + interval[1] + "]"
					+ ", len=" + interval[2];
		}else {
			return "RISEV2[" + this.filterType + ",...]";
		}
		
	}

}
