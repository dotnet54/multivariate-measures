package dotnet54.classifiers.tschief.splitters.rise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.MTSDataset;
import dotnet54.tscore.data.TimeSeries;
import dotnet54.tscore.exceptions.NotImplementedException;
import gnu.trove.list.array.TIntArrayList;
import dotnet54.classifiers.tschief.splitters.RandomTreeOnIndexSplitter;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import org.apache.commons.math3.util.Pair;

public class RISESplitterV1 extends NodeSplitter{

	public int numFeatures;//number of instances(features) used to split
	public TSChiefOptions.FeatureSelectionMethod featureSelectionMethod;
	protected transient RandomTreeOnIndexSplitter binarySplitter;
	protected transient RISETransformV1 riseTransform;
	public TSChiefOptions.RiseFeatures filterType;

	public RISESplitterV1(TSChiefNode node) throws Exception {
		super(node);
		splitterType = SplitterType.RIFSplitterV1;
		splitterClass = getClassName();
		if (node!=null){
			this.node.result.rif_count++;
		}else{
			throw new NotImplementedException();
		}
	}

	@Override
	public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();

		if (filterType.equals(TSChiefOptions.RiseFeatures.ACF)) {
			this.node.tree.result.rif_acf_count++;
		}else if (filterType.equals(TSChiefOptions.RiseFeatures.PACF)) {
			this.node.tree.result.rif_pacf_count++;
		}else if (filterType.equals(TSChiefOptions.RiseFeatures.ARMA)) {
			this.node.tree.result.rif_arma_count++;
		}else if (filterType.equals(TSChiefOptions.RiseFeatures.PS)) {
			this.node.tree.result.rif_ps_count++;
		}else if (filterType.equals(TSChiefOptions.RiseFeatures.DFT)) {
			this.node.tree.result.rif_dft_count++;
		}

		if (options.rise_feature_selection == TSChiefOptions.FeatureSelectionMethod.ConstantInt) {
			binarySplitter = new RandomTreeOnIndexSplitter(node);
			binarySplitter.setFeatureSelectionMethod(TSChiefOptions.FeatureSelectionMethod.ConstantInt);
			binarySplitter.setNumFeatures(options.rise_m);
		}else {
			binarySplitter = new RandomTreeOnIndexSplitter(node);
			binarySplitter.setFeatureSelectionMethod(options.rise_feature_selection);
		}

		riseTransform = new RISETransformV1(this.filterType, options.rise_num_intervals,
		options.rif_splitters_per_node / 4, options);
		riseTransform.fit(nodeTrainData);
		Dataset transformedNodeTrainData = riseTransform.transform(nodeTrainData);

		//TODO CHECK THIS -- assumes that its already set in the constructor
		if (options.rise_feature_selection == TSChiefOptions.FeatureSelectionMethod.ConstantInt) {
			
//			int num_intervals = (int) Math.ceil((float)num_gini_per_splitter / (float)AppContext.rif_min_interval); // 2 = ceil(12 / 9)
//			int max_attribs_needed_per_rif_type = (int) Math.ceil(num_gini_per_splitter / num_intervals);
//			binary_splitter.m = Math.min(trans.length(), max_attribs_needed_per_rif_type);// 6 = 12 / 2
			
			binarySplitter.setNumFeatures(Math.min(transformedNodeTrainData.length(), this.numFeatures));// 6 = 12 / 2

			if (options.verbosity > 1) {
//				System.out.println("rif m updated to: " + binary_splitter.m);
			}
		}

		Dataset fullTrainSet = options.getTrainingSet();
		trainIndices.setDataset(fullTrainSet); //TODO already set
		NodeSplitterResult result = binarySplitter.fitByIndices(transformedNodeTrainData, trainIndices);
		if (result == null){
//			transformedNodeTrainData = riseTransform.transform(nodeTrainData);
			return null;
		}
		if (result.splitIndices == null) {
			return null; //cant find a sensible split point for this data.
		}

		//TODO slow -- optimize this
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
		
		if (TSChiefNode.has_empty_split(result.splits)) {
//			throw new Exception("empty splits found! check for bugs");
		}
		this.isFitted = true;
		return result;
	}

	@Override
	public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		Dataset transformedTrainData = riseTransform.transform(nodeTrainData);

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

		if(options.tsf_transform_level == TSChiefOptions.TransformLevel.Node) {
			transformed_query = this.riseTransform.transformSeries(query);
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
	
	public String toString() {
		if (this.riseTransform != null) {

			return "RISEV1[" + this.riseTransform.toString() +  ", M:" + numFeatures + "," + binarySplitter + "]";
		}else {
			return "RISESplitterV1[" + this.filterType + ". untrained]";
		}
		
	}

}
