package dotnet54.applications.tschief;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.beust.jcommander.*;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.converters.EnumConverter;

import dotnet54.classifiers.tschief.TopKNodeSelector;
import dotnet54.classifiers.tschief.results.JsonHelper;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.splitters.boss.dev.BossDataset;
import dotnet54.tscore.Options;
import dotnet54.tscore.data.DataStore;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.io.CsvWriter;
import dotnet54.tscore.TSCore;
import dotnet54.util.BuildUtil;
import dotnet54.util.Util;
import dotnet54.tscore.io.DataArchive;
import dotnet54.tscore.io.UCR2015;
import gnu.trove.map.TIntIntMap;
import dotnet54.measures.Measure;

import com.beust.jcommander.converters.IParameterSplitter;

/**
 * 
 * @author shifaz
 * @email ahmed.shifaz@monash.edu
 *
 */

@Parameters(separators = "=")
public class TSChiefOptions extends Options{
	
	private final long serialVersionUID = -502980220452234173L;

	public final String TIMESTAMP_FORMAT_LONG = "yyyy-MM-dd HH:mm:ss.SSS";
	public final String TIMESTAMP_FORMAT_SHORT = "HH:mm:ss.SSS";

	public final String FILENAME_TIMESTAMP_FORMAT = "yyyy-MM-dd-HHmmss-SSS";
	public final String FILENAME_TIMESTAMP_FORMAT_SHORT = "yyyyMMdd-HHmmss-SSS";


	public final double NANOSEC_TO_MILLISEC = 1e6;	//format utils function take millisec as argument
	public final double NANOSEC_TO_SEC = 1e9;	//elapsed times are calculated in nanoseconds
	public final int ONE_MB = 1048576;
	public final int MAX_ITERATIONS_RAND_INTERVAL = 10000;

	public transient static JsonHelper jsonHelper = new JsonHelper();

	public String version = "2.3.1";
	public String buildDate = "2022.1.30";
	public String versionTag = "mpf-refactor";
	public String versionLong = version + "-" + buildDate +"-"+ versionTag;
	public String releaseNotes = "ReleaseNotes.txt";  // resource file name

	//********************************************************************
	//DEVELOPMENT and TESTING AREA -- 
	public String current_experiment_id;
	public LocalDateTime experiment_timestamp; //updated for each dataset
	public LocalDateTime application_start_timestamp; //updated only once
	public String application_start_timestamp_formatted;
	public ArrayList<String> repeat_guids;
	public String export_file_prefix = "";
	public String export_file_suffix = "";
	public String export_file_time_format = "yyyyMMdd"; //yyyyMMdd-HHmmss-SSS or yyyyMMdd
//	public String export_filename_format = "%dt-%r-%D";	//not implemented
	public boolean output_subdir_per_dataset = true;
	public boolean short_file_names = true;
	public String currentOutputFilePrefix;

	public boolean warmup_java = false;
	public boolean garbage_collect_after_each_repetition = true;
	public int print_test_progress_for_each_instances = 100;

	public boolean config_majority_vote_tie_break_randomly = true;
	public boolean config_skip_distance_when_exemplar_matches_query = true;
	public boolean config_use_random_choice_when_min_distance_is_equal = true;


	public String summary_file = "summary.csv"; //temp file to append summary of results - easier to use than exported files in -out folder


	public boolean binary_split = false;


	//TODO move these to DataStore class -- to manage multiple transformations, bagging, boosting, etc..
	private transient Dataset train_data;
	private transient Dataset test_data;
	public transient Dataset trainDataDerivative;
	public transient Dataset testDataDerivative;
	public int num_classes; //in both training and test  TODO refactor
	public int[] unique_classes;//in both training and test  TODO refactor
	public TIntIntMap class_distribution; //in both training and test

	//@charlotte experiments
	public boolean boosting = false;
	public boolean gini_split = false;
	//end @charlotte experiments

	//********************************************************************
	

	/*
	 * 
	 * DEFAULT SETTINGS, these are either overridden by command line arguments or populated at during execution
	 *
	 *
	 */

	// General settings

	@Parameter
	public List<String> parameters = new ArrayList<>();

	@Parameter(names = {"-verbosity", "-vb"}, description = "Print Verbosity")
	public int verbosity = 1; // 0, 1, 2, 3

	@Parameter(names = {"-version", "-v"}, description = "Shows app version")
	public boolean showVersion = false;

	@Parameter(names = {"-python"}, description = "Path to python executable, " +
			"useful for running some post processing scripts")
	public String pythonPath = "venv/Scripts/python";

	public String[] cmdArgs;
	public String resourcePath = "resources/";   // this is a path within the jar file
	public transient DecimalFormat decimalFormat = new DecimalFormat(TSCore.DECIMAL_PRINT_FORMAT);


	// Execution environment
	public Runtime runtime = Runtime.getRuntime();

	@Parameter(names = {"-dev"}, description = "DEVELOPMENT ONLY - ignore this when using command line, used for quick, preconfigured experiments")
	public String devMode; // enables dev mode, value is the name of the preconfigured set of settings to load

	@Parameter(names = {"-numThreads", "-threads", "-t"}, description = "Number of threads")
	public int numThreads = 0;	// if 0 all available CPUs are used
	public int numAvailableCPUs = runtime.availableProcessors();
	public long maxMemory = runtime.maxMemory() / ONE_MB;

	@Parameter(names = {"-numRepeats", "-repeats"}, description = "Number of times to repeat the experiment")
	public int numRepeats = 1;

	@Parameter(names = {"-repeat", "-repeatNo"}, description = "Repeat once, but saves to the folder of the given repetition number.")
	public int repeatNo = 0;
	public int currentRepetition = 0;

	public String hostName;


	// Random numbers related

	// WARNING -- needs to verify if all classes use this random seed,
	// some old classes uses ThreadLocalRandom which does not support seed
	// currently if using -threads > 1  and boss splitter -- due to hppc library, then random seed is not supported
	@Parameter(names = {"-seed"}, description = "Random seed. Default is 0, " +
	 "if seed is set to 0 System.nanoTime is used, else the given seed is used")
	public long randSeed = 0;

	@Parameter(names = {"-useTLRandom", "-use_tlr"}, description = "Use ThreadLocalRandom", arity=1)
	public boolean useTLRandom = false; // not fully implemented

//		SplittableRandom rand = new SplittableRandom();
//		Random rand = Thread.ThreadLocalRandom();

	public Random rand;			// shared across the application


	// Input paths and settings
	@Parameter(names = {"-cachePath", "cache_path", "-cache"}, description = "Folder to save/load cached ")
	public String cachePath = "cache/";

	@Parameter(names = {"-dataPath", "-data_path", "-data"}, description = "Path to datasets", required = true)
	public String dataPath = "E://";

	@Parameter(names = {"-archive", "-a"}, description = "Name of the archive folder, " +
			"this is combined with the path to datasets")
	public String archiveName = "";

	@Parameter(names = {"-datasets", "-d"}, description = "Comma separated list of datasets, expected to be " +
			"in dataPath/archiveName/datasetName/datasetName_TRAIN.ts or so. This is based on UCR archive structure",
			required = true, arity = 1, splitter = CommaParameterSplitter.class)
	public List<String> datasets = new ArrayList<String>();

	@Parameter(names = {"-csvFileHeader", "-csv_header"}, description = "Set to true if CSV files have header", arity=1)
	public boolean csvFileHeader = false;

	@Parameter(names = {"-csvTargetColumn", "-csv_target"}, description = "Index of the target or label column in csv file." +
			"Set to -1 if its the last column. (CURRENTLY SUPPORTS ONLY 0 = first column AND -1 last column)")
	public int csvTargetColumn = 0;


	// Output paths and settings

	@Parameter(names = {"-outPath", "-out", "-o"}, description = "Base output folder name")
	public String outputPath ="out/";

	// currentOutputPath is updated for every fold and repetition to append currentFold, currentRepetition
	public String currentOutputPath = outputPath;

//	public final int EXPORT_CSV = 1; // tree.csv + pred.csv + forest.csv
//	public final int EXPORT_JSON = 2;  // forest.json
//	public final int EXPORT_GRAPHVIZ = 4;  // graphviz trees
//	public final int EXPORT_DEBUG = 8;  // st.csv + int.csv
//	public final int EXPORT_JSON_SPLITS = 16;  // splits.json

//	@Parameter(names = {"-exportFiles", "-export"}, description = "File types to export {1=csv,2=json,4-graphviz,8=debug or sum of them}",
//			splitter = ExportFileSplitter.class)
//	public List<String> exportFiles = List.of("tree.csv", "forest.csv", "pred.csv", "forest.json", "splits.json", "debug.json");

	@Parameter(names = {"-exportFiles", "-export"},
	description = "Single letter code for file types to export \n" +
	 "{t=trees.csv,f=forest.csv,p=pred.csv,c=config.json,j=forest.json,s=splits.json,g=graphviz.png,d=debug.json}")
	public String exportFiles = "tfpc";

	@Parameter(names = {"-overwriteResults", "-ow"}, description = "Skip dataset or overwrite result files in the output folder if it already exists", arity=1)
	public boolean overwriteResults = true;

	// Data related
	@Parameter(names = {"-trainingFile", "-trainFile", "-train"}, description = "Full name of training file, if given -datasets is ignored")
	public String trainingFile;

	@Parameter(names = {"-testingFile", "-testFile", "-test"}, description = "Full name of training file, if given -datasets is ignored")
	public String testingFile;

	@Parameter(names = {"-normalize", "-norm"}, description = "Normalize dataset (z-normalization per series per dimension)", arity=1)
	public boolean normalize = false;

	@Parameter(names = {"-shuffleData", "-shuffle"}, description = "Shuffle train and test data", arity=1)
	public boolean shuffleData = false;

	// supports only default split and an integer as fold no, foldNo = 0, default train/test split TODO uniform, stratified
	@Parameter(names = {"-trainTestSplit"}, description = "{default, uniform, stratified, fold(int) - eg. fold0} , default split for for UEA/UCR datasets", arity=1)
	public String trainTestSplit = "default"; // {default, uniform, stratified, fold(int)-eg. fold0}, default split for for UEA/UCR datasets

	public int currentFold = 0;

	@Parameter(names = {"-trainingSplitRatio"}, description = "By default, trainingSplitRatio = 0.7, used only if trainTestSplit != default", arity=1)
	public double trainingSplitRatio = 0.7; // used only if trainTestSplit != default

	public boolean workingWithUCRArchive = false;
	public boolean workingWithMultrivariateData = false;
	public boolean workingWithVariableLengthData = false;

	public DataArchive archive;
	public UCR2015 ucr2015;
	public DataArchive.UCR2018UTS ucr2018uts;
	public DataArchive.UCR2018MTS ucr2018mts;
	public String datasetName;
	public DataStore dataStore;

	// Forest and Tree related general settings

	@Parameter(names = {"-numTrees", "-trees"}, description = "Number of trees in the ensemble (k). Default k = 500.")
	public int numTrees = 500;

	@Parameter(names = {"-ensembleVotingScheme", "-vote"}, description = "Options {majority|prob}. Default = majority")
	public String ensembleVotingScheme = "majority"; // prob not implemented

	@Parameter(names = {"-maxDepth"}, description = "Maximum depth of the trees. Default: -1 == no limit")
	public int maxDepth = -1; // -1 no restriction

	@Parameter(names = {"-leafGiniThreshold"}, description = "Gini of the node <= leafThreshold, node is converted to a leaf.")
	public int leafGiniThreshold = 0; // only pure leaves

	@Parameter(names = {"-minNodeSize"}, description = "If current node size <= minNodeSize, then conver it to a leaf.")
	public int minNodeSize = 1; // no limit

	@Parameter(names = {"-stopTreeIfNoUsableSplits"}, arity=1, description = "Stops tree building if there is no useful way to split" +
			"the data. E.g. if  is badly labeled and gini of children does not improve from parent. If true then convert such" +
			"nodes to a zeroR model.")
	public boolean stopTreeIfNoUsableSplits = true;

	@Parameter(names = {"-candidateSelectionMethod"}, description = "candidateSelectionMethod")
	public CandidateSelectionMethod candidateSelectionMethod = CandidateSelectionMethod.constant;

	@Parameter(names = {"-numCandidateSplits", "-c", "-C"}, description = "Maximum number of candidate splits per node per type of splitter." +
			"e.g. -C=ee:5,boss:100,rise:100. Actual number of candidate splits may be less for e.g. if dataset is too short.", required = true)
	public String numCandidateSplitsAsString;

	@Parameter(names = {"-numCandidateSplitsAtRoot", "-cRoot"}, description = "total num candidate splits at the root")
	public String numCandidateSplitsAtRoot;

	// using topk splits for the roots
	@Parameter(names = {"-useBestPoolOfSplittersForRoots"}, description = "generate a pool of splitters from which root nodes are selected", arity=1)
	public boolean useBestPoolOfSplittersForRoots;
	@Parameter(names = {"-splitterPoolType"}, description = "splitterPoolType ", arity=1)
	public String splitterPoolType = "queue"; //{queue,stratified_queue,stratified_prob}
	@Parameter(names = {"-splitterPoolSize"}, description = "splitterPoolSize in percentage with respect to pool size based on numTrees * numSplitters")
	public double splitterPoolSize = 1.0;

	public transient TopKNodeSelector topKNodeSelector;

	// if approximate gini at the node

	@Parameter(names = {"-useApproxGini"}, description = "Set to true if gini should be calculated on a subsample of instances reaching the node", arity=1)
	public boolean useApproxGini = false;
	@Parameter(names = {"-agSampling"}, description="Sampling method used to approximate gini, default = stratfied, ensuring that all classes are in the sample")
	public SamplingMethod agSampling = SamplingMethod.stratified;
	@Parameter(names = {"-agPercent"}, description="If using approximate gini, percentage of instances at the node to used to approximate the gini - sample size as a percentage")
	public double agPercent = 1;
	@Parameter(names = {"-agMaxSampleSize"}, description="If using approximate gini, percentage of instances at the node to used to approximate the gini - sample size as a percentage")
	public double agMaxSampleSize = 1;
	@Parameter(names = {"-agMinNodeSize"}, description="If using approximate gini, min samples needed at the node")
	public int agMinNodeSize = 100;
	@Parameter(names = {"-agMinClassSize"}, description="If using approximate gini, min samples per classes needed", arity=1)
	public int agMinClassSize = 0;
	@Parameter(names = {"-agMaxDepth"}, description="If using approximate gini, maximum depth upto which approx gini will be used" +
	 ", If -1, then this argument is ignored", arity=1)
	public int agMaxDepth = -1;
	
	/*
	 * 
	 * These settings are related to splitters, their default values or values overridden by command line args may be adjusted during
	 * input validations. For example: if #features to use by random forest is greater than #attributes then #features is adjusted
	 * 
	 */

	public int num_splitters_per_node;
	//actual num of splitters needed
	public int num_actual_splitters_needed;

	public int randf_splitters_per_node;
	public int rotf_splitters_per_node;
	public int st_splitters_per_node;
	public int boss_splitters_per_node;
	public int tsf_splitters_per_node;
	public int it_splitters_per_node;
	public int rt_splitters_per_node;
	public int cif_splitters_per_node;

	//command line args will fill this array
	public NodeSplitter.SplitterType[] enabledSplitters;
	public boolean randf_enabled = false;
	public boolean rotf_enabled = false;
	public boolean st_enabled = false;
	public boolean boss_enabled = false;
	public boolean tsf_enabled = false;
	public boolean it_enabled = false; //inception time
	public boolean rt_enabled = false; //rocket
	public boolean cif_enabled = false;

	// temp -- move
	public int num_actual_tsf_splitters_needed;
	public int num_actual_cif_splitters_needed;

	//EE
	public boolean ee_enabled = false;
	public int ee_splitters_per_node;

	//default measures - these are overwritten by command line args, if provided
	@Parameter(names = {"-ee_measures", "-eeMeasures"},
	description = "Enabled distance measures for similarity splitters",
	splitter = CommaParameterSplitter.class)
	public List<Measure> eeEnabledDistanceMeasures = new ArrayList<>(
		Arrays.asList(
			Measure.euc,
			Measure.dtwf,
			Measure.dtw,
			Measure.ddtwf,
			Measure.ddtw,
			Measure.wdtw,
			Measure.wddtw,
			Measure.lcss,
			Measure.msm,
			Measure.erp,
			Measure.twe
		)
	);
	@Parameter(names = {"-eeMeasurePerTree"}, arity=1)
	public boolean eeRandomMeasurePerTree = false;

	// EE early abandoning and purning
	@Parameter(names = {"-eeUseLB"}, arity=1)
	public boolean eeUseLB = false; // not implemented
	@Parameter(names = {"-eeUsePrunedEA"}, arity=1)
	public boolean eeUsePrunedEA = false;



	//Multivariate EE
	@Parameter(names = {"-lpIndependent"}, description = "lpIndependent multivariate series")
	public int lpIndependent = 1;
	@Parameter(names = {"-lpDepependent"}, description = "lpIndependent for multivariate series")
	public int lpDepependent = 2;
	@Parameter(names = {"-dimDependency"}, description = "dependency method")
	public DimensionDependencyMethod dimensionDependencyMethod = DimensionDependencyMethod.random;
	@Parameter(names = {"-dimensionsToUse", "-dimensions"},
	description = "Dimensions to use, " +
	 "e.g -dimensions= {1,2,...,D} | [1:-1] | all:max | uniform:min:max | beta:alpha:beta")
	public String dimensionsToUseAsString;
	public int[] dimensionsToUse; // if choosing at forest level
	public int numDimensionSubsetsToUsePerNode = 1;
	public DimensionSelectionMethod dimensionSelectionMethod = DimensionSelectionMethod.beta;
	public int minNumDimensions = 1;
	public int maxNumDimensions = -1; //-1 => all dims
	public int betaDistributionAlpha = 1;
	public int betaDistributionBeta = 3;
//	public double dimsLB = 0; // inclusive
//	public double dimsUB = 1.0; // inclusive
//	public double dimsMinThreshold = 1;

	//BOSS
	@Parameter(names = {"-bossParamSelection"})
	public ParamSelection bossParamSelection = ParamSelection.Random;
	@Parameter(names = {"-bossParamsFiles"})
	public String bossParamsFiles = "cache/boss/"; // default path if no path is provided in cmd args. A path is needed if ParamSelection method is PreLoadedSet
	public transient HashMap<String, List<BossDataset.BossParams>> boss_preloaded_params;
	@Parameter(names = {"-bossTransformLevel"})
	public TransformLevel bossTransformLevel = TransformLevel.Forest;
	@Parameter(names = {"-bossNumTransformations"})
	public int bossNumTransformations = 1000;
	@Parameter(names = {"-bossSplitMethod"})
	public SplitMethod bossSplitMethod = SplitMethod.NearestClass;
	@Parameter(names = {"-bossMinWindowLength"})
	public int bossMinWindowLength = 10;
	@Parameter(names = {"-bossMaxWindowLength"}, description = "int > 0, 0 == length of the series")
	public int bossMaxWindowLength = 0; // assume length of the series
	@Parameter(names = {"-bossWordLengths"}, splitter = CommaParameterSplitter.class)
	public List<Integer> bossWordLengths = new ArrayList<>(Arrays.asList(6,8,10,12,14,16));
	@Parameter(names = {"-bossAlphabetSizes"}, splitter = CommaParameterSplitter.class)
	public List<Integer> bossAlphabetSizes = new ArrayList<>(Arrays.asList(4));
	@Parameter(names = {"-bossNormParams"}, splitter = CommaParameterSplitter.class)
	public List<Boolean> bossNormParams = new ArrayList<>(Arrays.asList(false, true));
	public boolean bossUseNumerosityReduction = true;
	public NodeSplitter.SplitterType bossSplitterType = NodeSplitter.SplitterType.BossSplitterV2;

	//RISE
	@Parameter(names = {"-rise_transformLevel"})
	public TransformLevel rise_transform_level = TransformLevel.Node;
	@Parameter(names = {"-rise_numFeatures"})
	public int rise_m = 0; // use all attributes, feature bagging is done by selecting random intervals
	@Parameter(names = {"-rise_featureSelection"})
	public FeatureSelectionMethod rise_feature_selection = FeatureSelectionMethod.ConstantInt;
	@Parameter(names = {"-rise_minInterval"})
	public int rise_min_interval = 16;
	@Parameter(names = {"-rise_features"})
	public RiseFeatures rise_features = RiseFeatures.ACF_PACF_ARMA_PS_separately;
	public boolean rise_same_intv_component = false; //TODO //share same random interval for each set of component at least once per num rif splitters
	@Parameter(names = {"-rise_numIntervals"})
	public int rise_num_intervals = 0; //if 0 its determined auto,
	@Parameter(names = {"-riseEnabledTransforms"}, description = "riseEnabledTransforms",
			splitter = CommaParameterSplitter.class)
	public List<RiseFeatures> riseEnabledTransforms = new ArrayList<>(
			Arrays.asList(
					RiseFeatures.ACF,
					RiseFeatures.PACF,
					RiseFeatures.ARMA,
					RiseFeatures.PS
			)
	);
	public boolean rif_enabled = false;
	public NodeSplitter.SplitterType rif_splitter_type = NodeSplitter.SplitterType.RIFSplitterV2;
	public int rif_splitters_per_node;
	public int num_actual_rif_splitters_needed_per_type;

	//RandF
	@Parameter(names = {"-rf_numFeatures"})
	public int randf_m;
	@Parameter(names = {"-rf_featureSelection"})
	public FeatureSelectionMethod randf_feature_selection = FeatureSelectionMethod.Sqrt;

	//TSF
	public int tsf_trasformations;
	@Parameter(names = {"-tsf_transformLevel"})
	public TransformLevel tsf_transform_level = TransformLevel.Node;
	@Parameter(names = {"-tsf_numFeatures"})
	public int tsf_m = 0; // use all attributes, feature bagging is done by selecting random intervals
	@Parameter(names = {"-tsf_featureSelection"})
	public FeatureSelectionMethod tsf_feature_selection = FeatureSelectionMethod.ConstantInt;
	@Parameter(names = {"-tsf_minInterval"})
	public int tsf_min_interval = 3;
	@Parameter(names = {"-tsf_numIntervals"})
	public int tsf_num_intervals = 1;	//if 0 then sqrt(length) is used

	//ShapeletTransform
	@Parameter(names = {"-st_normalize"}, arity=1)
	public boolean st_normalize = true;
	@Parameter(names = {"-st_minLengthPercent"})
	public int st_min_length_percent = -1; //DEV if negative ignore percentages -- use absolute values based on st_min_length  and st_max_length
	@Parameter(names = {"-st_maxLengthPercent"})
	public int st_max_length_percent = -1; //DEV if negative ignore percentages -- use absolute values
	@Parameter(names = {"-st_minLength"})
	public int st_min_length = 1; //if <= 0 or if > series length, this will be adjusted to series length
	@Parameter(names = {"-st_maxLength"})
	public int st_max_length = 0; //if <= 0 or if > series length, this will be adjusted to series length
	@Parameter(names = {"-st_intervalGenerationMethod"})
	public String st_interval_method = "lengthfirst"; //{swap, lengthfirst}
	@Parameter(names = {"-st_paramFiles"})
	public String st_params_files = "cache/st-5356/"; // default path if no path is provided in cmd args. --- this is for a specific experiment:  refer to ShapeletTransformDataLoader class
	@Parameter(names = {"-st_paramSelection"})
	public ParamSelection st_param_selection = ParamSelection.Random; // {"random", "preload", "preload_data"} //preload -- loads only params and transforms in memory, preload_data loads params and transformed dataset from disk
	@Parameter(names = {"-st_thresholdSelection"})
	public ThresholdSelectionMethod st_threshold_selection = ThresholdSelectionMethod.BestGini; // how do we choose split point using the cut value that gives lowest gini? randomly? using median value?
	public boolean st_per_class = false; // -- TODO DEV need to implement
	@Parameter(names = {"-st_numRandomThresholds"})
	public int st_num_rand_thresholds = 1; //if using random threadhold for -st_threshold_selection, how many random values to test
	@Parameter(names = {"-st_featureSelection"})
	public FeatureSelectionMethod st_feature_selection = FeatureSelectionMethod.ConstantInt; //default is st_num_splitters to make sure same number of gini is calculated
	
	//InceptionTime
	@Parameter(names = {"-it_numFeatures"})
	public int it_m = 0;
	@Parameter(names = {"-it_featureSelection"})
	public FeatureSelectionMethod it_feature_selection = FeatureSelectionMethod.ConstantInt;
	@Parameter(names = {"-it_transformLevel"})
	public TransformLevel it_transform_level = TransformLevel.Forest;
	@Parameter(names = {"-it_cacheDir"})
	public String it_cache_dir = "cache/inception/";

	
	//RocketTree
	@Parameter(names = {"-rt_kernels"})
	public int rt_kernels;
	@Parameter(names = {"-rt_numFeatures"})
	public int rt_num_features = 0;
	@Parameter(names = {"-rt_featureSelection"})
	public FeatureSelectionMethod rt_feature_selection = FeatureSelectionMethod.ConstantInt;
	@Parameter(names = {"-rt_transformLevel"})
	public TransformLevel rt_transform_level = TransformLevel.Forest;
	@Parameter(names = {"-rt_cacheDir"})
	public String rt_cache_dir = "cache/rocket_tree/";
	@Parameter(names = {"-rt_splitMethod"})
	public SplitMethod rt_split_method = SplitMethod.RidgeClassifier;
	@Parameter(names = {"-rt_CVAtNode"}, arity=1)
	public boolean rt_cross_validate_at_node = true;
	@Parameter(names = {"-rt_normalize"}, arity=1)
	public boolean rt_normalize = true;
	public double rt_alphas[] = {1.00000000e-03, 4.64158883e-03, 2.15443469e-02, 1.00000000e-01,
		       4.64158883e-01, 2.15443469e+00, 1.00000000e+01, 4.64158883e+01,
		       2.15443469e+02, 1.00000000e+03}; //np.logspace(-3, 3, 10)
	//distributions for
	//length
	//weights
	//bias
	//dilation
	//padding

	//CIF
	@Parameter(names = {"-cif_splits"})
	public int cif_splits;
	@Parameter(names = {"-cif_interval_selection"})
	public String cif_interval_selection;
	@Parameter(names = {"-cif_feature_sample_size"})
	public int cif_feature_sample_size;
	@Parameter(names = {"-cif_features_use"})
	public String cif_features_use;
	@Parameter(names = {"-cif_same_features_per_interval"}, arity=1)
	public boolean cif_same_features_per_interval;
	@Parameter(names = {"-cif_use_catch22"}, arity=1)
	public boolean cif_use_catch22;
	@Parameter(names = {"-cif_use_tsf"}, arity=1)
	public boolean cif_use_tsf;
	@Parameter(names = {"-cif_use_rise"}, arity=1)
	public boolean cif_use_rise;


	public TSChiefOptions(){

	}

	/**
	 * Affected options
	 *
	 * -numCandidateMethod
	 * -candidateSelectionMethod
	 * -numCandidateSplits => numCandidateSplitsAsString
	 *
	 *
	 * @throws Exception
	 */
	private void parseCandidateSplitterArgs() throws Exception {

		ArrayList<NodeSplitter.SplitterType> enabledSplitters = new ArrayList<>();
		String[] split = numCandidateSplitsAsString.split(",");

		int total = 0;
		for (int i = 0; i < split.length; i++) {
			String temp[] = split[i].split(":");

			if (temp[0].equals("ee")) {
				ee_splitters_per_node = Integer.parseInt(temp[1]);
				total += ee_splitters_per_node;
				if (ee_splitters_per_node > 0) {
					ee_enabled = true;
					enabledSplitters.add(NodeSplitter.SplitterType.ElasticDistanceSplitter);
				}
			}else if (temp[0].equals("randf")) {
				randf_splitters_per_node = Integer.parseInt(temp[1]);
				total += randf_splitters_per_node;
				if (randf_splitters_per_node > 0) {
					randf_enabled = true;
					enabledSplitters.add(NodeSplitter.SplitterType.RandomForestSplitter);
				}
			}else if (temp[0].equals("rotf")) {
				rotf_splitters_per_node = Integer.parseInt(temp[1]);
				total += rotf_splitters_per_node;
				if (rotf_splitters_per_node > 0) {
					rotf_enabled = true;
					enabledSplitters.add(NodeSplitter.SplitterType.RotationForestSplitter);
				}
			}else if (temp[0].equals("st")) {
				st_splitters_per_node = Integer.parseInt(temp[1]);
				total += st_splitters_per_node;
				if (st_splitters_per_node > 0) {
					st_enabled = true;
					enabledSplitters.add(NodeSplitter.SplitterType.ShapeletTransformSplitter);
				}
			}else if (temp[0].equals("boss1")) {
				boss_splitters_per_node = Integer.parseInt(temp[1]);
				total += boss_splitters_per_node;
				if (boss_splitters_per_node > 0) {
					boss_enabled = true;
					bossSplitterType = NodeSplitter.SplitterType.BossSplitterV1;
					enabledSplitters.add(NodeSplitter.SplitterType.BossSplitterV1);
				}
			}else if (temp[0].equals("boss2")) {
				boss_splitters_per_node = Integer.parseInt(temp[1]);
				total += boss_splitters_per_node;
				if (boss_splitters_per_node > 0) {
					boss_enabled = true;
					bossSplitterType = NodeSplitter.SplitterType.BossSplitterV2;
					enabledSplitters.add(NodeSplitter.SplitterType.BossSplitterV2);
				}
			}else if (temp[0].equals("tsf")) {
				tsf_splitters_per_node = Integer.parseInt(temp[1]);
				total += tsf_splitters_per_node;
				if (tsf_splitters_per_node > 0) {
					tsf_enabled = true;
					enabledSplitters.add(NodeSplitter.SplitterType.TSFSplitter);
				}
			}else if (temp[0].equals("rise1")) {
				rif_splitters_per_node = Integer.parseInt(temp[1]);
				total += rif_splitters_per_node;
				if (rif_splitters_per_node > 0) {
					rif_enabled = true;
					rif_splitter_type = NodeSplitter.SplitterType.RIFSplitterV1;
					enabledSplitters.add(NodeSplitter.SplitterType.RIFSplitterV1);
				}
			}else if (temp[0].equals("rise2")) {
				rif_splitters_per_node = Integer.parseInt(temp[1]);
				total += rif_splitters_per_node;
				if (rif_splitters_per_node > 0) {
					rif_enabled = true;
					rif_splitter_type = NodeSplitter.SplitterType.RIFSplitterV2;
					enabledSplitters.add(NodeSplitter.SplitterType.RIFSplitterV2);
				}
			}else if (temp[0].equals("it")) {
				it_splitters_per_node = Integer.parseInt(temp[1]);
				total += it_splitters_per_node;
				if (it_splitters_per_node > 0) {
					it_enabled = true;
					enabledSplitters.add(NodeSplitter.SplitterType.InceptionTimeSplitter);
				}
			}else if (temp[0].equals("rt")) {
				rt_splitters_per_node = Integer.parseInt(temp[1]);
				total += rt_splitters_per_node;
				if (rt_splitters_per_node > 0) {
					rt_enabled = true;
					enabledSplitters.add(NodeSplitter.SplitterType.RocketTreeSplitter);
				}
			}else if (temp[0].equals("cif")) {
				cif_splitters_per_node = Integer.parseInt(temp[1]);
				total += cif_splitters_per_node;
				if (cif_splitters_per_node > 0) {
					cif_enabled = true;
					enabledSplitters.add(NodeSplitter.SplitterType.CIFSplitter);
				}
			}else {
				throw new Exception("Error parsing args:: Unknown splitter type in: " + numCandidateSplitsAsString);
			}
		}

		num_splitters_per_node = total;
		this.enabledSplitters = enabledSplitters.toArray(new NodeSplitter.SplitterType[enabledSplitters.size()]);
	}

	private Measure[] parseDistanceMeasures(String string) throws Exception {
		String list[] = string.trim().split(",");
		Measure[] enabled_measures = new Measure[list.length];

		for (int i = 0; i < list.length; i++) {
			switch(list[i].trim()) {
				case "euc":
					enabled_measures[i] = Measure.euclidean;
				break;
				case "dtw":
					enabled_measures[i] = Measure.dtw;
				break;
				case "dtwr":
					enabled_measures[i] = Measure.dtwcv;
				break;
				case "ddtw":
					enabled_measures[i] = Measure.ddtw;
				break;
				case "ddtwr":
					enabled_measures[i] = Measure.ddtwcv;
				break;
				case "wdtw":
					enabled_measures[i] = Measure.wdtw;
				break;
				case "wddtw":
					enabled_measures[i] = Measure.wddtw;
				break;
				case "lcss":
					enabled_measures[i] = Measure.lcss;
				break;
				case "erp":
					enabled_measures[i] = Measure.erp;
				break;
				case "twe":
					enabled_measures[i] = Measure.twe;
				break;
				case "msm":
					enabled_measures[i] = Measure.msm;
				break;

				default:
					throw new Exception("Invalid Commandline Argument(Unknown Distance Measure): " + list[i]);
			}
		}

		return enabled_measures;
	}

	private List<String> parseDatasetsOption(String options) throws Exception {
		String datasets[];

		if (options.equals("all")) {
			datasets = TSCore.getAllDatasets();	//get all
		}else if ((options.startsWith("random"))){
			int num_datasets;
			options = options.replace("random", "");

			if (options.isEmpty()) {
				num_datasets = 1;
			}else {
				num_datasets = Integer.parseInt(options);
			}
			datasets = TSCore.getNRandomDatasets(num_datasets, rand);

		}else if ((options.startsWith("set"))){
			options = options.replace("set", "");

			if (options.isEmpty()) {
				throw new Exception("Invalid set id for predefined collection of datasets");
			}

			int set_id = Integer.parseInt(options);

			datasets = TSCore.getPredefinedSet(set_id);

		}else if ((options.startsWith("range"))){
			options = options.replace("range", "");
			int start = Integer.parseInt(options.substring(0, options.indexOf("to")));
			int end = Integer.parseInt(options.substring(options.indexOf("o")+1));

			datasets = TSCore.getSubsetOfDatasets(start, end);

		}else { //just assume its a list of comma separated dataset names
			datasets = Arrays.stream(options.split(",")).map(String::trim).toArray(String[]::new);;
		}

		List<String> list = Arrays.asList(datasets);
		return list;
	}

	public enum DimensionSelectionMethod{
		set,
		range,
		all,
		sqrt,
		uniform,
		two_uniform,
		beta,
		half_normal,
		normal
	}

	public enum DimensionDependencyMethod{
		independent,
		dependent,
		both,
		random,
	}

	public enum CandidateSelectionMethod{
		constant, 	// fixed int e.g. -c=ee:5,boss:100,rise:100
		constbyDepthAndNodeSize,
		weighted,	// probability of choosing is weighted e.g. -c=ee:5,boss:100,rise:100 with 5/205 chance for ee
		prob, 		// as according to the given probabilities e.g. -c=ee:0.5,boss:0.2,rise:0.3 - MUST sum to 1
		probByDepthAndNodeSize,
		probByWins,
		differentAtRoot, // experimental 1 split at root, more below
	}

	public enum StoppingCriteria{
		MaxDepth,
		NoPurityImprovementOverParent,
		PurityThreshold,
	}
	
	public enum TransformLevel{
		Forest,
		Tree,
		Node
	}
	
	//different splitter types may use different methods to split the data. some methods may not make sense for some splitters
	//while some splitters may support more than one method. -- use other options to control the behavior 
	public enum SplitMethod{
		//uses classic decision tree split method, which may use one of the SplitEvaluateMethods such as gini 
		//results in binary splits
		//eg. RandomForest splitter uses this method
		RandomTreeStyle,	
		//picks one example per class and calculate distance/similarity to exemplar to split
		//results in n'nary splits
		//eg. ElasticDistance splitter uses this method
		NearestClass,	
		
		//TODO -- work on this
		//picks two examples and finds distance to them, then uses gini to find a good split point... 
		//eg Similarity Forest uses this method -- binary splits implemented by Charlotte uses this method
		BinarySplitNearestNeighbour,
		
		RidgeClassifier,
		LogisticClassifier,
	}
	
	//TODO only gini is implemented
	public enum SplitEvaluateMethods{
		Gini,
		InformationGain,
		GainRatio
	}
	
	public enum ThresholdSelectionMethod{
		BestGini,
		Median,
		Random
	}
	
	public enum FeatureSelectionMethod{
		Sqrt,
		Log2,
		Loge,
		ConstantInt	//if FeatureSelectionMethod == ConstantInt and m < 1 or m > length,then all attributes are used
	}	
	
	public enum RiseFeatures {
		ACF,
		PACF,
		PS,
		ARMA,
		ACF_PACF_ARMA,
		ACF_PACF_ARMA_PS_separately,	//divides num_rif_splitters / 4, and creates separate rif spliters for each individual type
		ACF_PACF_ARMA_PS_combined,	
		ACF_PACF_ARMA_DFT,
		DFT,
	}	
	
	public enum ParamSelection{
		Random,
		PreLoadedParams,	//precalculated parameter values are loaded from files -- must provide a path to files in the cmd args.
		PraLoadedDataAndParams, //precalculated parameter values and transformed dataset(s) are loaded from files -- must provide a path to files in the cmd args.
		CrossValidation	//not implemented
	}

	public enum IntervalSectionMethod{
		LengthThenEnd,
		StartThenEnd,
		TwoPointsAndSwap
	}

	public enum SamplingMethod{
		uniform,
		weighted, // weighted sampling based on class distribution but not gaurenteed that all classes are present
		stratified, // proportional stratified - make sures that all classes are at least represented
	}

// no longer using the initializer --  calling initializeAppConfig() explicitly in Main Application now
//	static {
//		
//	}

	public void parseAndValidateArgs(String[] args) throws Exception {
		cmdArgs = args; //store the original command line args

		args = TSChiefOptions.removeWhiteSpaces(args);
		JCommander jcmdr = JCommander.newBuilder().addObject(this).build();
		jcmdr.setAcceptUnknownOptions(true);
		jcmdr.parse(args);
		List<String> unknownParams = jcmdr.getUnknownOptions();

		//parse command line args -- during parsing cmd args, default values in AppConfig may be overwritten
//		parseCMD(args);

		parseCandidateSplitterArgs();

//		eeEnabledDistanceMeasures = parseDistanceMeasures(eeEnabledDistanceMeasures);
//		datasets =  parseDatasetsOption(datasets);

//		if (dimensionSubsetsAsString != null){
//			dimensionSubsets = parseDimenstionsToUse(dimensionSubsetsAsString, dimensions());
//		}

		// once parsing is done validate params
		validate();
	}

	public void validate() throws Exception {
		//NOTE do these after all params have been parsed from cmd line

		//extra validations
		if (bossParamSelection == ParamSelection.PreLoadedParams && ! archiveName.equals("ucr2015")) {
			throw new Exception("Cross validated BOSS params are only available for UCR 2015 datasets");
		}

		if(num_splitters_per_node == 0) {
			throw new Exception("Number of candidate splits per node must be greater than 0. " +
					"\nUse -s command line option. \nEg. \"-s=ee:5,boss:100,rif:100\" for default TSCHIEF settings" +
					"\nOr \"-s=ee:5\" for default Proximity Forest settings");
		}

	}

	public void initialize() throws Exception {

		versionLong += ";build_at=" + BuildUtil.getBuildDate();

		//-- DO THIS AFTER parsing cmd line
		//initialize random seed 
		
		//TODO WARNING implementing this feature is not complete or it may be broken due to inconsistencies in implementation among different classes
		//NOTE many classes use ThreadLocalRandom which doesnt allow setting a seed
		// research more about this -- SplitRandom is also used by some classes
		if (randSeed == 0) {	//if seed is 0 then lets use time this is the default
			randSeed = System.nanoTime(); //overwrite and store the new seed so that it can be exported later
			rand = new Random(randSeed);
		}else {
			rand = new Random(randSeed);
		}
		
		int test1 = rand.nextInt();
		int test2 = rand.nextInt();

		//init some classes that depend on random numbers
//		Sampler.setRand(AppConfig.rand);
//		Util.setRand(rand);

		//detect runtime environment info
		collectRuntimeEnvironmentInfo();
		
		//init some experiment management  -- this is to help organize  exported from experiments
		current_experiment_id = java.util.UUID.randomUUID().toString().substring(0, 8);
		//these unique ids helps to keep the output folder structure clean
		repeat_guids = new ArrayList<>(numRepeats);
		for (int i = 0; i < numRepeats; i++) {
			repeat_guids.add(java.util.UUID.randomUUID().toString().substring(0, 8));
		}
		
		//DEBUG feature -- used only in experiments when we measure very short runtimes. 
		//-- this code helps warm up java virtual machine by loading classes early -- helps measure more accurate time in some experiments
		if (warmup_java) {
			Util.warmUpJavaRuntime();
		}

		this.ucr2015 = new UCR2015(this.dataPath + "/" + UCR2015.archiveName + "/");
		this.ucr2018uts = new DataArchive.UCR2018UTS(this.dataPath + "/" + DataArchive.UCR2018UTS.archiveName + "/");
		this.ucr2018mts = new DataArchive.UCR2018MTS(this.dataPath + "/" + DataArchive.UCR2018MTS.archiveName + "/");

// commented 15-3-2021
//		if(tsf_enabled) {
//			//TODO tested only for 1 interval per splitter. check for more intervals
////			AppContext.num_splitters_per_node -= AppContext.tsf_splitters_per_node; //override this
////			AppContext.tsf_splitters_per_node = (int) Math.ceil( (float) AppContext.tsf_splitters_per_node / ( 3 * AppContext.tsf_num_intervals));
////			AppContext.tsf_splitters_per_node = (int) ( (float) AppContext.tsf_splitters_per_node / ( 3 * AppContext.tsf_num_intervals));
////			AppContext.tsf_splitters_per_node /= 3; //override this --works only if tsf_num_intervals == 1
////			AppContext.num_splitters_per_node += AppContext.tsf_splitters_per_node; //override this
//
//
////			AppContext.num_actual_tsf_splitters_needed_per_interval = (int) Math.ceil( (float) AppContext.tsf_splitters_per_node / ( 3 * AppContext.tsf_num_intervals));
//			num_actual_tsf_splitters_needed = tsf_splitters_per_node / 3;
//
//		}

// commented 15-3-2021 logic moved to node
//		if(rif_enabled) {
//			//TODO tested only for 1 interval per splitter. check for more intervals
//
//			int num_gini_per_type = rif_splitters_per_node / 4;	// eg. if we need 12 gini per type 12 ~= 50/4
//			int extra_gini = rif_splitters_per_node % 4;
//			//assume 1 interval per splitter
//			// 2 = ceil(12 / 9) if 9 = min interval length
//			int min_splitters_needed_per_type = (int) Math.ceil((float)num_gini_per_type / (float) rise_min_interval);
//			int max_attribs_to_use_per_splitter = (int) Math.ceil(num_gini_per_type / min_splitters_needed_per_type);
//
//			num_actual_rif_splitters_needed_per_type = min_splitters_needed_per_type;
//			rise_m = max_attribs_to_use_per_splitter;
//			int approx_gini_estimated = 4 * max_attribs_to_use_per_splitter * min_splitters_needed_per_type;
////			System.out.println("RISE: approx_gini_estimated: " + approx_gini_estimated);
//
//		}

//		if (!(rise_features == RiseFeatures.ACF_PACF_ARMA_PS_combined)) {
//			num_actual_splitters_needed =
//					ee_splitters_per_node +
//							randf_splitters_per_node +
//							rotf_splitters_per_node +
//							st_splitters_per_node +
//							boss_splitters_per_node +
//							num_actual_tsf_splitters_needed +
//							num_actual_rif_splitters_needed_per_type;	//TODO works if
//		}else {
//			num_actual_splitters_needed =
//					ee_splitters_per_node +
//							randf_splitters_per_node +
//							rotf_splitters_per_node +
//							st_splitters_per_node +
//							boss_splitters_per_node +
//							num_actual_tsf_splitters_needed +
//							rif_splitters_per_node;	//TODO works if
//		}

//		if (cif_enabled){
//			//TODO
//		}

	}

	public void printVersion	(){
		System.out.println("Version: " + version);
		System.out.println("BuildDate: " + buildDate);
		System.out.println("VersionTag: " + versionTag);
		System.out.println("Release Notes: " + releaseNotes);

//		print java version info and slurm environment info here

		System.exit(0);
	}
	
	public void collectRuntimeEnvironmentInfo() throws UnknownHostException {
	
        this.hostName = InetAddress.getLocalHost().getHostName();
        this.application_start_timestamp = LocalDateTime.now();
        this.application_start_timestamp_formatted = this.application_start_timestamp.format(DateTimeFormatter.ofPattern(this.FILENAME_TIMESTAMP_FORMAT));
        
        
        //check if running on cluster and store some env variables related to cluster 
        //this is also to help organize the experiments
        
        //TODO store slurm variables such as slurm job id
        
	}
	
	public Random getRand() {
		return rand;
	}

	public void setRand(Random newRandom) {
		rand = newRandom;
	}
	
	public Dataset getTrainingSet() {
		return train_data;
	}

	public void setTrainingSet(Dataset train_data) {
		this.train_data = train_data;
	}

	public Dataset getTestingSet() {
		return test_data;
	}

	public void setTestingSet(Dataset test_data) {
		this.test_data = test_data;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}
	
	public String getVersionString(boolean pathFriendly) {
		if (pathFriendly) {
			return this.version.replace(".", "-");
		}else {
			return this.version;
		}
		
	}

	public static String[] removeWhiteSpaces(String... args){
		for (String arg : args) {
			arg = arg.replaceAll("\\s{2,}", " ");
		}
		return args;
	}

	//  usage:  {[0:-1],...,{0,1,2,3,4,-1},random:numSubsets:subsetMin:subsetMax:dimMin:dimMax,all:subsetSize}
	//          -1 => all dims, MAX_DIM
	public void parseDimenstionsToUse_v1_deprecated(int numDimensions){
		String[] splits;

		if (dimensionsToUseAsString == null){
			dimensionsToUseAsString = "all";
		}

		if (dimensionsToUseAsString.startsWith("{")){
			// eg. -dimensions={1,2,3,4,5} -dimensions={1,-1,-2}
			this.dimensionSelectionMethod = DimensionSelectionMethod.set;
			String cleanedString = dimensionsToUseAsString.replaceAll("^\\{|\\}$", "");
			splits = cleanedString.split(",");
			dimensionsToUse = new int[splits.length];
			for (int i = 0; i < splits.length; i++) {
				int dim = Integer.parseInt(splits[i]);
				if (dim > 0 && dim < numDimensions) {
					dimensionsToUse[i] = dim;
				} else if (dim < 0) {
					dimensionsToUse[i] = numDimensions + dim;    // -1 => last dimension
				} else { // dim > numDimensions
					dimensionsToUse[i] = numDimensions;
				}
			}
		}else if (dimensionsToUseAsString.startsWith("[")){
			// eg. -dimensions=[min:max:step] -dimensions=[0:5] -dimensions=[0:5:1] -dimensions=[0:-1]
			this.dimensionSelectionMethod = DimensionSelectionMethod.range;
			String cleanedString = dimensionsToUseAsString.replaceAll("^\\[|\\]$", "");
			splits = cleanedString.split(":");
			int min = Integer.parseInt(splits[0]);
			int max = Integer.parseInt(splits[1]);
			int step = (splits.length > 2) ? Integer.parseInt(splits[2]) : 1;
			if (max < 0){
				max = numDimensions + max;
			}
			dimensionsToUse = new int[max - min + 1];
			for (int i = min; i < dimensionsToUse.length; i += step) {
				dimensionsToUse[i] = i;
			}
		}else if (dimensionsToUseAsString.startsWith("all")){
			// -dimensions=all
			this.dimensionSelectionMethod = TSChiefOptions.DimensionSelectionMethod.all;
			dimensionsToUse = new int[numDimensions];
			for (int i = 0; i < dimensionsToUse.length; i++) {
				dimensionsToUse[i] = i;
			}
		}else if(dimensionsToUseAsString.startsWith("sqrt")) {
			// -dimensions=sqrt -dimensions=sqrt:min
			// Math.max(size, Math.min(minSize, numDimensions));
//			subset_size = max(math.floor(math.sqrt(num_dims)), min(min_subset_size, num_dims))
			this.dimensionSelectionMethod = DimensionSelectionMethod.sqrt;
			splits = dimensionsToUseAsString.split(":");
			int dims = (int) Math.sqrt(numDimensions);

			this.maxNumDimensions = (splits.length > 1) ? Integer.parseInt(splits[1]) : numDimensions;
			dimensionsToUse = new int[maxNumDimensions];
			for (int i = 0; i < dimensionsToUse.length; i++) {
				dimensionsToUse[i] = rand.nextInt(numDimensions);
			}

		}else if (dimensionsToUseAsString.startsWith("uniform")){
			// -dimensions=uniform:min:max -dimensions=uniform -dimensions=uniform:0:-1
			splits = dimensionsToUseAsString.split(":");
			this.dimensionSelectionMethod = DimensionSelectionMethod.uniform;
			this.minNumDimensions = (splits.length > 1) ? Integer.parseInt(splits[1]) : 1;
			this.maxNumDimensions = (splits.length > 2) ? Integer.parseInt(splits[2]) : numDimensions;
			if (minNumDimensions < 0){
				minNumDimensions = numDimensions + minNumDimensions;
			}else if (minNumDimensions > numDimensions){
				minNumDimensions = numDimensions;
			}
			if (maxNumDimensions < 0){
				maxNumDimensions = numDimensions + maxNumDimensions;
			}else if (maxNumDimensions > numDimensions){
				maxNumDimensions = numDimensions;
			}
		}else if(dimensionsToUseAsString.startsWith("beta")) {
			// -dimensions=beta:alpha:beta -dimensions=beta:1:3
			splits = dimensionsToUseAsString.split(":");
			this.dimensionSelectionMethod = DimensionSelectionMethod.beta;
			this.betaDistributionAlpha = Integer.parseInt(splits[1]);
			this.betaDistributionBeta = Integer.parseInt(splits[2]);
			if (splits.length > 3){
				if (splits[3].startsWith("sqrt")){
					// TODO defer this calculation to EE splitter class
					this.minNumDimensions = (int) Math.ceil(Math.sqrt(numDimensions));
				}else if (splits[3] == "D"){
					this.minNumDimensions = numDimensions;
				}else{
					this.minNumDimensions = Integer.parseInt(splits[3]);
				}
			}
		}else if (dimensionsToUseAsString.startsWith("two_uniform")){
			// -dimensions=uniform:min:max -dimensions=uniform -dimensions=uniform:0:-1
			splits = dimensionsToUseAsString.split(":");
			this.dimensionSelectionMethod = DimensionSelectionMethod.uniform;
			this.minNumDimensions = (splits.length > 1) ? Integer.parseInt(splits[1]) : 1;
			this.maxNumDimensions = (splits.length > 2) ? Integer.parseInt(splits[2]) : numDimensions;
			if (minNumDimensions < 0){
				minNumDimensions = numDimensions + minNumDimensions;
			}else if (minNumDimensions > numDimensions){
				minNumDimensions = numDimensions;
			}
			if (maxNumDimensions < 0){
				maxNumDimensions = numDimensions + maxNumDimensions;
			}else if (maxNumDimensions > numDimensions){
				maxNumDimensions = numDimensions;
			}
		}else if(dimensionsToUseAsString.startsWith("cos")) {
		//TODO
			// -dimensions=cos
			splits = dimensionsToUseAsString.split(":");
			this.dimensionSelectionMethod = DimensionSelectionMethod.beta;
			this.betaDistributionAlpha = Integer.parseInt(splits[1]);
			this.betaDistributionBeta = Integer.parseInt(splits[2]);
			if (splits.length > 3){
				if (splits[3].startsWith("sqrt")){
					this.minNumDimensions = (int) Math.ceil(Math.sqrt(numDimensions));
				}else{
					this.minNumDimensions = Integer.parseInt(splits[3]); // if beta gives below min, use a uniform choice
				}
			}
		}else{
			//use all dims by default if arg is invalid or not provided
			this.dimensionSelectionMethod = TSChiefOptions.DimensionSelectionMethod.all;
			dimensionsToUse = new int[numDimensions];
			for (int i = 0; i < dimensionsToUse.length; i++) {
				dimensionsToUse[i] = i;
			}
//			throw new IllegalArgumentException("Invalid syntax for command line arg: " + dimensionsToUseAsString);
		}
	}

	public void parseDimenstionsToUse(int numDimensions){
		String[] splits;

		// set a default
		if (dimensionsToUseAsString == null){
			dimensionsToUseAsString = "all";
		}

		if (dimensionsToUseAsString.startsWith("{")){
			// use a predefined set, can repeat values, negative numbers cycle in reverse
			// eg. -dimensions={1,2,3,4,5} -dimensions={1,-1,-2}
			this.dimensionSelectionMethod = DimensionSelectionMethod.set;
			String cleanedString = dimensionsToUseAsString.replaceAll("^\\{|\\}$", "");
			splits = cleanedString.split(",");
			dimensionsToUse = new int[splits.length];
			for (int i = 0; i < splits.length; i++) {
				int dim = Integer.parseInt(splits[i]);
				if (dim > 0 && dim < numDimensions) {
					dimensionsToUse[i] = dim;
				} else if (dim < 0) {
					dimensionsToUse[i] = numDimensions + dim;    // -1 => last dimension
				} else { // dim > numDimensions
					dimensionsToUse[i] = numDimensions;
				}
			}
		}else if (dimensionsToUseAsString.startsWith("[")){
			// sets a specific range of dimensions to use
			// eg. -dimensions=[min:max:step] -dimensions=[0:5] -dimensions=[0:5:1] -dimensions=[0:-1]
			this.dimensionSelectionMethod = DimensionSelectionMethod.range;
			String cleanedString = dimensionsToUseAsString.replaceAll("^\\[|\\]$", "");
			splits = cleanedString.split(":");
			int min = Integer.parseInt(splits[0]);
			int max = Integer.parseInt(splits[1]);
			int step = (splits.length > 2) ? Integer.parseInt(splits[2]) : 1;
			if (max < 0){
				max = numDimensions + max;
			}
			dimensionsToUse = new int[max - min + 1];
			for (int i = min; i < dimensionsToUse.length; i += step) {
				dimensionsToUse[i] = i;
			}
		}else if (dimensionsToUseAsString.startsWith("all")){
			// -dimensions=all
			this.dimensionSelectionMethod = TSChiefOptions.DimensionSelectionMethod.all;
			dimensionsToUse = new int[numDimensions];
			for (int i = 0; i < dimensionsToUse.length; i++) {
				dimensionsToUse[i] = i;
			}
		}else if (dimensionsToUseAsString.startsWith("uniform")){
			// -dimensions=uniform:min:max -dimensions=uniform -dimensions=uniform:0:-1
			splits = dimensionsToUseAsString.split(":");
			this.dimensionSelectionMethod = DimensionSelectionMethod.uniform;

			// if an LB is given
			this.minNumDimensions = (splits.length > 1) ? Integer.parseInt(splits[1]) : 1;
			// fix range if invalid
			if (minNumDimensions < 0){
				// negatives cycle back
				minNumDimensions = numDimensions + minNumDimensions;
			}else if (minNumDimensions > numDimensions){
				minNumDimensions = 1;
			}

			// if a UB is given
			if (splits.length > 2) {
				if (splits[2].equals("D")){
					this.maxNumDimensions = numDimensions;
				}else{
					this.maxNumDimensions = Integer.parseInt(splits[2]);
				}
			}else{
				this.maxNumDimensions = numDimensions;
			}
			// fix range if invalid
			if (maxNumDimensions < 0){
				// negatives cycle back
				maxNumDimensions = numDimensions + maxNumDimensions;
			}else if (maxNumDimensions > numDimensions){
				maxNumDimensions = numDimensions;
			}

			if (maxNumDimensions < minNumDimensions){
				throw new IllegalArgumentException("Invalid syntax for [dimensions] command line arg: " + dimensionsToUseAsString);
			}
		}else if(dimensionsToUseAsString.startsWith("beta")) {
			// -dimensions=beta:alpha:beta -dimensions=beta:1:3
			splits = dimensionsToUseAsString.split(":");
			this.dimensionSelectionMethod = DimensionSelectionMethod.beta;
			this.betaDistributionAlpha = Integer.parseInt(splits[1]);
			this.betaDistributionBeta = Integer.parseInt(splits[2]);
//			if (splits.length > 3){
//				if (splits[3].startsWith("sqrt")){
//					// CHECK if there are bound issues due to (int) or ceil
//					this.minNumDimensions = (int) Math.ceil(Math.sqrt(numDimensions));
//				}else if (splits[3].equals("D")){
//					this.minNumDimensions = numDimensions;
//				}else{
//					this.minNumDimensions = Integer.parseInt(splits[3]);
//				}
//			}
		}else if (dimensionsToUseAsString.startsWith("two_uniform")){
			// -dimensions=two_uniform:threshold // threshold == 0.5  -- its hardcoded currently
			splits = dimensionsToUseAsString.split(":");
			this.dimensionSelectionMethod = DimensionSelectionMethod.two_uniform;
		}else if(dimensionsToUseAsString.startsWith("sqrt")) {
			// -dimensions=sqrt
			// TODO -dimensions=sqrt:min --- use sqrt from min, below min use uniform
			this.dimensionSelectionMethod = DimensionSelectionMethod.sqrt;
			this.maxNumDimensions = (int) Math.ceil(Math.sqrt(numDimensions));

		}else if(dimensionsToUseAsString.startsWith("cos")) {
			// TODO DEPRECATED
			// -dimensions=cos
			splits = dimensionsToUseAsString.split(":");
			this.dimensionSelectionMethod = DimensionSelectionMethod.beta;
			this.betaDistributionAlpha = Integer.parseInt(splits[1]);
			this.betaDistributionBeta = Integer.parseInt(splits[2]);
			if (splits.length > 3){
				if (splits[3].startsWith("sqrt")){
					this.minNumDimensions = (int) Math.ceil(Math.sqrt(numDimensions));
				}else{
					this.minNumDimensions = Integer.parseInt(splits[3]); // if beta gives below min, use a uniform choice
				}
			}
		}else{
			throw new IllegalArgumentException("Invalid syntax for [dimensions] command line arg: " + dimensionsToUseAsString);
		}
	}


	// params: usage:  [list:0,1,2,...,99; range:start,end,step; random:n,id,explicit]
	//
	// paramID = -1 == default random distribution,
	// paramID = -2 == uniform distribution U(0,1)
	// paramID = -3 == normal distribution N(0,1)
	// paramID = 0-99 == paramID as in EE
	// paramID >= 100 ignored, use custom values
	public List<Integer> parseParams(String params){
		List<Integer> paramIDs = new ArrayList();
		String[] paramArgs;

		if (params.startsWith("list")){
			paramIDs.clear();
			paramArgs = params.split(":")[1].split(",");
			for (String paramArg : paramArgs) {
				paramIDs.add(Integer.parseInt(paramArg));
			}
			int min = Collections.min(paramIDs);
			int max = Collections.max(paramIDs);
//            fileSuffixForParamsID = "-p_"+min + "_"+max;
		}else if (params.startsWith("range")){
			paramIDs.clear();
			paramArgs = params.split(":")[1].split(",");
			int start = Integer.parseInt(paramArgs[0]);
			int end = Integer.parseInt(paramArgs[1]);
			int step = Integer.parseInt(paramArgs[2]);
			for (int i = start; i < end; i += step) {
				paramIDs.add(i);
			}
//            this.fileSuffixForParamsID = "-p_"+start + "_"+end+"_"+step;
		}else if (params.startsWith("random")){
			paramIDs.clear();
			paramArgs = params.split(":")[1].split(",");
			int n  = Integer.parseInt(paramArgs[0]);
			int id = Integer.parseInt(paramArgs[1]);
			for (int i = 0; i < n; i++) {
				paramIDs.add(id);
			}
			int min = Collections.min(paramIDs);
			int max = Collections.max(paramIDs);
//            fileSuffixForParamsID = "-pr_"+min + "_"+max;
		}else{ //explicit
			paramIDs.clear();
		}

		Collections.sort(paramIDs);
		return paramIDs;
	}

	public class MeasureConverter extends EnumConverter {
		/**
		 * Constructs a new converter.
		 *
		 * @param optionName the option name for error reporting
		 * @param clazz      the enum class
		 */
		public MeasureConverter(String optionName, Class clazz) {
			super(optionName, clazz);
		}

		@Override
		public Measure convert(String name) {
			Measure measure = Measure.valueOf(name);
			return measure;
		}
	}

	public class WriteModeConverter implements IStringConverter<CsvWriter.WriteMode> {
		@Override
		public CsvWriter.WriteMode convert(String value) {
			CsvWriter.WriteMode convertedValue = CsvWriter.WriteMode.fromString(value);
			if(convertedValue == null) {
				throw new ParameterException("Value " + value + "can not be converted to WriteMode.");
			}
			return convertedValue;
		}
	}

	public class SemiColonSplitter implements IParameterSplitter {
		public List<String> split(String value) {
			return Arrays.asList(value.split(";"));
		}
	}

	public class ExportFileSplitter implements IParameterSplitter {
		public List<String> split(String value) {

			if (value.contains(",")){
				return Arrays.asList(value.split(","));
			}else{
				// use single letter codes
				ArrayList<String> fileTypes = new ArrayList<>(value.length());

				char[] types = value.toCharArray();
				for (int i = 0; i < types.length; i++) {

					switch (types[i]){
						case 't':
							fileTypes.add("tree.csv");
							break;
						case 'f':
							fileTypes.add("forest.csv");
							break;
						case 'p':
							fileTypes.add("pred.csv");
							break;
						case 'j':
							fileTypes.add("forest.json");
							break;
						case 's':
							fileTypes.add("splits.json");
							break;
						case 'd':
							fileTypes.add("debug.json");
							break;
						case 'g':
							fileTypes.add("graphviz.png");
							break;
					}
				}

				return fileTypes;
			}
		}
	}

}
