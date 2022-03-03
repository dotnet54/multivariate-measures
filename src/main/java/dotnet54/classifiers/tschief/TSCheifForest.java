package dotnet54.classifiers.tschief;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import dotnet54.applications.tschief.ExperimentRunner;
import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.Classifier;
import dotnet54.classifiers.ClassifierResult;
import dotnet54.classifiers.Ensemble;
import dotnet54.classifiers.tschief.results.TSChiefForestResult;
import dotnet54.classifiers.tschief.results.TSChiefTreeResult;
import dotnet54.classifiers.tschief.splitters.boss.dev.BossTransformContainer;
import dotnet54.classifiers.tschief.splitters.st.dev.ShapeletTransformDataStore;
import dotnet54.transformers.TransformContainer;
import dotnet54.tscore.dev.DataCache;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;
import dotnet54.tscore.*;
import dotnet54.tscore.exceptions.NotImplementedException;
import dotnet54.tscore.exceptions.NotSupportedException;
import dotnet54.tscore.threading.ClassificationTaskExecutor;
import dotnet54.util.ConsoleColors;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import dotnet54.classifiers.tschief.splitters.it.InceptionTimeDataStore;
import dotnet54.classifiers.tschief.splitters.rt.RocketTreeDataStore;
import dotnet54.util.PrintUtilities;

/**
 * 
 * @author shifaz
 * @email ahmed.shifaz@monash.edu
 *
 */

public class TSCheifForest implements Serializable, Classifier, Ensemble {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1183368028217094381L;
//	protected transient ExperimentResultCollector resultCollector;
	public TSChiefOptions tsChiefOptions;
	protected TSChiefForestResult result;
	public TopKNodeSelector topKNodeSelector;
	protected int forestID;
	protected TSCheifTree trees[];
//	protected int numTrees;
//	private transient MultiThreadedTasks parallel_tasks;
	private transient ClassificationTaskExecutor taskExecutor;

	//TODO REFACTOR to use BossTransformDataStore
	protected transient HashMap<String, TransformContainer> transforms;
	protected transient DataCache forestDataCache;
	
	//DEV
	public transient ShapeletTransformDataStore stDataStore;
	public transient InceptionTimeDataStore itDataStore;
	public transient RocketTreeDataStore rtDataStore;

	protected boolean isFitted;

	public TSCheifForest(TSChiefOptions tsChiefOptions) {
		this.tsChiefOptions = tsChiefOptions;
		this.forestID = forestID;
		this.taskExecutor = new ClassificationTaskExecutor(tsChiefOptions.numThreads);
//		this.numTrees = options.getInteger("numTrees");
//		this.numTrees = tsChiefArgs.numTrees;
	}

	public void cleanup() {
		this.taskExecutor.shutdown();
	}

	@Override
	public TSChiefForestResult fit(Dataset trainData) throws Exception {
		return this.fit(trainData, null, null);
	}

	@Override
	public TSChiefForestResult fit(Dataset trainData, DatasetIndex trainIndices, DebugInfo debugInfo) throws Exception {
		result = new TSChiefForestResult(this);
		result.beforeInit();
		trees = new TSCheifTree[tsChiefOptions.numTrees];

		transforms = new HashMap<String, TransformContainer>();
		forestDataCache = new DataCache();

		if (tsChiefOptions.verbosity == 1) {
			System.out.print("Forest Level Transformations (If needed): ...\n");
		}else if (tsChiefOptions.verbosity > 1) {
			System.out.print("Before Transformations: ");
			PrintUtilities.printMemoryUsage();
		}


		//if boss splitter is enabled, precalculate AppContext.boss_num_transforms of transforms at the forest level
		if (tsChiefOptions.boss_enabled) {

			if (tsChiefOptions.bossParamSelection == TSChiefOptions.ParamSelection.PreLoadedParams) {
				tsChiefOptions.boss_preloaded_params = ExperimentRunner.loadBossParams(tsChiefOptions);
			}

			if (tsChiefOptions.bossTransformLevel == TSChiefOptions.TransformLevel.Forest) {
				BossTransformContainer boss_transform_container =  new BossTransformContainer(tsChiefOptions);
				boss_transform_container.fit(trainData);

				result.boss_transform_time = System.nanoTime() - result.startTrainTime.get();

				transforms.put("boss", boss_transform_container);

				// DEV -- check memory usage after computing the transforms

//				long memoryBossTransforms = MemoryMeasurer.measureBytes(transforms);
//				Util.logger.info("Memory footprint after boss transforms (using instrumentation): " + memoryBossTransforms);

//				if (tsChiefOptions.devMode != null){
//					ObjectGraphMeasurer.Footprint footprint = ObjectGraphMeasurer.measure(transforms);
//					Util.logger.info("Memory footprint after boss transforms: " + footprint.toString());
//				}


			}else {
				// transformation will be done per tree -> check tree.train method
			}
		}

		if (tsChiefOptions.tsf_enabled) {
//			TSFTransformContainer tsf_transform_container =  new TSFTransformContainer(AppConfig.tsf_trasformations);
//			tsf_transform_container.fit(trainData);
//			transforms.put("tsf", tsf_transform_container);
			throw new NotSupportedException();
		}

//		if (AppContext.rif_enabled) {
//			RIFTransformContainer rif_transform_container =  new RIFTransformContainer(AppContext.rif_trasformations);
//			rif_transform_container.fit(train_data);
//			transforms.put("rif", rif_transform_container);
//		}

		if (tsChiefOptions.st_enabled) {
			stDataStore = new ShapeletTransformDataStore(tsChiefOptions);
			stDataStore.initBeforeTrain(trainData);
		}

		if (tsChiefOptions.it_enabled) {
			itDataStore = new InceptionTimeDataStore(tsChiefOptions);
			itDataStore.initBeforeTrain(trainData);
		}

		if (tsChiefOptions.rt_enabled) {
			System.out.println("TODO.......................");
		}

		if (tsChiefOptions.verbosity > 1) {
			System.out.print("After Transformations: boss trasnform time = " + result.boss_transform_time /1e9 + "s, ");
			PrintUtilities.printMemoryUsage();
		}

		if (tsChiefOptions.useBestPoolOfSplittersForRoots){
			topKNodeSelector =new TopKNodeSelector(this, tsChiefOptions);
			tsChiefOptions.topKNodeSelector = topKNodeSelector; // keep a reference for easy access by other classes

			topKNodeSelector.fit(trainData, trainIndices, debugInfo);
			System.out.println("Training topK splitters finished");
		}

		result.afterInit();

		result.beforeTrain(trainData);
		ClassificationTaskExecutor.TrainingTask trainingTasks[] = new ClassificationTaskExecutor.TrainingTask[tsChiefOptions.numTrees];

		for (int i = 0; i < tsChiefOptions.numTrees; i++) {
			trees[i] = new TSCheifTree(i, this, tsChiefOptions);
			trainingTasks[i] = taskExecutor.new TrainingTask(trees[i], trainData, i);
		}

		List<ClassifierResult> trainingResults = taskExecutor.runTasks(trainingTasks);

		for (int i = 0; i < trainingResults.size(); i++) {
			result.addBaseModelResult(i, (TSChiefTreeResult) trainingResults.get(i));
		}

//		List<Callable<Integer>> training_tasks = new ArrayList<Callable<Integer>>();
//		List<Future<Integer>> training_results;

//		int trees_per_thread = (tsChiefArgs.numTrees / tsChiefArgs.numThreads) + 1;

//		for (int i = 0; i < tsChiefArgs.numThreads; i++) {
//			int end = Math.min(tsChiefArgs.numTrees, (i*trees_per_thread)+trees_per_thread);
//			Callable<Integer> training_task = parallel_tasks.new TrainingTask(trees,
//					i*trees_per_thread, end, trainData);
//			training_tasks.add(training_task);
//		}

//		training_results = parallel_tasks.getExecutor().invokeAll(training_tasks);


//		//important to catch training exceptions
//		for (int i = 0; i < training_results.size(); i++) {
//			Future<Integer> future_int = training_results.get(i);
//			try {
//				future_int.get();//TODO return tree index that finished training
//			} catch (ExecutionException ex) {
//				ex.getCause().printStackTrace();
//				throw new Exception("Error During Training...");
//			}
//		}

		if (tsChiefOptions.verbosity > 0) {
			System.out.print("\n");
			//		System.gc();
			if (tsChiefOptions.verbosity > 2) {
				System.out.print("Testing: ");
				PrintUtilities.printMemoryUsage();
			}
		}

		isFitted = true;
		result.afterTrain();
		return result;
	}


	@Override
	public TSChiefForestResult predict(Dataset testData) throws Exception {
		return this.predict(testData,null, null);
	}

	@Override
	public TSChiefForestResult predict(Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo) throws Exception {
		result.beforeTest(testData);

//		result.allocateForPredictionResults(testData);
		System.out.println(ConsoleColors.RED + "Testing" + ConsoleColors.RESET);


		//TODO -> multi threaded transforms?
		//if we need to do a forest level transformation do it here

		//end transforms

		//if boss splitter is enabled, precalculate AppContext.boss_num_transforms of transforms at the forest level
//		if (Arrays.asList(AppContext.enabled_splitters).contains(SplitterType.BossSplitter) & AppContext.boss_transform_at_forest_level) {

		//TODO not precomputing test transformations, going to do this on the fly at node level

//			BossTransformContainer boss_transform_container =  (BossTransformContainer) transforms.get("boss");
//			boss_transform_container.transform(test_data);
//		}

		if (tsChiefOptions.it_enabled) {
			itDataStore.initBeforeTest(testData);
		}


		ClassificationTaskExecutor.PredictionTask predictionTasks[] = new ClassificationTaskExecutor.PredictionTask[tsChiefOptions.numTrees];
		for (int i = 0; i < trees.length; i++) {
			predictionTasks[i] = taskExecutor.new PredictionTask(trees[i], testData, i);
		}

		List<ClassifierResult> predictionResults = taskExecutor.runTasks(predictionTasks);

//		for (int i = 0; i < predictionResults.size(); i++) {
//			result.addBaseModelResult(i, (TSChiefTreeResult) predictionResults.get(i));
//		}

//		result.

//		//allocate trees to threads, each tree will handle at least trees/thread. (the remainder is distributed across threads)
//		TIntObjectMap<List<Integer>> tree_indices_per_thread = new TIntObjectHashMap<>(tsChiefArgs.numThreads);
//		for (int i = 0; i < trees.length; i++) {
//			int thread_id = i % tsChiefArgs.numThreads;
//			tree_indices_per_thread.putIfAbsent(thread_id, new ArrayList<Integer>());
//			tree_indices_per_thread.get(thread_id).add(i);
//		}

		//TODO TEST a dd assertion here to check if trees are divided to threads correctly

//		//create tasks
//		List<Callable<Integer>> testing_tasks = new ArrayList<Callable<Integer>>();
//		for (int i = 0; i < tsChiefArgs.numThreads; i++) {
//			if (tree_indices_per_thread.get(i) != null) {	//to handle case #trees < #threads
//				Callable<Integer> testing_task = parallel_tasks.new TestingPerModelTask(this,
//						tree_indices_per_thread.get(i), testData, result);
//				testing_tasks.add(testing_task);
//			}
//		}

//		try {
//		List<Future<Integer>> test_results;
//		test_results = parallel_tasks.getExecutor().invokeAll(testing_tasks);
//		if (tsChiefArgs.verbosity > 2) {
//			System.out.println("after  -- parallel_tasks.getExecutor().invokeAll(testing_tasks): ");
//		}



//			//TODO exceptions inside invoke all is not handled here
//		} catch (InterruptedException ex) {
//			   ex.getCause().printStackTrace();
//			   throw new Exception("Error During Testing...");
//		} catch (Exception ex) {
//			   ex.getCause().printStackTrace();
//			   throw new Exception("Error During Testing...");
//		}


//		//HOTFIX this helps catch exceptions inside invoke all
//		for (int i = 0; i < test_results.size(); i++) {
//			Future<Integer> future_int = test_results.get(i);
//			try {
//				future_int.get();
////				result.correct = result.correct + future_int.get().intValue();
//			} catch (ExecutionException ex) {
//				ex.getCause().printStackTrace();
//				throw new Exception("Error During Testing...");
//			}
//		}

		//evaluate predictions
		majorityVoteEnsemble(result, testData);

		if (tsChiefOptions.verbosity > 0) {
			System.out.println();
			if (tsChiefOptions.verbosity > 2) {
				System.out.println("Testing Completed: ");
			}
		}

		result.afterTest();
		return result;
	}


	@Override
	public ClassifierResult predictProba(Dataset testData) throws Exception {
		return this.predictProba(testData, null, null);
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
		return this.score(trainData, testData, null);
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
	public <T extends Options> void setOptions(T options) {
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

	public void majorityVoteEnsemble(TSChiefForestResult forestResult, Dataset testData) throws Exception {
		int testSize = testData.size();
		Integer[] predictedQueryLabelPerTree = new Integer[trees.length];
		int[] ensemblePredictedLabels = result.getPredictedLabels();

		// we define these once and reuse them for each query
		TIntIntMap numVoteCounter = new TIntIntHashMap();
		TIntList maxVoteCounter = new TIntArrayList();

		for (int i = 0; i < testSize; i++) {
			for (int j = 0; j < trees.length; j++) {
				int[] treePredictedLabels = this.trees[j].result.getPredictedLabels();
				predictedQueryLabelPerTree[j] = treePredictedLabels[i];
			}

			Integer predictedLabel = majorityVoteQuery(predictedQueryLabelPerTree, numVoteCounter, maxVoteCounter);
			ensemblePredictedLabels[i] = predictedLabel;
			if (predictedLabel.equals(testData.getSeries(i).label())) {
				result.correct++;
			}else{
				result.errors++;
			}

		}
		
//		if (tsChiefArgs.exportTypes > 0) {
//			resultCollector.exportPredictions(tsChiefArgs.currentOutputFilePrefix + "_r" + forestID
//					+ "_eid" + tsChiefArgs.current_experiment_id + "_rid" + resultCollector.repetition_id, treeResult, predicted_labels, testData);
//		}
		
	}
	
	
	private Integer majorityVoteQuery(Integer[] predictedQueryLabelPerTree, TIntIntMap numVoteCounter, TIntList maxVoteCounter) {
		int label;
		int maxVoteCount = -1;
		int currentVoteCount = 0;

		// clear since we reuse these across function calls
		numVoteCounter.clear();
		maxVoteCounter.clear();

		for (int i = 0; i < predictedQueryLabelPerTree.length; i++) {
			label = predictedQueryLabelPerTree[i];
			numVoteCounter.adjustOrPutValue(label, 1, 1);
		}
		
		for (int key : numVoteCounter.keys()) {
			currentVoteCount = numVoteCounter.get(key);
			
			if (currentVoteCount > maxVoteCount) {
				maxVoteCount = currentVoteCount;
				maxVoteCounter.clear();
				maxVoteCounter.add(key);
			}else if (currentVoteCount == maxVoteCount) {
				maxVoteCounter.add(key);
			}
		}

		int r = tsChiefOptions.getRand().nextInt(maxVoteCounter.size());

		// collect some useful statistics
		if (maxVoteCounter.size() > 1) {
//			this.result.majorityVoteTieBreakCounter++;
		}

		return maxVoteCounter.get(r);
	}


	public HashMap<String, TransformContainer> getTransforms() {
		return transforms;
	}

	
	public TSCheifTree[] getTrees() {
		return this.trees;
	}
	
	public TSCheifTree getTree(int i) {
		return this.trees[i];
	}


	public int getForestID() {
		return forestID;
	}

	public void setForestID(int forestID) {
		this.forestID = forestID;
	}

	@Override
	public int getSize() {
		if (trees != null){
			return this.trees.length;
		}else{
			return 0;
		}
	}

	@Override
	public Classifier[] getModels() {
		return this.trees;
	}

	@Override
	public Classifier getModel(int i) {
		return this.trees[i];
	}

}
