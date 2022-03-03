package dotnet54.classifiers.tschief.splitters.dev;

import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.applications.tschief.TSChiefOptions.FeatureSelectionMethod;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.tscore.data.ClassDistribution;
import dotnet54.tscore.exceptions.MultivariateDataNotSupportedException;
import dotnet54.tscore.exceptions.NotSupportedException;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;
import dotnet54.util.storage.pair.DblIntPair;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import dotnet54.util.Util;

import java.util.*;

public class RandomTreeSplitterMultiv extends NodeSplitter {

    protected final int NUM_CHILDREN = 2;
    protected final int LEFT_BRANCH = 0;
    protected final int RIGHT_BRANCH = 1;

    protected int bestDimension = 0; // TODO currently supports only 1 dimension
    protected int bestAttribute;
    protected double bestThreshold;

    protected int numFeatures;
    protected FeatureSelectionMethod featureSelectionMethod;

    // if false, selects the first m attributes that can give a valid split
    protected boolean randomizeAttribSelection = true;

    // extra info to help with debugging
    protected int bestSplitIndex; // split point is between bestSplitIndex and bestSplitIndex + 1
    protected double bestGini;
    protected int[][] leftDist;
    protected int[][] rightDist;

    protected boolean debugMode = false; //TODO set this to false!

    public RandomTreeSplitterMultiv(TSChiefNode node) throws Exception {
        this.node = node;
    }

    public RandomTreeSplitterMultiv(TSChiefNode node, int numFeatures) throws Exception {
        this.node = node;
        this.numFeatures = numFeatures;
    }

    public RandomTreeSplitterMultiv(
            TSChiefNode node, FeatureSelectionMethod feature_selection_method) throws Exception {
        this.node = node;
        this.featureSelectionMethod = feature_selection_method;
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

    @Override
    public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
        throw new NotSupportedException();
    }

    @Override
    public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices)
            throws Exception {
        throw new NotSupportedException();
    }

    public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices)
            throws Exception {
        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        int nodeSize = nodeTrainData.size();
        int length = nodeTrainData.length();

        // if no  -- return null, caller can convert this node to a leaf or throw an exception
        if (nodeSize == 0) {
            return null;
        }

        // if data.size == 1 or data.gini() == 0 -> these cases are handled at node.train();
        if (featureSelectionMethod == FeatureSelectionMethod.ConstantInt) {
            //pass
        } else if (featureSelectionMethod == FeatureSelectionMethod.Sqrt) {
            numFeatures = (int) Math.sqrt(length);
        } else if (featureSelectionMethod == FeatureSelectionMethod.Loge) {
            numFeatures = (int) Math.log(length);
        } else if (featureSelectionMethod == FeatureSelectionMethod.Log2) {
            numFeatures = (int) (Math.log(length / Math.log(2)));
        } else {
            numFeatures = length;
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
        int numAttribsUsed = 0; // if an attribute is not useful we keep trying until numAttribsUsed <= m
        TIntIntMap leftClassDist = new TIntIntHashMap();
        TIntIntMap rightClassDist = new TIntIntHashMap();
        List<DblIntPair> columnWithClass = new ArrayList<DblIntPair>(nodeSize);
        double bestThresholdSoFar = 0;
        int bestAttributeSoFar = 0;
        int bestSplitIndexSoFar = 0; // debug -> best threshold is selected after this index in the column
        double[] bestGiniPerAttrib = new double[length];
        int[] bestSplitIndexPerAttrib = new int[length];

        int[] attributes = new int[length]; // use length here, not numFeatures
        for (int i = 0; i < length; i++) {
            attributes[i] = i;
        }

        if (randomizeAttribSelection) {
            // NOTE performance -- this is not an inplace shuffle, change this to Yates-Fisher
            attributes = Util.shuffleArray(attributes);
        }

        // TODO check numAttribsUsed <= m or numAttribsUsed < m
        for (int i = 0; i < attributes.length && numAttribsUsed <= numFeatures; i++) {
            canUseAttribute = false;
            columnWithClass.clear(); // O(n) complexity
            leftClassDist.clear();
            rightClassDist.clear();
            leftNodeSize = 0;

            // clone the class distribution from the dataset
            TIntIntMap classDist = nodeTrainData.getClassDistribution();
            for (int key : classDist.keys()) {
                rightClassDist.put(key, classDist.get(key));
            }
            // extract the column
            for (int j = 0; j < nodeSize; j++) {
                // TODO using only dimension 1
                DblIntPair pair = new DblIntPair(nodeTrainData.getSeries(j)
                                .value(0, attributes[i]), nodeTrainData.getClass(j));
                columnWithClass.add(pair);
            }
            // sort the pairs
            // NOTE comparing doubles without a threshold
            Collections.sort(columnWithClass,(a, b) -> {
                        return Double.compare(a.key, b.key);
                    });

            double currentThreshold = columnWithClass.get(0).key;
            rightNodeSize = nodeSize - leftNodeSize;

            for (int j = 0; j < nodeSize - 1; j++) {
                currentClassLabel = columnWithClass.get(j).value;

                leftNodeSize++;
                leftClassDist.adjustOrPutValue(currentClassLabel, 1, 1);
                rightClassDist.adjustOrPutValue(currentClassLabel, -1, 0);
                rightNodeSize = nodeSize - leftNodeSize;

                if (columnWithClass.get(j + 1).key > columnWithClass.get(j).key) {
                    canSplit = true; // there is at least two distinct columnWithClass for at least one attribute
                    canUseAttribute = true;

                    leftGini = Util.gini(leftClassDist, leftNodeSize);
                    rightGini = Util.gini(rightClassDist, rightNodeSize);

                    weightedGini = ((double) leftNodeSize / nodeSize * leftGini)
                                    + ((double) rightNodeSize / nodeSize * rightGini);

                    // NOTE equal case not handled explicitly
                    if (weightedGini < bestWeightedGini) {
                        bestWeightedGini = weightedGini;
                        bestAttributeSoFar = attributes[i];
                        currentThreshold = (currentThreshold + columnWithClass.get(j + 1).key) / 2;
                        bestThresholdSoFar = currentThreshold;
                        bestSplitIndexSoFar = j;

                        leftDist = ClassDistribution.mapToArray(leftClassDist);
                        rightDist = ClassDistribution.mapToArray(rightClassDist);

                    } // end best case update
                } // end split point
            } // end each attribute

            if (canUseAttribute) {
                bestAttribute = bestAttributeSoFar;
                bestThreshold = bestThresholdSoFar;
                bestSplitIndex = bestSplitIndexSoFar;
                bestGiniPerAttrib[attributes[numAttribsUsed]] = bestWeightedGini;
                bestSplitIndexPerAttrib[attributes[numAttribsUsed]] = bestSplitIndexSoFar;
                numAttribsUsed++;
            } else {
                // no gain from this attribute, skip this attrib and try the next one
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

    @Override
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
}
