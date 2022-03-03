package dotnet54.classifiers.tschief.splitters.ee;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.measures.univariate.UnivariateMeasure;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.MTSDataset;
import dotnet54.tscore.data.TimeSeries;
import dotnet54.tscore.exceptions.NotSupportedException;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.math3.util.Pair;

/**
 * 
 * @author shifaz
 * @email ahmed.shifaz@monash.edu
 *
 */

public class ElasticDistanceSplitter extends NodeSplitter {

	protected Random rand;
	protected UnivariateMeasure measure;
	protected TIntObjectMap<double[][]> exemplars;

	//just to reuse this structure -- used during testing
	private List<Integer> closestNodes = new ArrayList<Integer>();

	public ElasticDistanceSplitter(TSChiefNode node){
		super(node);
		splitterType = SplitterType.ElasticDistanceSplitter;
		splitterClass = getClassName();

		if (node!=null){
			this.node.result.ee_count++;
			this.rand = options.getRand();
		}else{
			rand = new Random();
		}
	}
	
	public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
//		TIntObjectMap<Dataset> dataPerClass = nodeTrainData.splitByClass();	//TODO can be done once at node
		TIntObjectMap<Dataset> dataPerClass;
		if (node != null && node.trainSamplePerClass != null){
			dataPerClass = node.trainSamplePerClass;
		}else{
			dataPerClass = nodeTrainData.splitByClass();
		}

		if (options.eeRandomMeasurePerTree) {
			measure = node.tree.treeDistanceMeasure;
		}else {
			int r = rand.nextInt(options.eeEnabledDistanceMeasures.size());
			measure = UnivariateMeasure.createUnivariateMeasure(options.eeEnabledDistanceMeasures.get(r));
		}

		switch (measure.measureName){
			case euc:
				this.node.result.euc_count++;
				break;
			case dtwf:
				this.node.result.dtwf_count++;
				break;
			case dtw:
				this.node.result.dtw_count++;
				break;
			case ddtwf:
				this.node.result.ddtwf_count++;
				break;
			case ddtw:
				this.node.result.ddtw_count++;
				break;
			case wdtw:
				this.node.result.wdtw_count++;
				break;
			case wddtw:
				this.node.result.wddtw_count++;
				break;
			case lcss:
				this.node.result.lcss_count++;
				break;
			case msm:
				this.node.result.msm_count++;
				break;
			case erp:
				this.node.result.erp_count++;
				break;
			case twe:
				this.node.result.twe_count++;
				break;
		}

		measure.setRandomParams(nodeTrainData, rand);
		exemplars = new TIntObjectHashMap<double[][]>(nodeTrainData.getNumClasses());

		int branch = 0;
		for (int key : dataPerClass.keys()) {
			int r = rand.nextInt(dataPerClass.get(key).size());
			exemplars.put(branch, dataPerClass.get(key).getSeries(r).data());
			branch++;
		}

		// time is measured separately for fit and split since they can be called independently
		this.node.result.ee_time += (System.nanoTime() - startTime);
		this.isFitted = true;
		return split(nodeTrainData, trainIndices);
	}

	@Override
	public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();

		NodeSplitterResult result = new NodeSplitterResult(this, nodeTrainData.getNumClasses());
		int data_size = nodeTrainData.size();
		int closest_branch;

		for (int j = 0; j < data_size; j++) {
			closest_branch = findNearestExemplar(nodeTrainData.getSeries(j).data(), measure, exemplars);

			if (! result.splits.containsKey(closest_branch)){
				// initial capacity based on class distributions, this may be an over or an underestimate, but better than 0 initial capacity
				result.splits.put(closest_branch, new MTSDataset(nodeTrainData.getClassDistribution().get(closest_branch)));
			}

			result.splits.get(closest_branch).add(nodeTrainData.getSeries(j));
		}

		result.weightedGini = node.weighted_gini(nodeTrainData.size(), result.splits);
		weightedGini = result.weightedGini;
		this.node.weightedGiniPerSplitter.add(new Pair<>(this.getClassName(), weightedGini));
//		this.node.meanWeightedGini.increment(weightedGini);
//		this.node.sdtvWeightedGini.increment(weightedGini);

		// time is measured separately for fit and split since they can be called independently
		this.node.result.ee_time += (System.nanoTime() - startTime);
		return result;
	}

	@Override
	public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
		return findNearestExemplar(query.data(), measure, exemplars);
	}

	@Override
	public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		throw new NotSupportedException();
	}

	@Override
	public NodeSplitterResult splitByIndices(Dataset allTrainData, DatasetIndex trainIndices) throws Exception {
		throw new NotSupportedException();
	}

	// TODO check and remove synchronized
	private synchronized int findNearestExemplar(
			double[][] query, UnivariateMeasure distance_measure,
			TIntObjectMap<double[][]> exemplars) throws Exception{

		closestNodes.clear();
		double dist = Double.POSITIVE_INFINITY;
		double bsf = Double.POSITIVE_INFINITY;

		long elapsedTime = 0;
		for (int key : exemplars.keys()) {
			double[][] exemplar = exemplars.get(key);

			if (options.config_skip_distance_when_exemplar_matches_query && exemplar == query) {
				return key;
			}

			long startTime = System.nanoTime();
			dist = distance_measure.distance(query[0], exemplar[0]); //TODO using only dimension 0
			long endTime = System.nanoTime();
			elapsedTime += endTime - startTime;

			if (dist < bsf) {
				bsf = dist;
				closestNodes.clear();
				closestNodes.add(key);
			}else if (dist == bsf) {
//				if (distance == min_distance) {
//					System.out.println("min distances are same " + distance + ":" + min_distance);
//				}
				bsf = dist;
				closestNodes.add(key);
			}
		}


		switch (measure.measureName){
			case euc:
				this.node.result.euc_time += elapsedTime;
				break;
			case dtwf:
				this.node.result.dtwf_time += elapsedTime;
				break;
			case dtw:
				this.node.result.dtw_time += elapsedTime;
				break;
			case ddtwf:
				this.node.result.ddtwf_time += elapsedTime;
				break;
			case ddtw:
				this.node.result.ddtw_time += elapsedTime;
				break;
			case wdtw:
				this.node.result.wdtw_time += elapsedTime;
				break;
			case wddtw:
				this.node.result.wddtw_time += elapsedTime;
				break;
			case lcss:
				this.node.result.lcss_time += elapsedTime;
				break;
			case msm:
				this.node.result.msm_time += elapsedTime;
				break;
			case erp:
				this.node.result.erp_time += elapsedTime;
				break;
			case twe:
				this.node.result.twe_time += elapsedTime;
				break;
		}

		int r = rand.nextInt(closestNodes.size());
		return closestNodes.get(r);
	}
	
	public UnivariateMeasure getSimilarityMeasure() {
		return this.measure;
	}
	
	public String toString() {
		if (measure == null) {
			return "EE[untrained]";
		}else {
			return "EE[" + measure.toString() + "]"; //,e={" + "..." + "}
		}		
	}

}
