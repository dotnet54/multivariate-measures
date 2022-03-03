package dotnet54.classifiers.tschief.splitters.dev;

import dotnet54.applications.tschief.TSChiefOptions.FeatureSelectionMethod;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.tscore.data.*;
import dotnet54.tscore.exceptions.MultivariateDataNotSupportedException;
import dotnet54.tscore.io.DataLoader;
import dotnet54.util.Util;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import weka.core.Instances;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class FastRandomTreeSplitterV2 extends NodeSplitter {

    public final int NUM_CHILDREN = 2;
    public final int LEFT_BRANCH = 0;
    public final int RIGHT_BRANCH = 1;

    // for multivariate
    protected final int DEFAULT_DIM = 0;

    public int numFeatures;
    public boolean randomizeAttribSelection = true;

    public int bestDimension = 0; // TODO currently supports only 1 dimension
    public int bestAttribute;
    public double bestThreshold;

    protected Random rand;
    protected boolean debugMode = false;
    public boolean logNoGainAttributes = true;
    public boolean clearMemory = false;

    // storing this extra information for debugging and development purpose

    public double bestWGini;
    public int bestSplitIndex; // split point is between bestSplitIndex and bestSplitIndex + 1
    public int[][] leftDist;
    public int[][] rightDist;

    public int[] attributes;
    public double[][] bestWGiniPerAttrib;
    public double[][] bestThresholdPerAttrib;
    public int[][] bestSplitIndexPerAttrib;

    public double[][] sortedBestParams; // attribIndex, threshold, wgini

    public FastRandomTreeSplitterV2(Random rand) {
        this.rand = rand;
    }

    public FastRandomTreeSplitterV2(Random rand, int numFeatures) {
        this.numFeatures = numFeatures;
        this.rand = rand;
    }

    public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {

        if (trainIndices != null){
            throw new RuntimeException("Please use fitByIndices method");
        }

        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        int nodeSize = nodeTrainData.size();
        int length = nodeTrainData.length();
        int numClasses = nodeTrainData.getNumClasses();
        TIntIntMap parentClassDist = nodeTrainData.getClassDistribution();

        // if return == null, caller can convert this node to a leaf or throw an exception
        if (nodeSize == 0) {
            return null;
        }

        boolean canSplit = false;

        if (numFeatures == 0 || numFeatures > length) {
            numFeatures = length;
        }

        // use length here instead of numFeatures
        attributes = new int[length];
        for (int i = 0; i < length; i++) {
            attributes[i] = i;
        }

        // shuffle the original list of attributes
        if (randomizeAttribSelection) {
            shuffleArray(attributes);
        }

        bestThreshold = Double.NaN;
        bestWGini = Double.POSITIVE_INFINITY;
        bestWGiniPerAttrib = new double[numFeatures][2];
        bestThresholdPerAttrib = new double[numFeatures][2];
        bestSplitIndexPerAttrib = new int[numFeatures][2];
        sortedBestParams = new double[numFeatures][4];

        // use numFeatures here instead of length
        for (int i = 0; i < numFeatures; i++) {
            int attribIndex = attributes[i];
            double currentThreshold = fitAttribute(nodeTrainData, attribIndex, i);

            // if we cannot split on this attribute
            if (Double.isNaN(currentThreshold)){
                continue;
            }

            if (bestWGiniPerAttrib[i][1] < bestWGini){
                bestWGini = bestWGiniPerAttrib[i][1];
                bestAttribute = attribIndex;
                bestThreshold = currentThreshold;
                bestSplitIndex = bestSplitIndexPerAttrib[i][1];
                canSplit = true;
            }
        }

        Arrays.sort(sortedBestParams, new Comparator<double[]>() {
            @Override
            public int compare(double[] o1, double[] o2) {
                return (int) (o1[0] - o2[0]);
            }
        });

        // split the data if we can split
        if (canSplit) {
            return split(nodeTrainData, trainIndices);
        } else {
            // if return == null, caller can convert this node to a leaf or throw an exception
            return null;
        }
    }

    private double fitAttribute(Dataset nodeTrainData, int attibIndex, int fIndex) throws Exception {

        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        int nodeSize = nodeTrainData.size();
        int length = nodeTrainData.length();
        int numClasses = nodeTrainData.getNumClasses();

        // if no data -- return null, caller can convert this node to a leaf or throw an exception
        if (nodeSize == 0) {
            return Double.NaN;
        }

        double currentValue, prevValue;
        double currentThreshold = Double.NaN;
        int splitIndex = -1; // just for debugging, its the index after which currentThreshold is computed

        int leftSize, rightSize;
        double leftGini, rightGini;
        double currentWGini, bestWGini = Double.POSITIVE_INFINITY;  //weighted Gini

        TIntIntMap parentClassDist = nodeTrainData.getClassDistribution();
        int[][] parentDist = mapToArray(parentClassDist);
        int[][] currLeftDist = new int[numClasses][2];
        int[][] currRightDist = new int[numClasses][2];
        int[][] leftDist = new int[numClasses][2];
        int[][] rightDist = new int[numClasses][2];

        currLeftDist = copyDist(parentDist, true);
        currRightDist = copyDist(parentDist, false);
        leftSize = 0;
        rightSize = nodeSize - leftSize;

        // sort by attribute
//        nodeTrainData.sort(attibIndex);  -- dont use this, not thread-safe, causes issues with indices order

        // extract the column as attrib, class pair (class labels are converted to doubles)
        // using only dimension 0
        double[][] columnData = new double[nodeSize][2]; // attribute and class
        for (int j = 0; j < nodeSize; j++) {
            columnData[j][0] = nodeTrainData.getSeries(j).value(DEFAULT_DIM, attibIndex);
            columnData[j][1] = nodeTrainData.getClass(j).doubleValue();
        }
        // sort the pairs based on attribute values
        Arrays.sort(columnData, Comparator.comparingDouble(o -> o[0]));

        // set first value as the initial threshold
        // on first iteration this will skip prevValue < currentValue, and so initialize the currLeftDist
        prevValue = columnData[0][0]; // nodeTrainData.getSeries(0).value(attibIndex);

        for (int i = 0; i < nodeSize; i++) {
            currentValue = columnData[i][0]; // series.value(attibIndex);

            // if we can place a split point here
            if (prevValue < currentValue){

                // find the gini of each branch for splitting here
                leftGini = gini(currLeftDist, leftSize);
                rightGini = gini(currRightDist, rightSize);

                // find weighted gini using the fraction of data at each branch
                currentWGini = ((double) leftSize / nodeSize * leftGini)
                        + ((double) rightSize / nodeSize * rightGini);

                // we want to minimize the weighted gini (this maximizes the gain)
                if (currentWGini < bestWGini){

                    bestWGini = currentWGini;
                    currentThreshold = (prevValue + currentValue) / 2;
                    splitIndex = i;

                    // check for numeric precision issues -> based on the Weka implementation of RandomTree
                    if (currentThreshold <= prevValue) {
                        currentThreshold = currentValue;
                    }

                    // save best distribution
                    leftDist = copyDist(currLeftDist, false);
                    rightDist = copyDist(currRightDist, false);

                }

            }

            // update current value
            prevValue = currentValue;

            // shift distribution to the left
            shiftDist(currLeftDist, (int) columnData[i][1], +1);
            shiftDist(currRightDist, (int) columnData[i][1], -1);
            leftSize++;
            rightSize = nodeSize - leftSize;
        }

        // if we found a split point
        if (currentThreshold != Double.NaN) {
            bestWGiniPerAttrib[fIndex][0] = attibIndex;
            bestWGiniPerAttrib[fIndex][1] = bestWGini;
            bestThresholdPerAttrib[fIndex][0] = attibIndex;
            bestThresholdPerAttrib[fIndex][1] = currentThreshold;
            bestSplitIndexPerAttrib[fIndex][0] = attibIndex;
            bestSplitIndexPerAttrib[fIndex][1] = splitIndex;
            sortedBestParams[fIndex][0] = attibIndex;
            sortedBestParams[fIndex][1] = currentThreshold;
            sortedBestParams[fIndex][2] = bestWGini;
            sortedBestParams[fIndex][3] = splitIndex;
        } else {
            // no gain from this attribute
            // this could happen if data has no difference in the values
            // e.g. badly labeled data, uniform data coming from a sensor - no change in values across instances,
            //      a data transformation with bad params produced all 0 values, or same values
            // in such cases, try to use another attribute or if no attribute produces a gain,
            // then convert node to a leaf, and then perhaps use ZeroR model for prediction (majority vote on class)
            if (logNoGainAttributes){
                Util.logger.warn("No gain found in the attribute " + attibIndex + " " +
                        Thread.currentThread().getStackTrace()[0].getMethodName());
            }
        }

        return currentThreshold;
    }

    public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices){

        if (trainIndices != null){
            throw new RuntimeException("Please use splitByIndices method");
        }

        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        int nodeSize = nodeTrainData.size();
        TimeSeries series;
        NodeSplitterResult result = new NodeSplitterResult(this, NUM_CHILDREN);
        result.splits.put(LEFT_BRANCH, new MTSDataset(nodeSize/2)); // init capacity half the size
        result.splits.put(RIGHT_BRANCH, new MTSDataset(nodeSize/2)); // init capacity half the size

        for (int i = 0; i < nodeSize; i++) {
            series = nodeTrainData.getSeries(i);
            if (series.data()[bestDimension][bestAttribute] < bestThreshold) {
                result.splits.get(LEFT_BRANCH).add(series);
            } else {
                result.splits.get(RIGHT_BRANCH).add(series);
            }
        }

        return result;
    }

    // TODO BUG not using indices
    // Make sure that the original labels are encoded using a label encoder (class values are continuous)
    public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices)
            throws Exception {

        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        int nodeSize = nodeTrainData.size();
        int length = nodeTrainData.length();
        int numClasses = nodeTrainData.getNumClasses();
        TIntIntMap parentClassDist = nodeTrainData.getClassDistribution();

        // if return == null, caller can convert this node to a leaf or throw an exception
        if (nodeSize == 0) {
            return null;
        }

        boolean canSplit = false;

        if (numFeatures == 0 || numFeatures > length) {
            numFeatures = length;
        }

        // use length here instead of numFeatures
        attributes = new int[length];
        for (int i = 0; i < length; i++) {
            attributes[i] = i;
        }

        // shuffle the original list of attributes
        if (randomizeAttribSelection) {
            shuffleArray(attributes);
        }

        bestThreshold = Double.NaN;
        bestWGini = Double.POSITIVE_INFINITY;
        bestWGiniPerAttrib = new double[numFeatures][2];
        bestThresholdPerAttrib = new double[numFeatures][2];
        bestSplitIndexPerAttrib = new int[numFeatures][2];
        sortedBestParams = new double[numFeatures][4];

        // use numFeatures here instead of length
        for (int i = 0; i < numFeatures; i++) {
            int attribIndex = attributes[i];
            double currentThreshold = fitAttributeByIndices(nodeTrainData, trainIndices, attribIndex, i);

            // if we cannot split on this attribute
            if (Double.isNaN(currentThreshold)){
                continue;
            }

            if (bestWGiniPerAttrib[i][1] < bestWGini){
                bestWGini = bestWGiniPerAttrib[i][1];
                bestAttribute = attribIndex;
                bestThreshold = currentThreshold;
                bestSplitIndex = bestSplitIndexPerAttrib[i][1];
                canSplit = true;
            }
        }

        // split the data if we can split
        if (canSplit) {
            return splitByIndices(nodeTrainData, trainIndices);
        } else {
            // if return == null, caller can convert this node to a leaf or throw an exception
            return null;
        }
    }

    // TODO BUG use indices
    private double fitAttributeByIndices(Dataset nodeTrainData, DatasetIndex trainIndices, int attibIndex, int fIndex) throws Exception {

        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        int nodeSize = nodeTrainData.size();
        int length = nodeTrainData.length();
        int numClasses = nodeTrainData.getNumClasses();

        // if no data -- return null, caller can convert this node to a leaf or throw an exception
        if (nodeSize == 0) {
            return Double.NaN;
        }

        double currentValue, prevValue;
        double currentThreshold = Double.NaN;
        int splitIndex = -1; // just for debugging, its the index after which currentThreshold is computed

        int leftSize, rightSize;
        double leftGini, rightGini;
        double currentWGini, bestWGini = Double.POSITIVE_INFINITY;  //weighted Gini

        TIntIntMap parentClassDist = nodeTrainData.getClassDistribution();
        int[][] parentDist = mapToArray(parentClassDist);
        int[][] currLeftDist = new int[numClasses][2];
        int[][] currRightDist = new int[numClasses][2];
        int[][] leftDist = new int[numClasses][2];
        int[][] rightDist = new int[numClasses][2];

        currLeftDist = copyDist(parentDist, true);
        currRightDist = copyDist(parentDist, false);
        leftSize = 0;
        rightSize = nodeSize - leftSize;

        // sort by attribute
//        nodeTrainData.sort(attibIndex);  -- dont use this, not thread-safe, causes issues with indices order

        // extract the column as attrib, class pair (class labels are converted to doubles)
        // using only dimension 0
        double[][] columnData = new double[nodeSize][2]; // attribute and class
        for (int j = 0; j < nodeSize; j++) {
            columnData[j][0] = nodeTrainData.getSeries(j).value(DEFAULT_DIM, attibIndex);
            columnData[j][1] = nodeTrainData.getClass(j).doubleValue();
        }
        // sort the pairs based on attribute values
        Arrays.sort(columnData, Comparator.comparingDouble(o -> o[0]));

        // set first value as the initial threshold
        // on first iteration this will skip prevValue < currentValue, and so initialize the currLeftDist
        prevValue = columnData[0][0]; // nodeTrainData.getSeries(0).value(attibIndex);

        for (int i = 0; i < nodeSize; i++) {
            currentValue = columnData[i][0]; // series.value(attibIndex);

            // if we can place a split point here
            if (prevValue < currentValue){

                // find the gini of each branch for splitting here
                leftGini = gini(currLeftDist, leftSize);
                rightGini = gini(currRightDist, rightSize);

                // find weighted gini using the fraction of data at each branch
                currentWGini = ((double) leftSize / nodeSize * leftGini)
                        + ((double) rightSize / nodeSize * rightGini);

                // we want to minimize the weighted gini (this maximizes the gain)
                if (currentWGini < bestWGini){

                    bestWGini = currentWGini;
                    currentThreshold = (prevValue + currentValue) / 2;
                    splitIndex = i;

                    // check for numeric precision issues -> based on the Weka implementation of RandomTree
                    if (currentThreshold <= prevValue) {
                        currentThreshold = currentValue;
                    }

                    // save best distribution
                    leftDist = copyDist(currLeftDist, false);
                    rightDist = copyDist(currRightDist, false);

                }

            }

            // update current value
            prevValue = currentValue;

            // shift distribution to the left
            shiftDist(currLeftDist, (int) columnData[i][0], +1);
            shiftDist(currRightDist, (int) columnData[i][0], -1);
            leftSize++;
            rightSize = nodeSize - leftSize;
        }

        // if we found a split point
        if (currentThreshold != Double.NaN) {
            bestWGiniPerAttrib[fIndex][0] = attibIndex;
            bestWGiniPerAttrib[fIndex][1] = bestWGini;
            bestThresholdPerAttrib[fIndex][0] = attibIndex;
            bestThresholdPerAttrib[fIndex][1] = currentThreshold;
            bestSplitIndexPerAttrib[fIndex][0] = attibIndex;
            bestSplitIndexPerAttrib[fIndex][1] = splitIndex;
            sortedBestParams[fIndex][0] = attibIndex;
            sortedBestParams[fIndex][1] = currentThreshold;
            sortedBestParams[fIndex][2] = bestWGini;
            sortedBestParams[fIndex][3] = splitIndex;
        } else {
            // no gain from this attribute
            // this could happen if data has no difference in the values
            // e.g. badly labeled data, uniform data coming from a sensor - no change in values across instances,
            //      a data transformation with bad params produced all 0 values, or same values
            // in such cases, try to use another attribute or if no attribute produces a gain,
            // then convert node to a leaf, and then perhaps use ZeroR model for prediction (majority vote on class)
            if (logNoGainAttributes){
                Util.logger.warn("No gain found in the attribute " + attibIndex + " " +
                        Thread.currentThread().getStackTrace()[0].getMethodName());
            }
        }

        return currentThreshold;
    }

    /***
     *  is the full dataset, indices are indices of  that only reached the node
     *
     */
    public NodeSplitterResult splitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices)
            throws Exception {

        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        TimeSeries series;
        NodeSplitterResult result = new NodeSplitterResult(this, NUM_CHILDREN);
        result.splitIndices.put(LEFT_BRANCH, new TIntArrayList());
        result.splitIndices.put(RIGHT_BRANCH, new TIntArrayList());

        int[] indices = trainIndices.getIndices();
        // assert nodeTrainData.size() == indices.length
        // assert order of  in nodeTrainData and order of indices to original  match
//        int nodeSize = nodeTrainData.size();
        // TODO BUG use indices
        for (int i = 0; i < indices.length; i++) {
            // not using indices[i] here because nodeTrainDataset is a subset and it may be
            // transformed 
            series = nodeTrainData.getSeries(i);
            double attribVal = series.data()[bestDimension][bestAttribute];
            if (attribVal < bestThreshold) {
                result.splitIndices.get(LEFT_BRANCH).add(indices[i]);
            } else {
                result.splitIndices.get(RIGHT_BRANCH).add(indices[i]);
            }
        }

//        //TODO ONLY FOR DEBUGGING -- REMOVE THIS
//        if (debugMode){
//            this.nodeTrainData = nodeTrainData;
//        }


        //
        return result;
    }

    public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {

//        //TODO ONLY FOR DEBUGGING -- REMOVE THIS
//        int testSize = testData.size();
//        double[][] testColumn = new double[testSize][2];
//        double[][] testSortedColumn;
//        // extract the column
//        for (int j = 0; j < testSize; j++) {
//            testColumn[j][0] = nodeTrainData.getSeries(j).value(0, bestAttribute);
//            testColumn[j][1] = nodeTrainData.getClass(j).doubleValue();
//        }
//        testSortedColumn = Arrays.stream(testColumn).map(double[]::clone).toArray(double[][]::new);
//        Arrays.sort(testSortedColumn, Comparator.comparingDouble(o -> o[0]));


        if (query.data()[bestDimension][bestAttribute] < bestThreshold) {
            return LEFT_BRANCH;
        } else {
            return RIGHT_BRANCH;
        }
    }

    private double gini(int[][] distribution, int totalSize) {
        double sum = 0.0;
        double p;
//        final int CLASS_LABEL = 0;
        final int CLASS_COUNT = 1;

        for (int i = 0; i < distribution.length; i++) {
            p = (double) distribution[i][CLASS_COUNT] / totalSize;
            sum += p * p;
        }
        return 1 - sum;
    }

    private void shuffleArray(int[] array)
    {
        int index, temp;
        for (int i = array.length - 1; i > 0; i--)
        {
            index = rand.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private int[][] mapToArray(TIntIntMap distribution){
        int[][] dist = new int[distribution.size()][2];

        int i = 0;
        for (int key : distribution.keys()) {
            dist[i][0] = key;
            dist[i][1] = distribution.get(key);
            i++;
        }

        return dist;
    }

    private void reset2DIntArray(int[][] array){
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][j] = 0;
            }
        }
    }

    private void reset2DDoubleArray(double[][] array){
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][j] = 0;
            }
        }
    }

    private int[][] copy2DIntArray(int[][] array){
        int[][] out = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            out[i] = new int[array[i].length];
            for (int j = 0; j < out[i].length; j++) {
                out[i][j] = array[i][j];
            }
        }
        return out;
    }

    private int[][] copyDist(int[][] distribution, boolean reset){
        int[][] out = new int[distribution.length][];
        if (reset){
            for (int i = 0; i < distribution.length; i++) {
                out[i] = new int[2];
                out[i][0] = distribution[i][0];
            }
        }else{
            for (int i = 0; i < distribution.length; i++) {
                out[i] = new int[2];
                out[i][0] = distribution[i][0];
                out[i][1] = distribution[i][1];
            }
        }
        return out;
    }

    private void resetDist(int[][] distribution){
        for (int i = 0; i < distribution.length; i++) {
            distribution[i][1] = 0;
        }
    }

    private void shiftDist(int[][] distribution, int classLabel, int count){
        for (int i = 0; i < distribution.length; i++) {
            if (distribution[i][0] == classLabel){
                distribution[i][1] = distribution[i][1] + count;
                break;
            }
        }
    }

    public int getNumFeatures() {
        return numFeatures;
    }

    public void setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
    }

    public void setNumFeatures(
            int numFeatures, int length, FeatureSelectionMethod featureSelectionMethod) throws Exception {
        if (featureSelectionMethod == FeatureSelectionMethod.ConstantInt) {
            if (numFeatures <= 0 || numFeatures >= length) {
                setNumFeatures(length); // default for this case
            } else {
                setNumFeatures(numFeatures);
            }
        } else if (featureSelectionMethod == FeatureSelectionMethod.Sqrt) {
            setNumFeatures((int) Math.sqrt(length));
        } else {
            throw new Exception("Unknown feature selection method");
        }
    }

    public int getBestAttribute() {
        return bestAttribute;
    }

    public void setBestAttribute(int best_attribute) {
        this.bestAttribute = best_attribute;
    }

    public double getBestThreshold() {
        return bestThreshold;
    }

    public void setBestThreshold(double best_threshold) {
        this.bestThreshold = best_threshold;
    }

    public boolean getRandomizeAttribSelection() {
        return randomizeAttribSelection;
    }

    public void setRandomizeAttribSelection(boolean randomize) {
        this.randomizeAttribSelection = randomize;
    }

    public String toString() {
        return "GiniSplitter[m=" + numFeatures + ", a=" + bestAttribute + ", t=" + bestThreshold + "]";
    }

    public static void main(String[] args) throws Exception {

//        long seed = 0;
        long seed = System.nanoTime();

//        String datasetName = "DistalPhalanxTW"; //
        String datasetName = "ItalyPowerDemand"; //

        Dataset[] datasets = new DataLoader(DataLoader.univariateUAE2018).loadTrainAndTestSet(datasetName);
        Instances[] dataInstances = new Instances[2];
        dataInstances[0] = FastRandomTreeSplitterV2.loadDataThrowable("E:/data/Univariate2018_arff/" + datasetName + "/" + datasetName + "_TRAIN.arff");
        dataInstances[1] = FastRandomTreeSplitterV2.loadDataThrowable("E:/data/Univariate2018_arff/" + datasetName + "/" + datasetName + "_TEST.arff");
        int numFeatures = datasets[0].length();

        FastRandomTreeSplitterV1 splitterV1 = new FastRandomTreeSplitterV1(new Random(seed));
        splitterV1.setNumFeatures(numFeatures);
        ArrayIndex indices2 = new ArrayIndex(datasets[0]);
        indices2.syncByReplacing(datasets[0]);
        NodeSplitterResult result3 = splitterV1.fitByIndices(datasets[0], indices2);
        if (result3 != null){
            System.out.println("splitterV1 A: " + splitterV1.bestAttribute + " T: " + splitterV1.bestThreshold);
        }

        FastRandomTreeSplitterV2 splitterV2 = new FastRandomTreeSplitterV2(new Random(seed));
        splitterV2.setNumFeatures(numFeatures);
        NodeSplitterResult result = splitterV2.fit(datasets[0], null);
        if (result != null){
            System.out.println("splitterV2 A: " + splitterV2.bestAttribute + " T: " + splitterV2.bestThreshold);
        }

        WekaRandomTree wekaRTree = new WekaRandomTree();
        wekaRTree.setSeed((int) seed);
        wekaRTree.setKValue(numFeatures);
        wekaRTree.setMaxDepth(0);
        wekaRTree.buildClassifier(dataInstances[0]);
        System.out.println("WekaRT A: " + wekaRTree.m_Tree.m_Attribute + " T: " + wekaRTree.m_Tree.m_SplitPoint);


        assert splitterV1.bestAttribute == splitterV2.bestAttribute;
        assert splitterV1.bestThreshold == splitterV2.bestThreshold;

        // This assertion fails if smallest gini is same for more than one attrib e.g. DistalPhalanxTW
        // because attributes are evaluated in random order and
        // one implementation updates the best gini using < and the other using <= operator
        assert wekaRTree.m_Tree.m_Attribute == splitterV2.bestAttribute;
        assert wekaRTree.m_Tree.m_SplitPoint == splitterV2.bestThreshold;


        FastRandomTreeSplitterV2 splitter2 = new FastRandomTreeSplitterV2(new Random(seed));
        splitter2.setNumFeatures(numFeatures);
        ArrayIndex indices = new ArrayIndex(datasets[0]);
        indices.syncByReplacing(datasets[0]);
        NodeSplitterResult result2 = splitter2.fitByIndices(datasets[0], indices);
        if (result2 != null){
            System.out.println("splitterV2 Indices - A: " + splitter2.bestAttribute + " T: " + splitter2.bestThreshold);
        }

    }

    private static Instances loadDataThrowable(String fileName) throws IOException {
        File targetFile = new File(fileName);
        FileReader reader = new FileReader(targetFile);
        Instances inst = new Instances(reader);
        inst.setClassIndex(inst.numAttributes() - 1);
        reader.close();
        return inst;
    }

}
