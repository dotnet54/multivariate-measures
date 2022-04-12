package dotnet54.applications.tschief;

import dotnet54.util.PrintUtilities;
import dotnet54.util.Util;
import dotnet54.tscore.io.DataArchive;
import dotnet54.tscore.io.UCR2015;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Main entry point for TS-CHIEF application
 * 
 * @author shifaz
 * @email ahmed.shifaz@monash.edu
 *
 */

public class TSChiefApp {

	/**
	 * Multivariate2018_ts
	 *
	 * --cpu=32 --mem=500 --time=06:00:00
	 * ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition
	 * --cpu=32 --mem=500 --time=06:00:00
	 * HandMovementDirection,LSST,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration
	 * --cpu=32 --mem=500 --time=06:00:00
	 * Heartbeat,PenDigits,DuckDuckGeese,PhonemeSpectra,PEMS-SF,MotorImagery
	 * --cpu=32 --mem=1200 --time=06:00:00
	 * FaceDetection,EigenWorms
	 *
	 */

	/**
	 * Univariate2018_ts
	 * e5b100r100t1000k500.time.csv
	 *
	 * --cpu=32 --mem=500 --time=08:00:00 6hr
	 * TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry,ChlorineConcentration,WordSynonyms,Yoga,PhalangesOutlinesCorrect,CricketY,CricketX,CricketZ,Computers,Earthquakes,Worms,WormsTwoClass,FiftyWords,SmallKitchenAppliances,LargeKitchenAppliances,Haptics,CinCECGTorso,UWaveGestureLibraryZ,UWaveGestureLibraryX,UWaveGestureLibraryY,ScreenType,RefrigerationDevices,Mallat,ElectricDevices,InlineSkate,ShapesAll,
	 * --cpu=32 --mem=1000 --time=10:00:00 7hr
	 * StarLightCurves,UWaveGestureLibraryAll,Phoneme
	 * --cpu=32 --mem=1000 --time=12:00:00 6hr
	 * FordB,FordA
	 * --cpu=32 --mem=1000 --time=12:00:00 10hr
	 * NonInvasiveFetalECGThorax2,NonInvasiveFetalECGThorax1
	 * --cpu=32 --mem=1000 --time=24:00:00 16hr
	 * HandOutlines
	 */

	public static final String[] simple_args = new String[]{
			"-dev=default", // must be the first arg,
			"-data=E:/data/",
			"-export=tfpc", // tfpcjsgdz
			"-seed=0",
			"-threads=0",
			"-trees=100",
			"-norm=false",
			"-repeats=1",
			"-overwriteResults=false",

			"-out=out/k100e5",
			"-c=ee:5",
//			"-c=ee:5,boss2:100,rise2:100",

			//univariate
//			"-archive=Multivariate2018_ts",
//			"-datasets=BasicMotions",

			//multivariate
			"-archive=Univariate2018_ts",
//			"-datasets=ItalyPowerDemand", // DistalPhalanxTW,ItalyPowerDemand,DistalPhalanxOutlineAgeGroup
			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand", // DistalPhalanxTW,ItalyPowerDemand,DistalPhalanxOutlineAgeGroup
//			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry",
//			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry,ChlorineConcentration,WordSynonyms,Yoga,PhalangesOutlinesCorrect,CricketY,CricketX,CricketZ,Computers,Earthquakes,Worms,WormsTwoClass,FiftyWords,SmallKitchenAppliances,LargeKitchenAppliances,Haptics,CinCECGTorso,UWaveGestureLibraryZ,UWaveGestureLibraryX,UWaveGestureLibraryY,ScreenType,RefrigerationDevices,Mallat,ElectricDevices,InlineSkate,ShapesAll",
	};



	public static final String[] dev_args = new String[]{
			"-dev=default",  // must be the first arg
			"-data=E:/data/",
			"-export=tfpcjsd", // tfpcjsgdz //TODO export config only
			"-seed=0", // TODO
			"-threads=0",
			"-trees=100",
			"-norm=false",
			"-numRepeats=1",
			"-repeatNo=0",
			"-shuffle=false",
			"-trainTestSplit=default",  //{default,uniform,stratified}
			"-trainingSplitRatio=0.7",
			"-overwriteResults=true",

			// output
			"-out=out/post-doc/tmp/",

			//multivariate
//			"-archive=Multivariate2018_ts",
//			"-datasets=BasicMotions",

			//univariate
			"-archive=Univariate2018_ts",
			"-datasets=ItalyPowerDemand", // DistalPhalanxTW,ItalyPowerDemand,DistalPhalanxOutlineAgeGroup
//			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand", // DistalPhalanxTW,ItalyPowerDemand,DistalPhalanxOutlineAgeGroup
//			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry",
//			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry,ChlorineConcentration,WordSynonyms,Yoga,PhalangesOutlinesCorrect,CricketY,CricketX,CricketZ,Computers,Earthquakes,Worms,WormsTwoClass,FiftyWords,SmallKitchenAppliances,LargeKitchenAppliances,Haptics,CinCECGTorso,UWaveGestureLibraryZ,UWaveGestureLibraryX,UWaveGestureLibraryY,ScreenType,RefrigerationDevices,Mallat,ElectricDevices,InlineSkate,ShapesAll",


			//general

			// splits
			"-candidateSelectionMethod=constant", //{constant,constbyDepthAndNodeSize,differentAtRoot,prob}
			"-cRoot=e:1,b:1,r:1",
			"-c=ee:5",
//			"-c=boss2:100",
//			"-c=ee:5,boss2:100",
//			"-c=ee:5,boss2:10,rise2:10",
//			"-c=rise2:100",
//			"-c=ee:5,boss2:100,rise2:100",

			// topk
			"-useBestPoolOfSplittersForRoots=false", //false,true
			"-splitterPoolType=stratified_queue", //queue,stratified_queue,stratified_prob
			"-splitterPoolSize=1", // percentage of max pool size, based on numTrees * TotalNumSplitters

			// approx gini -- TODO fix sampling, percent to train
			"-useApproxGini=false",
			"-approxGiniSampling=stratifiedEnsuringAllClasses",
			"-percentToTrain=1",
			"-minNodeSizeForApproxGini=100",
			"-minNodeSizeForApproxGiniIsPerClass=false",

			// ee
			"-ee_measures=euc,dtwf,dtw,ddtwf,ddtw,wdtw,wddtw,lcss,erp,twe,msm",

			// multivariate PF
			"-dimDependency=random", //{independent,dependent,both,random}
//			"-dimensions={1,2,3,4,5}", // set
//			"-dimensions=[0:-1:1]", // range [min:max:step]
//			"-dimensions=all", // all
//			"-dimensions=sqrt:6", // sqrt:min
			"-dimensions=uniform:1:1", // uniform:min:max
//			"-dimensions=beta:1:1:1",  // beta:alpha:beta:min //TODO [:min:max]

			//boss

			//rise
//			"-rise_numIntervals=25",

			// cif
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

			// other experiments
			//rf
			//st
			//it
			//rt
	};

	public static final String[] univariate_dev_args = new String[]{
			"-dev=default",  // must be the first arg
			"-data=E:/data/",
//			"-export=tfpcjsd", // tfpcjsgdz
			"-export=tfpc", // tfpcjsgdz
			"-seed=0",
			"-threads=0",
			"-trees=100",
			"-norm=false",
			"-trainTestSplit=fold1",
			"-numRepeats=1",
			"-repeatNo=0",
			"-overwriteResults=true",
//			"-out=E:/git/experiments/ppa/out/pc1/pf/k100e5",
			"-out=out/folds_chief_tmp/",
			"-archive=Univariate2018_ts",
			"-datasets=ItalyPowerDemand",
//			"-datasets=PhalangesOutlinesCorrect,Wafer,Adiac,MiddlePhalanxOutlineAgeGroup,TwoPatterns,ElectricDevices,Crop,DistalPhalanxOutlineAgeGroup,DistalPhalanxOutlineCorrect", // DistalPhalanxTW,ItalyPowerDemand,DistalPhalanxOutlineAgeGroup
//			"-datasets=DistalPhalanxOutlineCorrect", // DistalPhalanxTW,ItalyPowerDemand,DistalPhalanxOutlineAgeGroup
//			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand", // DistalPhalanxTW,ItalyPowerDemand,DistalPhalanxOutlineAgeGroup
			// fastest 40 datasets approx 15 min for 2 repeats k100e5 - desktop 8cpu
//			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham",
//			"-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry,ChlorineConcentration,WordSynonyms,Yoga,PhalangesOutlinesCorrect,CricketY,CricketX,CricketZ,Computers,Earthquakes,Worms,WormsTwoClass,FiftyWords,SmallKitchenAppliances,LargeKitchenAppliances,Haptics,CinCECGTorso,UWaveGestureLibraryZ,UWaveGestureLibraryX,UWaveGestureLibraryY,ScreenType,RefrigerationDevices,Mallat,ElectricDevices,InlineSkate,ShapesAll",
//			"-datasets=InlineSkate,StarLightCurves,ShapesAll,Phoneme,UWaveGestureLibraryAll,FordA,NonInvasiveFetalECGThorax1,HandOutlines",

			"-c=ee:5",
			"-candidateSelectionMethod=constant", //{constant,constbyDepthAndNodeSize,differentAtRoot,prob}
			"-cRoot=e:1",

//			// if running sattelite dataset
//			"-train=E:/data/SatelliteFull/sk_stratified/SatelliteFull_TRAIN_c5145.csv", //1016 5145 17366 26050 39075 58612
//			"-test=E:/data/SatelliteFull/sk_stratified/SatelliteFull_TEST_1000.csv",
//			"-out=E:/git/experiments/ppa/out/pc1/pf/k100e5_sat",
//			"-out=E:/git/experiments/ppa/out/pc1/pf/k100e5_sat_ag30",
//			"-csvTargetColumn=-1",
//			"-csvFileHeader=true",

			"-useApproxGini=false",
			"-agSampling=stratified",
			"-agPercent=1",
			"-agMaxSampleSize=30",
			"-agMinNodeSize=1",
			"-agMinClassSize=1",
			"-agMaxDepth=-1",

			"-eeMeasures=euc,dtwf,dtw,ddtwf,ddtw,wdtw,wddtw,lcss,erp,twe,msm",
			"-eeUsePrunedEA=false",
	};

	public static final String[] multivariate_dev_args = new String[]{
			"-dev=default",  // must be the first arg
			"-data=E:/data/",
			"-export=tfpcjsd", // tfpcjsgdz
			"-seed=0",
			"-threads=0",
			"-trees=100",
			"-norm=false",
			"-numRepeats=1",
			"-repeatNo=0",
			"-overwriteResults=true",
//			"-out=E:/git/experiments/ppa/out/pc1/mpf/tmp",
			"-out=out/folds_chief_tmp/",
			"-archive=Multivariate2018_ts",
//			"-datasets=ERing,BasicMotions,Libras,RacketSports,AtrialFibrillation,NATOPS,Handwriting,Epilepsy,UWaveGestureLibrary,FingerMovements,ArticularyWordRecognition,HandMovementDirection,StandWalkJump,Cricket,SelfRegulationSCP1,SelfRegulationSCP2,EthanolConcentration,Heartbeat,LSST,PenDigits,DuckDuckGeese,PEMS-SF,PhonemeSpectra",
//			"-datasets=BasicMotions,ERing,AtrialFibrillation,FingerMovements,Heartbeat,NATOPS", //ERing,BasicMotions
			"-datasets=BasicMotions,ERing",
			"-c=ee:5",
			"-candidateSelectionMethod=constant", //{constant,constbyDepthAndNodeSize,differentAtRoot,prob}
			"-cRoot=e:1",

			"-useApproxGini=false",
			"-agSampling=stratified",
			"-agPercent=1",
			"-agMaxSampleSize=100",
			"-agMinNodeSize=1",
			"-agMinClassSize=1",
			"-agMaxDepth=-1",

			// multivariate PF
			"-dimDependency=random", //{independent,dependent,both,random}
//			"-dimensions={1,2,3,4,5}", // set of ints {1, ... , D}
//			"-dimensions=[0:-1:1]", // fixed range of ints [min:max:step]  e.g. [0:D:1] => all
//			"-dimensions=all", // all
//			"-dimensions=sqrt", // sqrt TODO
//			"-dimensions=uniform:1:1", // uniform:min:max  e.g. uniform:1:1 uniform:1:D
//			"-dimensions=uniform:1:D", // uniform:min:max  e.g. uniform:1:1 uniform:1:D
//			"-dimensions=beta:1:3:1",  // beta:alpha:beta:minThreshold
			"-dimensions=beta:1:3:sqrt",  // beta:alpha:beta:minThreshold
//			"-dimensions=beta:1:1",  // beta:alpha:beta:minThreshold
//			"-dimensions=two_uniform",
			"-eeMeasures=dtwf",
//			"-eeMeasures=euc,dtwf,dtw,ddtwf,ddtw,wdtw,wddtw,lcss,erp,twe,msm",
			"-eeUsePrunedEA=false",
	};

	//TODO: CAUTION timing experiments should be done using a single thread,
	// there may be a stats counter which needs to be changed to an AtomicInetger, need to check and verify this.
	// setting a random seed also doesnt work in a multithreaded environment due to limitations
	// of Java's ThreadLocalRandom Class
	// (its also broken in single thread if using BOSS, because of HPPC library's
	// Hashmap's non-deterministic iteration order  -- will try to fix this eventually)

	public static TSChiefOptions tsChiefOptions;

	public static void main(String[] args) {
		try {
			tsChiefOptions = new TSChiefOptions();
			tsChiefOptions.version = "2.3.1";
			tsChiefOptions.buildDate = "2022.01.30";
			tsChiefOptions.versionTag = "post-doc-cleanup";

			Util.logger.info("STARTING: " + LocalDateTime.now()
				+ ", hostname: " + InetAddress.getLocalHost().getHostName()
				+ ", cpus: " + Runtime.getRuntime().availableProcessors()
				+ ", max: " + Runtime.getRuntime().maxMemory()/1024/1024
				+ ", total " + Runtime.getRuntime().totalMemory()/1024/1024);

			// ********************************************************************************
			//TODO always COMMENT the lines below before compiling the jar file
			if (args.length == 0) {
				System.out.println("No command line args have been provided. " +
						"\nPlease provide the minimum required command line args." +
						"\nUse --help to print a list of supported args" +
						"\n*****THIS DEFAULTS TO RUN IN THE DEVELOPMENT MODE with compiled args in the jar file****"
						);

				args = dev_args;
//				args = ThesisExperimentConfigs.config_pf;
//				args = univariate_dev_args;
//				args = chapter6_mpf;

			}else if (args.length > 2 && args[1].startsWith("-dev=")){
				System.out.println("RUNNING IN DEVELOPMENT MODE with some precompiled configurations." +
				 "\n This is just to make it easier to reproduce old experiments");
				tsChiefOptions.devMode = args[1].toLowerCase().substring(6,args[1].length());
				if (ThesisExperimentConfigs.configs.containsKey(tsChiefOptions.devMode)){
					args = ThesisExperimentConfigs.configs.get(tsChiefOptions.devMode);
				}
			}
			System.out.println("ARGS:" + String.join(" ", args));
			// ********************************************************************************

			tsChiefOptions.parseAndValidateArgs(args);

			//initialize app configs after reading cmd args -- this partly initializes AppConfig
			//the values that cannot be initialized properly at this stage are initialized during input validation
			//after reading the input datafiles, because some validation requires information such as dataset size or length
			//input validaiton is done inside ExperimentRunner class

			tsChiefOptions.initialize();
			
			//NOTE:
			// --  since parsed command line values may be changed to some default values during input validation,
			// --  actual settings used during execution may be different.
			// --  serializing AppConfig after input validation will give the actual configuration settings used during execution.
			// --  runtime settings can be exported using the -export_level args -- AppConfig is serialized to a json file. (TODO fix this)			

			//If this is an experiment using the UCR archive, then some assumptions are made to keep things easy
			if (tsChiefOptions.archiveName != null && ! tsChiefOptions.archiveName.isEmpty()) {
				//-archive option supports multiple datasets to be specified,
				//loop through them and send each dataset to ExperimentRunner one by one, ExperimentRunner 

				//if using -archive option -train is assumed to be data_dir name instead of full file name
//				TSChiefArgs.dataPath = TSChiefArgs.trainingFile;

				switch (tsChiefOptions.archiveName) {
					case UCR2015.archiveName:
						tsChiefOptions.archive = tsChiefOptions.ucr2015;
						break;
					case DataArchive.UCR2018UTS.archiveName:
						tsChiefOptions.archive = tsChiefOptions.ucr2018uts;
						break;
					case DataArchive.UCR2018MTS.archiveName:
						tsChiefOptions.archive = tsChiefOptions.ucr2018mts;
						break;
				}

				for (int i = 0; i < tsChiefOptions.datasets.size(); i++) {
					tsChiefOptions.datasetName = tsChiefOptions.datasets.get(i);
//					TSChiefArgs.trainingFile = TSChiefArgs.archive.getTrainingFileFullPath(current_dataset);
//					TSChiefArgs.testingFile = TSChiefArgs.archive.getTestingFileFullPath(current_dataset);
					tsChiefOptions.trainingFile = tsChiefOptions.dataPath + "/" + tsChiefOptions.archiveName +
										"/" + tsChiefOptions.datasetName + "/" + tsChiefOptions.datasetName + "_TRAIN.ts";
					tsChiefOptions.testingFile = tsChiefOptions.dataPath + "/" + tsChiefOptions.archiveName +
							"/" + tsChiefOptions.datasetName + "/" + tsChiefOptions.datasetName + "_TEST.ts";

					tsChiefOptions.trainingFile = Util.toOsPath(tsChiefOptions.trainingFile); // because I work on both windows and mac
					tsChiefOptions.testingFile = Util.toOsPath(tsChiefOptions.testingFile);	// because I work on both windows and mac
					
					System.out.println("--------------------------------    START UCR DATASET: " + i + " (" + tsChiefOptions.datasetName + ")   -------------------------");
					
					//experiment runner creates forests and train them; it handles repeating the experiments for each dataset
					ExperimentRunner experiment = new ExperimentRunner(tsChiefOptions);
					experiment.run();				
					
					System.gc();
					System.out.println("--------------------------------   END DATASET: "+ tsChiefOptions.datasetName +"  -------------------------------------");
				}
				
			}else {
				
				//each ExperimentRunner object handles training and testing one dataset
				//configuration is read from AppConfig static class -- this is done to keep all settings in one place
				//to easily serialize AppConfig to a json file so that experiment configurations can be exported for future reference easily
				ExperimentRunner experiment = new ExperimentRunner(tsChiefOptions);
				experiment.run();
			}
			

			
		}catch(Exception e) {			
            PrintUtilities.abort(e);
		}

		Util.logger.info("Terminating Logger");
		// This is a hot fix for not properly shutting down executor service in ParallelFor class
		System.exit(0);
	}


}

