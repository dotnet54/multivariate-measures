package dotnet54.measures.multivariate;

import dotnet54.classifiers.knn.KnnTrainResult;
import dotnet54.tscore.exceptions.NotImplementedException;
import dotnet54.tscore.exceptions.NotSupportedException;
import dotnet54.tscore.data.Dataset;
import dotnet54.measures.Measure;
import java.util.Random;
import java.util.stream.IntStream;

public abstract class MultivariateMeasure {

    public static final int MAX_PARAMID = 100;

    //NOTE: for thead-safety make sure that class level variables are read only, or initialized once per parallel operation
    // I removed synchronised functions (added for defensive programming in PF and TSCHIEF) from multivariate distances
    // in TSCHIEF threading works because each tree runs on a different thread
    // so multiple threads will not be modifying same MultivariateSimilarityMeasure object at a given time.
    // if these classes are used where fields such as dimensionsToUse of same objects may be modified
    // by multiple threads, then thread-safety is not guaranteed
    // also note: ERP uses class level shared arrays

    //NOTE: do not modify these between threads
    protected boolean dependentDimensions;
    protected int[] dimensionsToUse;
    protected double pForIndependentDims = 1.0; //declared as double to experiment with fractional norms
    protected double pForDependentDims = 2.0;
    protected boolean useSquaredDiff = true;
    protected boolean adjustSquaredDiff = true;
    protected Measure measureName;

    //DEV
    public boolean debug = false;
    public boolean allocateLocalMemory = true;
    public boolean useDerivativeData = false; //flag to indicate if we need to cache derivative 
//    public boolean refreshParamCache = true;

    // if using a transformed dataset (e.g derivatives) set dataset to the transformed dataset
    public transient Dataset trainDataset; // reference to the full transformed dataset
    public transient Dataset testDataset; // reference to the full transformed dataset

    //DEV DEBUG
    public KnnTrainResult debugResult; //note thread unsafe

    public MultivariateMeasure(Measure measureName, boolean dependentDimensions, int[] dimensionsToUse) {
        this.measureName = measureName;
        this.dependentDimensions = dependentDimensions;
        this.dimensionsToUse = dimensionsToUse;
    }

    public double distance(int ixq, int ixc){
        if (trainDataset == null || testDataset == null){
            throw new RuntimeException("Set a dataset before using this method, if using a transformed dataset (e.g derivatives) set dataset to the transformed dataset");
        }

        if (useDerivativeData && (trainDataset == null || testDataset == null)){
            System.out.println("WARN: make sure that the  passed to the function is already transformed.");
        }

        double[][] series1 = trainDataset.getSeriesData(ixq);
        double[][] series2 = testDataset.getSeriesData(ixc);

        double distance = this.distance(series1, series2, Double.POSITIVE_INFINITY);

        if (Double.isNaN(distance)){
            distance = this.distance(series1, series2, Double.POSITIVE_INFINITY);
            throw new RuntimeException("distance == Nan in method distance(ixq, ixc) for ixq:" + ixq + " ixc: " + ixc);
        }

        return distance;
    }

    public double distance(double[][] series1, double[][] series2, double bsf){

        //use all dimensions by default
        if (dimensionsToUse == null){
            dimensionsToUse = IntStream.range(0, series1.length).toArray();
        }

        if (dependentDimensions){
            if (pForDependentDims == 1.0){
                return distanceDep(series1, series2, bsf); // TODO check
            }else if (pForDependentDims == 2.0){
//                return Math.sqrt(distanceDep(series1, series2, bsf));
                return distanceDep(series1, series2, bsf);
            }else{
                throw new NotSupportedException("Not in implemented in this package. Use lpmultivar package");
            }
        }else {
            if (pForIndependentDims == 1.0){
                double total = 0;
                for (int dimension : dimensionsToUse) {
//                    total += Math.abs(distanceIndep(series1, series2, dimension, bsf));
//                    total += distanceIndep(series1, series2, dimension, bsf);

                    //NOTE in the implementation in TSML project, they take sqrt here,
                    //NOTE it also doesnt conform to the original definition in Shokoohi-Yekta et al, 2017
                    //NOTE it doesnt make a lot of sense to me though but keeping it here to compare accuracy
                    total += Math.sqrt(distanceIndep(series1, series2, dimension, bsf));
                }
                return total;
            }else if (pForIndependentDims == 2.0){
                double total = 0;
                double dist;
                for (int dimension : dimensionsToUse) {
                    dist = distanceIndep(series1, series2, dimension, bsf);

                    //if return value is not squared
//                    total += (dist*dist);

                    // if return value from the distance function is already squared
                    // (eg. squared values from cost matrix or squared euclidean)
                    total += dist;

                }
//                return Math.sqrt(total); // not necessary
                return total;
            }else{
                throw new NotSupportedException("Not in implemented in this package. Use lpmultivar package");
            }
        }
    }

    public abstract double distanceIndep(double[][] series1, double[][] series2, int dimension, double bsf);

    public abstract double distanceDep(double[][] series1, double[][] series2, double bsf);

    // Used by DTW_D, WDTW_D, ERP_D and MSM_D
    protected final double squaredEuclideanDistanceFromPointToPoint(double[][] series1, int i,
                                                                    double[][] series2, int j) {
        double total = 0;
        double diff;
        for (int dimension : dimensionsToUse) {
            diff = series1[dimension][i] - series2[dimension][j];
            total += diff * diff;
        }
        return total;
    }

//    //TODO check thread safety
//    protected void getDerivative(double[] outputArray, double[] vector) {
//        for (int i = 1; i < vector.length - 1 ; i++) {
//            outputArray[i] = ((vector[i] - vector[i - 1]) + ((vector[i + 1] - vector[i - 1]) / 2.0)) / 2.0;
//        }
//        outputArray[0] = outputArray[1];
//        outputArray[outputArray.length - 1] = outputArray[outputArray.length - 2];
//    }

    public double distanceNaive(double[] series1, double[] series2){
        throw new NotImplementedException();
    }

    public Dataset getTrainDataset() {
        return trainDataset;
    }

    public void setTrainDataset(Dataset dataset) {
        this.trainDataset = dataset;
    }

    public Dataset getTestDataset() {
        return testDataset;
    }

    public void setTestDataset(Dataset dataset) {
        this.testDataset = dataset;
    }

    public boolean isDependentDimensions(){
        return dependentDimensions;
    }

    public void setDependentDimensions(boolean dependentDimensions){
        this.dependentDimensions = dependentDimensions;
    }

    public int[] getDimensionsToUse(){
        return dimensionsToUse;
    }

    public void setDimensionsToUse(int[] dimensionsToUse){
        this.dimensionsToUse = dimensionsToUse;
    }

    public double getpForIndependentDims() {
        return pForIndependentDims;
    }

    public void setpForIndependentDims(double pForIndependentDims) {
        this.pForIndependentDims = pForIndependentDims;
    }

    public double getpForDependentDims() {
        return pForDependentDims;
    }

    public void setpForDependentDims(double pForDependentDims) {
        this.pForDependentDims = pForDependentDims;
    }

    public boolean isAdjustSquaredDiff() {
        return adjustSquaredDiff;
    }

    public void setAdjustSquaredDiff(boolean adjustSquaredDiff) {
        this.adjustSquaredDiff = adjustSquaredDiff;
    }

    public boolean isUseSquaredDiff() {
        return useSquaredDiff;
    }

    public void setUseSquaredDiff(boolean useSquaredDiff) {
        this.useSquaredDiff = useSquaredDiff;
    }

    //NOTE trainData should be the transformed set, if required eg. derivatives
    //also, normally this is the subset of  if inside a tree node  -- will probably change this to use indices
    public abstract void setRandomParams(Dataset trainData, Random rand);

    //NOTE trainData should be the transformed set, if required eg. derivatives
    //also, normally this is the subset of  if inside a tree node -- will probably change this to use indices
    public abstract void initParamsByID(Dataset trainData, Random rand);

    public abstract void setParamsByID(int paramID, int seriesLength, Random rand);

    public abstract void setRandomParamByID(Dataset trainData, Random rand);

    public double getDblParam(String name)  {
        throw new RuntimeException("Invalid parameter name");
    }

    public void setDblParam(String name, double value)  {
        throw new RuntimeException("Invalid parameter");

    }

    public int getIntParam(String name)  {
        throw new RuntimeException("Invalid parameter");

    }

    public void setIntParam(String name, int value)  {
        throw new RuntimeException("Invalid parameter name");
    }

    public String getClassName(){
        return this.getClass().getSimpleName();
    }

    public Measure getMeasureName(){
        return measureName;
    }

    public static MultivariateMeasure createSimilarityMeasure(Measure measureName,
                                                              boolean dependentDimensions,
                                                              int[] dimensionsToUse){
        MultivariateMeasure measure = null;
        switch (measureName) {
            case euc:
            case euclidean:
                measure = new Euclidean(measureName, dependentDimensions, dimensionsToUse, true);
                break;
            case dtwf:
                measure = new DTW(measureName, dependentDimensions, dimensionsToUse, -1);
                break;
            case dtw:
            case dtwcv:
            case dtwr:
                measure = new DTW(measureName, dependentDimensions, dimensionsToUse,-1);
                break;
            case ddtwf:
                measure = new DDTW(measureName, dependentDimensions, dimensionsToUse, -1);
                break;
            case ddtw:
            case ddtwcv:
            case ddtwr:
                measure = new DDTW(measureName, dependentDimensions, dimensionsToUse,-1);
                break;
            case wdtw:
                measure = new WDTW(measureName, dependentDimensions, dimensionsToUse, 0);
                break;
            case wddtw:
                measure = new WDDTW(measureName, dependentDimensions, dimensionsToUse,0);
                break;
            case lcss:
                measure = new LCSS(measureName, dependentDimensions, dimensionsToUse, -1, 0, null);
                break;
            case msm:
                measure = new MSM(measureName, dependentDimensions, dimensionsToUse,1);
                break;
            case erp:
                measure = new ERP(measureName, dependentDimensions, dimensionsToUse, -1 , null);
                break;
            case twe:
                measure = new TWE(measureName, dependentDimensions, dimensionsToUse, 0, 1);
                break;
            default:
                throw new RuntimeException("Unknown similarity measure");
        }

        return measure;
    }


    public static MultivariateMeasure createSimilarityMeasureWithRandomParams(Measure measureName,
                                                                              boolean dependentDimensions,
                                                                              int[] dimensionsToUse,
                                                                              Dataset dataset,
                                                                              Random rand) throws Exception {
        MultivariateMeasure measure = null;

        switch (measureName) {
            case euc:
            case euclidean:
                measure = new Euclidean(measureName, dependentDimensions, dimensionsToUse, true);
                break;
            case dtwf:
                measure = new DTW(measureName, dependentDimensions, dimensionsToUse, dataset.length());
                break;
            case dtw:
            case dtwcv:
            case dtwr:
                measure = new DTW(measureName, dependentDimensions, dimensionsToUse,
                        DTW.getRandomWindowSize(dataset, rand));
                break;
            case ddtwf:
                measure = new DDTW(measureName, dependentDimensions, dimensionsToUse, dataset.length());
                break;
            case ddtw:
            case ddtwcv:
            case ddtwr:
                measure = new DDTW(measureName, dependentDimensions, dimensionsToUse,
                        DTW.getRandomWindowSize(dataset, rand));
                break;
            case wdtw:
                measure = new WDTW(measureName, dependentDimensions, dimensionsToUse,
                        WDTW.getRandomG(dataset, rand));
                break;
            case wddtw:
                measure = new WDDTW(measureName, dependentDimensions, dimensionsToUse,
                        WDTW.getRandomG(dataset, rand));
                break;
            case lcss:
                measure = new LCSS(measureName, dependentDimensions, dimensionsToUse,
                        LCSS.getRandomWindowSize(dataset, rand),
                        LCSS.getRandomEpsilon(dataset, rand),
                        LCSS.getRandomEpsilonPerDim(dataset, rand));
                break;
            case msm:
                measure = new MSM(measureName, dependentDimensions, dimensionsToUse,
                        MSM.getRandomCost(dataset, rand));
                break;
            case erp:
                measure = new ERP(measureName, dependentDimensions, dimensionsToUse,
                        ERP.getRandomWindowSize(dataset, rand), ERP.getRandomGs(dataset, rand));
                break;
            case twe:
                measure = new TWE(measureName, dependentDimensions, dimensionsToUse,
                        TWE.getRandomNu(dataset, rand), TWE.getRandomLambda(dataset,rand));
                break;
            default:
                throw new Exception("Unknown similarity measure");
        }

        return measure;
    }

    @Override
    public String toString(){
        return "dep="+dependentDimensions
                +",|d|="+(dimensionsToUse!=null?dimensionsToUse.length:0)
                +",lpI="+ pForIndependentDims
                +",lpD="+ pForDependentDims
                +",adj="+adjustSquaredDiff;
    }


    //support functions

    //if equal choose the diagonal
    public final int max(int left, int up, int diagonal) {
        if (left > up) {
            if (left > diagonal) {
                // left > up and left > diagonal
                return left;
            } else {
                // diagonal >= left > up
                return diagonal;
            }
        } else {
            if (up > diagonal) {
                // up > left and up > diagonal
                return up;
            } else {
                // diagonal >= up > left
                return diagonal;
            }
        }
    }

    //if equal choose the diagonal
    public final double min(double left, double up, double diagonal) {
        if (left < up) {
            if (left < diagonal) {
                // left < up and left < diagonal
                return left;
            } else {
                // diagonal <= left < up
                return diagonal;
            }
        } else {
            if (up < diagonal) {
                // up < left and up < diagonal
                return up;
            } else {
                // diagonal <= up < left
                return diagonal;
            }
        }
    }

//    public double[] getTimePoint(double[][] series, int index, int[] dimensionsToUse){
//        double[] timePoint = new double[series.length];
//        for (int dimension : dimensionsToUse) {
//            timePoint[dimension] = series[dimension][index];
//        }
//        return timePoint;
//    }

//    public final double squaredDistanceScalar(double A, double B) {
//        return (A - B) * (A - B);
//    }

//    public final double squaredDistanceVector(double[] A, double[] B, int[] dimensionsToUse) {
//        double total = 0;
//        for (int dimension : dimensionsToUse) {
//            total += ((A[dimension] - B[dimension]) * (A[dimension] - B[dimension]));
//        }
//        return total;
//    }

//    public final double squaredDistanceVector(double[] A, double[][] B, int j, int[] dimensionsToUse) {
//        double total = 0;
//        for (int dimension : dimensionsToUse) {
//            total += ((A[dimension] - B[dimension][j]) * (A[dimension] - B[dimension][j]));
//        }
//        return total;
//    }


    //TODO --- DEV

//    //generic implementation -- skeleton
//    public double distanceElastic(double[] series1Dimension, double[] series2Dimension,
//                                  double bsf, int windowSize) {
//        int length1 = series1Dimension.length;
//        int length2 = series2Dimension.length;
//        int maxLength = Math.max(length1, length2);
//
//        if (windowSize < 0 || windowSize > maxLength) {
//            windowSize = maxLength;
//        }
//
//        double[] prevRow = new double[maxLength];
//        double[] currentRow = new double[maxLength];
//
//        if (prevRow == null || prevRow.length < maxLength) {
//            prevRow = new double[maxLength];
//        }
//
//        if (currentRow == null || currentRow.length < maxLength) {
//            currentRow = new double[maxLength];
//        }
//
//        int i, j;
//        double prevVal;
//        double thisSeries1Val = series1Dimension[0];
//
//        // initialising the first row - do this in prevRow so as to save swapping rows before next row
//        prevVal = prevRow[0] = squaredDistanceScalar(thisSeries1Val, series2Dimension[0]);
//
//        for (j = 1; j < Math.min(length2, 1 + windowSize); j++) {
//            prevVal = prevRow[j] = prevVal + squaredDistanceScalar(thisSeries1Val, series2Dimension[j]);
//        }
//
//        // the second row is a special case
//        if (length1 >= 2){
//            thisSeries1Val = series1Dimension[1];
//
//            if (windowSize>0){
//                currentRow[0] = prevRow[0]+ squaredDistanceScalar(thisSeries1Val, series2Dimension[0]);
//            }
//
//            // in this special case, neither matrix[1][0] nor matrix[0][1] can be on the (shortest) minimum path
//            prevVal = currentRow[1]=prevRow[0]+ squaredDistanceScalar(thisSeries1Val, series2Dimension[1]);
//            int jStop = (windowSize + 2 > length2) ? length2 : windowSize + 2;
//
//            for (j = 2; j < jStop; j++) {
//                // for the second row, matrix[0][j - 1] cannot be on a (shortest) minimum path
//                prevVal = currentRow[j] = Math.min(prevVal, prevRow[j - 1]) + squaredDistanceScalar(thisSeries1Val, series2Dimension[j]);
//            }
//        }
//
//        // third and subsequent rows
//        for (i = 2; i < length1; i++) {
//            int jStart;
//            int jStop = (i + windowSize >= length2) ? length2-1 : i + windowSize;
//
//            // the old currentRow becomes this prevRow and so the currentRow needs to use the old prevRow
//            double[] tmp = prevRow;
//            prevRow = currentRow;
//            currentRow = tmp;
//
//            thisSeries1Val = series1Dimension[i];
//
//            if (i - windowSize < 1) {
//                jStart = 1;
//                currentRow[0] = prevRow[0] + squaredDistanceScalar(thisSeries1Val, series2Dimension[0]);
//            }
//            else {
//                jStart = i - windowSize;
//            }
//
//            if (jStart <= jStop){
//                // If jStart is the start of the window, [i][jStart-1] is outside the window.
//                // Otherwise jStart-1 must be 0 and the path through [i][0] can never be less than the path directly from [i-1][0]
//                prevVal = currentRow[jStart] = Math.min(prevRow[jStart - 1], prevRow[jStart])+ squaredDistanceScalar(thisSeries1Val, series2Dimension[jStart]);
//                for (j = jStart+1; j < jStop; j++) {
//                    prevVal = currentRow[j] = min(prevVal, prevRow[j], prevRow[j - 1])
//                            + squaredDistanceScalar(thisSeries1Val, series2Dimension[j]);
//                }
//
//                if (i + windowSize >= length2) {
//                    // the window overruns the end of the sequence so can have a path through prevRow[jStop]
//                    currentRow[jStop] = min(prevVal, prevRow[jStop], prevRow[jStop - 1]) + squaredDistanceScalar(thisSeries1Val, series2Dimension[jStop]);
//                }
//                else {
//                    currentRow[jStop] = Math.min(prevRow[jStop - 1], prevVal) + squaredDistanceScalar(thisSeries1Val, series2Dimension[jStop]);
//                }
//            }
//        }
//        //TODO bug if series 1 length == 1, value is in prevrow instead of current row
////		TODO prevVal
//        return Math.sqrt(currentRow[length2 - 1]);
//    }


}
