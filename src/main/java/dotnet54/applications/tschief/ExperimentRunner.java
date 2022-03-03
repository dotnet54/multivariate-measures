package dotnet54.applications.tschief;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import dotnet54.classifiers.tschief.results.TSChiefForestResult;
import dotnet54.classifiers.tschief.splitters.boss.dev.BossDataset;
import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.exceptions.NotImplementedException;
import dotnet54.tscore.io.DataLoader;
import dotnet54.tscore.TSCore;
import dotnet54.classifiers.tschief.TSCheifForest;
import dotnet54.util.PrintUtilities;
import dotnet54.util.Util;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * 
 * @author shifaz
 * @email ahmed.shifaz@monash.edu
 *
 */

public class ExperimentRunner {

	public TSChiefOptions tsChiefOptions;
	public Dataset trainData;
	public Dataset testData;
	
//	private MultiThreadedTasks parallelTasks;
	private String outputFilePrefix;

	public ExperimentRunner(TSChiefOptions tsChiefOptions){
		this.tsChiefOptions = tsChiefOptions;
//		parallelTasks = new MultiThreadedTasks();
	}

	public void run() throws Exception {
		
		if (tsChiefOptions.verbosity > 0) {
			System.out.println("Version: " + tsChiefOptions.version + " (" + tsChiefOptions.versionTag + "); Current Time: "
					+ tsChiefOptions.application_start_timestamp.format(DateTimeFormatter.ofPattern(tsChiefOptions.TIMESTAMP_FORMAT_LONG)));
		}	
		
		//read all input files
		//we assume no header in the csv files, and that class label is in the first column, modify if necessary
		DataLoader dataLoader = new DataLoader(tsChiefOptions);
		trainData =  dataLoader.loadTrainingSet(tsChiefOptions.trainingFile);
		//its important to pass training  reference to the test  loader so that it will use same label encoder
		testData =  dataLoader.loadTestingSet(tsChiefOptions.testingFile, trainData);

		// TODO move this check to classifiers
		if (trainData.isVariableLength() || testData.isVariableLength()){
			throw new RuntimeException("Variable length datasets are not supported");
		}

		// TODO move this check to classifiers
		if (trainData.hasMissingValues() || testData.hasMissingValues()){
			throw new RuntimeException("Datasets with missing values are not supported");
		}

		//if not using default train test split
		if (! tsChiefOptions.trainTestSplit.equals("default")){
			Util.logger.info("Sampling a new train/test split..." + tsChiefOptions.trainTestSplit);
			//TODO create a new train test split
			if (tsChiefOptions.trainTestSplit.startsWith("fold")){
				tsChiefOptions.currentFold = Integer.parseInt(tsChiefOptions.trainTestSplit.substring(4,tsChiefOptions.trainTestSplit.length()));

				if (tsChiefOptions.currentFold != 0){
					// resample using indices loaded from a  file (indices of 30 folds are precomputed in these files)

//
					Dataset[] resampledDatasets = DataLoader.resampleUsingIndicesFile(trainData, testData,
							tsChiefOptions.currentFold, tsChiefOptions.archiveName, tsChiefOptions.datasetName);

					trainData = resampledDatasets[0];
					testData = resampledDatasets[1];
				}else{
					// default train/test split == fold0
				}

			}else{
				throw new NotImplementedException();
			}
		}

		validateAndInitializeRunTimeConfiguration();
		
		if (tsChiefOptions.verbosity > 0) {
			PrintUtilities.printConfiguration();
			PrintUtilities.printDatasetInfo();
		}
		

		//if we need to shuffle
		if (tsChiefOptions.shuffleData) {
			Util.logger.info("Shuffling training and test set...");
			trainData.shuffle(tsChiefOptions.randSeed);	//NOTE seed
			testData.shuffle(tsChiefOptions.randSeed);
		}

		if (tsChiefOptions.normalize) {
			Util.logger.info("Normalizing training and test set...");
			normalizeDataset(tsChiefOptions.datasetName, trainData, true, false,  "train");
			normalizeDataset(tsChiefOptions.datasetName, testData, true, false,  "test");
		}

		// TODO - build any caches that can be reused across repetitions of the same model here
		// Currently derivatives are cached inside the forest model,
		// but it can be done once here and reused for every repetition of the training/testing
//		if (tsChiefOptions.cacheDerivativeDatasets) {
//			Util.logger.info("Caching derivative transforms of the training and test set...");
//			DerivativeTransformer dt = new DerivativeTransformer();
//			tsChiefOptions.trainDataDerivative = dt.transform(trainData, null, null);
//			tsChiefOptions.testDataDerivative = dt.transform(testData, null, null);
//		}

		tsChiefOptions.currentRepetition = tsChiefOptions.repeatNo;
		for (int i = 0; i < tsChiefOptions.numRepeats; i++) {
			
			if (tsChiefOptions.verbosity > 0) {
				System.out.println("======================================== Repetition No: " + (i+1) + " (" +tsChiefOptions.datasetName+ ") ========================================");
				
//				if (tsChiefArgs.verbosity > 1) {
//					System.out.println("Threads: MaxPool=" + parallelTasks.getThreadPool().getMaximumPoolSize()
//							+ ", Active: " + parallelTasks.getThreadPool().getActiveCount()
//							+ ", Total: " + Thread.activeCount());
//				}


			}else if (tsChiefOptions.verbosity == 0 && i == 0){
				System.out.println("#,Repetition, Dataset, Accuracy, TrainTime(ms), TestTime(ms), AvgDepthPerTree");
			}

			tsChiefOptions.currentOutputPath = tsChiefOptions.outputPath + File.separator
			+ "fold" + tsChiefOptions.currentFold + File.separator
			+ "repeat" + tsChiefOptions.currentRepetition + File.separator;


			boolean resultsExists = checkIfResultsAlreadyExists(tsChiefOptions.datasetName);

			if (tsChiefOptions.overwriteResults && resultsExists){
				Util.logger.info("Results already exist: Overwriting the results for "+tsChiefOptions.datasetName
					+ ", fold: " + tsChiefOptions.currentFold + ", repeat, " + tsChiefOptions.currentRepetition);
			}else if (!tsChiefOptions.overwriteResults && resultsExists){
				Util.logger.info("Results already exist: Skipping the experiment for "+tsChiefOptions.datasetName
						+ ", fold: " + tsChiefOptions.currentFold + ", repeat, " + tsChiefOptions.currentRepetition);
				if (tsChiefOptions.numRepeats == 1){
					break;
				}else{
					tsChiefOptions.currentRepetition++;
					continue;
				}
			}

			//create model
			TSCheifForest forest;

			
			//train model
			if (tsChiefOptions.boosting) {
				forest = null; //force termination -- DEV only
//				forest = new BoostedTSChiefForest(i, parallelTasks);
//				forest.fit(trainData);
			} else {
				Options options = new Options();
//				options.setInteger("numTrees", tsChiefArgs.numTrees);
				forest = new TSCheifForest(tsChiefOptions);
				forest.fit(trainData);
			}
//			Thread.sleep(500);

			//test model
			TSChiefForestResult result = forest.predict(testData);

			//print and export resultS
			printResults(result, i, "");
			
//			if (tsChiefArgs.verbosity > 1) {
//				System.out.println("Threads: MaxPool=" + parallelTasks.getThreadPool().getMaximumPoolSize()
//						+ ", Active: " + parallelTasks.getThreadPool().getActiveCount()
//						+ ", Total: " + Thread.activeCount());
//			}

			if (tsChiefOptions.exportFiles.contains("t")){
				// export *.tree.csv files, which contains one row for each tree
				result.exportTreeCsv();
			}
			if (tsChiefOptions.exportFiles.contains("f")){
				// export *.forest.csv files, which contain a single row of aggregates across the trees
				result.exportForestCsv();
			}
			if (tsChiefOptions.exportFiles.contains("p")){
				// export *.pred.csv, which contains a row for each query and a column for prediction of each tree
				result.exportPredCsv(testData);
			}
			if (tsChiefOptions.exportFiles.contains("c")){
				// export *.config.json, which contains a json serialization of TSChiefOptions
				result.exportConfigJson();
			}
			if (tsChiefOptions.exportFiles.contains("j")){
				// export *.forest.json, which contains a json serialization of TSChiefOptions,
				// and a minimal version of serialized forest with tree hierarchies
				result.exportForestJson();
			}
			if (tsChiefOptions.exportFiles.contains("s")){
				// export *.splits.json, which is a flattened version with a json array of split
				// information for each candidate split at the node
				result.exportSplitsJson();
			}
			if (tsChiefOptions.exportFiles.contains("d")){
				// export various debug files
//				result.exportIntervalsCsv();
//				result.exportShapelets(datasetName, i, outputFilePrefix);

				result.exportAGSampleSizes();
				result.exportDims();
			}
			if (tsChiefOptions.exportFiles.contains("g")){
				// export png files drawing each tree rendered using Graphviz's Dot renderer
				try{
					result.exportTreeGraphs();
				}catch (Exception e){
					// graphviz export is still experimental
					Util.logger.warn("Error occurred while exporting graphviz files: " + e.getMessage());
					e.printStackTrace();
				}
			}


//			result.exportSimpleSummaryCSV(datasetName, i);

			if (tsChiefOptions.garbage_collect_after_each_repetition) {
				System.gc();
			}
			
			if (tsChiefOptions.verbosity > 0) {
				System.out.println();
			}

			tsChiefOptions.currentRepetition++;
		}

	}
	
	/**
	 * 
	 * Once input files have been read. validate command line arguments and adjust runtime configurations as necessary
	 * 
	 * Call this after reading input files such as training and test files
	 * @throws Exception 
	 * 
	 */

	private void validateAndInitializeRunTimeConfiguration() throws Exception {

		//TODO FIXME setup environment
		File training_file = new File(tsChiefOptions.trainingFile);

		// if it's a UCR/UEA dataset, just clean up the datasetName
		if (tsChiefOptions.archiveName != null){
			String fileName = training_file.getName();
			// ignore any extension
			tsChiefOptions.datasetName = fileName.substring(0, fileName.indexOf("."));
			// replace any special substrings, i.e. "_TRAIN" or "_TEST"
			tsChiefOptions.datasetName = tsChiefOptions.datasetName.replaceAll("_TRAIN", "");
			tsChiefOptions.datasetName = tsChiefOptions.datasetName.replaceAll("_TEST", "");
		}
		tsChiefOptions.setDatasetName(tsChiefOptions.datasetName);


		String outputFilePrefix = createOutPutFile(tsChiefOptions.datasetName);	//giving same prefix for all repeats so thats easier to group using pandas
		tsChiefOptions.currentOutputFilePrefix = outputFilePrefix;
		
		//TODO refactor
		//do initializations or param validations that can only be done after  have been loaded, these include setting params that
		//depends on the length of series, etc...
		
		if (tsChiefOptions.st_min_length <= 0  || tsChiefOptions.st_min_length > trainData.length()) {
			tsChiefOptions.st_min_length = trainData.length();
		}		
		
		if (tsChiefOptions.st_max_length <= 0 ||  tsChiefOptions.st_max_length > trainData.length()) {
			tsChiefOptions.st_max_length =  trainData.length();
		}
		
		if (tsChiefOptions.st_min_length > tsChiefOptions.st_max_length) {
			tsChiefOptions.st_min_length = tsChiefOptions.st_max_length;
		}

		tsChiefOptions.setTrainingSet(trainData);
		tsChiefOptions.setTestingSet(testData);
		updateClassDistribution(trainData, testData);

//		if (tsChiefOptions.dimensionsToUseAsString != null){
			tsChiefOptions.parseDimenstionsToUse(trainData.dimensions());
//		}

	}

	public void updateClassDistribution(Dataset train_data, Dataset test_data) {

		tsChiefOptions.class_distribution = new TIntIntHashMap();

		//create a copy //TODO can be done better?
		for (int key : train_data.getClassDistribution().keys()) {
			if (tsChiefOptions.class_distribution.containsKey(key)) {
				tsChiefOptions.class_distribution.put(key, tsChiefOptions.class_distribution.get(key) + train_data.getClassDistribution().get(key));
			}else {
				tsChiefOptions.class_distribution.put(key, train_data.getClassDistribution().get(key));
			}
		}


		//create a copy //TODO can be done better?
		for (int key : test_data.getClassDistribution().keys()) {
			if (tsChiefOptions.class_distribution.containsKey(key)) {
				tsChiefOptions.class_distribution.put(key, tsChiefOptions.class_distribution.get(key) + test_data.getClassDistribution().get(key));
			}else {
				tsChiefOptions.class_distribution.put(key, test_data.getClassDistribution().get(key));
			}
		}

		tsChiefOptions.num_classes = tsChiefOptions.class_distribution.keys().length;
		tsChiefOptions.unique_classes = tsChiefOptions.class_distribution.keys();

	}

	private String createOutPutFile(String datasetName) throws IOException {
		tsChiefOptions.experiment_timestamp = LocalDateTime.now();
		String timestamp =  tsChiefOptions.experiment_timestamp.format(DateTimeFormatter.ofPattern( tsChiefOptions.export_file_time_format));

		String fileName;
		if ( tsChiefOptions.output_subdir_per_dataset){

			fileName =  datasetName + File.separator + timestamp + "_" + datasetName;
		}else{
			fileName =  timestamp + "_" + datasetName;
		}

				
		return fileName;
	}

	/**
	 * Some simple checks to make sure that results exists in the folder and that they have some data
	 * (because a terminated experiment, by out of memory error or time limit on the cluster,
	 * might have created a file but may have incomplete output
	 *
	 * check for for forest, tree and pred files only
	 *
	 * @return
	 */
	private boolean checkIfResultsAlreadyExists(String datasetName) throws IOException {
	//	check for for forest, tree and pred files only
		Path forest = Paths.get(tsChiefOptions.currentOutputPath + "/" + datasetName + "/" + datasetName + ".forest.csv");
		if (Files.notExists(forest) || Files.size(forest) == 0){
			return false;
		}

		Path tree = Paths.get(tsChiefOptions.currentOutputPath + "/" + datasetName + "/" + datasetName + ".tree.csv");
		if (Files.notExists(tree) || Files.size(tree) == 0){
			return false;
		}

		// TODO check if numTrees in forest file matches num rows in trees file

		Path pred = Paths.get(tsChiefOptions.currentOutputPath + "/" + datasetName + "/" + datasetName + ".pred.csv");
		if (Files.notExists(pred) || Files.size(pred) == 0){
			return false;
		}

		return true;
	}

	public static int countLines(File aFile) throws IOException {
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new FileReader(aFile));
			while ((reader.readLine()) != null);
			return reader.getLineNumber();
		} catch (Exception ex) {
			return -1;
		} finally {
			if(reader != null)
				reader.close();
		}
	}

	public static void normalizeDataset(String datasetName, Dataset dataset, boolean perSeries,
										boolean verbose, String suffix){


		System.out.println("zNormalizing: " + datasetName + "_" + suffix);
		double meanBeforeNormalize = dataset.getMean();
		double stdBeforeNormalize = dataset.getStdv();
		//test mean and std per series per dimension
		double[][] meansBeforeNorm = dataset.getMeanPerSeriesPerDimension();
		double[][] stdBeforeNorm = dataset.getStdvPerSeriesPerDimension();
		double meanMPDBN = Arrays.stream(meansBeforeNorm).flatMapToDouble(Arrays::stream).average().getAsDouble();
		double meanSPDBN = Arrays.stream(stdBeforeNorm).flatMapToDouble(Arrays::stream).average().getAsDouble();
		if (Double.isNaN(meanMPDBN)){
			throw new RuntimeException("WARNING: terminating for safety: meanMPDBN = " + meanMPDBN);
		}
		if (Double.isNaN(meanMPDBN)){
			throw new RuntimeException("WARNING: terminating for safety: meanSPDBN = " + meanMPDBN);
		}
		if(verbose) {
			System.out.println("mean of mean per dim per series before normalization: " + meanMPDBN);
			System.out.println("mean of std per dim per series before normalization: " + meanSPDBN);
		}

		dataset.zNormalize(true);
		double meanAfterNormalize = dataset.getMean();
		double stdAfterNormalize = dataset.getStdv();
//        if (Double.isNaN(meanAfterNormalize)){
//            throw new RuntimeException("WARNING: terminating for safety: meanAfterNormalize = " + meanAfterNormalize);
//        }
//        if (Double.isNaN(stdAfterNormalize)){
//            throw new RuntimeException("WARNING: terminating for safety: stdAfterNormalize = " + stdAfterNormalize);
//        }
		if(verbose){
			System.out.println("mean: before " + meanBeforeNormalize + " after " + meanAfterNormalize);
			System.out.println("std: before " + stdBeforeNormalize + " after " + stdAfterNormalize);
		}

		//test mean and std per series per dimension
		double[][] meansAfterNorm = dataset.getMeanPerSeriesPerDimension();
		double[][] stdAfterNorm = dataset.getStdvPerSeriesPerDimension();
		double meanMPDAN = Arrays.stream(meansAfterNorm).flatMapToDouble(Arrays::stream).average().getAsDouble();
		double meanSPDAN = Arrays.stream(stdAfterNorm).flatMapToDouble(Arrays::stream).average().getAsDouble();
		if (Double.isNaN(meanMPDAN)){
			throw new RuntimeException("WARNING: terminating for safety: meanMPDAN = " + meanMPDAN);
		}
		if (Double.isNaN(meanSPDAN)){
			throw new RuntimeException("WARNING: terminating for safety: meanSPDAN = " + meanSPDAN);
		}
		if(verbose){
			System.out.println("mean of mean per dim per series (should be close to 0): " + meanMPDAN);
			System.out.println("mean of std per dim per series (should be close to 1): " + meanSPDAN);
		}
	}

	public void printResults(TSChiefForestResult result, int experiment_id, String prefix) {
//		System.out.println(prefix+ "-----------------Experiment No: "
//				+ experiment_id + " (" +datasetName+ "), Forest No: "
//				+ (this.forest_id) +"  -----------------");

		if (tsChiefOptions.verbosity > 0) {
			String time_duration = DurationFormatUtils.formatDuration((long) ( result.train_time_ns / tsChiefOptions.NANOSEC_TO_MILLISEC), "H:m:s.SSS");
			System.out.format("%sTraining Time: %fms (%s)\n",prefix,  result.train_time_ns/ tsChiefOptions.NANOSEC_TO_MILLISEC, time_duration);
			time_duration = DurationFormatUtils.formatDuration((long) ( result.test_time_ns / tsChiefOptions.NANOSEC_TO_MILLISEC), "H:m:s.SSS");
			System.out.format("%sPrediction Time: %fms (%s)\n",prefix,  result.test_time_ns / tsChiefOptions.NANOSEC_TO_MILLISEC, time_duration);


			System.out.format("%sCorrect(TP+TN): %d vs Incorrect(FP+FN): %d\n",prefix,   result.correct,  result.errors);
			System.out.println(prefix+"Accuracy: " + result.accuracy);
			System.out.println(prefix+"Error Rate: "+ result.error_rate);
		}




		//this is just the same info in a single line, used to grep from output and save to a csv, use the #: marker to find the line easily

		//the prefix REPEAT# is added to this comma separated line easily use grep from command line to filter outputs to a csv file
		//just a quick method to filter important info while in command line

		String pre = "REPEAT#," + (experiment_id+1) +" ,";
		System.out.print(pre + tsChiefOptions.datasetName);
		System.out.print(", " +  result.accuracy);
		System.out.print(", " +  result.train_time_ns / tsChiefOptions.NANOSEC_TO_MILLISEC + " ms") ;
		System.out.print(", " +  result.test_time_ns / tsChiefOptions.NANOSEC_TO_MILLISEC + " ms");
		System.out.print(", " +  result.depth + "\u00B1(" + result.depth_std + ")");
		System.out.println();
	}

	//NOTE this experiment is set up for UCR datasets -- it will work for any dataset if params are stored in the same format
	public static HashMap<String, List<BossDataset.BossParams>> loadBossParams(TSChiefOptions tsChiefOptions) throws Exception{
		HashMap<String, List<BossDataset.BossParams>> boss_params = new HashMap<>();

		System.out.println("Loading precalculated boss params from: " + tsChiefOptions.bossParamsFiles);

		File folder = new File(tsChiefOptions.bossParamsFiles);
		File[] listOfFiles = folder.listFiles();

		String[] datasets = TSCore.getAllDatasets();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				String dataset = findMatch(file.getName(), datasets);

				if (dataset != null) {
					List<BossDataset.BossParams> param_list = loadBossParamFile(file);

					boss_params.put(dataset, param_list);

				}else {
					throw new Exception("Failed to load boss params from the file: " + file.getName());
				}
			}
		}


		return boss_params;
	}

	public static List<BossDataset.BossParams> loadBossParamFile(File file) throws FileNotFoundException {
		List<BossDataset.BossParams> param_list = new ArrayList<>();

		Scanner s = new Scanner(file);
		while (s.hasNext()){
			String line = s.next();
			String[] words = line.split(",");

			boolean norm = Boolean.parseBoolean(words[1]);
			int w = Integer.parseInt(words[2]);
			int l = Integer.parseInt(words[3]);

			BossDataset.BossParams params = new BossDataset.BossParams(norm, w, l, 4);

			param_list.add(params);
		}
		s.close();

		return param_list;
	}

	public static String findMatch(String fileName, String[] datasets) {
		for (String string : datasets) {

			//HOTFIX

			String temp = fileName.toLowerCase();
			temp = temp.substring(0, temp.indexOf("-"));

			if (temp.equals(string.toLowerCase())) {
				return string;
			}
		}
		return null;
	}


}
