package dotnet54.classifiers.tschief.splitters.cif;

import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.classifiers.tschief.splitters.dev.FastRandomTreeSplitterV3;
import dotnet54.classifiers.tschief.splitters.rise.ACF;
import dotnet54.classifiers.tschief.splitters.rise.ARMA;
import dotnet54.classifiers.tschief.splitters.rise.PACF;
import dotnet54.tscore.data.*;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.math3.util.Pair;
import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CIFSplitter extends NodeSplitter {

    protected int attSubsampleSize;
    protected int numAttributes;
    protected int startNumAttributes;
    protected List<List<Integer>> subsampleAtts;
    protected boolean outlierNorm = true;
    protected List<Integer> enabledFeatures;
//    protected boolean useCatch22Features = true;
//    protected boolean useTsfFeatures = true;
//    protected boolean useRiseFeatures = false;
    protected boolean useCatch22Features = false;
    protected boolean useTsfFeatures = false;
    protected boolean useRiseFeatures = true;
    protected boolean useSameFeaturesPerInterval = true;
    protected int minIntervalLength = 3;
    protected int maxLag = 3;
    protected int numIntervals = -1;
    protected String intervalSelectionMethod = "splits_based"; // {sqrt, splits_based, const}
    protected int numSplits;
    protected transient Catch22 c22;
    protected FastRandomTreeSplitterV3 binarySplitter;
    protected IntervalList intervals;
    protected Random rand;
    protected RiseFeatureTransformer rise;
    protected transient DoubleFFT_1D fft = null;

    public CIFSplitter(TSChiefNode node) {
        super(node);
        splitterType = SplitterType.CIFSplitter;
        splitterClass = getClassName();
        if (node!=null){
            this.node.result.cif_count++;
            this.rand = options.getRand();
        }else{
            rand = new Random();
        }

        this.numSplits = options.cif_splits;
        this.intervalSelectionMethod = options.cif_interval_selection;
        this.attSubsampleSize = options.cif_feature_sample_size;
//        cif_features_use TODO
        this.useSameFeaturesPerInterval = options.cif_same_features_per_interval;
        this.useCatch22Features = options.cif_use_catch22;
        this.useTsfFeatures = options.cif_use_tsf;
        this.useRiseFeatures = options.cif_use_rise;

        this.enabledFeatures = new ArrayList<>();
        if (useCatch22Features){
            for (int i = 0; i < 22; i++) {
                enabledFeatures.add(i);
            }
        }
        if (useTsfFeatures){
            enabledFeatures.add(22); //mean
            enabledFeatures.add(23); //std
            enabledFeatures.add(24); //slope
        }
        if (useRiseFeatures){
            enabledFeatures.add(25); //acf
            enabledFeatures.add(26); //pacf
            enabledFeatures.add(27); //arma
            enabledFeatures.add(28); //ps
        }

        this.numAttributes = enabledFeatures.size();
    }

    @Override
    public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
        long startTime = System.nanoTime();

        if (this.attSubsampleSize > this.numAttributes){
            this.attSubsampleSize = this.numAttributes;
        }

        if (intervalSelectionMethod.equals("sqrt")){
            this.numIntervals = (int) (Math.sqrt(nodeTrainData.length()) * Math.sqrt(nodeTrainData.dimensions()));
        }else if (intervalSelectionMethod.equals("splits_based")){
            this.numIntervals = numSplits / attSubsampleSize;
        }else if (intervalSelectionMethod.equals("const")){
            int pos  = intervalSelectionMethod.indexOf(':');
            if (pos >= 0){
                this.numIntervals = Integer.parseInt(intervalSelectionMethod.substring(pos + 1));
                if (this.numIntervals < 1){
                    this.numIntervals = 1;
                }
            }else{
                throw new IllegalArgumentException("-cif_interval_selection " + intervalSelectionMethod);
            }
        }else{
            throw new IllegalArgumentException("-cif_interval_selection " + intervalSelectionMethod);
        }

        intervals = new IntervalList(rand);
        intervals.generateNIntervalsUsingStartThenEnd(numIntervals, nodeTrainData.length());

        c22 = new Catch22();
        if (useTsfFeatures){
            c22.setNumFeatures(25);
        }else{
            c22.setNumFeatures(22);
        }
        c22.setOutlierNormalise(true);

        rise = new RiseFeatureTransformer();

        Dataset transformedNodeTrainData = new MTSDataset(nodeTrainData.size());
        int numIntervals = intervals.intervals.size();

        // select a random set of features of size attSubsampleSize = 8
        subsampleAtts = new ArrayList<>();
        for (int i = 0; i < numIntervals; i++) {
//            for (int n = 0; n < numAttributes; n++){
//                subsampleAtts.get(i).add(n);
//            }
//            while (subsampleAtts.get(i).size() > attSubsampleSize){
//                subsampleAtts.get(i).remove(rand.nextInt(subsampleAtts.get(i).size()));
//            }
            Collections.shuffle(enabledFeatures, rand);
            List<Integer> featuresToUse =  new ArrayList<>(enabledFeatures.subList(0, attSubsampleSize));
            subsampleAtts.add(featuresToUse);
        }

        for (int n = 0; n < nodeTrainData.size(); n++) {
            TimeSeries ts = nodeTrainData.getSeries(n);
            double[][] seriesData = ts.data();
            double[][] transformedData = new double[seriesData.length][numIntervals * attSubsampleSize];

            for (int i = 0; i < numIntervals; i++) {
                int[] interval = intervals.intervals.get(i);

                for (int d = 0; d < seriesData.length; d++) {
                    for (int a = 0; a < subsampleAtts.get(i).size(); a++) {
                        double[] intervalData = new double[interval[IntervalList.LENGTH]];
                        System.arraycopy(seriesData[d], interval[IntervalList.START], intervalData, 0, interval[IntervalList.LENGTH]);
                        // fix series index
                        double feature;
                        int featureIndex;
                        if (useSameFeaturesPerInterval){
                            featureIndex = subsampleAtts.get(0).get(a);
                        }else{
                            featureIndex = subsampleAtts.get(i).get(a);
                        }
                        if (featureIndex < 25){
                            feature = c22.getSummaryStatByIndex(featureIndex, n, intervalData);
                            if (Double.isNaN(feature) || Double.isInfinite(feature)) {
                                feature = 0;
                            }
                            transformedData[d][i * attSubsampleSize + a] = feature;
                        }else{
                            //rise experiment
                            double[] featureSet;

                            if (featureIndex == 25){
                                //acf
                                featureSet = ACF.transformArray(intervalData, maxLag);
                            }else if(featureIndex == 26){
                                //pacf
                                double[] acf = ACF.transformArray(intervalData, maxLag);
                                featureSet = PACF.transformArray(acf, maxLag);
                            }else if(featureIndex == 27){
                                //arma
                                double[] acf = ACF.transformArray(intervalData, maxLag);
                                double[] pacf = PACF.transformArray(acf, maxLag);
                                featureSet =  ARMA.transformArray(intervalData, acf, pacf, maxLag, false);
                            }else if(featureIndex == 28){
                                //ps
                                this.fft = new DoubleFFT_1D(intervalData.length);
                                double[] dft = new double[2 * intervalData.length];
                                System.arraycopy(intervalData, 0, dft, 0, intervalData.length);
                                this.fft.complexForward(dft);
                                featureSet = new double[intervalData.length];
                                for(int j=0;j<featureSet.length;j++){
                                    featureSet[j] = dft[j*2] * dft[j*2] + dft[j*2+1] * dft[j*2+1];
                                }
                            }else{
                                featureSet = null;
                            }

                            for (int j = 0; j < featureSet.length; j++) {
                                if (featureSet[j] == Double.NaN) {
                                    featureSet[j] = 0;
                                }
                            }

//                          for (int j = 0; j < featureSet.length; j++) {
                                int r  = rand.nextInt(featureSet.length);
                                transformedData[d][i * attSubsampleSize + a] = featureSet[r];
//                          }
                        }
                    }
                }
            }
            TimeSeries intervalSeries = new MTimeSeries(transformedData, ts.label());
            transformedNodeTrainData.add(intervalSeries);
        }

        binarySplitter = new FastRandomTreeSplitterV3(options.getRand());
        binarySplitter.setNumFeatures(0); //TODO cif_features_use

        // TODO debug overwite for each tree/node
//        transformedNodeTrainData.saveToFile("out/local/cif/temp.ts", false);

        Dataset fullTrainSet = options.getTrainingSet();
        trainIndices.setDataset(fullTrainSet); //TODO already set
        NodeSplitterResult result = binarySplitter.fitByIndices(transformedNodeTrainData, trainIndices);
        if (result.splitIndices == null) {
            return null; //cant find a sensible split point for this data.
        }

        //TODO slow -- optimize this
        for (int k : result.splitIndices.keys()) {
            TIntArrayList ind = result.splitIndices.get(k);
            Dataset temp = new MTSDataset(ind.size());
            for (int i = 0; i < ind.size(); i++) {
                temp.add(fullTrainSet.getSeries(ind.get(i)));
            }
            result.splits.putIfAbsent(k, temp);
        }

        result.weightedGini = node.weighted_gini(nodeTrainData.size(), result.splits);
        weightedGini = result.weightedGini;

        //time is measured separately for split function from train function as it can be called separately -- to prevent double counting
        this.node.result.cif_splitter_train_time += (System.nanoTime() - startTime);

        if (TSChiefNode.has_empty_split(result.splits)) {
//			throw new Exception("empty splits found! check for bugs");
        }
        this.isFitted = true;
        return result;
    }

    @Override
    public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
        long startTime = System.nanoTime();
        Dataset transformedTrainData = c22.transform(nodeTrainData, null);
        NodeSplitterResult result = binarySplitter.splitByIndices(transformedTrainData, trainIndices);
        result.splitter = this;
        Dataset fullTrainSet = options.getTrainingSet();
        for (int k : result.splitIndices.keys()) {
            TIntArrayList ind = result.splitIndices.get(k);
            Dataset temp = new MTSDataset(ind.size());
            for (int i = 0; i < ind.size(); i++) {
                temp.add(fullTrainSet.getSeries(ind.get(i)));
            }
            result.splits.putIfAbsent(k, temp);
        }

        result.weightedGini = node.weighted_gini(nodeTrainData.size(), result.splits);
        weightedGini = result.weightedGini;
        this.node.weightedGiniPerSplitter.add(new Pair<>(this.getClassName(), weightedGini));
//		this.node.meanWeightedGini.increment(weightedGini);
//		this.node.sdtvWeightedGini.increment(weightedGini);

        //time is measured separately for split function from train function as it can be called separately -- to prevent double counting
        this.node.result.cif_splitter_train_time += (System.nanoTime() - startTime);
        return result;
    }

    @Override
    public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
        TimeSeries transformedQuery;
        int numIntervals = intervals.intervals.size();

        double[][] seriesData = query.data();
        double[][] transformedData = new double[seriesData.length][numIntervals * attSubsampleSize];
        for (int i = 0; i < numIntervals; i++) {
            int[] interval = intervals.intervals.get(i);

            for (int d = 0; d < seriesData.length; d++) {
                for (int a = 0; a < subsampleAtts.get(i).size(); a++) {
                    double[] intervalData = new double[interval[IntervalList.LENGTH]];
                    System.arraycopy(seriesData[d], interval[IntervalList.START], intervalData, 0, interval[IntervalList.LENGTH]);

                    // fix series index
                    double feature;
                    int featureIndex;
                    if (useSameFeaturesPerInterval){
                        featureIndex = subsampleAtts.get(0).get(a);
                    }else{
                        featureIndex = subsampleAtts.get(i).get(a);
                    }

                    if (featureIndex < 25){
                        feature = c22.getSummaryStatByIndex(featureIndex, -1, intervalData);
                        if (Double.isNaN(feature) || Double.isInfinite(feature)) {
                            feature = 0;
                        }
                        transformedData[d][i * attSubsampleSize + a] = feature;
                    }else{
                        //rise experiment
                        double[] featureSet;

                        if (featureIndex == 25){
                            //acf
                            featureSet = ACF.transformArray(intervalData, maxLag);
                        }else if(featureIndex == 26){
                            //pacf
                            double[] acf = ACF.transformArray(intervalData, maxLag);
                            featureSet = PACF.transformArray(acf, maxLag);
                        }else if(featureIndex == 27){
                            //arma
                            double[] acf = ACF.transformArray(intervalData, maxLag);
                            double[] pacf = PACF.transformArray(acf, maxLag);
                            featureSet =  ARMA.transformArray(intervalData, acf, pacf, maxLag, false);
                        }else if(featureIndex == 28){
                            //ps
                            this.fft = new DoubleFFT_1D(intervalData.length);
                            double[] dft = new double[2 * intervalData.length];
                            System.arraycopy(intervalData, 0, dft, 0, intervalData.length);
                            this.fft.complexForward(dft);
                            featureSet = new double[intervalData.length];
                            for(int j=0;j<featureSet.length;j++){
                                featureSet[j] = dft[j*2] * dft[j*2] + dft[j*2+1] * dft[j*2+1];
                            }
                        }else{
                            featureSet = null;
                        }

                        for (int j = 0; j < featureSet.length; j++) {
                            if (featureSet[j] == Double.NaN) {
                                featureSet[j] = 0;
                            }
                        }

//                          for (int j = 0; j < featureSet.length; j++) {
                        transformedData[d][i * attSubsampleSize + a] = featureSet[0];
//                          }
                    }
                }
            }
        }

        transformedQuery = new MTimeSeries(transformedData, query.label());
        return binarySplitter.predict(transformedQuery, testData, queryIndex);
    }

    @Override
    public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
        return null;
    }

    @Override
    public NodeSplitterResult splitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
        return null;
    }
}
