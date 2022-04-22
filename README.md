# Multivariate Similarity Measures for Time Series Classification

This repository contains the source code of the following paper.
Please cite the paper if using the source code in this repository.

```
This paper is currently review, for now please cite the ArXiv link bellow

https://arxiv.org/pdf/2102.10231.pdf
```

## Abstract
This paper contributes multivariate versions of seven commonly used elastic similarity measures for time series data analytics. 
Elastic similarity measures are a class of similarity measures that can compensate for misalignments in the time axis of time series data. 
We adapt two existing strategies used in a multivariate version of the well-known 
Dynamic Time Warping (DTW), namely, Independent and Dependent DTW, to these seven measures.

While these measures can be applied to various time series analysis tasks, we demonstrate their utility on multivariate 
time series classification using the nearest neighbor classifier. On 23 well-known datasets, we demonstrate that each of the measures 
but one achieve the highest accuracy relative to others on at least one dataset, supporting the value of developing a 
suite of multivariate similarity measures. We also demonstrate that there are datasets for which either the dependent 
versions of all measures are more accurate than their independent counterparts or vice versa.
In addition, we also construct a nearest neighbor based ensemble of the measures and show that it is 
competitive to other  state-of-the-art single-strategy multivariate time series classifiers.


## Source Code

Since this close is closely related to the TS-CHIEF project (https://github.com/dotnet54/TS-CHIEF), some utility functions from the project are
used in Multivariate Elastic Ensemble. This includes code to read ARFF, CSV or TS files, supporting math and
statistical functions, and so on. 

This project was developed using IntelliJ IDE and uses Maven to manage its dependencies. 
You can import the project to IntelliJ, and it will automatically set up the project and import dependencies using the
``pom.xml`` file.

### Multivariate Similarity Measures

Source code for the multivariate similarity measures are present in the folder 

``src/main/java/dotnet54/measures/multivariate``


### Multivariate Elastic Ensemble (MEE)

The main entry point for Multivariate Elastic Ensemble is the class ``MultivariateEEApp`` in the folder

``src/main/java/dotnet54/measures/multivariate``

### Datasets

UCR/UEA datasets used in these experiments can be obtained from http://timeseriesclassification.com/dataset.
It is recommended to download the files in ``.ts`` file format. However, ``.csv`` files are also supported.

We have uploaded the index files for our resample expriments to the folder ``cache/Multivariate2018_ts_INDICES``.
The ``.csv`` files in this folder contain one row for each of the 30 folds, and columns contains the list of indices in the fold

### Results

The results of all experiments can be found in the folder ``data``. Particularly, aggregated results are in
 ``summary`` folder, while full raw data is available in folder ``raw``.
We also provide the scripts to post process data in the ``scripts`` folder


## Using the application

### Command Line Options

We provide a very comprehensive command line arguments for the application implemented in ``MultivariateEEApp``.
It is implemented using JCommander in the class ``MultivariateEEArgs``. 
On the terminal ``-help`` will print all available commands, or you can go through ``MultivariateEEArgs`` class
to easily figure out more details on how to use it.

### Training MEE
Here is a a sample command line argument to train a sample dataset
```
java -Xmx32g -cp ""mEE-v1.0.jar:lib/*"
application.papers.MultivariateEEApp
-seed=6463564 -threads=0 -fileOutput=overwrite 
-exportTrainQueryFile=true -exportTestQueryFile=true 
-saveTransformedDatasets=false 
-testParamFileSuffixes=i,d,id -generateBestParamFiles=false 
-runLOOCV=true -runTesting=false 
-dep=false,true -dims=AllDims 
-lpIndep=1 -lpDep=2 -norm=false 
-o=out/i1d2-norm/train/ 
-testDir=out/i1d2-norm/test/ 
-data=E:/data/ -archive=Multivariate2018_ts 
-params=range:0,100,1 
-measures=euc,dtwf,dtwr,ddtwf,ddtwr,wdtw,wddtw,lcss,erp,msm,twe 
-datasets=BasicMotions,LSST,RocketTree
```

I am using ``-cp`` flag for JVM and then providing main class name as ``"mEE-v1.0.jar:lib/*"
applications.mee.MultivariateEE`` in the command line because ``pom.xml`` file in the
project is configured to package the main file for TS-CHIEF by default.

There is a lot of cmd options defined in the file ``MultivarKNNArgs``.
However, end users would not need to use most of them, as they are added
only to help with my own experimentation.

### Testing MEE

I did not write code in a way to sequentially perform training and testing in one run.
This is because I ran leave-one-out-cross validation to find the best parameter for each
measure, and to speed up, I ran LOOCV on nodes of a cluster.

So once training phase is run, it will output temporary csv files (e.g. to the folder
``-o=out/i1d2-norm/train/``)
which contain training results for each dataset, each measure and each parameter and
each dependency method (independent or dependent).

Once all partial training results are obtained, download them from different servers
and put them into a single folder. Then run the provided python script to find best param in each grouping
(e.g. I tried different groupings such as best among all independent, or dependent
or both combined). The python script will collate and aggregate temporary training
results and produce a set of files with best parameters.

You must then feed this file back to the java application with command line
options set as below to run the testing:

Change
``-runLOOCV=true -runTesting=false ``
To
``-runLOOCV=false -runTesting=true ``

Then set the wording directory for testing using ``-testDir``, e.g.
``-testDir=out/i1d2-norm/test/.``
This is the directory to which the python script outputted the final LOOCV results

The post-processing python scripts are in the file ``applications.mee.mee-test.py``

If anyone does not want to redo the LOOCV, I have provided the best param files in the
folder ``TODO``.



## Related Research

Other related papers, please cite them if using the source code:

### Proximity Forest
https://github.com/dotnet54/TS-CHIEF

https://arxiv.org/abs/1808.10594

```
Lucas, B., Shifaz, A., Pelletier, C., O’Neill, L., Zaidi, N., Goethals, B., ... & 
Webb, G. I. (2019). Proximity forest: an effective and scalable distance-based 
classifier for time series. Data Mining and Knowledge Discovery, 33(3), 607-635.
```

### TS-CHIEF
https://github.com/dotnet54/TS-CHIEF

https://arxiv.org/abs/1906.10329

```
Shifaz, A., Pelletier, C., Petitjean, F., & Webb, G. I. (2020). 
TS-CHIEF: a scalable and accurate forest algorithm for time series classification. 
Data Mining and Knowledge Discovery, 34(3), 742-775.
```
