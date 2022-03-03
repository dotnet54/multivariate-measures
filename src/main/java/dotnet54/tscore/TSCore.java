package dotnet54.tscore;

import java.util.*;

public class TSCore {

    public static final String DECIMAL_PRINT_FORMAT = "#0.00000";

    public static String[] getPredefinedSet(int set_id) {
        final int num_sets = 50;
        String[][] datasets = new String[num_sets][];

        //set 1
        datasets[0] = new String[] {
                "ECG5000","TwoLeadECG", "SonyAIBORobotSurface1", "CricketZ", "ToeSegmentation1", "DistalPhalanxTW","Meat","Worms",
                "UWaveGestureLibraryZ", "CBF", "ShapeletSim", "Wine", "MiddlePhalanxOutlineCorrect", "Mallat", "Lightning2",
                "LargeKitchenAppliances", "DistalPhalanxOutlineAgeGroup", "Ham", "MedicalImages", "SyntheticControl", "PhalangesOutlinesCorrect",
                "UWaveGestureLibraryX", "FiftyWords",  "Computers",  "FacesUCR", "ShapesAll", "InlineSkate", "CinCECGtorso", "Phoneme", "FordA"
        };

        //set 2
        datasets[1] = new String[] {
                "ItalyPowerDemand", "BirdChicken", "ChlorineConcentration", "Symbols", "Haptics", "ScreenType", "Wafer", "ProximalPhalanxOutlineAgeGroup",
                "SonyAIBORobotSurface2", "Herring", "Adiac", "FaceAll", "DistalPhalanxOutlineCorrect", "RefrigerationDevices", "SmallKitchenAppliances",
                "Earthquakes", "Coffee", "ArrowHead", "ECG200", "OliveOil", "OSULeaf", "CricketY", "BeetleFly","CricketX",
                "MoteStrain",  "Yoga", "FordB", "MiddlePhalanxOutlineAgeGroup",   "StarlightCurves", "NonInvasiveFetalECGThorax2"
        };

        //set 3
        datasets[2] = new String[] {
                "Car", "Plane", "TwoPatterns",  "WormsTwoClass", "DiatomSizeReduction", "Fish", "InsectWingbeatSound",
                "Strawberry", "ProximalPhalanxOutlineCorrect", "Trace", "ECGFiveDays", "GunPoint", "Lightning7",
                "MiddlePhalanxTW", "ToeSegmentation2", "Beef", "WordSynonyms", "FaceFour",  "ElectricDevices",
                "SwedishLeaf", "ProximalPhalanxTW","NonInvasiveFetalECGThorax1", "UWaveGestureLibraryY", "HandOutlines", "UWaveGestureLibraryAll"
        };

        //set4 = 8 slowest datasets
        datasets[3] = new String[] {
                "NonInvasiveFetalECGThorax1","NonInvasiveFetalECGThorax2", "FordA", "FordB",
                "Phoneme","UWaveGestureLibraryAll","StarlightCurves", "HandOutlines"
        };

        //set5 = 77 without the 8 slowest
        datasets[4] = new String[] {
                "Adiac","ArrowHead","Beef","BeetleFly","BirdChicken","Car","CBF","ChlorineConcentration","CinCECGtorso","Coffee",
                "Computers","CricketX","CricketY","CricketZ","DiatomSizeReduction","DistalPhalanxOutlineAgeGroup",
                "DistalPhalanxOutlineCorrect","DistalPhalanxTW","Earthquakes","ECG200","ECG5000","ECGFiveDays",
                "ElectricDevices", "FaceAll","FaceFour","FacesUCR","FiftyWords","Fish","GunPoint",
                "Ham","Haptics","Herring","InlineSkate","InsectWingbeatSound","ItalyPowerDemand",
                "LargeKitchenAppliances","Lightning2","Lightning7","Mallat","Meat","MedicalImages","MiddlePhalanxOutlineAgeGroup",
                "MiddlePhalanxOutlineCorrect","MiddlePhalanxTW","MoteStrain",
                "OliveOil","OSULeaf","PhalangesOutlinesCorrect","Plane","ProximalPhalanxOutlineAgeGroup",
                "ProximalPhalanxOutlineCorrect","ProximalPhalanxTW","RefrigerationDevices","ScreenType",
                "ShapeletSim","ShapesAll","SmallKitchenAppliances","SonyAIBORobotSurface1","SonyAIBORobotSurface2",
                "Strawberry","SwedishLeaf","Symbols","SyntheticControl","ToeSegmentation1",
                "ToeSegmentation2","Trace","TwoLeadECG","TwoPatterns","UWaveGestureLibraryX",
                "UWaveGestureLibraryY","UWaveGestureLibraryZ","Wafer","Wine","WordSynonyms","Worms","WormsTwoClass","Yoga"
        };

        //set6 -- set 5 and then set 4 ordered with fastest first
        datasets[5] = new String[] {
                "Adiac","ArrowHead","Beef","BeetleFly","BirdChicken","Car","CBF","ChlorineConcentration","CinCECGtorso","Coffee",
                "Computers","CricketX","CricketY","CricketZ","DiatomSizeReduction","DistalPhalanxOutlineAgeGroup",
                "DistalPhalanxOutlineCorrect","DistalPhalanxTW","Earthquakes","ECG200","ECG5000","ECGFiveDays",
                "ElectricDevices", "FaceAll","FaceFour","FacesUCR","FiftyWords","Fish","GunPoint",
                "Ham","Haptics","Herring","InlineSkate","InsectWingbeatSound","ItalyPowerDemand",
                "LargeKitchenAppliances","Lightning2","Lightning7","Mallat","Meat","MedicalImages","MiddlePhalanxOutlineAgeGroup",
                "MiddlePhalanxOutlineCorrect","MiddlePhalanxTW","MoteStrain",
                "OliveOil","OSULeaf","PhalangesOutlinesCorrect","Plane","ProximalPhalanxOutlineAgeGroup",
                "ProximalPhalanxOutlineCorrect","ProximalPhalanxTW","RefrigerationDevices","ScreenType",
                "ShapeletSim","ShapesAll","SmallKitchenAppliances","SonyAIBORobotSurface1","SonyAIBORobotSurface2",
                "Strawberry","SwedishLeaf","Symbols","SyntheticControl","ToeSegmentation1",
                "ToeSegmentation2","Trace","TwoLeadECG","TwoPatterns","UWaveGestureLibraryX",
                "UWaveGestureLibraryY","UWaveGestureLibraryZ","Wafer","Wine","WordSynonyms","Worms","WormsTwoClass","Yoga",
                "Phoneme","UWaveGestureLibraryAll","StarlightCurves",
                "NonInvasiveFetalECGThorax1","NonInvasiveFetalECGThorax2", "FordA", "FordB",
                "HandOutlines"
        };

        //set7 = small to medium datasets
        datasets[6] = new String[] {
                "Adiac", "ArrowHead", "Beef", "BeetleFly", "BirdChicken", "Car", "CBF", "ChlorineConcentration", "CinCECGtorso", "Coffee",
                "Computers", "CricketX", "CricketY", "CricketZ", "DiatomSizeReduction", "DistalPhalanxOutlineAgeGroup", "DistalPhalanxOutlineCorrect",
                "DistalPhalanxTW", "Earthquakes", "ECG200", "ECG5000", "ECGFiveDays", "FaceFour", "FacesUCR",
                "FiftyWords", "Fish",  "GunPoint", "Ham",  "InsectWingbeatSound",
                "ItalyPowerDemand", "LargeKitchenAppliances", "Lightning2", "Lightning7",  "Meat", "MedicalImages", "MiddlePhalanxOutlineAgeGroup",
                "MiddlePhalanxOutlineCorrect", "MiddlePhalanxTW", "MoteStrain",
                "OliveOil", "OSULeaf", "PhalangesOutlinesCorrect",  "Plane", "ProximalPhalanxOutlineAgeGroup", "ProximalPhalanxOutlineCorrect",
                "ProximalPhalanxTW", "RefrigerationDevices", "ScreenType", "ShapeletSim", "ShapesAll", "SmallKitchenAppliances",
                "SonyAIBORobotSurface1", "SonyAIBORobotSurface2",  "Strawberry", "SwedishLeaf", "Symbols", "SyntheticControl",
                "ToeSegmentation1", "ToeSegmentation2", "Trace", "TwoLeadECG", "TwoPatterns",  "Wafer", "Wine", "WordSynonyms",  "Yoga"
        };

        //set8 -- excluded from set 7
        datasets[7] = new String[] {
                 "ElectricDevices", "FaceAll","FordA", "FordB","HandOutlines", "Haptics", "Herring", "InlineSkate","Mallat",
                 "NonInvasiveFetalECGThorax1", "NonInvasiveFetalECGThorax2", "Phoneme","StarlightCurves","UWaveGestureLibraryAll",
                 "UWaveGestureLibraryX", "UWaveGestureLibraryY", "UWaveGestureLibraryZ","Worms", "WormsTwoClass",
        };


        //set9 -- set 6 and then set 7 ordered with fastest first
        datasets[8] = new String[] {
                "Adiac", "ArrowHead", "Beef", "BeetleFly", "BirdChicken", "Car", "CBF", "ChlorineConcentration", "CinCECGtorso", "Coffee",
                "Computers", "CricketX", "CricketY", "CricketZ", "DiatomSizeReduction", "DistalPhalanxOutlineAgeGroup", "DistalPhalanxOutlineCorrect",
                "DistalPhalanxTW", "Earthquakes", "ECG200", "ECG5000", "ECGFiveDays", "FaceFour", "FacesUCR",
                "FiftyWords", "Fish",  "GunPoint", "Ham",  "InsectWingbeatSound",
                "ItalyPowerDemand", "LargeKitchenAppliances", "Lightning2", "Lightning7",  "Meat", "MedicalImages", "MiddlePhalanxOutlineAgeGroup",
                "MiddlePhalanxOutlineCorrect", "MiddlePhalanxTW", "MoteStrain",
                "OliveOil", "OSULeaf", "PhalangesOutlinesCorrect",  "Plane", "ProximalPhalanxOutlineAgeGroup", "ProximalPhalanxOutlineCorrect",
                "ProximalPhalanxTW", "RefrigerationDevices", "ScreenType", "ShapeletSim", "ShapesAll", "SmallKitchenAppliances",
                "SonyAIBORobotSurface1", "SonyAIBORobotSurface2",  "Strawberry", "SwedishLeaf", "Symbols", "SyntheticControl",
                "ToeSegmentation1", "ToeSegmentation2", "Trace", "TwoLeadECG", "TwoPatterns",  "Wafer", "Wine", "WordSynonyms",  "Yoga",
                "FaceAll","Haptics", "Herring", "InlineSkate","Mallat",
                "UWaveGestureLibraryX", "UWaveGestureLibraryY", "UWaveGestureLibraryZ","Worms", "WormsTwoClass",
                "ElectricDevices",
                "Phoneme","StarlightCurves","UWaveGestureLibraryAll",
                "FordA", "FordB",
                "NonInvasiveFetalECGThorax1", "NonInvasiveFetalECGThorax2",
                "HandOutlines",
        };


        //ordered by fastest first, split into 4 sets

        //set10
        datasets[9] = new String[] {
                "SonyAIBORobotSurface1", "SonyAIBORobotSurface2", "ItalyPowerDemand", "TwoLeadECG", "MoteStrain", "ECGFiveDays", "CBF",
                "GunPoint", "Coffee", "DiatomSizeReduction", "ArrowHead", "ECG200", "FaceFour", "BirdChicken", "BeetleFly",
                "ToeSegmentation1", "ToeSegmentation2", "Symbols", "Wine", "ShapeletSim", "Plane", "SyntheticControl", "OliveOil",
                "Beef", "Trace", "Meat", "DistalPhalanxTW", "ProximalPhalanxOutlineAgeGroup", "DistalPhalanxOutlineAgeGroup",
                "MiddlePhalanxOutlineAgeGroup", "ProximalPhalanxTW", "MiddlePhalanxTW", "Lightning7", "FacesUCR", "MedicalImages",
                "Herring", "Car", "MiddlePhalanxOutlineCorrect", "DistalPhalanxOutlineCorrect", "ProximalPhalanxOutlineCorrect",
        };

        //set11
        datasets[10] = new String[] {
                "Ham", "ECG5000", "Lightning2", "SwedishLeaf", "InsectWingbeatSound", "TwoPatterns", "Wafer", "FaceAll", "Fish",
                "Mallat", "OSULeaf", "ChlorineConcentration", "Adiac", "Strawberry", "WordSynonyms", "Yoga", "CinCECGtorso",
                "PhalangesOutlinesCorrect", "CricketX", "CricketY", "CricketZ", "Computers", "Earthquakes"
        };

        //set12
        datasets[11] = new String[] {
                "UWaveGestureLibraryX", "UWaveGestureLibraryZ", "UWaveGestureLibraryY", "WormsTwoClass", "FiftyWords", "Worms",
                "SmallKitchenAppliances", "Haptics", "LargeKitchenAppliances", "ScreenType", "RefrigerationDevices", "ElectricDevices",
                "InlineSkate", "ShapesAll"
        };

        //set13
        datasets[12] = new String[] {
                "StarlightCurves", "Phoneme", "UWaveGestureLibraryAll", "FordA", "FordB",
                "NonInvasiveFetalECGThorax2", "NonInvasiveFetalECGThorax1", "HandOutlines"
        };

        //set14 -> reserve


        //set15 - > 40 random sorted by train time --     #p.get_sorted_rand_datasets(p.chief_exp['e5b100r100k100'], p.datasets,40, 5)['dataset']
        //ItalyPowerDemand,TwoLeadECG,Car,GunPoint,Coffee,FaceFour,ArrowHead,BeetleFly,Wine,ShapeletSim,Beef,MiddlePhalanxOutlineAgeGroup,DistalPhalanxTW,DistalPhalanxOutlineAgeGroup,Lightning7,MedicalImages,MiddlePhalanxOutlineCorrect,CBF,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,ECG200,SwedishLeaf,TwoPatterns,Mallat,ChlorineConcentration,Yoga,CinCECGtorso,PhalangesOutlinesCorrect,CricketY,FiftyWords,UWaveGestureLibraryZ,Worms,Haptics,StarlightCurves,Phoneme,UWaveGestureLibraryAll,FordA,NonInvasiveFetalECGThorax1
        datasets[15] =  new String[] {
                "ItalyPowerDemand","TwoLeadECG","Car","GunPoint","Coffee","FaceFour","ArrowHead","BeetleFly"
                ,"Wine","ShapeletSim","Beef","MiddlePhalanxOutlineAgeGroup","DistalPhalanxTW","DistalPhalanxOutlineAgeGroup"
                ,"Lightning7","MedicalImages","MiddlePhalanxOutlineCorrect","CBF","DistalPhalanxOutlineCorrect"
                ,"ProximalPhalanxOutlineCorrect","Ham","Lightning2","ECG200","SwedishLeaf","TwoPatterns","Mallat"
                ,"ChlorineConcentration","Yoga","CinCECGtorso","PhalangesOutlinesCorrect","CricketY","FiftyWords","UWaveGestureLibraryZ","Worms"
                ,"Haptics","StarlightCurves","Phoneme","UWaveGestureLibraryAll","FordA","NonInvasiveFetalECGThorax1"
        };

        /*
         * util.misc.py
        # default random seed = 7787765 -- edited HandOutlines <-> StarlightCurves
                # sample
                # CBF,Computers,CricketX,CricketZ,DiatomSizeReduction,DistalPhalanxOutlineAgeGroup,DistalPhalanxOutlineCorrect,DistalPhalanxTW,
                # ECG200,ECG5000,Earthquakes,FaceFour,FacesUCR,FordB,GunPoint,HandOutlines,Herring,InsectWingbeatSound,ItalyPowerDemand,
                # LargeKitchenAppliances,Lightning7,Mallat,Meat,MiddlePhalanxOutlineAgeGroup,MiddlePhalanxTW,NonInvasiveFetalECGThorax1,
                # OSULeaf,OliveOil,Plane,RefrigerationDevices,ScreenType,ShapesAll,SmallKitchenAppliances,SwedishLeaf,SyntheticControl,
                # ToeSegmentation1,UWaveGestureLibraryX,UWaveGestureLibraryY,Wafer,Worms
                # not_in_sample
                # Adiac,ArrowHead,Beef,BeetleFly,BirdChicken,Car,ChlorineConcentration,CinCECGtorso,Coffee,CricketY,ECGFiveDays,ElectricDevices,\
                # FaceAll,FiftyWords,Fish,FordA,Ham,Haptics,InlineSkate,Lightning2,MedicalImages,MiddlePhalanxOutlineCorrect,MoteStrain,\
                # NonInvasiveFetalECGThorax2,PhalangesOutlinesCorrect,Phoneme,ProximalPhalanxOutlineAgeGroup,ProximalPhalanxOutlineCorrect,\
                # ProximalPhalanxTW,ShapeletSim,SonyAIBORobotSurface1,SonyAIBORobotSurface2,StarlightCurves,Strawberry,Symbols,ToeSegmentation2,\
                # Trace,TwoLeadECG,TwoPatterns,UWaveGestureLibraryAll,UWaveGestureLibraryZ,Wine,WordSynonyms,WormsTwoClass,Yoga
                */
                //dev set
        datasets[16] =  new String[] {
                "CBF","Computers","CricketX","CricketZ","DiatomSizeReduction","DistalPhalanxOutlineAgeGroup","DistalPhalanxOutlineCorrect",
                "DistalPhalanxTW","ECG200","ECG5000","Earthquakes","FaceFour","FacesUCR","FordB","GunPoint","StarlightCurves",
                "Herring","InsectWingbeatSound","ItalyPowerDemand","LargeKitchenAppliances","Lightning7","Mallat","Meat",
                "MiddlePhalanxOutlineAgeGroup","MiddlePhalanxTW","NonInvasiveFetalECGThorax1","OSULeaf","OliveOil","Plane",
                "RefrigerationDevices","ScreenType","ShapesAll","SmallKitchenAppliances","SwedishLeaf","SyntheticControl",
                "ToeSegmentation1","UWaveGestureLibraryX","UWaveGestureLibraryY","Wafer","Worms"
        };
                //holdout set
        datasets[17] =  new String[] {
                "Adiac","ArrowHead","Beef","BeetleFly","BirdChicken","Car","ChlorineConcentration","CinCECGtorso","Coffee","CricketY",
                "ECGFiveDays","ElectricDevices","FaceAll","FiftyWords","Fish","FordA","Ham","Haptics","InlineSkate","Lightning2",
                "MedicalImages","MiddlePhalanxOutlineCorrect","MoteStrain","NonInvasiveFetalECGThorax2","PhalangesOutlinesCorrect",
                "Phoneme","ProximalPhalanxOutlineAgeGroup","ProximalPhalanxOutlineCorrect","ProximalPhalanxTW","ShapeletSim",
                "SonyAIBORobotSurface1","SonyAIBORobotSurface2","Strawberry","Symbols","ToeSegmentation2",
                "Trace","TwoLeadECG","TwoPatterns","UWaveGestureLibraryAll","UWaveGestureLibraryZ","Wine","WordSynonyms","WormsTwoClass","Yoga","HandOutlines"
        };

        return datasets[set_id-1];
    }

    public static String[] getNRandomDatasets(int num_datasets, Random rand) {
        String[] datasets = getAllDatasets();

        String[] random_set = new String[num_datasets];

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < datasets.length; i++) {
            indices.add(i);
        }

        Collections.shuffle(indices, rand);

        for (int i = 0; i < random_set.length; i++) {
            random_set[i] = datasets[indices.get(i)];
        }

        return random_set;
    }

    public static String[] getSubsetOfDatasets(int start, int end) {
        String[] datasets = getAllDatasets();
        String[] subset = new String[end - start];

        int j = 0;
        for (int i = start; i < end; i++) {
            subset[j++] = datasets[i];
        }

        return subset;
    }

    public static String[] getAllDatasets() {
        String[] datasets = {
                "Adiac", "ArrowHead", "Beef", "BeetleFly", "BirdChicken", "Car", "CBF", "ChlorineConcentration", "CinCECGtorso", "Coffee",
                "Computers", "CricketX", "CricketY", "CricketZ", "DiatomSizeReduction", "DistalPhalanxOutlineAgeGroup", "DistalPhalanxOutlineCorrect",
                "DistalPhalanxTW", "Earthquakes", "ECG200", "ECG5000", "ECGFiveDays", "ElectricDevices", "FaceAll", "FaceFour", "FacesUCR",
                "FiftyWords", "Fish", "FordA", "FordB", "GunPoint", "Ham", "HandOutlines", "Haptics", "Herring", "InlineSkate", "InsectWingbeatSound",
                "ItalyPowerDemand", "LargeKitchenAppliances", "Lightning2", "Lightning7", "Mallat", "Meat", "MedicalImages", "MiddlePhalanxOutlineAgeGroup",
                "MiddlePhalanxOutlineCorrect", "MiddlePhalanxTW", "MoteStrain", "NonInvasiveFetalECGThorax1", "NonInvasiveFetalECGThorax2",
                "OliveOil", "OSULeaf", "PhalangesOutlinesCorrect", "Phoneme", "Plane", "ProximalPhalanxOutlineAgeGroup", "ProximalPhalanxOutlineCorrect",
                "ProximalPhalanxTW", "RefrigerationDevices", "ScreenType", "ShapeletSim", "ShapesAll", "SmallKitchenAppliances",
                "SonyAIBORobotSurface1", "SonyAIBORobotSurface2", "StarlightCurves", "Strawberry", "SwedishLeaf", "Symbols", "SyntheticControl",
                "ToeSegmentation1", "ToeSegmentation2", "Trace", "TwoLeadECG", "TwoPatterns", "UWaveGestureLibraryAll", "UWaveGestureLibraryX",
                "UWaveGestureLibraryY", "UWaveGestureLibraryZ", "Wafer", "Wine", "WordSynonyms", "Worms", "WormsTwoClass", "Yoga"
            };

        return datasets;
    }

}
