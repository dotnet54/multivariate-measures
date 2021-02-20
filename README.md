# Multivariate Similarity Measures for Time Series Classification

This repository guides to the supporting source code for the paper. 
If using the code linked in this repository, please cite the paper.

```
TODO
```

## Abstract
Elastic similarity measures are a class of similarity measures specifically 
designed to work with time series data. When scoring the similarity between 
two time series, they allow points that do not correspond in timestamps to be aligned. 
This can compensate for misalignments in the time axis of time series data, and for 
similar processes that proceed at variable and differing paces.
Elastic similarity measures are widely used in machine learning tasks such as 
classification, clustering and outlier detection when using time series data.

There is a multitude of research on various univariate elastic similarity measures. 
However, except for multivariate versions of the well known Dynamic Time Warping (DTW)
there is a lack of work to generalise other similarity measures for multivariate cases.
This paper adapts two existing strategies used in multivariate DTW, namely, Independent and Dependent DTW, 
to several commonly used elastic similarity measures.

Using 23 datasets from the University of East Anglia (UEA) multivariate archive, 
for nearest neighbour classification, we demonstrate that each measure outperforms 
all others on at least one dataset and that there are datasets for which
either the dependent versions of all measures are more accurate than their independent 
counterparts or vice versa. This latter finding suggests that these differences 
arise from a fundamental property of the data. We also show that an ensemble of 
such nearest neighbour classifiers is highly competitive with other state-of-the-art 
multivariate time series classifiers.

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

## Source Code
This project is closely related to the TS-CHIEF project. We plan to use these similarity
 measures to further develop the TS-CHIEF algorithm. Therefore, to avoid maintaining same source code
in two different projects, we have hosted all source code related to this project in
TS-CHIEF repository. 

https://github.com/dotnet54/TS-CHIEF

In this guide, using the TS-CHIEF source code, we will explain which java 
packages contain the source code necessary to 
reproduce the results in this paper. 

The main function for this paper is in the ``MultivarKNNApp`` class in package 
``application.test.knn``

Sorce code for the similarity measures are in the packahe ``distance.multivariate``

### Training
Command line args are simple as they are implemented using JCommander in the class MultivarKNNArgs
an example command line for training is

```
java -Xmx32g -cp ""mEE-v1.0.jar:lib/*"
application.papers.MultivariateEE -data=../data/ -a=Multivariate2018_ts 
-seed=6463564 -threads=0 -fileOutput=overwrite 
-exportTrainQueryFile=true -exportTestQueryFile=true 
-saveTransformedDatasets=false 
-testParamFileSuffixes=i,d,id -generateBestParamFiles=false 
-runLOOCV=true -runTesting=false 
-dep=false,true -dims=AllDims 
-lpIndep=1 -lpDep=2 -norm=false 
-o=out/i1d2-norm/train/ 
-testDir=out/i1d2-norm/test/ 
-params=range:0,100,1 
-m=euc,dtwf,dtwr,ddtwf,ddtwr,wdtw,wddtw,lcss,erp,msm,twe 
-d=BasicMotions
```

I am using ``-cp`` flag for JVM and then providing main class name as ``"mEE-v1.0.jar:lib/*"
applications.mee.MultivariateEE`` in the command line because ``pom.xml`` file in the
project is configured to package the main file for TS-CHIEF by default.

There is a lot of cmd options defined in the file ``MultivarKNNArgs``.
However, end users would not need to use most of them, as they are added
only to help with my own experimentation.

### Testing

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

### Results

You will find the csv files for all experimental results in the folder ``docs/results/multivariate-measures``