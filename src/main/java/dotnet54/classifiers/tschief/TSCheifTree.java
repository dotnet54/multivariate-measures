package dotnet54.classifiers.tschief;

import java.util.List;
import java.util.Random;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.Classifier;
import dotnet54.classifiers.tschief.results.TSChiefTreeResult;
import dotnet54.classifiers.tschief.splitters.boss.dev.BossDataset;
import dotnet54.classifiers.tschief.splitters.boss.dev.BossTransformContainer;
import dotnet54.tscore.data.ArrayIndex;
import dotnet54.tscore.dev.DataCache;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;
import dotnet54.measures.univariate.UnivariateMeasure;
import dotnet54.classifiers.ClassifierResult;
import dotnet54.tscore.DebugInfo;
import dotnet54.tscore.Options;
import dotnet54.tscore.exceptions.NotImplementedException;
import dotnet54.classifiers.tschief.splitters.boss.BOSS;
import dotnet54.classifiers.tschief.splitters.boss.BossSplitterV1;
import dotnet54.classifiers.tschief.splitters.boss.SFA;

import dotnet54.tscore.exceptions.NotSupportedException;
import gnu.trove.map.TIntObjectMap;
import dotnet54.util.Util;

/**
 * 
 * @author shifaz
 * @email ahmed.shifaz@monash.edu
 *
 */

public class TSCheifTree implements Classifier {
	public transient TSChiefOptions tsChiefOptions;

	protected int forestID;	//TODO remove
	protected int treeID;
	protected TSChiefNode root;
	protected int numNodes = 0;
	protected int numLeaves = 0;
	protected int depth = 0;
	protected boolean isFitted;

	protected Random rand;
	protected transient TSCheifForest forest;	//NOTE
	
	public UnivariateMeasure treeDistanceMeasure; //only used if AppContext.random_dm_per_node == false

	//TODO temp keeping per tree because if we choose to do boosting this set may differ from the dataset at forest level
	// better to use indices?
	public transient Dataset treeLevelTrainingData;
	protected transient DataCache treeDataCache;
	public TSChiefTreeResult result;

	//TODO after training deallocate this
	public transient BossDataset treeLevelBossTrainData; //FIXME done at root node for all datasets, indices should match with original dataset
	public transient BossDataset treeLevelBossTestData; //FIXME done at root node for all datasets, indices should match with original dataset
	public transient BossDataset.BossParams treeLevelBossTrainingParams;	//use this to transform test dataset
	public transient SFA teeeLevelBossSFATransform;
	
//	public transient TSFTransformContainer tsf_transfomer;
//	public transient Dataset tsf_train_data;
//	public transient Dataset tsf_test_data;

//	public transient RIFTransformContainer treeLevelRiseTransform;
//	public transient Dataset treeLevelRiseTransformedTrainData;
//	public transient Dataset treeLevelRiseTransformedTestData;

	public TSCheifTree(int tree_id, TSCheifForest forest, TSChiefOptions options) {
		this.tsChiefOptions = options;
		this.forest = forest;
		this.forestID = forest.forestID;
		this.treeID = tree_id;
		this.rand = options.getRand(); //ThreadLocalRandom.current();
		treeDataCache = new DataCache();
	}

	@Override
	public ClassifierResult fit(Dataset trainData) throws Exception {
		result = new TSChiefTreeResult(this);
		result.beforeTrain(trainData);

		ArrayIndex indices = new ArrayIndex(trainData, true);
		this.fit(trainData, indices, null);

		result.afterTrain();
		isFitted = true;
		return result;
	}

	@Override
	public ClassifierResult fit(Dataset trainData, DatasetIndex trainIndices, DebugInfo debugInfo) throws Exception {
		result = new TSChiefTreeResult(this);
		result.beforeTrain(trainData);

		if (tsChiefOptions.boss_enabled & (tsChiefOptions.bossTransformLevel == TSChiefOptions.TransformLevel.Forest)) {
			//it must be done at forest level
		}else if (tsChiefOptions.boss_enabled & (tsChiefOptions.bossTransformLevel == TSChiefOptions.TransformLevel.Tree)) {
			treeLevelBossTrainData = boss_transform_at_tree_level(trainData, true);
		}else if (tsChiefOptions.boss_enabled) {
			throw new Exception("ERROR: transformation level not supported for boss");
		}

//		if (AppConfig.tsf_enabled) {
//			tsf_transfomer = new TSFTransformContainer(1);
//			tsf_transfomer.fit(trainData);
//			tsf_train_data = tsf_transfomer.transform(trainData);
//		}

//		if (AppContext.rif_enabled) {
//			rif_transfomer = new RIFTransformContainer(1);
//			rif_transfomer.fit();
//			rif_train_data = rif_transfomer.transform();
//		}

		// keeping a reference to this, useful if each tree is trained on a subset
		// also used to extract indices at node level DEV quick fix
		this.treeLevelTrainingData = trainData;

		if (tsChiefOptions.useBestPoolOfSplittersForRoots && this.treeID == -1){
			this.root = new TSChiefNode(this, null, -1, null);
		}else{
			this.root = new TSChiefNode(this, null, numNodes++, null);
		}

//		this.root.classDistributionStr = trainData.getClassDistribution().toString();		//TODO debug only - memory leak

		if (tsChiefOptions.eeRandomMeasurePerTree) {	//DM is selected once per tree
			if (trainData.isMultivariate()){
				throw new NotSupportedException("This was an experimental feature which not implemented in the multivariate version");
			}else{
				int r = tsChiefOptions.getRand().nextInt(tsChiefOptions.eeEnabledDistanceMeasures.size());
				treeDistanceMeasure =  UnivariateMeasure.createUnivariateMeasure(tsChiefOptions.eeEnabledDistanceMeasures.get(r));
			}
		}

		this.root.fit(trainData, trainIndices);
		result.afterTrain();
		isFitted = true;
		return result;
	}

	@Override
	public ClassifierResult predict(Dataset testData) throws Exception{
		return this.predict(testData, null, null);
	}

	@Override
	public ClassifierResult predict(Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo) throws Exception {
		result.beforeTest(testData);
		int testSize = testData.size();
		int[] predictedLabels = result.getPredictedLabels();
		
		//TODO do transformation of dataset here
		//if transformations are enabled perform transformation here
		//select parameters for the transformation from random sampling of CV params obtained before
		//param selection is per tree
		if (tsChiefOptions.boss_enabled & (tsChiefOptions.bossTransformLevel == TSChiefOptions.TransformLevel.Forest)) {
			//it must be done at forest level
		}else if (tsChiefOptions.boss_enabled & (tsChiefOptions.bossTransformLevel == TSChiefOptions.TransformLevel.Tree)) {
			treeLevelBossTestData = boss_transform_at_tree_level(testData, false);
		}else if (tsChiefOptions.boss_enabled){
			throw new Exception("ERROR: transformation level not supported for boss");
		}

//		if (AppConfig.tsf_enabled) {
//			tsf_test_data = tsf_transfomer.transform(test);
//		}
		
//		if (AppContext.rif_enabled) {
//			rif_test_data = rif_transfomer.transform(test);
//		}
		
		for (int i = 0; i <testSize; i++) {
			TimeSeries query = testData.getSeries(i);
			Integer predictedLabel = predictSeries(query, i);
			
			//TODO assert predictedLabel = null;
			if (predictedLabel == null) {
				throw new Exception("ERROR: possible bug detected: predicted label is null");
			}

			predictedLabels[i] = predictedLabel;
			
			//TODO object equals or == ??
			if (predictedLabel.equals(query.label())) {
				result.correct++;	//NOTE accuracy for tree
			}else{
				result.errors++;
			}
		}

		result.afterTest();
		return this.result;
	}


	private int predictSeries(TimeSeries query, int queryIndex) throws Exception {
		//transform dataset using the params selected during the training phase.

		StringBuilder sb = new StringBuilder();
//		sb.append(queryIndex + ":");

		TSChiefNode node = this.root;
		TSChiefNode prev;
		int depth = 0;
		int[] labels = tsChiefOptions.getTrainingSet().getUniqueClasses();
		int childNodeIndex = -1;

		while(node != null && !node.isLeaf()) {
			prev = node;	// not needed, saving previous only for debugging
			childNodeIndex = node.predict(null, query, queryIndex); //TODO CHECK null TEMP
//			sb.append(childNodeIndex + "-");
//			System.out.println(childNodeIndex);
//			if (node.children.get(childNodeIndex) == null) {
//				System.out.println("null child, using random choice");
//				//TODO check #class train != test
//
//				return labels[AppConfig.getRand().nextInt(labels.length)];
//			}

			node = node.children.get(childNodeIndex);
			if (node == null) {
				System.out.println("null node found: " + childNodeIndex);
				int tmp = prev.predict(null, query, queryIndex); // DEBUG
				return childNodeIndex;
			}
			depth++;
		}

//		if (node == null) {
//			System.out.println("null node found, returning random label ");
//			return labels[AppConfig.getRand().nextInt(labels.length)];
//		}else if (node.label() == null) {
//			System.out.println("null label found, returning random label");
//			return labels[AppConfig.getRand().nextInt(labels.length)];
//		}

		if (node == null) {
			Util.logger.warn("null node found, returning exemplar label " + childNodeIndex);
			return childNodeIndex;
		}else if (node.label() == null) {
			Util.logger.warn("null label found, returning exemplar label" + childNodeIndex);
			return childNodeIndex;
		}

		// leaf testDistribution
		node.testDistribution.adjustOrPutValue(query.label(), +1,1);

//		sb.append(">" + node.label());

//		System.out.println(sb.toString());

		return node.label();
	}


	@Override
	public ClassifierResult predictProba(Dataset testData) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public ClassifierResult predictProba(Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public int predict(TimeSeries query) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public int predict(int queryIndex) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public double predictProba(TimeSeries query) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public double score(Dataset trainData, Dataset testData) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public double score(Dataset trainData, Dataset testData, DebugInfo debugInfo) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public Options getOptions() {
		return this.tsChiefOptions;
	}

	@Override
	public void setOptions(Options options) {
		if (options != null && options instanceof TSChiefOptions){
			this.tsChiefOptions = (TSChiefOptions) options;
		}else{
			throw new ClassCastException();
		}
	}

	@Override
	public ClassifierResult getTrainResults() {
		return result;
	}

	@Override
	public ClassifierResult getTestResults() {
		return result;
	}

	public TSCheifForest getForest() {
		return this.forest;
	}

	public TSChiefNode getRootNode() {
		return this.root;
	}

	//TODO predict distribution
//	public int[] predict_distribution(double[] query) throws Exception {
//		Node node = this.root;
//
//		while(!node.is_leaf()) {
//			node = node.children[node.splitter.predict_by_splitter(query)];
//		}
//
//		return node.label();
//	}
//	
	public int getTreeID() {
		return treeID;
	}





	//************************************** START stats -- development/debug code



	private BossDataset.BossParams bossParamSelect(Dataset data, TSChiefOptions.ParamSelection method) throws Exception {
		BossDataset.BossParams boss_params = null;
		int[] word_len = BossTransformContainer.getBossWordLengths();

//		SplittableRandom rand = new SplittableRandom();
		Random rand = tsChiefOptions.getRand();

		if (method == TSChiefOptions.ParamSelection.Random) {


			//choose random params

			boolean norm = rand.nextBoolean();
			int w = Util.getRandomNumberInRange(10, data.length(), true);
			int l = word_len[rand.nextInt(word_len.length)];

			boss_params = new BossDataset.BossParams(norm, w, l, 4);

		}else if (method == TSChiefOptions.ParamSelection.PreLoadedParams){
			//choose best from predefined values

			List<BossDataset.BossParams> best_boss_params = tsChiefOptions.boss_preloaded_params.get(tsChiefOptions.getDatasetName());
			int r2  = rand.nextInt(best_boss_params.size()); //pick a random set of params among the best params for this datatset
			boss_params = best_boss_params.get(r2);

		}else {
			throw new Exception("Boss param selection method not supported");
		}


		this.treeLevelBossTrainingParams = boss_params;	//store to use during testing
		return boss_params;
	}

	private BossDataset boss_transform_at_tree_level(Dataset data, boolean train_sfa) throws Exception {
		//if transformations are enabled perform transformation here
		//select parameters for the transformation from random sampling of CV params obtained before
		//param selection is per tree


		//transform the dataset using the randomly picked set of params;

		TimeSeries[] samples = data.toArray();

		if (train_sfa == true) {
			bossParamSelect(data, tsChiefOptions.bossParamSelection);
			System.out.println("boss training params for tree: " + this.treeID + " " + treeLevelBossTrainingParams);

			teeeLevelBossSFATransform = new SFA(SFA.HistogramType.EQUI_DEPTH);
			teeeLevelBossSFATransform.fitWindowing(samples, treeLevelBossTrainingParams.windowLength, treeLevelBossTrainingParams.wordLength, treeLevelBossTrainingParams.alphabetSize, treeLevelBossTrainingParams.normMean, treeLevelBossTrainingParams.lower_bounding);
		}

		final int[][] words = new int[samples.length][];

		for (int i = 0; i < samples.length; i++) {
			short[][] sfaWords = teeeLevelBossSFATransform.transformWindowing(samples[i]);
			words[i] = new int[sfaWords.length];
			for (int j = 0; j < sfaWords.length; j++) {
				words[i][j] = (int) SFA.Words.createWord(sfaWords[j], treeLevelBossTrainingParams.wordLength, (byte) SFA.Words.binlog(treeLevelBossTrainingParams.alphabetSize));
			}
		}

		BOSS.BagOfPattern[] histograms = BossSplitterV1.createBagOfPattern(words, samples, treeLevelBossTrainingParams.wordLength, treeLevelBossTrainingParams.alphabetSize);


//		System.out.println(boss_dataset.toString());

//		TIntObjectMap<BagOfPattern>  class_hist =  boss_dataset.get_class_histograms();
//		System.out.println(class_hist.toString());

//		System.out.println(boss_dataset.get_sorted_class_hist(class_hist).toString());


		//		bopPerClass.put(key, bag);

		return new BossDataset(data, histograms, treeLevelBossTrainingParams);
	}
	
	public int getNumNodes() {
		int counter = countNumNodes(root);
		if (numNodes != counter) {
			Util.logger.error("Error: error in function countNumNodes! " + numNodes + " " + counter);
			return numNodes;
		}else {
			return numNodes;
		}
	}	

	public int countNumNodes(TSChiefNode node) {
		if (node.children == null) {
			return 1;
		}
		int count = 0;
		for (int key : node.children.keys()) {
			count+= countNumNodes(node.children.get(key));
		}
		return count+1;
	}
	
	public int getNumLeaves() {
		int counter = countNumLeaves(root);
		if (numLeaves != counter) {
			Util.logger.error("Error: error in function countNumLeaves! " + numLeaves + " " + counter);
			return numLeaves;
		}else {
			return numLeaves;
		}
	}	
	
	public int countNumLeaves(TSChiefNode node) {
		if (node.children == null) {
			return 1;
		}
		int count = 0;
		for (int key : node.children.keys()) {
			count+= countNumLeaves(node.children.get(key));
		}
		return count;
	}
	
	public int getNumInternalNodes() {
		int counter = countNumLeaves(root);
		int numInternalNodes = numNodes - numLeaves;
		if (numInternalNodes != counter) {
			Util.logger.error("Error: error in function countNumInternalNodes! " + numInternalNodes + " " + counter);
			return numInternalNodes;
		}else {
			return numInternalNodes;
		}
	}
	
	public int countNumInternalNodes(TSChiefNode node) {
		if (node.children == null) {
			return 0;
		}
		int count = 0 ;
		for (int key : node.children.keys()) {
			count+= countNumInternalNodes(node.children.get(key));
		}
		return count+1;
	}
	
	public int getDepth() {
		int counter = findDepth(root);
		if (depth != counter) {
			Util.logger.error("Error: error in function getDepth! " + depth + " " + counter);
			return depth;
		}else {
			return depth;
		}
	}
	
	public int findDepth(TSChiefNode node) {
		if (node.children == null) {
			return 0;
		}
		int depth = 0;
		for (int key : node.children.keys()) {
			depth = Math.max(depth, findDepth(node.children.get(key)));
		}

		return depth+1;
	}
	
	public int findMinDepth(TSChiefNode n) {
		if (n.children == null) {
			return 0;
		}
		int depth = 0;
		for (int key : n.children.keys()) {
			depth = Math.min(depth, findMinDepth(n.children.get(key)));
			
		}
		return depth+1;
	}

	public List<TSChiefNode> getNodes(){
		// TODO recursively collect all nodes and flatten to a list
		return null;
	}

	/**
	 * This is a debug function to detect buggy splitters/experimental ideas which might produce empty splits
	 *
	 * @param splits
	 * @return
	 * @throws Exception
	 */
	public static boolean isValidSplit(TIntObjectMap<Dataset> splits) throws Exception {

		if (splits.size() < 2){
			return false;
		}

		for (int key : splits.keys()) {
			if (splits.get(key) == null || splits.get(key).size() == 0) {
				return true;
			}
		}
		return false;
	}

}
