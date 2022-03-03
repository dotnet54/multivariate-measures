package dotnet54.classifiers.tschief.splitters.dev;

import dotnet54.applications.tschief.TSChiefOptions.FeatureSelectionMethod;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.tscore.exceptions.MultivariateDataNotSupportedException;
import dotnet54.tscore.exceptions.NotSupportedException;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;

import java.util.*;

public class FastRandomTreeSplitterV1 extends NodeSplitter {

    protected final int NUM_CHILDREN = 2;
    protected final int LEFT_BRANCH = 0;
    protected final int RIGHT_BRANCH = 1;

    public int numFeatures;

    // if false, selects the first m attributes that can give a valid split
    protected boolean randomizeAttribSelection = true;

    protected int bestDimension = 0; // TODO currently supports only 1 dimension
    public int bestAttribute;
    public double bestThreshold;

    // extra info to help with debugging
    protected int bestSplitIndex; // split point is between bestSplitIndex and bestSplitIndex + 1
    public double bestWGini;
    protected int[][] leftDist;
    protected int[][] rightDist;

    protected Random rand;
    protected boolean debugMode = false;
    public double[][] bestParamsPerAttrib; // attribIndex, threshold, wgini, splitPoint

    public FastRandomTreeSplitterV1(Random rand) {
        this.rand = rand;
    }

    public FastRandomTreeSplitterV1(Random rand, int numFeatures) {
        this.numFeatures = numFeatures;
        this.rand = rand;
    }


    public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices){
        throw new NotSupportedException();
    }


    public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices){
        throw new NotSupportedException();
    }

    public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices)
            throws Exception {

        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        // for multivariate
        final int DEFAULT_DIM = 0;

        // for extracting columns
        final int COL_ATTRIB = 0;
        final int COL_CLASS = 1;

        // for class distributions
        final int DIST_CLASS = 0;
        final int DIST_COUNT = 1;

        int nodeSize = nodeTrainData.size();
        int length = nodeTrainData.length();
        int numClasses = nodeTrainData.getNumClasses();
        TIntIntMap parentClassDist = nodeTrainData.getClassDistribution();

        // if no  -- return null, caller can convert this node to a leaf or throw an exception
        if (nodeSize == 0) {
            return null;
        }

        if (numFeatures == 0 || numFeatures > length) {
            numFeatures = length;
        }

        boolean canSplit = false;
        boolean canUseAttribute = false;
        double weightedGini = Double.POSITIVE_INFINITY;
        double bestWeightedGini = Double.POSITIVE_INFINITY;
        int leftNodeSize = 0, rightNodeSize;
        double leftGini, rightGini;
        int currentClassLabel;
        double currentThreshold;
        int numAttribsUsed = 0; // if an attribute is not useful we keep trying until numAttribsUsed <= m
        int[][] parentDist = mapToArray(parentClassDist);
        int[][] tmpLeftDist = new int[numClasses][2];
        int[][] tmpRightDist = new int[numClasses][2];
        double[][] columnWithClass = new double[nodeSize][2];
        double bestThresholdSoFar = 0;
        int bestAttributeSoFar = 0;
        int bestSplitIndexSoFar = 0; // debug -> best threshold is selected after this index in the column
        bestParamsPerAttrib = new double[length][4];

        int[] attributes = new int[length]; // use length here, not numFeatures
        for (int i = 0; i < length; i++) {
            attributes[i] = i;
        }

        if (randomizeAttribSelection) {
            shuffleArray(attributes);
        }

        for (int i = 0; i < attributes.length && numAttribsUsed <= numFeatures; i++) {
            canUseAttribute = false;
            reset2DDoubleArray(columnWithClass);
            tmpLeftDist = copyDist(parentDist, true);
            tmpRightDist = copyDist(parentDist, false);
            leftNodeSize = 0;
            rightNodeSize = nodeSize - leftNodeSize;


            // extract the column as attrib, class pair (class labels are converted to doubles)
            // using only dimension 0
            for (int j = 0; j < nodeSize; j++) {
                columnWithClass[j][COL_ATTRIB] = nodeTrainData.getSeries(j).value(DEFAULT_DIM, attributes[i]);
                columnWithClass[j][COL_CLASS] = nodeTrainData.getClass(j).doubleValue();
            }
            // sort the pairs based on attribute values
            Arrays.sort(columnWithClass, Comparator.comparingDouble(o -> o[COL_ATTRIB]));

            // set first value as the initial threshold
            currentThreshold = columnWithClass[0][COL_ATTRIB];

            for (int j = 0; j < nodeSize - 1; j++) {
                currentClassLabel = (int) columnWithClass[j][COL_CLASS];

                leftNodeSize++;
                shiftDist(tmpLeftDist, currentClassLabel, +1);
                shiftDist(tmpRightDist, currentClassLabel, -1);
                rightNodeSize = nodeSize - leftNodeSize;

                if (columnWithClass[j + 1][COL_ATTRIB] > columnWithClass[j][COL_ATTRIB]) {
                    canSplit = true; // there is at least two distinct columnWithClass for at least one attribute
                    canUseAttribute = true;

                    leftGini = gini(tmpLeftDist, leftNodeSize);
                    rightGini = gini(tmpRightDist, rightNodeSize);

                    weightedGini = ((double) leftNodeSize / nodeSize * leftGini)
                                    + ((double) rightNodeSize / nodeSize * rightGini);

                    // NOTE equal case not handled explicitly
                    if (weightedGini < bestWeightedGini) {
                        bestWeightedGini = weightedGini;
                        bestAttributeSoFar = attributes[i];
                        // bug 30/3/2021
//                        currentThreshold = (currentThreshold + columnWithClass[j + 1][COL_ATTRIB]) / 2;
                        // fix 30/3/2021
                        currentThreshold = (columnWithClass[j][COL_ATTRIB] + columnWithClass[j + 1][COL_ATTRIB]) / 2;
                        bestThresholdSoFar = currentThreshold;
                        bestSplitIndexSoFar = j;

                        leftDist = copyDist(tmpLeftDist, false);
                        rightDist = copyDist(tmpRightDist, false);

                    } // end best case update
                } // end split point
            } // end each attribute

            if (canUseAttribute) {
                bestAttribute = bestAttributeSoFar;
                bestThreshold = bestThresholdSoFar;
                bestSplitIndex = bestSplitIndexSoFar;
                bestWGini = bestWeightedGini;
                bestParamsPerAttrib[attributes[numAttribsUsed]][0] = bestAttributeSoFar;
                bestParamsPerAttrib[attributes[numAttribsUsed]][1] = bestThresholdSoFar;
                bestParamsPerAttrib[attributes[numAttribsUsed]][2] = bestWeightedGini;
                bestParamsPerAttrib[attributes[numAttribsUsed]][3] = bestSplitIndexSoFar;
                numAttribsUsed++;
            } else {
                // no gain from this attribute, skip this attrib and try the next one
//                TSChiefOptions.logger.warn("No gain found in the attribute " + attributes[i]
//                + " numFeatures " + numFeatures);
            }
        } // end attrib loop

        // if we can make a sensible split using any attribute, split the ,
        // else return null -> assign max gini to this during evaluation, if there
        // are no splitter better than that, use class distribution at node for prediction;
        if (canSplit) {
            return splitByIndices(nodeTrainData, trainIndices);
        } else {
            // if no attribute can give us a valid split point eg. if all columnWithClass are same
            //			System.out.println("cannot split this  in a sensible way...");
            return null;
        }
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
        for (int i = 0; i < indices.length; i++) {
            // not using indices[i] here because nodeTrainDataset is a subset and it may be
            // transformed 
            series = nodeTrainData.getSeries(i);
            if (series.data()[bestDimension][bestAttribute] < bestThreshold) {
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

    //TODO ONLY FOR DEBUGGING -- REMOVE THIS
    // keeping a copy so that we can inspect it in the predict method
//    private transient Dataset nodeTrainData;


    public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {

//        //TODO ONLY FOR DEBUGGING -- REMOVE THIS
//        int trainSize = nodeTrainData.size();
//        double[][] trainColumn = new double[trainSize][2];
//        double[][] trainSortedColumn;
//        // extract the column
//        for (int j = 0; j < trainSize; j++) {
//            trainColumn[j][0] = nodeTrainData.getSeries(j).value(0, bestAttribute);
//            trainColumn[j][1] = nodeTrainData.getClass(j).doubleValue();
//        }
//        trainSortedColumn = Arrays.stream(trainColumn).map(double[]::clone).toArray(double[][]::new);
//        Arrays.sort(trainSortedColumn, Comparator.comparingDouble(o -> o[0]));


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

}
