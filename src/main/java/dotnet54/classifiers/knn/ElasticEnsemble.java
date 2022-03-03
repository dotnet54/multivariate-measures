package dotnet54.classifiers.knn;

import dotnet54.classifiers.Classifier;
import dotnet54.classifiers.Ensemble;
import dotnet54.classifiers.ClassifierResult;
import dotnet54.tscore.DebugInfo;
import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;

public class ElasticEnsemble implements Classifier, Ensemble {
    @Override
    public ClassifierResult fit(Dataset trainData) throws Exception {
        return null;
    }

    @Override
    public ClassifierResult fit(Dataset trainData, DatasetIndex trainIndices, DebugInfo debugInfo) throws Exception {
        return null;
    }

    @Override
    public ClassifierResult predict(Dataset testData) throws Exception {
        return null;
    }

    @Override
    public ClassifierResult predict(Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo) throws Exception {
        return null;
    }

    @Override
    public ClassifierResult predictProba(Dataset testData) throws Exception {
        return null;
    }

    @Override
    public ClassifierResult predictProba(Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo) throws Exception {
        return null;
    }

    @Override
    public int predict(TimeSeries query) throws Exception {
        return 0;
    }

    @Override
    public int predict(int queryIndex) throws Exception {
        return 0;
    }

    @Override
    public double predictProba(TimeSeries query) throws Exception {
        return 0;
    }

    @Override
    public double score(Dataset trainData, Dataset testData) throws Exception {
        return 0;
    }

    @Override
    public double score(Dataset trainData, Dataset testData, DebugInfo debugInfo) throws Exception {
        return 0;
    }

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public void setOptions(Options options) {

    }

    @Override
    public ClassifierResult getTrainResults() {
        return null;
    }

    @Override
    public ClassifierResult getTestResults() {
        return null;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public Classifier[] getModels() {
        return new Classifier[0];
    }

    @Override
    public Classifier getModel(int i) {
        return null;
    }
}
