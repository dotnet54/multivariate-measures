package dotnet54.classifiers.tschief;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.splitters.boss.BossSplitterV1;
import dotnet54.classifiers.tschief.splitters.boss.BossSplitterV2;
import dotnet54.classifiers.tschief.splitters.dev.FastRandomTreeSplitterV3;
import dotnet54.classifiers.tschief.splitters.ee.ElasticDistanceSplitter;
import dotnet54.classifiers.tschief.splitters.rise.RISESplitterV1;
import dotnet54.classifiers.tschief.splitters.rise.RISESplitterV2;
import dotnet54.tscore.DebugInfo;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TopKNodeSelector {

    public transient TSChiefOptions tsChiefOptions;
//    public PriorityBlockingQueue<GiniSplitterPair> splitters;
    public Random rand;

    public ConcurrentLinkedQueue<GiniSplitterPair> splitters;
//    public ConcurrentSkipListSet<GiniSplitterPair> splitters;
//    public transient ConcurrentSkipListSet<GiniSplitterPair> splittersCopy;
    public TSCheifTree topKSplitTree;

    // stratified
    public HashMap<String, ConcurrentLinkedQueue<GiniSplitterPair>> stratifiedList;

    public int numSplittersInPool;
    public int poolMissCount;
    public int poolHitCount;
    public long trainTime; //ns

    private AtomicInteger pollCounterForStratList = new AtomicInteger(0);
    private int numAddCalls = 0;

    public static class GiniSplitterPair implements Comparable<GiniSplitterPair>{
        public Double gini;
        public NodeSplitter splitter;
        public GiniSplitterPair(Double gini, NodeSplitter splitter){
            this.gini = gini;
            this.splitter = splitter;
        }

        @Override
        public int compareTo(GiniSplitterPair o) {
            return o.gini.compareTo(gini);
        }

        @Override
        public String toString(){
            return gini + " := " + splitter.toString();
        }
    }

    /**
     *  100 trees each 5 splits
     *  so train 500 splitters and select top 100 as root
     *
     */
    public TopKNodeSelector(TSCheifForest forest, TSChiefOptions options){
        this.tsChiefOptions = options;
        this.rand = options.getRand();
//        splitters = new PriorityBlockingQueue<>();
        splitters = new ConcurrentLinkedQueue<>();
//        splitters = new ConcurrentSkipListSet<>();
//        splittersCopy = new ConcurrentSkipListSet<>();
        topKSplitTree = new TSCheifTree(-1, forest, options);

        stratifiedList = new HashMap<>(3);
        stratifiedList.put("e", new ConcurrentLinkedQueue<>());
        stratifiedList.put("b", new ConcurrentLinkedQueue<>());
        stratifiedList.put("r", new ConcurrentLinkedQueue<>());

    }

    public void fit(Dataset trainData, DatasetIndex trainIndices, DebugInfo debugInfo) throws Exception {
        long startTime = System.nanoTime();

        int numTrees = tsChiefOptions.numTrees;

        int cEE = topKSplitTree.tsChiefOptions.ee_splitters_per_node;
        tsChiefOptions.ee_splitters_per_node = (int) (numTrees * cEE * topKSplitTree.tsChiefOptions.splitterPoolSize);

        int cBoss = topKSplitTree.tsChiefOptions.boss_splitters_per_node;
        tsChiefOptions.boss_splitters_per_node = (int) (numTrees * cBoss * topKSplitTree.tsChiefOptions.splitterPoolSize);

        int cRise = topKSplitTree.tsChiefOptions.rif_splitters_per_node;
        tsChiefOptions.rif_splitters_per_node = (int) (numTrees * cRise * topKSplitTree.tsChiefOptions.splitterPoolSize);

        topKSplitTree.fit(trainData);

        tsChiefOptions.ee_splitters_per_node = cEE;
        tsChiefOptions.boss_splitters_per_node = cBoss;
        tsChiefOptions.rif_splitters_per_node = cRise;

        // hot fix for rise

        //just a quick ugly sorting
        List<GiniSplitterPair> tmp = new ArrayList<>(splitters);
        Collections.sort(tmp,  Collections.reverseOrder());
        splitters = new ConcurrentLinkedQueue<>(tmp);
        // TODO random tie break

        // stratified
        List<GiniSplitterPair> tmpE = new ArrayList<>(stratifiedList.get("e"));
        Collections.sort(tmpE,  Collections.reverseOrder());
        ConcurrentLinkedQueue<GiniSplitterPair> se = new ConcurrentLinkedQueue<>(tmpE);
        stratifiedList.put("e", se);

        List<GiniSplitterPair> tmpB = new ArrayList<>(stratifiedList.get("b"));
        Collections.sort(tmpB,  Collections.reverseOrder());
        ConcurrentLinkedQueue<GiniSplitterPair> sb = new ConcurrentLinkedQueue<>(tmpB);
        stratifiedList.put("b", sb);

        List<GiniSplitterPair> tmpR = new ArrayList<>(stratifiedList.get("r"));
        Collections.sort(tmpR,  Collections.reverseOrder());
        ConcurrentLinkedQueue<GiniSplitterPair> sr = new ConcurrentLinkedQueue<>(tmpR);
        stratifiedList.put("r", sr);

//        if (tsChiefOptions.topkMethod.equals("queue")){
//            numSplittersInPool = splitters.size();
//        }else if (tsChiefOptions.topkMethod.equals("stratified_queue") || tsChiefOptions.topkMethod.equals("stratified_prob")){
//            for (String key : stratifiedList.keySet()) {
//                numSplittersInPool += stratifiedList.get(key).size();
//            }
//        }

        trainTime = System.nanoTime() - startTime;
    }

    public void add(GiniSplitterPair pair){
        numAddCalls++;

        if (numSplittersInPool % tsChiefOptions.numTrees == 0){
            System.out.print(numSplittersInPool + "*");
        }


        // TODO HACKY!!! REFACTOR
        if (pair.splitter instanceof RISESplitterV2){
            GiniSplitterPair newPair;
            RISESplitterV2 riseSplitterV2 = (RISESplitterV2) pair.splitter;
            FastRandomTreeSplitterV3 binarySplitter = riseSplitterV2.binarySplitter;
            RISESplitterV2 clonedSplitter;

            // TODO numFeaturesUsed, and skip attributes without gain
            for (int i = 0; i < binarySplitter.numFeatures; i++) {
                clonedSplitter = riseSplitterV2.clone();

                // fields to change
                //    public int bestAttribute;
                //    public double bestThreshold;
                //    public double bestWGini;
                //    public int bestSplitPoint; // index after which bestThreshold was calculated
                //    public int[] leftDist;
                //    public int[] rightDist;

                clonedSplitter.binarySplitter.bestAttribute = (int) binarySplitter.bestParamsPerAttrib[i][0];
                clonedSplitter.binarySplitter.bestThreshold = binarySplitter.bestParamsPerAttrib[i][1];
                clonedSplitter.binarySplitter.bestWGini = binarySplitter.bestParamsPerAttrib[i][2];
                clonedSplitter.binarySplitter.bestSplitPoint = (int) binarySplitter.bestParamsPerAttrib[i][3];

                // TODO
                clonedSplitter.binarySplitter.leftDist = null;
                clonedSplitter.binarySplitter.rightDist = null;

                newPair = new GiniSplitterPair(clonedSplitter.binarySplitter.bestWGini, clonedSplitter);

                if (tsChiefOptions.splitterPoolType.equals("queue")){
                    splitters.add(newPair);
//                splittersCopy.add(newPair);
                }else if (tsChiefOptions.splitterPoolType.equals("stratified_queue") || tsChiefOptions.splitterPoolType.equals("stratified_prob")){
                    stratifiedList.get("r").add(newPair);
                }
                numSplittersInPool++;

            }


        }else if (pair.splitter instanceof RISESplitterV1){
            // TODO partial support - stores the best attribute only
            if (tsChiefOptions.splitterPoolType.equals("queue")){
                splitters.add(pair);
//                splittersCopy.add(newPair);
            }else if (tsChiefOptions.splitterPoolType.equals("stratified_queue") || tsChiefOptions.splitterPoolType.equals("stratified_prob")){
                stratifiedList.get("r").add(pair);
            }
            numSplittersInPool++;

        }else if (pair.splitter instanceof BossSplitterV1){
            if (tsChiefOptions.splitterPoolType.equals("queue")){
                splitters.add(pair);
//                splittersCopy.add(newPair);
            }else if (tsChiefOptions.splitterPoolType.equals("stratified_queue") || tsChiefOptions.splitterPoolType.equals("stratified_prob")){
                stratifiedList.get("b").add(pair);
            }
            numSplittersInPool++;

        }else if (pair.splitter instanceof BossSplitterV2){
            if (tsChiefOptions.splitterPoolType.equals("queue")){
                splitters.add(pair);
//                splittersCopy.add(newPair);
            }else if (tsChiefOptions.splitterPoolType.equals("stratified_queue") || tsChiefOptions.splitterPoolType.equals("stratified_prob")){
                stratifiedList.get("b").add(pair);
            }
            numSplittersInPool++;

        }else if (pair.splitter instanceof ElasticDistanceSplitter){
            if (tsChiefOptions.splitterPoolType.equals("queue")){
                splitters.add(pair);
//                splittersCopy.add(newPair);
            }else if (tsChiefOptions.splitterPoolType.equals("stratified_queue") || tsChiefOptions.splitterPoolType.equals("stratified_prob")){
                stratifiedList.get("e").add(pair);
            }
            numSplittersInPool++;

        }else {
            throw new RuntimeException("Splitter type " + pair.splitter.getClassName() + " is not supported for topk method");
        }

    }

    public synchronized GiniSplitterPair next(){

        if (tsChiefOptions.splitterPoolType.equals("queue")){
            if (splitters.size() > 0){
                poolHitCount++;
                return splitters.poll();
            }else{
                poolMissCount++;
                return null;
            }
        }else if (tsChiefOptions.splitterPoolType.equals("stratified_queue")){
            int counter = pollCounterForStratList.get();
            GiniSplitterPair nextPair;

            if (stratifiedList.size() == 0){
                poolMissCount++;
                return null;
            }

            if (counter % 3 == 0){
                nextPair = stratifiedList.get("e").poll();
            }else if (counter % 3 == 1){
                nextPair = stratifiedList.get("b").poll();
            }else{
                nextPair = stratifiedList.get("r").poll();
            }
            pollCounterForStratList.incrementAndGet();
            poolHitCount++;
            return nextPair;
        }else if (tsChiefOptions.splitterPoolType.equals("stratified_prob")){
            double prob = rand.nextDouble();
            double threshold = 1/3.0;
            GiniSplitterPair nextPair;

            if (stratifiedList.size() == 0){
                poolMissCount++;
                return null;
            }

            if (prob < threshold){
                nextPair = stratifiedList.get("e").poll();
            }else if (prob < threshold * 2){
                nextPair = stratifiedList.get("b").poll();
            }else{
                nextPair = stratifiedList.get("r").poll();
            }
            poolHitCount++;
            return nextPair;
        }else{
            poolMissCount++;
            return null;
        }
    }

}
