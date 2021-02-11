# Multivariate Similarity Measures for Time Series Classification

Currently all the code for this paper is in the project for TS-CHIEF in branch called multivariate.


The main function for this paper is in the MultivarKNNApp class in package application.test.knn
Command line args are simple as they are implemented using JCommander in the class MultivarKNNArgs


https://github.com/dotnet54/TS-CHIEF


an example command line for training is

screen -Logfile screen.log -L java -Xmx64g -cp "mKNN_v0111.jar:lib/*" application.test.knn.MultivarKNNApp -data=../data/ -a=Multivariate2018_ts -seed=6463564 -t=0 -vb=1 -useTLRandom=false -tf=yyyy-MM-dd-HHmmSS -fileOutput=overwrite -exportTrainQueryFile=true -exportTestQueryFile=true -saveTransformedDatasets=false -testDir=out/i1d2-norm/test/ -testParamFileSuffixes=i,d,id,b -generateBestParamFiles=false -python=venv/Scripts/python -dep=false,true -dims=AllDims -lpIndep=1 -lpDep=2 -norm=false -runLOOCV=true -runTesting=false -adjustSquaredDiff=true -useSquaredDiff=true -o=out/i1d2-norm/train/ -sf=summary/i1d2-norm.csv -params=range:0,100,1 -m=euc,dtwf,dtwr,ddtwf,ddtwr,wdtw,wddtw,lcss,erp,msm,twe -d=BasicMotions

I use a set of python scripts to aggregate LOOCV results which was run on multiple nodes in a cluster. 
Once aggregation is done using the provided python scripts, place the result files in -testDir=out/i1d2-norm/test/ and change command lines to -runTesting=true and -runLOOCV=false 

Much of the code used for this paper has dependency classes in TS-CHIEF project.
I am working on extracting only the minimal set of classes to a new IntelliJ project to be hosted in this repo.
