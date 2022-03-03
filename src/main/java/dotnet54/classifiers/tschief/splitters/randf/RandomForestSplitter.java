package dotnet54.classifiers.tschief.splitters.randf;

import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.applications.tschief.TSChiefOptions.FeatureSelectionMethod;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.classifiers.tschief.splitters.RandomTreeSplitter;

import java.util.Random;

public class RandomForestSplitter extends RandomTreeSplitter {
	protected Random rand;

	public RandomForestSplitter(TSChiefNode node) throws Exception {
		super(node);
		splitterType = SplitterType.RandomForestSplitter;
		splitterClass = getClassName();
		if (node!=null){
			this.node.result.randf_count++;
			this.rand = options.getRand();
		}else{
			rand = new Random();
		}
	}
	
//	public RandomForestSplitter(TSChiefNode node, int m) throws Exception {
//		super(node, m);
//		node.tree.result.randf_count++;
//	}
//
//	public RandomForestSplitter(TSChiefNode node, FeatureSelectionMethod feature_selection_method) throws Exception {
//		super(node, feature_selection_method);
//		node.tree.result.randf_count++;
//	}

	@Override
	public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();

		if (options.randf_feature_selection == FeatureSelectionMethod.Sqrt) {
			numFeatures = (int) Math.sqrt(nodeTrainData.length());
		}else if (options.randf_feature_selection == FeatureSelectionMethod.Loge) {
			numFeatures = (int) Math.log(nodeTrainData.length());
		}else if (options.randf_feature_selection == FeatureSelectionMethod.Log2) {
			numFeatures = (int) (Math.log(nodeTrainData.length()/ Math.log(2)));
		}else {
			numFeatures = options.randf_m; //TODO verify
		}

		NodeSplitterResult result =  super.fit(nodeTrainData, trainIndices);
		this.node.tree.result.randf_splitter_train_time += (System.nanoTime() - startTime);
		return result;
	}

	@Override
	public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();

		NodeSplitterResult result = super.split(nodeTrainData, trainIndices);
		result.splitter = this;

		this.node.tree.result.randf_splitter_train_time += (System.nanoTime() - startTime);
		return result;
	}

	public String toString() {
		return "RandF[m=" + numFeatures + "]";
	}

	
}
