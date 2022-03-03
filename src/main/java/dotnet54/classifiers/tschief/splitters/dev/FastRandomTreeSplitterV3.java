package dotnet54.classifiers.tschief.splitters.dev;

import dotnet54.applications.tschief.TSChiefOptions.FeatureSelectionMethod;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.tscore.data.*;
import dotnet54.tscore.exceptions.MultivariateDataNotSupportedException;
import dotnet54.tscore.exceptions.NotSupportedException;
import dotnet54.tscore.io.DataLoader;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import weka.core.Instances;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FastRandomTreeSplitterV3 extends NodeSplitter {

    // for multivariate

    public Random rand;
    public int numFeatures;
    public boolean randomizeAttribSelection = true;

    public int bestDimension; // TODO currently supports only 1 dimension
    public int bestAttribute;
    public double bestThreshold;
    public double bestWGini;
    public int bestSplitPoint; // index after which bestThreshold was calculated

    // TODO annotate GSON adaptor
    public int[][] leftDist;
    public int[][] rightDist;

    // extras for debugging
    public boolean debugMode = true;

    // TODO annotate GSON adaptor
    public transient double[][] bestParamsPerAttrib; // attribIndex, threshold, wgini, splitPoint

//    splitIndices

    public FastRandomTreeSplitterV3(Random rand) {
        this.rand = rand;
    }

    public FastRandomTreeSplitterV3(Random rand, int numFeatures) {
        this.numFeatures = numFeatures;
        this.rand = rand;
    }

    public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
        throw new NotSupportedException();
    }

    public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices){
        throw new NotSupportedException();
    }


    /**
     *
     * Make sure that the original labels are encoded using a label encoder (i.e class values are continuous)
     *
     * @param nodeTrainData
     * @param trainIndices
     * @return
     * @throws Exception
     */
    // TODO BUG not using indices
    public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices)
            throws Exception {

        if (nodeTrainData.isMultivariate()) {
            throw new MultivariateDataNotSupportedException();
        }

        int nodeSize = nodeTrainData.size();
        int length = nodeTrainData.length();
        int numClasses = nodeTrainData.getNumClasses(); // numClasses in the full training set

        boolean canSplit = false;
        int numUsableAttribs = 0;
        int[] attributes = new int[length];
        double currentValue, prevValue;
        double currentThreshold = Double.NaN;
        int currSplitPoint = 0; // just for debugging, its the index after which currentThreshold is computed
        int leftSize = 0, rightSize = nodeSize - leftSize;
        double leftGini, rightGini;
        double currentWGini = Double.POSITIVE_INFINITY, wGini = Double.POSITIVE_INFINITY;  //weighted Gini
        double[][] columnData = new double[nodeSize][2]; // attribute and class
        TIntIntMap parentClassDist = nodeTrainData.getClassDistribution();
        int[][] parentDist = mapTo2DArray(parentClassDist);
        int[][] currLeftDist = new int[numClasses][2];
        int[][] currRightDist = new int[numClasses][2];
//        System.arraycopy(parentDist,0, currRightDist, 0, currRightDist.length);
        currRightDist = copy2DDist(parentDist, false);

        leftDist = new int[numClasses][2];
        rightDist = new int[numClasses][2];
        bestAttribute = -1;
        bestThreshold = Double.NaN;
        bestWGini = Double.POSITIVE_INFINITY;
        bestSplitPoint = -1;

        // if in debug mode, save all intermediate results
        if (debugMode){
//            bestParamsPerAttrib = new double[numFeatures][4]; // [attribIndex, threshold, wgini, splitPoint]
            bestParamsPerAttrib = new double[attributes.length][4]; // [attribIndex, threshold, wgini, splitPoint]
        }

        // if return == null, caller can convert this node to a leaf or throw an exception
        if (nodeSize == 0) {
            return null;
        }

        if (numFeatures <= 0 || numFeatures > length) {
            numFeatures = length;
        }

        // use length here instead of numFeatures
        for (int i = 0; i < length; i++) {
            attributes[i] = i;
        }

        // shuffle the original list of attributes
        if (randomizeAttribSelection) {
            shuffleArray(attributes);
        }

        // numUsableAttribs is incremented if there is a gain found in using the attribute only,
        // so if we dont find a gain in the attribute, we may keep checking more than numFeatures up to attributes.length
        // numUsableAttribs - numFeatures == # extra attributes we checked since we ignored attributes without gain
        for (int j = 0; j < attributes.length && numUsableAttribs < numFeatures; j++) {
//        for (int j = 0; j < numFeatures; j++) {
            int attribIndex = attributes[j];

            if (j > numFeatures){
//                TSChiefOptions.logger.warn("Testing attribute " + j + " / " + numFeatures + " gain found in " + numUsableAttribs);
            }

            // copy the column as an [attrib, class] pair to sort them
            // this is more expensive than using nodeTrainData.sort(attribIndex), but this is thread safe
            for (int i = 0; i < nodeSize; i++) {
                columnData[i][0] = nodeTrainData.getSeries(i).value(0, attribIndex);
                columnData[i][1] = nodeTrainData.getClass(i).doubleValue();
            }
            // sort the pairs based on attribute values
            Arrays.sort(columnData, Comparator.comparingDouble(o -> o[0]));

            // set first value as the initial threshold, first iteration initializes the currLeftDist
            prevValue = columnData[0][0]; // nodeTrainData.getSeries(0).value(attibIndex);

            for (int i = 0; i < nodeSize; i++) {
                currentValue = columnData[i][0]; // series.value(attibIndex);

                // if we can place a split point here
                if (prevValue < currentValue){

                    // find the gini of each branch for splitting here
                    leftGini = gini2D(currLeftDist, leftSize);
                    rightGini = gini2D(currRightDist, rightSize);

                    // find weighted gini using the fraction of data at each branch
                    wGini = ((double) leftSize / nodeSize * leftGini)
                            + ((double) rightSize / nodeSize * rightGini);

                    // we want to minimize the weighted gini (this maximizes the gain)
                    if (wGini < currentWGini){

                        currentWGini = wGini;
                        currentThreshold = (prevValue + currentValue) / 2;
                        currSplitPoint = i - 1; // index after which this currentThreshold was found

                        // check for numeric precision issues -> based on the Weka implementation of RandomTree
                        if (currentThreshold <= prevValue) {
                            currentThreshold = currentValue;
                        }

                        // save best left and right distribution
//                        System.arraycopy(currLeftDist,0, leftDist, 0, leftDist.length);
//                        System.arraycopy(currRightDist,0, rightDist, 0, rightDist.length);
                        leftDist = copy2DDist(currLeftDist, false);
                        rightDist = copy2DDist(currRightDist, false);
                    }
                }

                // update current value
                prevValue = currentValue;

                // shift distribution to the left
                int classValue = (int) columnData[i][1];
//                currLeftDist[classValue]++;
//                currRightDist[classValue]--;
                shift2DDist(currLeftDist, classValue, +1);
                shift2DDist(currRightDist, classValue, -1);
                leftSize++;
                rightSize = nodeSize - leftSize;
            }

            // if in debug mode, save all intermediate results
            if (debugMode){
                // if we found a split point, copy to best params for this feature
                bestParamsPerAttrib[j][0] = attribIndex;
                bestParamsPerAttrib[j][1] = currentThreshold;
                bestParamsPerAttrib[j][2] = currentWGini;
                bestParamsPerAttrib[j][3] = currSplitPoint;

                if (Double.isNaN(currentThreshold)) {
                    // no gain from this attribute
                    // this could happen if data has no difference in the values
                    // e.g. badly labeled data, uniform data coming from a sensor - no change in values across instances,
                    //      a data transformation with bad params produced all 0 values, or same values
                    // in such cases, try to use another attribute or if no attribute produces a gain,
                    // then convert node to a leaf, and then perhaps use ZeroR model for prediction (majority vote on class)

//                    TSChiefOptions.logger.warn("No gain found in the attribute " + attribIndex
//                    + " numFeatures " + numFeatures);

                }else{
                    // this attribute can produce a valid split
                    numUsableAttribs++;
                }
            }

            // copy to best params out of all features
            if (currentWGini < bestWGini){
                bestAttribute = attribIndex;
                bestThreshold = currentThreshold;
                bestWGini = currentWGini;
                bestSplitPoint = currSplitPoint;
                canSplit = true;
            }

            // reset to reuse next turn
//            resetIntArray(currLeftDist);
//            System.arraycopy(parentDist,0, currRightDist, 0, currRightDist.length);
            reset2DIntArray(currLeftDist);
            currRightDist = copy2DDist(parentDist, false);
            leftSize = 0;
            rightSize = nodeSize - leftSize;
            currentThreshold = Double.NaN;
            currentWGini = Double.POSITIVE_INFINITY;
            currSplitPoint = 0;
        }

        // split the data if we can split
        if (canSplit) {
            return splitByIndices(nodeTrainData, trainIndices);
        } else {
            // none of the attributes provided any gain, e.g if all have same data repeated
            // if return == null, caller can convert this node to a leaf and use a zeroR model to classify
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
        NodeSplitterResult result = new NodeSplitterResult(this, 2);
        int[] indices = trainIndices.getIndices();
        result.splitIndices.put(0, new TIntArrayList(indices.length / 2));
        result.splitIndices.put(1, new TIntArrayList(indices.length / 2));
        // assert nodeTrainData.size() == indices.length
        // assert order of  in nodeTrainData and order of indices to original  match
        // TODO BUG use indices
        for (int i = 0; i < indices.length; i++) {
            // not using indices[i] here because nodeTrainDataset is a subset and it may be transformed
            series = nodeTrainData.getSeries(i);
            double attribVal = series.data()[bestDimension][bestAttribute];
            if (attribVal < bestThreshold) {
                result.splitIndices.get(0).add(indices[i]);
            } else {
                result.splitIndices.get(1).add(indices[i]);
            }
        }
        return result;
    }

    public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
        double attribVal = query.data()[bestDimension][bestAttribute];
        if (attribVal < bestThreshold) {
            return 0;
        } else {
            return 1;
        }
    }

    private double gini(int[] distribution, int totalSize) {
        double sum = 0.0;
        double p;
        for (int i = 0; i < distribution.length; i++) {
            p = (double) distribution[i] / totalSize;
            sum += p * p;
        }
        return 1 - sum;
    }

    private double gini2D(int[][] distribution, int totalSize) {
        double sum = 0.0;
        double p;
        for (int i = 0; i < distribution.length; i++) {
            p = (double) distribution[i][1] / totalSize;
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

    private int[] mapToArray(TIntIntMap distribution){
        int[] dist = new int[distribution.size()];
        int i = 0;
        for (int key : distribution.keys()) {
            dist[i] = distribution.get(key);
            i++;
        }
        return dist;
    }

    private int[][] mapTo2DArray(TIntIntMap distribution){
        int[][] dist = new int[distribution.size()][2];
        int i = 0;
        for (int key : distribution.keys()) {
            dist[i][0] = key;
            dist[i][1] = distribution.get(key);
            i++;
        }
        return dist;
    }

    private void resetIntArray(int[] array){
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }

    private void reset2DIntArray(int[][] array){
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][j] = 0;
            }
        }
    }

    private void resetDoubleArray(double[] array){
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
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

    private int[][] copy2DDist(int[][] distribution, boolean reset){
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

    private void reset2DDist(int[][] distribution){
        for (int i = 0; i < distribution.length; i++) {
            distribution[i][1] = 0;
        }
    }

    private void shift2DDist(int[][] distribution, int classLabel, int count){
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
        dataInstances[0] = FastRandomTreeSplitterV3.loadDataThrowable("E:/data/Univariate2018_arff/" + datasetName + "/" + datasetName + "_TRAIN.arff");
        dataInstances[1] = FastRandomTreeSplitterV3.loadDataThrowable("E:/data/Univariate2018_arff/" + datasetName + "/" + datasetName + "_TEST.arff");
        int numFeatures = datasets[0].length();

        FastRandomTreeSplitterV1 splitterV1 = new FastRandomTreeSplitterV1(new Random(seed));
        splitterV1.setNumFeatures(numFeatures);
        ArrayIndex indices2 = new ArrayIndex(datasets[0]);
        indices2.syncByReplacing(datasets[0]);
        NodeSplitterResult result3 = splitterV1.fitByIndices(datasets[0], indices2);
        if (result3 != null){
            System.out.println("splitterV1 A: " + splitterV1.bestAttribute + " T: " + splitterV1.bestThreshold);
        }

        FastRandomTreeSplitterV2 splitterV3 = new FastRandomTreeSplitterV2(new Random(seed));
        splitterV3.setNumFeatures(numFeatures);
        NodeSplitterResult result = splitterV3.fit(datasets[0], null);
        if (result != null){
            System.out.println("splitterV3 A: " + splitterV3.bestAttribute + " T: " + splitterV3.bestThreshold);
        }

        WekaRandomTree wekaRTree = new WekaRandomTree();
        wekaRTree.setSeed((int) seed);
        wekaRTree.setKValue(numFeatures);
        wekaRTree.setMaxDepth(0);
        wekaRTree.buildClassifier(dataInstances[0]);
        System.out.println("WekaRT A: " + wekaRTree.m_Tree.m_Attribute + " T: " + wekaRTree.m_Tree.m_SplitPoint);


        assert splitterV1.bestAttribute == splitterV3.bestAttribute;
        assert splitterV1.bestThreshold == splitterV3.bestThreshold;

        // This assertion fails if smallest gini is same for more than one attrib e.g. DistalPhalanxTW
        // because attributes are evaluated in random order and
        // one implementation updates the best gini using < and the other using <= operator
        assert wekaRTree.m_Tree.m_Attribute == splitterV3.bestAttribute;
        assert wekaRTree.m_Tree.m_SplitPoint == splitterV3.bestThreshold;


        FastRandomTreeSplitterV3 splitter2 = new FastRandomTreeSplitterV3(new Random(seed));
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
