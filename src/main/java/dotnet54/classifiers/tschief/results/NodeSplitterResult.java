package dotnet54.classifiers.tschief.results;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.tscore.data.Dataset;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class NodeSplitterResult implements Comparable<NodeSplitterResult>{

    public TSChiefOptions.SplitMethod splitMethod;
    public NodeSplitter splitter;
    public TIntObjectMap<Dataset> splits;
    public TIntObjectMap<TIntArrayList> splitIndices;
    public Double weightedGini;
    public long startTime;
    public long endTime;
    public long elapsedTime;

    public NodeSplitterResult(NodeSplitter splitter, int numChildren){
        this.splitter = splitter;
        this.splits = new TIntObjectHashMap<>(numChildren);
        this.splitIndices = new TIntObjectHashMap<>(numChildren);
        this.weightedGini = Double.POSITIVE_INFINITY;
    }

    @Override
    public int compareTo(NodeSplitterResult o) {
        return o.weightedGini.compareTo(weightedGini);
    }

    @Override
    public String toString(){
        return weightedGini + "";
//        return splits.toString();
    }
}
