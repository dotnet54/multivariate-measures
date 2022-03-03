package dotnet54.applications.tschief;

import java.util.HashMap;

public class ThesisExperimentConfigs {

    public static HashMap<String, String[]> configs = new HashMap<String, String[]>();

    public static String[] config_pf = new String[]{
            "-data=E:/data/",
            "-export=tfpcjsd", // tfpcjsgdz
            "-seed=0",
            "-threads=0",
            "-trees=100",
            "-norm=false",
            "-repeats=1",
            "-shuffle=false",
            "-trainTestSplit=default",  //{default,uniform,stratified}
            "-overwriteResults=false",

            "-archive=Univariate2018_ts",
//			"-datasets=ItalyPowerDemand"
            "-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1",
//            "-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry,ChlorineConcentration,WordSynonyms,Yoga,CricketY,CricketX,CricketZ,Computers,Earthquakes,Worms,WormsTwoClass,FiftyWords,SmallKitchenAppliances,LargeKitchenAppliances,Haptics,CinCECGTorso,UWaveGestureLibraryZ,UWaveGestureLibraryX,UWaveGestureLibraryY,ScreenType,RefrigerationDevices,Mallat,InlineSkate,ShapesAll,PhalangesOutlinesCorrect,UWaveGestureLibraryAll,Phoneme,StarLightCurves,ElectricDevices",

            "-out=out/thesis/dev/k100e5",
            "-candidateSelectionMethod=constant", //{constant,constbyDepthAndNodeSize,differentAtRoot,prob}
            "-c=ee:5",

            // ee
            "-ee_measures=euc,dtwf,dtw,ddtwf,ddtw,wdtw,wddtw,lcss,erp,twe,msm",
    };

    public static String[] config_ts_chief = new String[]{
            "-data=E:/data/",
            "-export=tfpcjsd", // tfpcjsgdz
            "-seed=0",
            "-threads=0",
            "-trees=100",
            "-norm=false",
            "-repeats=1",
            "-shuffle=false",
            "-trainTestSplit=default",  //{default,uniform,stratified}
            "-overwriteResults=false",

            "-archive=Univariate2018_ts",
//			"-datasets=ItalyPowerDemand"
            "-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1",
//            "-datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry,ChlorineConcentration,WordSynonyms,Yoga,CricketY,CricketX,CricketZ,Computers,Earthquakes,Worms,WormsTwoClass,FiftyWords,SmallKitchenAppliances,LargeKitchenAppliances,Haptics,CinCECGTorso,UWaveGestureLibraryZ,UWaveGestureLibraryX,UWaveGestureLibraryY,ScreenType,RefrigerationDevices,Mallat,InlineSkate,ShapesAll,PhalangesOutlinesCorrect,UWaveGestureLibraryAll,Phoneme,StarLightCurves,ElectricDevices",

            "-out=out/thesis/dev/k100e5b100r100",
            "-candidateSelectionMethod=constant", //{constant,constbyDepthAndNodeSize,differentAtRoot,prob}

            // version 2
			"-c=ee:5,boss2:100,rise2:100",

            // version 1
//            "-c=ee:5,boss1:100,rise1:100",

            // ee
            "-ee_measures=euc,dtwf,dtw,ddtwf,ddtw,wdtw,wddtw,lcss,erp,twe,msm",

            //boss

            //rise
//			"-rise_numIntervals=25",

    };

    public static final String[] chapter2_pf = new String[]{
            "-dev=default",  // must be the first arg
            "-data=E:/data/",
//			"-export=tfpcjsd", // tfpcjsgdz
            "-export=tfpc", // tfpcjsgdz
            "-seed=0",
            "-threads=0",
            "-trees=100",
            "-norm=false",
            "-numRepeats=1",
            "-repeatNo=0",
            "-overwriteResults=true",
//			"-out=E:/git/experiments/ppa/out/pc1/pf/k100e5",
            "-out=E:/git/experiments/ppa/out/pc1/pf/k100e5_pea",
            "-archive=Univariate2018_ts",
            "-datasets=Fish",
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
            "-eeUsePrunedEA=true",
    };

    public static final String[] chapter6_mpf = new String[]{
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

    static {
        configs.put("pf", config_pf);
        configs.put("ts_chief", config_ts_chief);
    }


}
