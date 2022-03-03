package dotnet54.applications.tschief;

/**
 * This class contains some predefined command line args for some experiments
 * 
 * @author ashi32
 *
 */
public class TSChiefRunConfigurations {

	
	
//	java -Xmx40000m -jar app1118_tsf_rif.jar -train=../..//ucr/ -boss_params=random -boss_trasformations=500 -export=3 -ucr=all -out=out/fg_boss100t500_r5_1118 -s=boss:100
//	-train=E://resampled/InlineSkate/InlineSkate_TRAIN_32.arff -test=E://resampled/InlineSkate/InlineSkate_TEST_32.arff -target_column=last -boss_trasformations=100 -export=3 -repeats=1 -threads=1 -trees=100 -out=out/resample/InlineSkate/temp/  -s=ee:5,boss:100,tsf:100,rif:100
//	-train=E://Phonemes/CSV/weka_stratified512/all512_train_b32.arff -test=E://Phonemes/CSV/weka_stratified512/all512_test.arff -target_column=last -boss_trasformations=100 -export=3 -repeats=1 -threads=1 -trees=100 -out=out/phoneme/temp/  -s=tsf:20
	
//java -Xmx200000m -jar app1123_tsf_rif.jar  -train=../..//SatelliteFull/SatelliteFull_TRAIN_c$1.arff -test=../..//SatelliteFull/SatelliteFull_TEST_1000.arff -target_column=last -boss_trasformations=1000 -export=3 -repeats=1 -threads=1 -trees=100 -out=output/app1123/$1/  -s=ee:5,boss:100,rif:100
//java -Xmx200000m -jar cote_experiments_v3.jar  hivecote false predefined ../..//SatelliteFull/ SatelliteFull SatelliteFull_TRAIN_c$1 SatelliteFull_TEST_1000 output/hcote/$1 .arff 1

	
	
	
	//TODO test support file paths with a space?
	public static final String[] test_args = new String[]{
//			-ucr option is optional, if not used -train and -test should give full paths to training and test files
//			"-ucr=range0to10", //supports a range from start to end (end is exclusive)  eg. range0to84 
//			"-ucr=random{n}" //support selecting n random datasets eg. -ucr=random10 selects 10 random datasets, default n = 1
//			"-ucr=set4" //supports selecting a predefined set
//			"-ucr=all" //runs all 85 ucr datasets == to range0to84
//			"-ucr=ItalyPowerDemand", 	//runs a single ucr dataset
//			"-ucr=ItalyPowerDemand,Beef,Wine,DistalPhalanxOutlineAgeGroup,GunPoint" //runs a list of datasets, separated by comma
			"-ucr=Coffee", 
//			"-ucr=Adiac,ArrowHead,Beef,BeetleFly,BirdChicken,Car,CBF,ChlorineConcentration,Coffee,Computers,CricketX,CricketY,CricketZ,DiatomSizeReduction,DistalPhalanxOutlineAgeGroup,DistalPhalanxOutlineCorrect,DistalPhalanxTW,Earthquakes,ECG200,ECG5000,ECGFiveDays",
//			"-ucr=FaceAll,FaceFour,FacesUCR,FiftyWords,Fish,GunPoint,Ham,InsectWingbeatSound,LargeKitchenAppliances,Lightning2,Lightning7,Meat,MedicalImages,MiddlePhalanxOutlineAgeGroup,MiddlePhalanxOutlineCorrect,MiddlePhalanxTW,MoteStrain,OliveOil,OSULeaf,PhalangesOutlinesCorrect,Plane,ProximalPhalanxOutlineAgeGroup,ProximalPhalanxOutlineCorrect,ProximalPhalanxTW,RefrigerationDevices,ScreenType,ShapeletSim,ShapesAll,SmallKitchenAppliances,SonyAIBORobotSurface1,SonyAIBORobotSurface2,Strawberry,SwedishLeaf,Symbols,SyntheticControl,ToeSegmentation1,ToeSegmentation2,Trace,TwoLeadECG,TwoPatterns,UWaveGestureLibraryX,UWaveGestureLibraryY,UWaveGestureLibraryZ,Wafer,Wine,WordSynonyms,Worms,WormsTwoClass,Yoga",
			"-train=E://ucr/",
			"-test=E://ucr/",	//optional arg, if not specified, same folder as training path is searched
//			"-train=E://satellite/sample100000_TRAIN.txt", 	//use full path to the file if not using -ucr option
//			"-test=E://satellite/sample100000_TEST.txt", 	//use full path to the file if not using -ucr option
			"-out=output/dev/temp",
//			"-out=E:/git/experiments/02oct18/s10ag10m1_eerandf",
			"-repeats=1",
			"-trees=100",   
			"-shuffle=false",	//keep this false unless you want to shuffle training or test sets. not needed for ucr train/test splits we use
//			"-jvmwarmup=true",	//disabled -- not implemented in this version
			"-export=0",	//if 0 no export, if 1 only json, if 2 only csv, if 3 both csv and json
			"-verbosity=2",	//if 0 min printing, 1 is ideal, if 2 or more lots of debug printing
			"-csv_has_header=false", 
			"-target_column=first",	//{first,last} which column is the label column in your csv file?
			"-threads=1",	//0 = one thread per available core (including virtual cores)
			
			"-binary_split=false",
			"-gini_split=false",
			"-boosting=false",
			"-min_leaf_size=1",	// set to 1 to get pure node
			
			//TODO difficult to implement this because ThreadLocalRandom does not support setting seed, now looking at SplittableRandom class
			//requires lots of refactoring and some research to make this work with multithreading. -- WIP (work in progress)
//			"-rand_seed=0" 	//TODO if 0 use System.nanoTime(), else use this number
			
			//#candidate splits to try at each node
//			"-s=ee:5,boss:100",
			"-s=st:1",
//			"-s_prob=10:equal",	//TODO implementation not finished
//			"-splitters=boss, ee", //dont need this setting now, enable splitters implicitly using the -s option			

			"-ee_approx_gini=false",
			"-ee_approx_gini_min=10",
			"-ee_approx_gini_min_per_class=true",
			"-ee_approx_gini_percent=1",
			"-ee_dm=euc,dtw,dtwr,ddtw,ddtwr,wdtw,wddtw,lcss,twe,erp,msm",
			"-ee_dm_on_node=true", //dm selection per tree or per node
			
			"-randf_m=sqrt",	// {sqrt, loge, log2, 0<integer<#features} 
			
			"-boss_params=random",	//// {random, best, cv_on_sample}  //TODO only random and best is implemented, best needs param files in a folder in project dir
			"-boss_trasformations=100",	//number of transformations to precompute at the transform level - only forest level supports this, tree level uses a single transform per tree-- will require large amount of memory for a big dataset
			"-boss_preload_params_path=settings/boss/",	//if -boss_params=best, params are loaded from this path
			"-boss_transform_level=forest", // {forest,tree,node}, node is not implemented. 
			"-boss_split_method=nearest", // {gini, nearest}, if nearest, will find similarity of each instance at node to a random example from each class, similar to Proximity Forest, if gini, will split similar to a classic decision tree using gini //NOTE: gini doesnt work well -- need to check why
           
			//TODO

//			"tsf_trasformations=0",	//not valid, when transform level is node
			"-tsf_m=0",	// use all attributes to train splitter, feature bagging is handled by the random intervals
			"-tsf_min_interval=3", //TODO NOTE min x was used in HiveCOTE paper, this doesnt match code
//			"-tsf_transform_level=node", //only node is implemented
			"-tsf_num_intervals=1", //detect auto
//			"tsf_feature_selection=int"
			
//			"-rif_trasformations=0", 	//not valid, when transform level is node
			"-rif_m=0",
			"-rif_min_interval=16",	//TODO NOTE min 16 was used in HiveCOTE paper, this doesnt match code, if min interval is close to m ...
//			"-rif_transform_level=node", //only node is implemented
			"-rif_components=acf_pacf_arma_ps_sep",
			"-rif_same_intv_component=false",	//use same random interval for each set of component
			"-tsf_num_intervals=1", //detect auto 
			

	};
	
	public static final String[] prod_args = new String[]{
			"-machine=pc",
			"-ucr=Coffee",
			"-out=output/dev/temp-time2",
			"-trees=10",
			"-s=ee:5,rif:100,boss:100"
	};

	public static final String[] boss_exp = new String[]{
			"-train=E://ucr/",
			"-repeats=2",
			"-ucr=set5",
			"-out=output/1124/e5b50t1000rif50/",
			"-boss_trasformations=1000",
			"-s=ee:5,boss:50,rif:50"
	};	
	
	public static final String[] resample_exp = new String[]{
			"-train=E://resampled/InlineSkate/InlineSkate_TRAIN_2048.csv",
			"-test=E://resampled/InlineSkate/InlineSkate_TEST_2048.csv",
			"-target_column=last",
			"-repeats=1",
			"-out=output/1124/resample/temp/",
			"-boss_trasformations=1000",
			"-s=ee:5,boss:100,rif:100",
			"-threads=0"
	};
	
	
	public static final String[] sat_args = new String[]{
			"-train=E:\\\\SatelliteFull\\sk_stratified\\SatelliteFull_TRAIN_c26050.arff",
			"-test=E:\\\\SatelliteFull\\sk_stratified\\SatelliteFull_TEST_1000.arff",
			"-target_column=last",
			"-csv_has_header=true",
			"-repeats=1",
			"-out=output/dev/temp-time2-sat",
			"-trees=10",
			"-s=ee:5",
			"-threads=1"
	};



	//SITS experiments - bck 20.2.2020
	public static int sampleSize = 26050;
	public static final String[] sat_args2 = new String[]{
			"-train=E:\\\\SatelliteFull\\sk_stratified\\SatelliteFull_TRAIN_c"+sampleSize+".arff",
			"-test=E:\\\\SatelliteFull\\sk_stratified\\SatelliteFull_TEST_1000.arff",
			"-target_column=last",
			"-csv_has_header=true",
			"-repeats=1",
			"-results=output/dev/tmp-sits/_results.txt",
			"-out=output/dev/tmp-sits",
			"-boss_trasformations=1000",
			"-trees=100",
			"-s=ee:5,boss:100,rif:100",
			"-export=3",
			"-threads=0",
			"-seed=0"
	};


	public static final String[] dev_args_17_3_2021 = new String[]{
			"-data=E:/data/",
			"-results=out/temp.csv",
			"-threads=0",
			"-repeats=1",
			"-export=3",
			"-verbosity=1",
			"-seed=0",  //TODO WARNING doesnt work with BOSS due to HPPC library, also if -threads > 1
			"-trees=100",

			// uncomment for multivariate experiments
//			// ArticularyWordRecognition,AtrialFibrillation,BasicMotions,CharacterTrajectories,Cricket,DuckDuckGeese
//			// EigenWorms,Epilepsy,ERing,EthanolConcentration,FaceDetection,FingerMovements,HandMovementDirection,
//			// Handwriting,Heartbeat,InsectWingbeat,JapaneseVowels,Libras,LSST,MotorImagery,NATOPS,PEMS-SF,PenDigits,
//			// PhonemeSpectra,RacketSports,SelfRegulationSCP1,SelfRegulationSCP2,SpokenArabicDigits,StandWalkJump,
//			// UWaveGestureLibrary
//

//			//not normalized
//			"-datasets=ArticularyWordRecognition,AtrialFibrillation,BasicMotions,Cricket,DuckDuckGeese,Epilepsy,ERing," +
//					"EthanolConcentration,FaceDetection,FingerMovements,HandMovementDirection,Handwriting,Heartbeat" +
//					"Libras,LSST,MotorImagery,NATOPS,PEMS-SF,PenDigits,PhonemeSpectra,RacketSports,SelfRegulationSCP1," +
//					"SelfRegulationSCP2,StandWalkJump,UWaveGestureLibrary",
//			"-data=E:/data/pca_transformed/",
//			"-archive=exp-95+norm",
//			"-out=out/local/mpf/pca/exp-95+norm/",
//			"-datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PhonemeSpectra,PEMS-SF,MotorImagery,FaceDetection,EigenWorms",
//			"-datasets=EthanolConcentration,Heartbeat,PenDigits,DuckDuckGeese,PhonemeSpectra,PEMS-SF,MotorImagery,FaceDetection,EigenWorms",

//			"-data=E:/data/",
			"-archive=Multivariate2018_ts",
			"-datasets=BasicMotions",
			"-out=out/local/dev/",


//			"-c=ee:5",
			"-ee_measures=euc,dtwf,dtw,ddtwf,ddtw,wdtw,wddtw,lcss,erp,twe,msm",
//			"-ee_use_random_params=true",
//			"-ee_window_size=-1", //-1 = full window
//			"-ee_epsilon=0.01", // for double comparison
//			"-ee_cost=1", 	// cost of matching/missmatch, msm = c, erp = g, twe = lambda
//			"-ee_penalty=0",	//penalty for large warping, wdtw = g, twe = nu
//
			"-dimDependency=random", //{indepdendent,dependent,both,random}

//			"-dimensions={1,2,3,4,5}", // set
//			"-dimensions=[0:-1:1]", // range [min:max:step]
//			"-dimensions=all", // all
//			"-dimensions=sqrt:6", // sqrt:min
//			"-dimensions=uniform:1:2", // uniform:min:max
			"-dimensions=beta:1:3",  // beta:alpha:beta

			// uncomment for univariate experiments
			"-archive=Univariate2018_ts",
			"-out=out/local/dev",
			"-datasets=ItalyPowerDemand",
//			"-datasets=CBF,ItalyPowerDemand,CBF,Coffee,Fish,BeetleFly,DistalPhalanxOutlineAgeGroup,ToeSegmentation2," +
//			 "GunPoint,Ham,CricketX,Wine,Yoga,Plane,MoteStrain,ECGFiveDays", // DistalPhalanxOutlineAgeGroup BeetleFly Fish ToeSegmentation2 ItalyPowerDemand, HandOutlines, Coffee
//			"-c=rise2:100",
			"-c=ee:5,boss:100,rise:100",
			"-selectRootFromBestCandidates=false",
			"-cif_splits=100",
			"-cif_interval_selection=splits_based", // {sqrt, splits_based == cif_splits / cif_feature_sample_size, const:+int}
			"-cif_feature_sample_size=8",
			"-cif_features_use=-1", //{all, random, balanced_per_feature}
			"-cif_same_features_per_interval=true",
//			"-cif_use_catch22=false",
//			"-cif_use_tsf=false",
//			"-cif_use_rise=true",
			"-cif_use_catch22=true",
			"-cif_use_tsf=true",
			"-cif_use_rise=false",

			// uncomment for ee experiments
//			"-ee_dm=euc",
//			"-ee_dm=euc,dtw,dtwr,ddtw,ddtwr,wdtw,wddtw,lcss,erp,twe,msm",

			// uncomment for st experiments
////			"-st_min_length_percent=0",
////			"-st_max_length_percent=1",
//			"-st_min_length=10",
//			"-st_max_length=0",
//			"-st_interval_method=lengthfirst", //{lengthfirst, swap} method used to sample intervals
////			"-s=st:100",
//			"-st_params=preload_data",  //{random, preload,preload_data} --randomly or load params from files (default location for -st_params_files=settings/st)
//			"-st_threshold_selection=bestgini", //how do we choose split point ? { bestgini, median, random}
//			"-st_num_rand_thresholds=1", // > 1
//			"-st_feature_selection=int",
//			"-st_params_files=settings/st-5356/",

			// uncomment for it experiments
//			"-s=ee:5,boss:100,rif:100,it:1",
//			"-it_m=100",

			// uncomment for rt experiments
//			"-rt_m=100",

			// uncomment for cif experiments
	};


	public static final String[] public_release_test_args = new String[]{
			"-train=E://ucr2015/ItalyPowerDemand/ItalyPowerDemand_TRAIN.txt",
			"-test=E://ucr2015/ItalyPowerDemand/ItalyPowerDemand_TEST.txt",
			"-target_column=first",
			"-csv_has_header=false",
			"-repeats=1",
			"-results=out/results.csv",
			"-out=out/ts_chief/dev/",
			"-boss_trasformations=1000",
			"-trees=100",
			"-s=ee:5,boss:100,rif:100,st:2,it:5",
			"-export=3",
			"-threads=0",
			"-seed=0"
	};

}
