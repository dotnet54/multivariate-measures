package dotnet54.classifiers.tschief.splitters;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.TimeSeries;

public abstract class NodeSplitter {

	public enum SplitterType{
		ElasticDistanceSplitter,
		RandomForestSplitter,
		RandomPairwiseShapeletSplitter,
		ShapeletTransformSplitter,
		RotationForestSplitter,
		CrossDomainSplitter,
		WekaSplitter,
		BossSplitterV1,
		BossSplitterV2,
		TSFSplitter,
		RIFSplitterV1,
		RIFSplitterV2,
		InceptionTimeSplitter,
		RocketTreeSplitter,
		CIFSplitter
	}

	public boolean isFitted;
	public double weightedGini;

	protected transient TSChiefOptions options;
	protected transient TSChiefNode node;
	protected SplitterType splitterType;
	protected String splitterClass;


	public NodeSplitter() {

	}

	public NodeSplitter(TSChiefOptions options) {
		this.options = options;
	}

	public NodeSplitter(TSChiefNode node) {
		this.node = node;
		if (node != null){
			this.options = node.tsChiefOptions;
		}
	}

	public abstract NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception;

	public abstract NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception;

//	public abstract int predict(TimeSeries query) throws Exception;

	public abstract int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception;

	public abstract NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception;

	public abstract NodeSplitterResult splitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception;

	public String getClassName(){
		return this.getClass().getSimpleName();
	}

	public SplitterType getSplitterType(){
		return splitterType;
	}

	@Override
	public String toString(){
		return splitterType.toString();
	}

}
