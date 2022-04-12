
# examples

 screen -Logfile run_ch2_pf_1.log -L java -Xmx64g -cp "ts-chief-2.1.1-0106-thesis.jar:lib/*" dotnet54.applications.tschief.TSChiefApp -data=../data/ -archive=Univariate2018_ts -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -trees=100 -out=out/thesis/pf/k100e5 -c=ee:5 -datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry,ChlorineConcentration,WordSynonyms,Yoga,CricketY,CricketX,CricketZ,Computers,Earthquakes,Worms,WormsTwoClass,FiftyWords,SmallKitchenAppliances,LargeKitchenAppliances,Haptics,CinCECGTorso,UWaveGestureLibraryZ,UWaveGestureLibraryX,UWaveGestureLibraryY,ScreenType,RefrigerationDevices,Mallat,InlineSkate,ShapesAll,PhalangesOutlinesCorrect,UWaveGestureLibraryAll,Phoneme,StarLightCurves,ElectricDevices

python3 launcher.py --app=ts-chief-2.1.1-0106-thesis.jar --job=auto --cpu=32 --mem=3000 --time=24:00:00 --args="-data=../data/ -archive=Univariate2018_ts -numRepeats=1 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -trees=100 -out=out/thesis/pf/k100e5 -c=ee:5" --datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll,InsectWingbeatSound,Strawberry,ChlorineConcentration,WordSynonyms,Yoga,CricketY,CricketX,CricketZ,Computers,Earthquakes,Worms,WormsTwoClass,FiftyWords,SmallKitchenAppliances,LargeKitchenAppliances,Haptics,CinCECGTorso,UWaveGestureLibraryZ,UWaveGestureLibraryX,UWaveGestureLibraryY,ScreenType,RefrigerationDevices,Mallat,InlineSkate,ShapesAll,PhalangesOutlinesCorrect,UWaveGestureLibraryAll,Phoneme,StarLightCurves,ElectricDevices

# fast 50
 screen -Logfile run_ch2_pf_1.log -L java -Xmx64g -cp "ts-chief-2.1.1-0106-thesis.jar:lib/*" dotnet54.applications.tschief.TSChiefApp -data=../data/ -archive=Univariate2018_ts -numRepeats=2 -repeatNo=0 -threads=0 -seed=0 -export=tfpc -trees=100 -out=out/dev/pf/k100e5 -c=ee:5 -datasets=TwoLeadECG,SonyAIBORobotSurface1,SonyAIBORobotSurface2,MoteStrain,ECGFiveDays,Coffee,GunPoint,CBF,ItalyPowerDemand,FaceFour,ECG200,ArrowHead,BirdChicken,BeetleFly,DiatomSizeReduction,ToeSegmentation2,Wine,ToeSegmentation1,Plane,ShapeletSim,SyntheticControl,OliveOil,Beef,Meat,Trace,Symbols,DistalPhalanxTW,MiddlePhalanxOutlineAgeGroup,ProximalPhalanxOutlineAgeGroup,DistalPhalanxOutlineAgeGroup,ProximalPhalanxTW,MiddlePhalanxTW,Lightning7,Herring,Car,MedicalImages,MiddlePhalanxOutlineCorrect,DistalPhalanxOutlineCorrect,ProximalPhalanxOutlineCorrect,Ham,Lightning2,FacesUCR,SwedishLeaf,ECG5000,Fish,Wafer,OSULeaf,TwoPatterns,Adiac,FaceAll

#------------------------------------------------------------------------

# 1 cpu experiments to test

# -- accuracy on SITS against EE, BOSS-VS, WEASEL

# -- scalability on n - SITS

# -- scalability memory - SITS

# -- scalability on n - UCR

# -- scalability on L - UCR

# -- scalability memory - UCR

# -- accuracy on UCR

# -- number of candidates

# -- ensemble size

# -- ablation study using distance measures

# -- approximate gini

# -- early abandoning

# -- changing #candidates as a function of depth

# -- early stopping tree building

# -- on tree vs on node

# -- majority voting vs prob voting







