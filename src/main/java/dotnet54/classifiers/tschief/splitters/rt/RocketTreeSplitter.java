package dotnet54.classifiers.tschief.splitters.rt;

import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;

public class RocketTreeSplitter extends NodeSplitter {

    public RocketTreeSplitter(TSChiefNode node) {
        super(node);
    }

    @Override
    public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
        return null;
    }

    @Override
    public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
        return null;
    }

    @Override
    public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
        return 0;
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
