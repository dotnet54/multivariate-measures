package dotnet54.classifiers;

import dotnet54.tscore.DebugInfo;
import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;

public interface Classifier {

    public ClassifierResult fit(Dataset trainData) throws Exception;

    public ClassifierResult fit(Dataset trainData, DatasetIndex trainIndices, DebugInfo debugInfo) throws Exception;

    public ClassifierResult predict(Dataset testData) throws Exception;

    public ClassifierResult predict(Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo) throws Exception;

    public ClassifierResult predictProba(Dataset testData) throws Exception;

    public ClassifierResult predictProba(Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo) throws Exception;

    public int predict(TimeSeries query) throws Exception;

    public int predict(int testQueryIndex) throws Exception;

    public double predictProba(TimeSeries query) throws Exception;

    public double score(Dataset trainData, Dataset testData) throws Exception;

    public double score(Dataset trainData, Dataset testData, DebugInfo debugInfo) throws Exception;

    public <T extends Options> T getOptions();

    public <T extends Options> void setOptions(T options);

    public ClassifierResult getTrainResults();

    public ClassifierResult getTestResults();

    // DEV
//    public void fit(Indexer trainIndices) throws Exception;

//    public ClassifierResult predict(Indexer testIndices) throws Exception;

//    public void fit(Indexer trainIndices, DebugInfo debugInfo) throws Exception;

//    public ClassifierResult predict(Indexer testIndices, DebugInfo debugInfo) throws Exception;

}
