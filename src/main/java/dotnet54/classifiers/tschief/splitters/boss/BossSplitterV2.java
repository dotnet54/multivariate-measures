package dotnet54.classifiers.tschief.splitters.boss;

import com.carrotsearch.hppc.cursors.IntIntCursor;
import dotnet54.applications.tschief.TSChiefOptions.TransformLevel;
import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.splitters.boss.dev.BossDataset;
import dotnet54.classifiers.tschief.splitters.boss.dev.BossDataset.BossParams;
import dotnet54.classifiers.tschief.splitters.boss.dev.BossTransformContainer;
import dotnet54.tscore.data.*;
import dotnet54.tscore.exceptions.NotImplementedException;
import dotnet54.tscore.exceptions.NotSupportedException;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossSplitterV2 extends NodeSplitter {

	protected Random rand;
	protected BossParams bossParams;
	protected transient TIntObjectHashMap<BOSS.BagOfPattern> exemplars;

	// TODO refactor
	// memory expensive but makes call to split from fit faster
	// set this to null at the end of every split call, next split call should fetch again
	// this does save time for first call to split from fit function
	private transient List<BOSS.BagOfPattern> nodeTrainDataBossTransformed;

	//just to reuse this structure -- used during testing
	private List<Integer> closestNodes = new ArrayList<Integer>();

	public BossSplitterV2(TSChiefNode node) {
		super(node);
		splitterType = SplitterType.BossSplitterV2;
		splitterClass = getClassName();
		if (node!=null){
			this.node.result.boss_count++;
			this.rand = options.getRand();
		}else{
			rand = new Random();
		}

	}

	@Override
	public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		int r;

		if (options.bossTransformLevel ==  TransformLevel.Forest) {
			// get forest level transformer
			BossTransformContainer transforms = ((BossTransformContainer)this.node.tree.getForest().getTransforms().get("boss"));

			// pick a random set of boss parameters
			r = rand.nextInt(transforms.boss_params.size());
			bossParams = transforms.boss_params.get(r);

			//get the full transformed dataset using the selected parameters
			BossDataset fullBossDataset = transforms.boss_datasets.get(bossParams.toString());

			// get bop objects from the dataset ~ (planned to refactor in the future, temporary bridge to get the data)
			BOSS.BagOfPattern[] fullBossDatasetBoP = fullBossDataset.getTransformed_data();;

			//fetch node data and cache them
			// NOTE memory usage, this caching is very temporary, split function should set it to null
			nodeTrainDataBossTransformed = new ArrayList<BOSS.BagOfPattern>();
			int[] indices = trainIndices.getIndices();
			for (int j = 0; j < indices.length; j++) {
				nodeTrainDataBossTransformed.add(fullBossDatasetBoP[indices[j]]);
			}

		}else if (options.bossTransformLevel ==  TransformLevel.Tree) {
			throw new NotImplementedException();
		}else{
			throw new NotImplementedException();
		}

		// TODO if using forest or tree level transforms, this can be done once at the node
		//split by class
		TIntObjectMap<List<BOSS.BagOfPattern>> bossDataPerClass = splitByClass(nodeTrainDataBossTransformed);

		//pick one random example per class
		exemplars = new TIntObjectHashMap<>();
		for (int key : bossDataPerClass.keys()) {
			r = rand.nextInt(bossDataPerClass.get(key).size());
			BOSS.BagOfPattern example = bossDataPerClass.get(key).get(r);
			exemplars.put(key, example);
		}

		//time is measured separately for split function from train function as it can be called separately -- to prevent double counting
		this.node.result.boss_time += (System.nanoTime() - startTime);
		this.isFitted = true;
		return split(nodeTrainData, trainIndices);
	}


	private TIntObjectMap<List<BOSS.BagOfPattern>> splitByClass(List<BOSS.BagOfPattern> transformed_data) {
		TIntObjectMap<List<BOSS.BagOfPattern>> split =  new TIntObjectHashMap<List<BOSS.BagOfPattern>>();
		Integer label;
		List<BOSS.BagOfPattern> class_set = null;

		for (int i = 0; i < transformed_data.size(); i++) {
			label = transformed_data.get(i).getLabel();
			if (! split.containsKey(label)) {
				class_set = new ArrayList<BOSS.BagOfPattern>();
				split.put(label, class_set);
			}
			
			split.get(label).add(transformed_data.get(i));
		}
		
		return split;
	}

	@Override
	public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		int r;
		int dataSize = nodeTrainData.size();
		NodeSplitterResult result = new NodeSplitterResult(this, nodeTrainData.getNumClasses());

		// check if we have transformed datasets cahed, if not refetch transformed data
		if (nodeTrainDataBossTransformed == null){
			if (options.bossTransformLevel ==  TransformLevel.Forest) {
				// get forest level transformer
				BossTransformContainer transforms = ((BossTransformContainer)this.node.tree.getForest().getTransforms().get("boss"));

				//get the full transformed dataset using the selected parameters
				BossDataset fullBossDataset = transforms.boss_datasets.get(bossParams.toString());

				// get bop objects from the dataset ~ (planned to refactor in the future, temporary bridge to get the data)
				BOSS.BagOfPattern[] fullBossDatasetBoP = fullBossDataset.getTransformed_data();

				//fetch node data and cache them
				// NOTE memory usage, this caching is very temporary, split function should set it to null
				nodeTrainDataBossTransformed = new ArrayList<BOSS.BagOfPattern>();
				int[] indices = trainIndices.getIndices();
				for (int j = 0; j < indices.length; j++) {
					nodeTrainDataBossTransformed.add(fullBossDatasetBoP[indices[j]]);
				}

			}else if (options.bossTransformLevel ==  TransformLevel.Tree) {
				throw new NotImplementedException();
			}else{
				throw new NotImplementedException();
			}
		}


		int closestBranch;
		for (int j = 0; j < nodeTrainDataBossTransformed.size(); j++) {
			closestBranch = findNearestExemplar(nodeTrainDataBossTransformed.get(j));

			if (! result.splits.containsKey(closestBranch)){
				// initial capacity based on class distributions, this may be an over or an underestimate, but better than 0 initial capacity
				result.splits.put(closestBranch, new MTSDataset(nodeTrainData.getClassDistribution().get(closestBranch)));
			}

			result.splits.get(closestBranch).add(nodeTrainData.getSeries(j));
		}

		result.weightedGini = node.weighted_gini(nodeTrainData.size(), result.splits);
		weightedGini = result.weightedGini;
		this.node.weightedGiniPerSplitter.add(new Pair<>(this.getClassName(), weightedGini));
//		this.node.meanWeightedGini.increment(weightedGini);
//		this.node.sdtvWeightedGini.increment(weightedGini);

		// allow freeing this memory
		nodeTrainDataBossTransformed = null;

		// time is measured separately for fit and split since they can be called independently
		this.node.result.boss_time += (System.nanoTime() - startTime);
		return result;
	}

	// TODO check and remove synchronized
	private synchronized int findNearestExemplar(BOSS.BagOfPattern query) throws Exception{

		closestNodes.clear();
		double dist = Double.POSITIVE_INFINITY;
		double bsf = Double.POSITIVE_INFINITY;

		long elapsedTime = 0;
		for (int key : exemplars.keys()) {
			BOSS.BagOfPattern exemplar = exemplars.get(key);

			if (options.config_skip_distance_when_exemplar_matches_query && exemplar == query) {
				return key;
			}

			long startTime = System.nanoTime();
			dist = BossDistance(query, exemplar); //TODO using only dimension 0
			long endTime = System.nanoTime();
			elapsedTime += endTime - startTime;

			if (dist < bsf) {
				bsf = dist;
				closestNodes.clear();
				closestNodes.add(key);
			}else if (dist == bsf) {
				bsf = dist;
				closestNodes.add(key);
			}
		}

		int r = rand.nextInt(closestNodes.size());
		return closestNodes.get(r);
	}

	@Override
	public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
		BOSS.BagOfPattern queryBoss;

		if (options.bossTransformLevel == TransformLevel.Forest) {
			// get forest level transformer
			BossTransformContainer transforms = ((BossTransformContainer)this.node.tree.getForest().getTransforms().get("boss"));

			// transform the query
			queryBoss = transforms.transform_series_using_sfa(query, transforms.sfa_transforms.get(bossParams.toString()));

		}else if (options.bossTransformLevel ==  TransformLevel.Tree) {
//			queryBoss = this.node.tree.treeLevelBossTestData.getTransformed_data()[queryIndex];
			throw new NotImplementedException();
		}else{
	//			fitUsingNodeLevelTransforms(nodeTrainData, trainIndices);
			throw new NotImplementedException();
		}

		return findNearestExemplar(queryBoss);
	}

	@Override
	public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		throw new NotSupportedException();
	}

	@Override
	public NodeSplitterResult splitByIndices(Dataset allTrainData, DatasetIndex trainIndices) throws Exception {
		throw new NotSupportedException();
	}

	protected long BossDistance(BOSS.BagOfPattern query, BOSS.BagOfPattern bop) {
		long distance = 0;
		for (IntIntCursor key : query.getBag()) {
			long diff = key.value - bop.getBag().get(key.key);
			distance += diff * diff;
		}
		return distance;
	}
	

	public String toString() {
		return "BossSplitter[bossParams:" + bossParams + "]";
	}
	
}
