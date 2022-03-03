package dotnet54.classifiers.tschief;

import dotnet54.classifiers.tschief.results.TSChiefForestResult;
import dotnet54.classifiers.tschief.results.TSChiefTreeResult;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.classifiers.tschief.splitters.boss.BossSplitterV2;
import dotnet54.classifiers.tschief.splitters.ee.ElasticDistanceSplitterWithPEA;
import dotnet54.classifiers.tschief.splitters.rise.RISESplitterV2;
import dotnet54.measures.multivariate.MultivariateMeasure;
import dotnet54.measures.univariate.UnivariateMeasure;
import dotnet54.classifiers.tschief.splitters.boss.BossSplitterV1;
import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.tscore.data.ArrayIndex;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;
import dotnet54.util.InputParser;
import dotnet54.util.Util;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.splitters.ee.ElasticDistanceSplitter;
import dotnet54.classifiers.tschief.splitters.ee.MultivariateElasticDistanceSplitter;
import dotnet54.classifiers.tschief.splitters.it.InceptionTimeSplitter;
import dotnet54.classifiers.tschief.splitters.randf.RandomForestSplitter;
import dotnet54.classifiers.tschief.splitters.rise.RISESplitterV1;
import dotnet54.classifiers.tschief.splitters.rt.RocketTreeSplitter;
import dotnet54.classifiers.tschief.splitters.st.ForestShapeletTransformSplitter;
import dotnet54.classifiers.tschief.splitters.st.RandomShapeletTransformSplitter;
import dotnet54.util.Sampler;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.*;

public class TSChiefNode {

    public transient TSCheifTree tree;
    public transient TSChiefTreeResult result;
    public transient TSChiefOptions tsChiefOptions;
    public transient TSChiefNode parent;

    public Random rand;

    public int nodeID;
    public int nodeDepth;
    public int nodeSize;
    public boolean isFitted;
    public boolean isLeaf;
    public Integer label;
    public TIntObjectMap<TSChiefNode> children;
    public NodeSplitter bestSplitter;

    // for dev use
    public Integer branchID;
    public transient boolean debugMode = true;
    public transient boolean clearMemory = true;
    public transient boolean storeAllSplitsInMemory = true; // store all splits to avoid resplitting TODO
    public transient boolean terminateOnWarning = false;
    public TIntIntMap trainDistribution; // class distribution of data reaching this node during train time
    public TIntIntMap testDistribution; // class distribution of data reaching this node during test time
    public double trainDataGini;
    public transient boolean reTrainUsingFullData = false;

    // temp caches

    // free the memory after train
    // cached to be used in nearest class splitters e.g. ee, boss
    public transient TIntObjectMap<Dataset> trainDataPerClass;
    public transient TIntObjectMap<Dataset> trainSamplePerClass;

    public List<Pair<String, Double>> weightedGiniPerSplitter = new ArrayList<>();
//    public Mean meanWeightedGini = new Mean();
//    public StandardDeviation sdtvWeightedGini = new StandardDeviation();

    // if in debug mode we can keep all trained splitters at the node ~ THIS MAY USE A LOT OF MEMORY
//    protected List<NodeSplitter> splitters = new ArrayList<NodeSplitter>(tsChiefOptions.num_actual_splitters_needed);
//    protected List<NodeSplitter> topSplitters = new ArrayList<NodeSplitter>(5);

    // dev only - not fully implemented
    public EnumeratedDistribution numSplitSelectorDist;

    public TSChiefNode(TSCheifTree tree, TSChiefNode parent, int nodeID, Integer branchID) {
        this.tree = tree;
        this.parent = parent;
        this.nodeID = nodeID;
        this.tsChiefOptions = tree.tsChiefOptions;
        this.rand = tsChiefOptions.getRand();
        this.branchID = branchID;
        this.isLeaf = false;

        if (tree == null) {
            this.result = new TSChiefTreeResult(null);
        } else {
            this.result = tree.result;
        }

        if (parent == null) {
            this.nodeDepth = 0;
        } else {
            this.nodeDepth = parent.nodeDepth + 1;
        }

    }

    public void fit(Dataset trainData, DatasetIndex trainIndices) throws Exception {
        this.nodeSize = trainData.size();

        if (trainData != null){
            // TODO create a clone
            this.trainDistribution = trainData.getClassDistribution();
            this.trainDataGini = Util.gini(this.trainDistribution);

            // just allocating the initial capacity, this is populated in predict calls
            this.testDistribution = new TIntIntHashMap(trainDistribution.size()); // fill during test time
        }else{
            throw new RuntimeException("Error: cannot train the node, training data is null at the node");
        }


        // if stopping conditions are reached, convert the node to a leaf and then return
        if (stopBuildingTree(trainData, trainIndices)){
            return;
        }

        // node training
        TIntObjectMap<Dataset> bestSplit = null;

        // if using top k roots
        if (tsChiefOptions.useBestPoolOfSplittersForRoots && nodeID != -1 && nodeDepth == 0) {
            TopKNodeSelector.GiniSplitterPair pair = tsChiefOptions.topKNodeSelector.next();
            if (pair != null){
                bestSplitter = pair.splitter;
                NodeSplitterResult bestNodeSplitterResult = bestSplitter.split(trainData, trainIndices);
                if (bestNodeSplitterResult != null){
                    bestSplit = bestNodeSplitterResult.splits;
                }
            }else{
                // if we fail to use topk splitters,
                // log a warning and then train normally (if num splitters trained by topk < k)
                Util.logger.info("TODO: top queue is empty - use normal training");
            }
        }

        // train normally
        if (bestSplit == null){
            bestSplit = fitSplitters(trainData, trainIndices);
        }

        // if we cannot do a sensible split on the training data, convert the node to a leaf and return
        if (bestSplit == null || TSCheifTree.isValidSplit(bestSplit)) {
            this.label = Util.majority_class(trainData);
            this.isLeaf = true;
            if (terminateOnWarning) {
                throw new RuntimeException("Failed to create a sensible split at node " + nodeID
                        + " with data " + this.trainDistribution + ", converting node to a leaf with label " + label);
            } else {
                Util.logger.warn("Failed to create a sensible split at node " + nodeID
                        + " with data " + this.trainDistribution + ", converting node to a leaf with label " + label);
            }

            return;
        }

        // create child nodes first --  done separately from training loop for debugging reasons
        // (to set breakpoints and inspect all children before going into next level for training)
        this.children = new TIntObjectHashMap<TSChiefNode>(bestSplit.size());
        for (int key : bestSplit.keys()) {
            this.children.put(key, new TSChiefNode(tree, this, tree.numNodes++, key));
        }

        // train children using the best splitter
        for (int key : bestSplit.keys()) {
            TSChiefNode child = this.children.get(key);
            //TODO temp solution
            ArrayIndex childDataIndices;
            if (tree == null) {
                // root node -> top k splitter doesnt set tree and doesnt support approx gini
                childDataIndices = new ArrayIndex(trainData, bestSplit.get(key));
            } else {
                // by default this reference points to forest level training data, but the node reads the reference to the data through the tree,
                // this to support future ideas such as bagging e.g. to train each tree a different subset
                childDataIndices = new ArrayIndex(tree.treeLevelTrainingData, bestSplit.get(key));
            }
            this.children.get(key).fit(bestSplit.get(key), childDataIndices);
        }

        isFitted = true;
    }

    private boolean stopBuildingTree(Dataset trainData, DatasetIndex trainIndices){

        if (trainData == null) {
//            this.label = Util.majority_class(trainData); // NullPointerException
            this.isLeaf = true;
            if (tree != null){
                this.tree.numLeaves++;
            }
            if (terminateOnWarning) {
                throw new RuntimeException("Training data is null at the node");
            } else {
                Util.logger.warn("Training data is null at the node");
                return true;
            }
        }

        if (trainData.size() == 0) {
//            this.label = Util.majority_class(trainData); // NullPointerException
            this.isLeaf = true;
            if (tree != null){
                this.tree.numLeaves++;
                this.tree.depth = Math.max(nodeDepth, this.tree.depth);
            }
            if (terminateOnWarning) {
                throw new RuntimeException("Training data is empty at the node");
            } else {
                Util.logger.warn("Training data is empty at the node");
                return true;
            }
        }

        // TODO - compare doubles with a threshold
        if (trainData.gini() <= tsChiefOptions.leafGiniThreshold) {
            this.label = Util.majority_class(trainData);
            this.isLeaf = true;
            if (tree != null){
                this.tree.numLeaves++;
                this.tree.depth = Math.max(nodeDepth, this.tree.depth);
            }
            return true;
        }

        if (trainData.size() <= tsChiefOptions.minNodeSize) {
            this.label = Util.majority_class(trainData);
            this.isLeaf = true;
            if (tree != null){
                this.tree.numLeaves++;
                this.tree.depth = Math.max(nodeDepth, this.tree.depth);
            }
            return true;
        }

        // if maxDepth == -1, then there is no limit on the tree depth
        if (tsChiefOptions.maxDepth != -1 && nodeDepth >= tsChiefOptions.maxDepth) {
            this.label = Util.majority_class(trainData);
            this.isLeaf = true;
            if (tree != null){
                this.tree.numLeaves++;
                this.tree.depth = Math.max(nodeDepth, this.tree.depth);
            }
            return true;
        }

        return false;
    }

    private TIntObjectMap<Dataset> fitSplitters(Dataset trainData, DatasetIndex trainIndices) throws Exception {
        long nodeSplitDataStartTime = System.nanoTime();

        Dataset trainSample;
        ArrayIndex trainSampleIndices = null;

        double weightedGini = Double.POSITIVE_INFINITY;
        double bestWeightedGini = Double.POSITIVE_INFINITY;
        NodeSplitter currentSplitter = null;
        NodeSplitterResult currentSplitterResult = null;
        NodeSplitterResult bestNodeSplitterResult = null;

        // DEV -- storing all the results is memory inefficient, but easier to debug
        List<NodeSplitter> splitters = new ArrayList<NodeSplitter>(tsChiefOptions.num_actual_splitters_needed);
        List<NodeSplitterResult> splitterResults = new ArrayList<NodeSplitterResult>(tsChiefOptions.num_actual_splitters_needed);

        long nodeDataPrepStartTime = System.nanoTime();
        // split node data into classes once here -- to be used in nearest class based splitters, stratified sampling
        // remember to free this memory
        trainDataPerClass = trainData.splitByClass();
        if (tsChiefOptions.useApproxGini) {
            // make sure that at least two classes are in the sample, it makes no sense to find gini of a sample of single class as its always 0
            trainSample = sample(trainData);
            trainSampleIndices = new ArrayIndex(tree.treeLevelTrainingData, trainSample);
            trainSamplePerClass = trainSample.splitByClass();
        } else {
            trainSample = trainData;
            trainSampleIndices = (ArrayIndex) trainIndices;
            trainSamplePerClass = trainDataPerClass;
        }
        this.result.data_fetch_time += (System.nanoTime() - nodeDataPrepStartTime);

        // ee
        if (tsChiefOptions.ee_enabled) {
            fitEESplitter(trainSample, trainSampleIndices, splitters, splitterResults);
        }

        // boss version 1 - used in tschief paper
        if (tsChiefOptions.boss_enabled && tsChiefOptions.bossSplitterType == NodeSplitter.SplitterType.BossSplitterV1) {
            fitBossSplitterV1(trainSample, trainSampleIndices, splitters, splitterResults);
        }

        // boss version 2 - new enhancements since publication
        if (tsChiefOptions.boss_enabled && tsChiefOptions.bossSplitterType == NodeSplitter.SplitterType.BossSplitterV2) {
            fitBossSplitterV2(trainSample, trainSampleIndices, splitters, splitterResults);
        }

        // rise version 1 - used in tschief paper
        if (tsChiefOptions.rif_enabled && tsChiefOptions.rif_splitter_type == NodeSplitter.SplitterType.RIFSplitterV1) {
            fitRiseSplitterV1(trainSample, trainSampleIndices, splitters, splitterResults);
        }

        // rise version 2 - new enhancements since publication
        if (tsChiefOptions.rif_enabled && tsChiefOptions.rif_splitter_type == NodeSplitter.SplitterType.RIFSplitterV2) {
            fitRiseSplitterV2(trainSample, trainSampleIndices, splitters, splitterResults);
        }


        // find the best rise splitter
        int bestIndex = 0;
        int prevBestIndex = 0;
        for (int i = 0; i < splitterResults.size(); i++) {
            NodeSplitterResult result = splitterResults.get(i);
            if (result == null) {
                continue;
            }
            if (splitterResults.get(i).weightedGini < bestWeightedGini) {
                bestWeightedGini = splitterResults.get(i).weightedGini;
                bestIndex = i;
                prevBestIndex = i;
            } else if (weightedGini == bestWeightedGini) {
                // random tie break
                bestIndex = rand.nextBoolean() ? bestIndex : prevBestIndex;
            }
        }
        bestSplitter = splitters.get(bestIndex);
        bestNodeSplitterResult = splitterResults.get(bestIndex);

        // if an approximate gini was found using a subsample, then split the full node training data using best split function
        if (tsChiefOptions.useApproxGini & reTrainUsingFullData){
            bestNodeSplitterResult = bestSplitter.split(trainData, trainIndices);
            TSCheifTree.isValidSplit(bestNodeSplitterResult.splits);
        }

        //split the whole dataset using the (approximately) best splitter
//        if (tsChiefOptions.eeUseApproxGini){
//		    bestSplit =  best_splitter.split(, null);
//        }

        if (clearMemory) {
            trainDataPerClass = null;
            trainSamplePerClass = null;
            splitters = null;
            splitterResults = null;
            weightedGiniPerSplitter.clear();
        }

        if (bestNodeSplitterResult == null) {
            return null;
        }

        if (tsChiefOptions.verbosity > 4) {
            System.out.print(" BEST:" + bestSplitter.toString() + ", wgini:" + tsChiefOptions.decimalFormat.format(bestWeightedGini) + ", splits:");
            for (int key : bestNodeSplitterResult.splits.keys()) {
                System.out.print(" " + bestNodeSplitterResult.splits.get(key).toString());
            }
            System.out.println();
        }

        storeStatsForBestSplitter();

//		if (has_empty_split(bestSplit)) {
//			throw new Exception("empty splits found! check for bugs");//
//		}

        this.result.split_evaluator_train_time += (System.nanoTime() - nodeSplitDataStartTime);

        if (tsChiefOptions.useBestPoolOfSplittersForRoots && this.nodeID == -1) {
            // dont train children if training the pool for top k splitters
            return null; // force terminate building tree
        } else {
            return bestNodeSplitterResult.splits;
        }
    }

    private int selectNumCandidateSplits(int providedNumberOfSplits, NodeSplitter.SplitterType splitterType){
        int adjustedNumberOfSplits = 0;

        if (tsChiefOptions.candidateSelectionMethod == TSChiefOptions.CandidateSelectionMethod.constant) {
            adjustedNumberOfSplits = providedNumberOfSplits;
        }else if (tsChiefOptions.candidateSelectionMethod == TSChiefOptions.CandidateSelectionMethod.differentAtRoot) {
            // experimental -> try few splits at the root, and more below

            if (nodeDepth == 0 && nodeID != -1) {
                String[] types = tsChiefOptions.numCandidateSplitsAtRoot.split(",");

                if (splitterType == NodeSplitter.SplitterType.ElasticDistanceSplitter){
                    for (String type : types) {
                        if (type.startsWith("e")){
                            int pos = type.indexOf(":");
                            String v = type.substring(pos+1);
                            adjustedNumberOfSplits = Integer.parseInt(v);
                        }
                    }
                }else if (splitterType == NodeSplitter.SplitterType.BossSplitterV1 || splitterType == NodeSplitter.SplitterType.BossSplitterV2){
                    for (String type : types) {
                        if (type.startsWith("b")){
                            int pos = type.indexOf(":");
                            String v = type.substring(pos+1);
                            adjustedNumberOfSplits = Integer.parseInt(v);
                        }
                    }
                }else if (splitterType == NodeSplitter.SplitterType.RIFSplitterV1 || splitterType == NodeSplitter.SplitterType.RIFSplitterV2){
                    for (String type : types) {
                        if (type.startsWith("r")){
                            int pos = type.indexOf(":");
                            String v = type.substring(pos+1);
                            adjustedNumberOfSplits = Integer.parseInt(v);
                        }
                    }
                }else {
                    adjustedNumberOfSplits = 1;
                }
            } else {
                adjustedNumberOfSplits = providedNumberOfSplits;
            }
        }else if (tsChiefOptions.candidateSelectionMethod == TSChiefOptions.CandidateSelectionMethod.constbyDepthAndNodeSize) {
            adjustedNumberOfSplits = (int) Math.sqrt(nodeSize);
            adjustedNumberOfSplits = InputParser.clamp(adjustedNumberOfSplits,1, providedNumberOfSplits);
        }else if (tsChiefOptions.candidateSelectionMethod == TSChiefOptions.CandidateSelectionMethod.weighted) {
            // TODO
            List<Pair<Integer, Double>> probs = new ArrayList<>();
            probs.add(new Pair<>(1, 5.0/205));
            probs.add(new Pair<>(2, 100.0/205));
            probs.add(new Pair<>(3, 100.0/205));
            numSplitSelectorDist = new EnumeratedDistribution(probs);
            Integer value = (Integer) numSplitSelectorDist.sample();
            adjustedNumberOfSplits = value;
        }else if (tsChiefOptions.candidateSelectionMethod == TSChiefOptions.CandidateSelectionMethod.prob) {
            // TODO
            List<Pair<Integer, Double>> probs = new ArrayList<>();
            probs.add(new Pair<>(1, 5.0/205));
            probs.add(new Pair<>(2, 100.0/205));
            probs.add(new Pair<>(3, 100.0/205));
            numSplitSelectorDist = new EnumeratedDistribution(probs);
            Integer value = (Integer) numSplitSelectorDist.sample();
            adjustedNumberOfSplits = value;
        }else if (tsChiefOptions.candidateSelectionMethod == TSChiefOptions.CandidateSelectionMethod.probByDepthAndNodeSize) {
            // TODO
            List<Pair<Integer, Double>> probs = new ArrayList<>();
            probs.add(new Pair<>(1, 5.0/205));
            probs.add(new Pair<>(2, 100.0/205));
            probs.add(new Pair<>(3, 100.0/205));
            numSplitSelectorDist = new EnumeratedDistribution(probs);
            Integer value = (Integer) numSplitSelectorDist.sample();
            adjustedNumberOfSplits = value;
        } else if (tsChiefOptions.candidateSelectionMethod == TSChiefOptions.CandidateSelectionMethod.probByWins) {
            // TODO
            List<Pair<Integer, Double>> probs = new ArrayList<>();
            probs.add(new Pair<>(1, 5.0/205));
            probs.add(new Pair<>(2, 100.0/205));
            probs.add(new Pair<>(3, 100.0/205));
            numSplitSelectorDist = new EnumeratedDistribution(probs);
            Integer value = (Integer) numSplitSelectorDist.sample();
            adjustedNumberOfSplits = value;
        }  else {
            throw new RuntimeException("numCandidateSelectionMethod not supported");
        }

        return adjustedNumberOfSplits;
    }

    private void fitEESplitter(Dataset trainData, ArrayIndex nodeIndices,
                               List<NodeSplitter> splitters, List<NodeSplitterResult> splitterResults) throws Exception {

        NodeSplitter currentSplitter;
        NodeSplitterResult currentSplitterResult;

        // TODO NodeSplitter.SplitterType.Multivariate?
        int numEESplits = selectNumCandidateSplits(tsChiefOptions.ee_splitters_per_node, NodeSplitter.SplitterType.ElasticDistanceSplitter);

        for (int i = 0; i < numEESplits; i++) {
            if (trainData.isMultivariate()) {
                currentSplitter = new MultivariateElasticDistanceSplitter(this);
                splitters.add(currentSplitter);
                currentSplitterResult = currentSplitter.fit(trainData, nodeIndices);
                splitterResults.add(currentSplitterResult);
            } else if (tsChiefOptions.eeUsePrunedEA){
                currentSplitter = new ElasticDistanceSplitterWithPEA(this);
                splitters.add(currentSplitter);
                currentSplitterResult = currentSplitter.fit(trainData, nodeIndices);
                splitterResults.add(currentSplitterResult);
            }else{
                currentSplitter = new ElasticDistanceSplitter(this);
                splitters.add(currentSplitter);
                currentSplitterResult = currentSplitter.fit(trainData, nodeIndices);
                splitterResults.add(currentSplitterResult);
            }

            if (currentSplitterResult == null) {
                continue;
            }

            double weightedGini = weighted_gini(nodeSize, currentSplitterResult.splits);
            currentSplitterResult.weightedGini = weightedGini;
            if (tsChiefOptions.useBestPoolOfSplittersForRoots && this.nodeID == -1) {
                tsChiefOptions.topKNodeSelector.add(
                        new TopKNodeSelector.GiniSplitterPair(currentSplitterResult.weightedGini, currentSplitter)
                );
            }

            tree.result._splitInfoCollector.splitInfo.add(currentSplitterResult);
        }
    }

    private void fitBossSplitterV1(Dataset trainData, ArrayIndex nodeIndices,
                                 List<NodeSplitter> splitters, List<NodeSplitterResult> splitterResults) throws Exception {
        NodeSplitter currentSplitter;
        NodeSplitterResult currentSplitterResult;
        int numBossSplits = selectNumCandidateSplits(tsChiefOptions.boss_splitters_per_node, NodeSplitter.SplitterType.BossSplitterV1);

        for (int i = 0; i < numBossSplits; i++) {
            currentSplitter = new BossSplitterV1(this);
            splitters.add(currentSplitter);
            currentSplitterResult = currentSplitter.fit(trainData, nodeIndices);
            splitterResults.add(currentSplitterResult);

            if (currentSplitterResult == null) {
                continue;
            }

            double weightedGini = weighted_gini(nodeSize, currentSplitterResult.splits);
            currentSplitterResult.weightedGini = weightedGini;
            if (tsChiefOptions.useBestPoolOfSplittersForRoots && this.nodeID == -1) {
                tsChiefOptions.topKNodeSelector.add(
                        new TopKNodeSelector.GiniSplitterPair(weightedGini, currentSplitter)
                );
            }
            tree.result._splitInfoCollector.splitInfo.add(currentSplitterResult);
        }

    }

    private void fitBossSplitterV2(Dataset trainData, ArrayIndex nodeIndices,
                                   List<NodeSplitter> splitters, List<NodeSplitterResult> splitterResults) throws Exception {
        NodeSplitter currentSplitter;
        NodeSplitterResult currentSplitterResult;
        int numBossSplits = selectNumCandidateSplits(tsChiefOptions.boss_splitters_per_node, NodeSplitter.SplitterType.BossSplitterV2);

        for (int i = 0; i < numBossSplits; i++) {
            currentSplitter = new BossSplitterV2(this);
            splitters.add(currentSplitter);
            currentSplitterResult = currentSplitter.fit(trainData, nodeIndices);
            splitterResults.add(currentSplitterResult);

            if (currentSplitterResult == null) {
                continue;
            }

            double weightedGini = weighted_gini(nodeSize, currentSplitterResult.splits);
            currentSplitterResult.weightedGini = weightedGini;
            if (tsChiefOptions.useBestPoolOfSplittersForRoots && this.nodeID == -1) {
                tsChiefOptions.topKNodeSelector.add(
                        new TopKNodeSelector.GiniSplitterPair(weightedGini, currentSplitter)
                );
            }
            tree.result._splitInfoCollector.splitInfo.add(currentSplitterResult);
        }

    }


    private List<NodeSplitterResult> fitRiseSplitterV1(Dataset trainData, ArrayIndex nodeIndices,
                                                       List<NodeSplitter> splitters,
                                                       List<NodeSplitterResult> splitterResults) throws Exception {
        NodeSplitterResult currentSplitterResult;
        // rise 1
        if (tsChiefOptions.rif_enabled && tsChiefOptions.rif_splitter_type == NodeSplitter.SplitterType.RIFSplitterV1) {
            // old rise splitter -> version in paper
            int numRiseSplits = selectNumCandidateSplits(tsChiefOptions.rif_splitters_per_node, NodeSplitter.SplitterType.RIFSplitterV1);

            // ASSUMPTION: 1 random interval of a set min length (default=16) is tested per splitter instance
            // TODO check maxLag
            // TODO check numRiseSplits < 4

            // -c=rise:100 = 100 / 4 = 25 rise splits per function is guaranteed
            // -c=rise:50 = 50 / 4 = 12
            int numSplitsPerType = numRiseSplits / 4;

            // if numRiseSplits is not a multple of 4
            // we select random functions for to distribute the remaining splits among the 4 functions
            // in a round robin fashion to ensure same function is not picked more than once
            // 100 % 4 = 0
            // 50 % 4 = 2 extra splits are choosen from random functions
            int extraSplits = numRiseSplits % 4;

            // ceil(25 / 16) = min 2 intervals per type is needed given each splitter uses 1 interval of min length 16
            // ceil(12 / 16) = 1
            int minNumIntervalsNeededPerType = (int) Math.ceil((float) numSplitsPerType / (float) tsChiefOptions.rise_min_interval);

            // exp
            boolean autoSelectNumIntervals = true;
            if (autoSelectNumIntervals) {
                minNumIntervalsNeededPerType = (int) Math.ceil((float) numSplitsPerType / (float) tsChiefOptions.rise_min_interval);
            } else {
                minNumIntervalsNeededPerType = Math.min(numSplitsPerType, numSplitsPerType/2);
            }

            // 2 * 4 = 8 intervals in total are used
            // 1 * 4 = 4
            int totalNumIntervals = minNumIntervalsNeededPerType * 4;

            // each splitter can use numFeatures <= length of generated interval (min length 16)
            // so pick features to subsample unformly between intervals by setting numFeatures for each splitter (thus interval)
            // floor(25 / 2) = 12
            // floor(12 / 1) = 12
            int maxFeaturesPerInterval = numSplitsPerType / minNumIntervalsNeededPerType;
            int extraSplitsPerType = numSplitsPerType % minNumIntervalsNeededPerType;

            // 100 - (12 * 8) = 100 - 96 = 4 extra splits
            // 50 - (12 * 4) = 50 - 48 = 2 reminder splits to distribute
            int remainingSplits = numRiseSplits - (maxFeaturesPerInterval * totalNumIntervals);

            int numFeaturesToUse;

            tsChiefOptions.num_actual_rif_splitters_needed_per_type = minNumIntervalsNeededPerType;
            tsChiefOptions.rise_m = maxFeaturesPerInterval;

            for (int i = 0; i < totalNumIntervals; i++) {
                RISESplitterV1 riseSplitterV1 = null;

                if (tsChiefOptions.rise_features == TSChiefOptions.RiseFeatures.ACF_PACF_ARMA_PS_separately) {

                    if (remainingSplits > 0) {
                        numFeaturesToUse = maxFeaturesPerInterval + 1;
                        remainingSplits--;
                    } else {
                        numFeaturesToUse = maxFeaturesPerInterval;
                    }

                    //divide appox equally (best if divisible by 4)
                    if (i % 4 == 0) {
                        riseSplitterV1 = new RISESplitterV1(this);
                        riseSplitterV1.filterType = TSChiefOptions.RiseFeatures.ACF;
                        riseSplitterV1.numFeatures = numFeaturesToUse;
                        splitters.add(riseSplitterV1);
                    } else if (i % 4 == 1) {
                        riseSplitterV1 = new RISESplitterV1(this);
                        riseSplitterV1.filterType = TSChiefOptions.RiseFeatures.PACF;
                        riseSplitterV1.numFeatures = numFeaturesToUse;
                        splitters.add(riseSplitterV1);
                    } else if (i % 4 == 2) {
                        riseSplitterV1 = new RISESplitterV1(this);
                        riseSplitterV1.filterType = TSChiefOptions.RiseFeatures.ARMA;
                        riseSplitterV1.numFeatures = numFeaturesToUse;
                        splitters.add(riseSplitterV1);
                    } else if (i % 4 == 3) {
                        riseSplitterV1 = new RISESplitterV1(this);
                        riseSplitterV1.filterType = TSChiefOptions.RiseFeatures.PS;
                        riseSplitterV1.numFeatures = numFeaturesToUse;
                        splitters.add(riseSplitterV1);
                    }
                } else {
                    riseSplitterV1 = new RISESplitterV1(this);
                    riseSplitterV1.filterType = tsChiefOptions.rise_features;
                    splitters.add(riseSplitterV1);
                }

                currentSplitterResult = riseSplitterV1.fit(trainData, nodeIndices);
                splitterResults.add(currentSplitterResult);
                if (currentSplitterResult == null) {
                    continue; // TODO check the effect on topK class, if all splitters returned null
//                                return null;
                }
                double weightedGini = weighted_gini(nodeSize, currentSplitterResult.splits);
                currentSplitterResult.weightedGini = weightedGini;
                if (tsChiefOptions.useBestPoolOfSplittersForRoots && this.nodeID == -1) {
                    tsChiefOptions.topKNodeSelector.add(
                            new TopKNodeSelector.GiniSplitterPair(weightedGini, riseSplitterV1)
                    );
                }
                tree.result._splitInfoCollector.splitInfo.add(currentSplitterResult);

            }

        } // end rise1
        return splitterResults;
    }

    private void fitRiseSplitterV2(Dataset trainData, ArrayIndex nodeIndices,
                                   List<NodeSplitter> splitters, List<NodeSplitterResult> splitterResults) throws Exception {
        NodeSplitterResult currentSplitterResult;
        int numRiseSplits = selectNumCandidateSplits(tsChiefOptions.rif_splitters_per_node, NodeSplitter.SplitterType.RIFSplitterV2);

        if (numRiseSplits > 0) {
            List<TSChiefOptions.RiseFeatures> features = new LinkedList<TSChiefOptions.RiseFeatures>(tsChiefOptions.riseEnabledTransforms);
            Collections.shuffle(features);
            int numFunctions = tsChiefOptions.riseEnabledTransforms.size();
            int minFeaturesPerFunction = numRiseSplits / numFunctions;
            int numExtraFeatures = numRiseSplits % numFunctions;

            // ASSUME that each splitter works on one interval and one function

            for (int i = 0; i < features.size(); i++) {

                if(numExtraFeatures > 0){
                    minFeaturesPerFunction = minFeaturesPerFunction + 1;
                    numExtraFeatures--;
                }

                int minFeaturesGeneratedInterval = tsChiefOptions.rise_min_interval; // TODO maxLag
                int numIntervalsPerFunction = tsChiefOptions.rise_num_intervals;

                int minIntervalsPerFunction = 0;
                int minFeaturesPerInterval;
                int numExtraFeaturesPerInterval;

                if (numIntervalsPerFunction < 1){
                    // select the number of intervals for each function automatically

                    // ceil 25 / 16 = 2
                    minIntervalsPerFunction = (int) Math.ceil((double) minFeaturesPerFunction / minFeaturesGeneratedInterval);
                    // 25 / 2 = 12
                    minFeaturesPerInterval = minFeaturesPerFunction / minIntervalsPerFunction;
                    // 25 % 2 = 1
                    numExtraFeaturesPerInterval = minFeaturesPerFunction % minIntervalsPerFunction;
                }else {
                    // try to use the given number of intervals for each function - adjust if necessary

                    // 1 <= 10 <= 25
                    minIntervalsPerFunction = InputParser.clamp(numIntervalsPerFunction, 1, minFeaturesPerFunction);
                    // 25 / 10 = 2
                    minFeaturesPerInterval = minFeaturesPerFunction / minIntervalsPerFunction;
                    // 25 % 10 = 5
                    numExtraFeaturesPerInterval = minFeaturesPerFunction % minIntervalsPerFunction;

                    // TODO only necessary if the splitter implementation use 1 interval
                    // if num intervals is too low to generate enough features auto adjust if necessary
                    if (minFeaturesPerInterval > minFeaturesGeneratedInterval){
                        minIntervalsPerFunction = (int) Math.ceil((double) minFeaturesPerFunction / minFeaturesGeneratedInterval);
                        minFeaturesPerInterval = minFeaturesPerFunction / minIntervalsPerFunction;
                        numExtraFeaturesPerInterval = minFeaturesPerFunction % minIntervalsPerFunction;
                    }
                }

                for (int j = 0; j < minIntervalsPerFunction; j++) {
                    RISESplitterV2 riseSplitter = new RISESplitterV2(this);
                    riseSplitter.filterType = features.get(i);

                    if (numExtraFeaturesPerInterval > 0) {
                        riseSplitter.numFeatures = minFeaturesPerInterval + 1;
                        numExtraFeaturesPerInterval--;
                    } else {
                        riseSplitter.numFeatures = minFeaturesPerInterval;
                    }

                    splitters.add(riseSplitter);
                    currentSplitterResult = riseSplitter.fit(trainData, nodeIndices);
                    splitterResults.add(currentSplitterResult);
                    if (currentSplitterResult == null) {
                        continue;
//                                return null;
                    }
                    double weightedGini = weighted_gini(nodeSize, currentSplitterResult.splits);
                    currentSplitterResult.weightedGini = weightedGini;
                    if (tsChiefOptions.useBestPoolOfSplittersForRoots && this.nodeID == -1) {
                        tsChiefOptions.topKNodeSelector.add(
                                new TopKNodeSelector.GiniSplitterPair(weightedGini, riseSplitter)
                        );
                    }
                }
            }

        }

    }


//    private TIntObjectMap<Dataset> fitSplitters(Dataset trainData, Index trainIndices) throws Exception {
//        long nodeFitStartTime = System.nanoTime();
//
//        if (tsChiefOptions.selectRootFromBestCandidates && this.nodeID == -1){
//            initializeTopKSplitters(trainData);
//        }else{
//            initializeSplitters(trainData);
//        }
//
//        int nodeSize = trainData.size();
//        NodeSplitterResult nodeSplitterResult = null;
//        NodeSplitterResult bestNodeSplitterResult = null;
//        double weightedGini = Double.POSITIVE_INFINITY;
//        double bestWeightedGini = Double.POSITIVE_INFINITY;
//        RISESplitterV1 temp_rif;
//
//        //TODO HIGH PRIORITY -- increases runtime a lot, can be avoided if using indices at all places
//        // but using indices always means we have to use double referencing to access
//        // just  measuring actual time taken to prepare indices at train time, if this grows with increasing
//        // training size this is an important bottleneck for training time.
//        long nodeDataPrepStartTime = System.nanoTime();
//        ArrayIndex nodeIndices;
//        if (tree == null){
//            // root node -> top k splitter doesnt set tree
//            nodeIndices = new ArrayIndex(trainData);
//        }else{
//            nodeIndices = new ArrayIndex(this.tree.treeLevelTrainingData);
//        }
//        nodeIndices.sync(trainData);
//        this.result.data_fetch_time += (System.nanoTime() - nodeDataPrepStartTime);
//        int numSplitters = splitters.size();
//
//        //if we are fitting on a sample, currently we use all  at the node, but this could be useful for large
//        //datasets
//        Dataset trainingSample;
//        if (tsChiefOptions.eeUseApproxGini) {
//            trainingSample = sample(trainData, tsChiefOptions.eeApproxGiniMinSampleSize, tsChiefOptions.eeApproxGiniUseMinPerClass);
//        }else {
//            trainingSample = trainData; //save time by not copying all the , if we do gini on 100%
//        }
//
//        if (tsChiefOptions.selectRootFromBestCandidates && nodeID != -1 && nodeDepth == 0) {
//            bestSplitter = tsChiefOptions.topKNodeSelector.next().splitter;
//            bestNodeSplitterResult = bestSplitter.split(trainData, trainIndices);
//        }else{
//
//            for (int i = 0; i < numSplitters; i++) {
//                long startTime = System.nanoTime();
//                NodeSplitter currentSplitter = splitters.get(i);
//
//                if (currentSplitter instanceof ElasticDistanceSplitter ||
//                        currentSplitter instanceof MultivariateElasticDistanceSplitter) {
//                    //splitter.train(numSplitters)
//                    nodeSplitterResult = currentSplitter.fit(trainData, nodeIndices);
//                    result.ee_time += (System.nanoTime() - startTime);
//                }else if (currentSplitter instanceof RandomForestSplitter) {
//                    //splitter.train(numSplitters)
//                    nodeSplitterResult = currentSplitter.fit(trainData, nodeIndices);
//                    //splitter.test(numSplitters')
////				splits = splitters[i].split(, null);
//                    result.randf_time += (System.nanoTime() - startTime);
//                }else if (currentSplitter instanceof RotationForestSplitter) {
//                    //splitter.train(numSplitters)
//                    nodeSplitterResult = currentSplitter.fit(trainData, nodeIndices);
//                    //splitter.test(numSplitters')
////				splits = splitters[i].split(, null);
//                    result.rotf_time += (System.nanoTime() - startTime);
//                }else if (currentSplitter instanceof RandomShapeletTransformSplitter
//                        || currentSplitter instanceof ForestShapeletTransformSplitter) {
//                    nodeSplitterResult = currentSplitter.fit(trainData, nodeIndices);
//                    result.st_time += (System.nanoTime() - startTime);
//                }else if (currentSplitter instanceof BossSplitter) {
//                    //splitter.train(numSplitters)
//                    nodeSplitterResult = currentSplitter.fit(trainData, nodeIndices);
//                    //splitter.test(numSplitters')
////				splits = splitters[i].split(, null);
//                    result.boss_time += (System.nanoTime() - startTime);
////            }else if (currentSplitter instanceof TSFSplitter) {
////                //splitter.train(numSplitters)
////                splits = currentSplitter.fit(trainData, nodeIndices);
////                //splitter.test(numSplitters')
//////				splits = splitters[i].split(, null);
////                stats.tsf_time += (System.nanoTime() - startTime);
//                }else if (currentSplitter instanceof RISESplitterV1) {
//                    //splitter.train(numSplitters)
//                    nodeSplitterResult = currentSplitter.fit(trainData, nodeIndices);
//                    //splitter.test(numSplitters')
////				splits = splitters[i].split(, null);
//
//                    long delta = System.nanoTime() - startTime;
//                    result.rif_time += (delta);
//
//                    temp_rif = (RISESplitterV1) currentSplitter;
//                    if (temp_rif.filterType.equals(TSChiefOptions.RiseFeatures.ACF)) {
//                        result.rif_acf_time+=delta;
//                    }else if (temp_rif.filterType.equals(TSChiefOptions.RiseFeatures.PACF)) {
//                        result.rif_pacf_time+=delta;
//                    }else if (temp_rif.filterType.equals(TSChiefOptions.RiseFeatures.ARMA)) {
//                        result.rif_arma_time+=delta;
//                    }else if (temp_rif.filterType.equals(TSChiefOptions.RiseFeatures.PS)) {
//                        result.rif_ps_time+=delta;
//                    }else if (temp_rif.filterType.equals(TSChiefOptions.RiseFeatures.DFT)) {
//                        result.rif_dft_time+=delta;
//                    }
//
//                }else if (currentSplitter instanceof CIFSplitter) {
//                    nodeSplitterResult = currentSplitter.fit(trainData, nodeIndices);
//                    result.cif_time += (System.nanoTime() - startTime);
//                }else {
//
//                    //splitter.train(numSplitters)
//                    nodeSplitterResult = currentSplitter.fit(trainData, nodeIndices);
//                    //splitter.test(numSplitters')
////				splits = splitters[i].split(, null);
//
//                    //TODO stat update in constructor
////				throw new Exception("Unsupported Splitter Type");
//                }
//
//                if (nodeSplitterResult == null){
//                    return null;
//                }
//
//                weightedGini = weighted_gini(nodeSize, nodeSplitterResult.splits);
//                nodeSplitterResult.weightedGini = weightedGini;
//                if (tsChiefOptions.selectRootFromBestCandidates && this.nodeID == -1){
//                    tsChiefOptions.topKNodeSelector.add(
//                            new TopKNodeSelector.GiniSplitterPair(weightedGini, currentSplitter)
//                    );
//                }
//
//                if (tsChiefOptions.verbosity > 4) {
//                    System.out.print("  S" + i + ": " + currentSplitter.toString() + ", wgini=" + tsChiefOptions.decimalFormat.format(weightedGini) +", parent=" + trainingSample);
//                    System.out.print( "   -->splits =");
//                    for (int key : nodeSplitterResult.splits.keys()) {
//                        System.out.print(" " + nodeSplitterResult.splits.get(key).toString());
//                    }
//                    System.out.println();
//                }
//
//                //TODO if equal take a random choice? or is it fine without it
//                if (weightedGini <  bestWeightedGini) {
//                    bestWeightedGini = weightedGini;
//                    bestNodeSplitterResult = nodeSplitterResult;
////                    selectedBestSplitter = currentSplitter;
//                    topSplitters.clear();
//                    topSplitters.add(currentSplitter);
//                }else if (weightedGini == bestWeightedGini) {	//NOTE if we enable this then we need update bestSplit again -expensive
//                    topSplitters.add(splitters.get(i));
//                }
//            } //end for loop
//
//            //failed to find any valid split point
//            if (topSplitters.size() == 0) {
//                return null;
//            }else if (topSplitters.size() == 1) {
//                bestSplitter = topSplitters.get(0);	//then use stored best split
//            }else { //if we have more than one splitter with equal best gini
//                int r =  rand.nextInt(topSplitters.size());
//                bestSplitter = topSplitters.get(r);
//                // then we need to find the best split again, can't reuse best split we stored before.
//                bestNodeSplitterResult = bestSplitter.split(trainData, nodeIndices);
//
//                if (tsChiefOptions.verbosity > 4) {
//                    System.out.println("best_splitters.size() == " + topSplitters.size());
//                }
//            } // end if (topSplitters.size() == 0)
//        } // end selectRootFromBestCandidates
//
//        //split the whole dataset using the (approximately) best splitter
////		bestSplit =  best_splitter.split(, null); //TODO check
//
//        //allow gc to deallocate unneeded memory
////		for (int j = 0; j < splitters.length; j++) {
////			if (splitters[j] != best_splitter) {
////				splitters[j] = null;
////			}
////		}
//
//        if (clearMemory){
//            splitters = null;
//            topSplitters.clear();
//        }
//
//        if (tsChiefOptions.verbosity > 4) {
//            System.out.print(" BEST:" + bestSplitter.toString() + ", wgini:" + tsChiefOptions.decimalFormat.format(bestWeightedGini) + ", splits:");
//            for (int key : bestNodeSplitterResult.splits.keys()) {
//                System.out.print(" " + bestNodeSplitterResult.splits.get(key).toString());
//            }
//            System.out.println();
//        }
//
////        storeStatsForBestSplitter(); //TODO temp disabled
//
////		if (has_empty_split(bestSplit)) {
////			throw new Exception("empty splits found! check for bugs");//
////		}
//
//        this.result.split_evaluator_train_time += (System.nanoTime() - nodeFitStartTime);
//
//        if (tsChiefOptions.selectRootFromBestCandidates && this.nodeID == -1){
//            return null; // force terminate building tree
//        }else{
//            return bestNodeSplitterResult.splits;
//        }
//    }
//
//    private void initializeSplitters(Dataset trainData) throws Exception {
//
//        int n = tsChiefOptions.num_splitters_per_node;
//        int total_added = 0;
//
//        if (tsChiefOptions.ee_enabled) {
//            for (int i = 0; i < tsChiefOptions.ee_splitters_per_node; i++) {
//                if (trainData.isMultivariate()){
//                    splitters.add(new MultivariateElasticDistanceSplitter(this));
//                }else{
//                    splitters.add(new ElasticDistanceSplitter(this));
//                }
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.randf_enabled) {
//            for (int i = 0; i < tsChiefOptions.randf_splitters_per_node; i++) {
////					splitters[total_added++] = new RandomForestSplitter(node);
//                if (tsChiefOptions.randf_feature_selection == TSChiefOptions.FeatureSelectionMethod.ConstantInt) {
//                    splitters.add(new RandomForestSplitter(this, tsChiefOptions.randf_m));
//                }else {
//                    splitters.add(new RandomForestSplitter(this, tsChiefOptions.randf_feature_selection));
//                }
//
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.rotf_enabled) {
//            for (int i = 0; i < tsChiefOptions.rotf_splitters_per_node; i++) {
//                splitters.add(new RotationForestSplitter(this));
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.st_enabled) {
//            for (int i = 0; i < tsChiefOptions.st_splitters_per_node; i++) {
//
//                if (tsChiefOptions.st_param_selection == TSChiefOptions.ParamSelection.Random) {
//
//                    splitters.add(new RandomShapeletTransformSplitter(this));
//
//                }else if (tsChiefOptions.st_param_selection == TSChiefOptions.ParamSelection.PreLoadedParams ||
//                        tsChiefOptions.st_param_selection == TSChiefOptions.ParamSelection.PraLoadedDataAndParams) {
//
//                    splitters.add(new ForestShapeletTransformSplitter(this));
//                    total_added++;
//
//                    break; //NOTE: if using ForestShapeletTransformSplitter -- FeatureSelectionMethod.ConstantInt is used to set m of the gini splitter to get the correct #gini required
//                }else {
//                    throw new Exception("Unsupported parameter selection method for shapelet splitter -- invalid arg: -st_params ");
//                }
//
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.boss_enabled) {
//            for (int i = 0; i < tsChiefOptions.boss_splitters_per_node; i++) {
//
//                if (tsChiefOptions.boss_split_method == TSChiefOptions.SplitMethod.RandomTreeStyle) {
////                    splitters.add(new BossBinarySplitter(this));
//                    throw new NotSupportedException();
//                }else {
//                    splitters.add(new BossSplitter(this));
//                }
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.tsf_enabled) {
//            throw new NotSupportedException();
////            num_actual_tsf_splitters_needed = tsf_splitters_per_node / 3;
////            for (int i = 0; i < AppConfig.num_actual_tsf_splitters_needed; i++) {
////                splitters.add(new TSFSplitter(this));
////                total_added++;
////            }
//        }
//
//        if (tsChiefOptions.rif_enabled) {
//
//            if (tsChiefOptions.rif_splitter_type == TSChiefOptions.SplitterType.RIFSplitterV1){
//                // old rise splitter -> version in paper
//
//                int num_gini_per_type = tsChiefOptions.rif_splitters_per_node / 4;	// eg. if we need 12 gini per type 12 ~= 50/4
//                int extra_gini = tsChiefOptions.rif_splitters_per_node % 4;
//                //assume 1 interval per splitter
//                // 2 = ceil(12 / 9) if 9 = min interval length
//                int min_splitters_needed_per_type = (int) Math.ceil((float)num_gini_per_type / (float) tsChiefOptions.rise_min_interval);
//                int max_attribs_to_use_per_splitter = (int) Math.ceil(num_gini_per_type / min_splitters_needed_per_type);
//
//                tsChiefOptions.num_actual_rif_splitters_needed_per_type = min_splitters_needed_per_type;
//                tsChiefOptions.rise_m = max_attribs_to_use_per_splitter;
//                int approx_gini_estimated = 4 * max_attribs_to_use_per_splitter * min_splitters_needed_per_type;
////			System.out.println("RISE: approx_gini_estimated: " + approx_gini_estimated);
//
//                int rif_total = tsChiefOptions.num_actual_rif_splitters_needed_per_type * 4;
//                int reminder_gini = tsChiefOptions.rif_splitters_per_node - (tsChiefOptions.rise_m * rif_total);
//                int m;
//
//                for (int i = 0; i < rif_total; i++) {
//
//                    if (tsChiefOptions.rise_features == TSChiefOptions.RiseFeatures.ACF_PACF_ARMA_PS_separately) {
//                        RISESplitterV1 splitter;
//
//                        //TODO quick fix to make sure that rif_splitters_per_node of ginis are actually calculated
//                        if (reminder_gini > 0) {
//                            m = tsChiefOptions.rise_m + 1;
//                            reminder_gini--;
//                        }else {
//                            m = tsChiefOptions.rise_m;
//                        }
//
//                        //divide appox equally (best if divisible by 4)
//                        if (i % 4 == 0) {
//                            RISESplitterV1 sp = new RISESplitterV1(this, TSChiefOptions.RiseFeatures.ACF, m);
//                            splitters.add(sp);
//                        }else if (i % 4 == 1) {
//                            RISESplitterV1 sp = new RISESplitterV1(this, TSChiefOptions.RiseFeatures.PACF, m);
//                            splitters.add(sp);
//                        }else if (i % 4 == 2) {
//                            RISESplitterV1 sp = new RISESplitterV1(this, TSChiefOptions.RiseFeatures.ARMA, m);
//                            splitters.add(sp);
//                        }else if (i % 4 == 3) {
//                            RISESplitterV1 sp = new RISESplitterV1(this, TSChiefOptions.RiseFeatures.PS, m);
//                            splitters.add(sp);
//                        }
//
//                    }else {
//                        splitters.add(new RISESplitterV1(this, tsChiefOptions.rise_features));
//                    }
//
//                    total_added++;
//                }
//            }else{
//                // rewritten rise
//                TSChiefOptions.RiseFeatures[] filters = new TSChiefOptions.RiseFeatures[] {
//                        TSChiefOptions.RiseFeatures.ACF,
//                        TSChiefOptions.RiseFeatures.PACF,
//                        TSChiefOptions.RiseFeatures.ARMA,
//                        TSChiefOptions.RiseFeatures.PS,
//                };
//                for (int i = 0; i < filters.length; i++) {
//                    RISESplitterV2 riseSplitter = new RISESplitterV2(this);
//                    // use all
//                    riseSplitter.filterType = filters[i];
//                    riseSplitter.numFeatures = tsChiefOptions.rif_splitters_per_node / filters.length;
//                    // pick a random one
////                    riseSplitter.filterType = filters[rand.nextInt(filters.length)];
//                    splitters.add(riseSplitter);
//                    total_added++;
//                }
//            }
//
//
//        }	//rise
//
//        if (tsChiefOptions.it_enabled) {
//            for (int i = 0; i < tsChiefOptions.it_splitters_per_node; i++) {
//                splitters.add(new InceptionTimeSplitter(this));
//                total_added++;
//            }
//        }
//
////			if (AppConfig.rt_enabled) {
////				for (int i = 0; i < AppConfig.rt_splitters_per_node; i++) {
////					splitters.add(new RocketTreeSplitter(node));
////					total_added++;
////				}
////			}
//
//        if (tsChiefOptions.cif_enabled) {
//            for (int i = 0; i < tsChiefOptions.cif_splitters_per_node; i++) {
//                splitters.add(new CIFSplitter(this));
//                total_added++;
//            }
//        }
//
//        //ASSERT total_added == AppContext.num_actual_splitters_needed
//
//    }
//
//    private void initializeTopKSplitters(Dataset trainData) throws Exception {
//        clearMemory = false;
//
//        int numSplittersToTrain = tsChiefOptions.num_splitters_per_node;
//        int total_added = 0;
//
//        if (tsChiefOptions.ee_enabled) {
//            for (int i = 0; i < tsChiefOptions.ee_splitters_per_node; i++) {
//                if (trainData.isMultivariate()){
//                    splitters.add(new MultivariateElasticDistanceSplitter(this));
//                }else{
//                    splitters.add(new ElasticDistanceSplitter(this));
//                }
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.randf_enabled) {
//            for (int i = 0; i < tsChiefOptions.randf_splitters_per_node; i++) {
////					splitters[total_added++] = new RandomForestSplitter(node);
//                if (tsChiefOptions.randf_feature_selection == TSChiefOptions.FeatureSelectionMethod.ConstantInt) {
//                    splitters.add(new RandomForestSplitter(this, tsChiefOptions.randf_m));
//                }else {
//                    splitters.add(new RandomForestSplitter(this, tsChiefOptions.randf_feature_selection));
//                }
//
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.rotf_enabled) {
//            for (int i = 0; i < tsChiefOptions.rotf_splitters_per_node; i++) {
//                splitters.add(new RotationForestSplitter(this));
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.st_enabled) {
//            for (int i = 0; i < tsChiefOptions.st_splitters_per_node; i++) {
//
//                if (tsChiefOptions.st_param_selection == TSChiefOptions.ParamSelection.Random) {
//
//                    splitters.add(new RandomShapeletTransformSplitter(this));
//
//                }else if (tsChiefOptions.st_param_selection == TSChiefOptions.ParamSelection.PreLoadedParams ||
//                        tsChiefOptions.st_param_selection == TSChiefOptions.ParamSelection.PraLoadedDataAndParams) {
//
//                    splitters.add(new ForestShapeletTransformSplitter(this));
//                    total_added++;
//
//                    break; //NOTE: if using ForestShapeletTransformSplitter -- FeatureSelectionMethod.ConstantInt is used to set m of the gini splitter to get the correct #gini required
//                }else {
//                    throw new Exception("Unsupported parameter selection method for shapelet splitter -- invalid arg: -st_params ");
//                }
//
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.boss_enabled) {
//            for (int i = 0; i < tsChiefOptions.boss_splitters_per_node; i++) {
//
//                if (tsChiefOptions.boss_split_method == TSChiefOptions.SplitMethod.RandomTreeStyle) {
////                    splitters.add(new BossBinarySplitter(this));
//                    throw new NotSupportedException();
//                }else {
//                    splitters.add(new BossSplitter(this));
//                }
//                total_added++;
//            }
//        }
//
//        if (tsChiefOptions.tsf_enabled) {
//            throw new NotSupportedException();
////            for (int i = 0; i < AppConfig.num_actual_tsf_splitters_needed; i++) {
////                splitters.add(new TSFSplitter(this));
////                total_added++;
////            }
//        }
//
//
//        if (tsChiefOptions.rif_enabled) {
//
//            if (tsChiefOptions.rif_splitter_type == TSChiefOptions.SplitterType.RIFSplitterV1){
//                // old rise splitter -> version in paper
//
//                int num_gini_per_type = tsChiefOptions.rif_splitters_per_node / 4;	// eg. if we need 12 gini per type 12 ~= 50/4
//                int extra_gini = tsChiefOptions.rif_splitters_per_node % 4;
//                //assume 1 interval per splitter
//                // 2 = ceil(12 / 9) if 9 = min interval length
//                int min_splitters_needed_per_type = (int) Math.ceil((float)num_gini_per_type / (float) tsChiefOptions.rise_min_interval);
//                int max_attribs_to_use_per_splitter = (int) Math.ceil(num_gini_per_type / min_splitters_needed_per_type);
//
//                tsChiefOptions.num_actual_rif_splitters_needed_per_type = min_splitters_needed_per_type;
//                tsChiefOptions.rise_m = max_attribs_to_use_per_splitter;
//                int approx_gini_estimated = 4 * max_attribs_to_use_per_splitter * min_splitters_needed_per_type;
////			System.out.println("RISE: approx_gini_estimated: " + approx_gini_estimated);
//
//                int rif_total = tsChiefOptions.num_actual_rif_splitters_needed_per_type * 4;
//                int reminder_gini = tsChiefOptions.rif_splitters_per_node - (tsChiefOptions.rise_m * rif_total);
//                int m;
//
//                for (int i = 0; i < rif_total; i++) {
//
//                    if (tsChiefOptions.rise_features == TSChiefOptions.RiseFeatures.ACF_PACF_ARMA_PS_separately) {
//                        RISESplitterV1 splitter;
//
//                        //TODO quick fix to make sure that rif_splitters_per_node of ginis are actually calculated
//                        if (reminder_gini > 0) {
//                            m = tsChiefOptions.rise_m + 1;
//                            reminder_gini--;
//                        }else {
//                            m = tsChiefOptions.rise_m;
//                        }
//
//                        //divide appox equally (best if divisible by 4)
//                        if (i % 4 == 0) {
//                            RISESplitterV1 sp = new RISESplitterV1(this, TSChiefOptions.RiseFeatures.ACF, m);
//                            splitters.add(sp);
//                        }else if (i % 4 == 1) {
//                            RISESplitterV1 sp = new RISESplitterV1(this, TSChiefOptions.RiseFeatures.PACF, m);
//                            splitters.add(sp);
//                        }else if (i % 4 == 2) {
//                            RISESplitterV1 sp = new RISESplitterV1(this, TSChiefOptions.RiseFeatures.ARMA, m);
//                            splitters.add(sp);
//                        }else if (i % 4 == 3) {
//                            RISESplitterV1 sp = new RISESplitterV1(this, TSChiefOptions.RiseFeatures.PS, m);
//                            splitters.add(sp);
//                        }
//
//                    }else {
//                        splitters.add(new RISESplitterV1(this, tsChiefOptions.rise_features));
//                    }
//
//                    total_added++;
//                }
//            }else{
//                // rewritten rise
//                TSChiefOptions.RiseFeatures[] filters = new TSChiefOptions.RiseFeatures[] {
//                        TSChiefOptions.RiseFeatures.ACF,
//                        TSChiefOptions.RiseFeatures.PACF,
//                        TSChiefOptions.RiseFeatures.ARMA,
//                        TSChiefOptions.RiseFeatures.PS,
//                };
//
//                int numFeaturesPerFunction = tsChiefOptions.rif_splitters_per_node / filters.length;
//                int numFeaturesUsed;
//                for (int i = 0; i < filters.length; i++) {
////                    numFeaturesUsed = 0;
////                    while (numFeaturesUsed < numFeaturesPerFunction){
//                        RISESplitterV2 riseSplitter = new RISESplitterV2(this);
//                        // use all
//                        riseSplitter.filterType = filters[i];
//                        riseSplitter.numFeatures = numFeaturesPerFunction;
//                        // pick a random one
////                        riseSplitter.filterType = filters[rand.nextInt(filters.length)];
//                        splitters.add(riseSplitter);
//                        total_added++;
////                    }
//                }
//            }
//        }	//rise
//
//        if (tsChiefOptions.it_enabled) {
//            for (int i = 0; i < tsChiefOptions.it_splitters_per_node; i++) {
//                splitters.add(new InceptionTimeSplitter(this));
//                total_added++;
//            }
//        }
//
////			if (AppConfig.rt_enabled) {
////				for (int i = 0; i < AppConfig.rt_splitters_per_node; i++) {
////					splitters.add(new RocketTreeSplitter(node));
////					total_added++;
////				}
////			}
//
//        if (tsChiefOptions.cif_enabled) {
//            for (int i = 0; i < tsChiefOptions.cif_splitters_per_node; i++) {
//                splitters.add(new CIFSplitter(this));
//                total_added++;
//            }
//        }
//
//        //ASSERT total_added == AppContext.num_actual_splitters_needed
//
//    }


    private void storeStatsForBestSplitter() {
        if (bestSplitter instanceof ElasticDistanceSplitter) {
            this.result.ee_win++;
            UnivariateMeasure dm = ((ElasticDistanceSplitter) bestSplitter).getSimilarityMeasure();
            switch (dm.measureName) {
                case euc:
                    this.result.euc_win++;
                    break;
                case dtwf:
                    this.result.dtwf_win++;
                    break;
                case dtw:
                    this.result.dtw_win++;
                    break;
                case ddtwf:
                    this.result.ddtwf_win++;
                    break;
                case ddtw:
                    this.result.ddtw_win++;
                    break;
                case wdtw:
                    this.result.wdtw_win++;
                    break;
                case wddtw:
                    this.result.wddtw_win++;
                    break;
                case lcss:
                    this.result.lcss_win++;
                    break;
                case twe:
                    this.result.twe_win++;
                    break;
                case erp:
                    this.result.erp_win++;
                    break;
                case msm:
                    this.result.msm_win++;
                    break;
                default:
                    break;
            }

        }if (bestSplitter instanceof MultivariateElasticDistanceSplitter) {
            this.result.ee_win++;
            MultivariateElasticDistanceSplitter splitter = (MultivariateElasticDistanceSplitter) bestSplitter;
            MultivariateMeasure dm = splitter.getMeasure();
            switch (dm.getMeasureName()) {
                case euc:
                    this.result.euc_win++;
                    break;
                case dtwf:
                    this.result.dtwf_win++;
                    break;
                case dtw:
                    this.result.dtw_win++;
                    break;
                case ddtwf:
                    this.result.ddtwf_win++;
                    break;
                case ddtw:
                    this.result.ddtw_win++;
                    break;
                case wdtw:
                    this.result.wdtw_win++;
                    break;
                case wddtw:
                    this.result.wddtw_win++;
                    break;
                case lcss:
                    this.result.lcss_win++;
                    break;
                case twe:
                    this.result.twe_win++;
                    break;
                case erp:
                    this.result.erp_win++;
                    break;
                case msm:
                    this.result.msm_win++;
                    break;
                default:
                    break;
            }

            if (splitter.isUsingDependentDimensions()){
                this.result.dim_dep_win++;
            }else{
                this.result.dim_indep_win++;
            }

        } else if (bestSplitter instanceof RandomForestSplitter) {
            this.result.randf_win++;
//        } else if (bestSplitter instanceof RotationForestSplitter) {
//            this.result.rotf_win++;
        } else if (bestSplitter instanceof RandomShapeletTransformSplitter) {
            this.result.st_win++;

            //store shapelet that won

            RandomShapeletTransformSplitter temp = (RandomShapeletTransformSplitter) bestSplitter;
            ((TSChiefForestResult) this.tree.getForest().getTrainResults()).winShapelets.add(temp.best_shapelet.toString());

        } else if (bestSplitter instanceof ForestShapeletTransformSplitter) {
            this.result.st_win++;

            //store shapelet that won
            ForestShapeletTransformSplitter temp = (ForestShapeletTransformSplitter) bestSplitter;
            ((TSChiefForestResult) this.tree.getForest().getTrainResults()).winShapelets.add(temp.best_shapelet.toString());

        } else if (bestSplitter instanceof BossSplitterV1 || bestSplitter instanceof BossSplitterV2) {
            this.result.boss_win++;
//        }else if (selectedBestsplitter instanceof TSFSplitter) {
//            this.stats.tsf_win++;
        } else if (bestSplitter instanceof RISESplitterV1) {
            this.result.rif_win++;

            RISESplitterV1 rif = (RISESplitterV1) bestSplitter;
            if (rif.filterType.equals(TSChiefOptions.RiseFeatures.ACF)) {
                this.result.rif_acf_win++;
            } else if (rif.filterType.equals(TSChiefOptions.RiseFeatures.PACF)) {
                this.result.rif_pacf_win++;
            } else if (rif.filterType.equals(TSChiefOptions.RiseFeatures.ARMA)) {
                this.result.rif_arma_win++;
            } else if (rif.filterType.equals(TSChiefOptions.RiseFeatures.PS)) {
                this.result.rif_ps_win++;
            } else if (rif.filterType.equals(TSChiefOptions.RiseFeatures.DFT)) {
                this.result.rif_dft_win++;
            }
        } else if (bestSplitter instanceof RISESplitterV2) {
            this.result.rif_win++;

            RISESplitterV2 rif = (RISESplitterV2) bestSplitter;
            if (rif.filterType.equals(TSChiefOptions.RiseFeatures.ACF)) {
                this.result.rif_acf_win++;
            } else if (rif.filterType.equals(TSChiefOptions.RiseFeatures.PACF)) {
                this.result.rif_pacf_win++;
            } else if (rif.filterType.equals(TSChiefOptions.RiseFeatures.ARMA)) {
                this.result.rif_arma_win++;
            } else if (rif.filterType.equals(TSChiefOptions.RiseFeatures.PS)) {
                this.result.rif_ps_win++;
            } else if (rif.filterType.equals(TSChiefOptions.RiseFeatures.DFT)) {
                this.result.rif_dft_win++;
            }
        } else if (bestSplitter instanceof InceptionTimeSplitter) {
            this.result.it_win++;
        } else if (bestSplitter instanceof RocketTreeSplitter) {
            this.result.rt_win++;
        }
    }


    public static boolean has_empty_split(TIntObjectMap<Dataset> splits) throws Exception {

        for (int key : splits.keys()) {
            if (splits.get(key) == null || splits.get(key).size() == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * selects a sample of data at the node to approximate the gini calculation
     *
     *	# e.g. 100 = max(20*2,100)
     * 	min_size = max(min_node_classes * node_classes, min_node_size)
     * 	# e.g 100 < 500
     * 	if min_size < node_size
     * 		# e.g 300 = (0.5 * (500-100))+100)
     * 		sample_size = (percent_train * (node_size - min_size)) + min_size
     * 		subsample(D, sample_size)
     *
     * @param data
     * @param approx_gini_min
     * @param approx_gini_min_per_class
     * @return
     * @throws Exception
     */
    private Dataset sample(Dataset data) throws Exception {
        Dataset sample;
        int sampleSize = 0;
        int numClassesAtNode = data.getNumClasses();

        int minSize = Math.max(tsChiefOptions.agMinClassSize * numClassesAtNode, tsChiefOptions.agMinNodeSize);
        minSize = Math.max(minSize, numClassesAtNode); // to make sure at least one from each class is there is sampling

        if (tsChiefOptions.agMaxDepth != -1 && tsChiefOptions.agMaxDepth < this.nodeDepth) {
            sample = data; // faster and easier :)
        } else if (tsChiefOptions.agPercent == 1.0){
            sampleSize = (int) Math.min(Math.max(tsChiefOptions.agMaxSampleSize,
                tsChiefOptions.agMinClassSize * numClassesAtNode), data.size());
            sample = Sampler.stratifiedSample2(data, sampleSize, tsChiefOptions.rand);
//            assert sampleSize == sample.size();
            reTrainUsingFullData = true;
        }else if (minSize < data.size()) {
            // TODO check floor or ceil
            // TODO fix to be able to use both agMaxSampleSize or agMinNodeSize
            sampleSize = (int) ((tsChiefOptions.agPercent * (data.size() - minSize)) + minSize);  
            sample = Sampler.stratifiedSample2(data, sampleSize, tsChiefOptions.rand);
//            assert sampleSize == sample.size();
            reTrainUsingFullData = true;
        } else {
            sample = data;
        }
//        // make sure at least one instance from each class is in the sample
//        for (int key : trainDataPerClass.keys()) {
//            int r = rand.nextInt(trainDataPerClass.get(key).size());
//            sample.add(trainDataPerClass.get(key).getSeries(r));
//        }

        // DEV log stats
        this.result._agSampleSizes.add(sample.size());

        return sample;
    }

    public int predict(Dataset testData, TimeSeries query, int queryIndex) throws Exception {
        this.testDistribution.adjustOrPutValue(query.label(), +1,1);
        return bestSplitter.predict(query, testData, queryIndex);
    }

    public double weighted_gini(double parent_size, TIntObjectMap<Dataset> splits) throws Exception {
        double wgini = 0.0;
        double gini;
        double split_size = 0;

        if (splits == null) {
            return Double.POSITIVE_INFINITY;
        }

        for (int key : splits.keys()) {
            if (splits.get(key) == null) {
                //TODO throw RunTimeException or return null - this is a special debuging case
                gini = 1;
                split_size = 0;
            } else {
                gini = splits.get(key).gini();
                split_size = (double) splits.get(key).size();
            }
            wgini = wgini + (split_size / parent_size) * gini;
        }

        return wgini;
    }

    public boolean isLeaf() {
        return this.isLeaf;
    }

    public boolean isRoot() {
        return nodeDepth == 0;
    }

    public Integer label() {
        return this.label;
    }

    public TIntObjectMap<TSChiefNode> getChildren() {
        return this.children;
    }

    public String toString() {
        if (trainDistribution != null){
            return "nodeID: "+ nodeID +", depth: " + nodeDepth + ", dist:" + trainDistribution.toString();
        }else{
            return "nodeID: "+ nodeID +", depth: " + nodeDepth;
        }
    }

    public TSChiefNode getParent() {
        return parent;
    }

    public void setParent(TSChiefNode parent) {
        this.parent = parent;
    }

    public TSCheifTree getTree() {
        return tree;
    }

    public void setTree(TSCheifTree tree) {
        this.tree = tree;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getNodeDepth() {
        return nodeDepth;
    }

    public void setNodeDepth(int nodeDepth) {
        this.nodeDepth = nodeDepth;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    public void setChildren(TIntObjectMap<TSChiefNode> children) {
        this.children = children;
    }

    public TIntIntMap getTrainDistribution() {
        return trainDistribution;
    }

}
