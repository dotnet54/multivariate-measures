package dotnet54.classifiers.tschief.splitters.st;

import java.util.Random;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.classifiers.tschief.splitters.RandomTreeOnIndexSplitter;
import dotnet54.classifiers.tschief.splitters.st.dev.ShapeletTransformDataStore;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.MTSDataset;
import dotnet54.tscore.data.TimeSeries;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/*
 * 
 * 
 * At each node take a sample (does it needs to have all the classes?)
 * convert the sample into k shapelets
 * at each node select a random exemplar from each class, and find all shapelet candidates for all of them
 * 
 * 
 * 
 */

public class ForestShapeletTransformSplitter extends RandomTreeOnIndexSplitter {
	
	Random rand;
	public ShapeletEx best_shapelet;	//stored normalized, if enabled
//	public SplitCriterion best_criterion; 
//	List<ShapeletEx> shapelets;
	protected TSChiefOptions options;

	public ForestShapeletTransformSplitter(TSChiefNode node) throws Exception {
		super(node);
		this.options = node.tsChiefOptions;
		super.featureSelectionMethod = options.st_feature_selection;
		this.rand = options.getRand();
		this.node.tree.result.st_count++;		
	}
	
	public String toString() {
		return "ST-forest[gsplit=" + super.toString()  +"]";
	}

	@Override
	public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		setNumFeatures(options.it_m, nodeTrainData.length(), options.st_feature_selection);
		
		Dataset stTrainingSet = this.node.tree.getForest().stDataStore.getTrainingSet();
//		MTSDataset nodeDataset = new MTSDataset(indices.length, stTrainingSet.length());
//		for (int j = 0; j < indices.length; j++) {
//			nodeDataset.add(stTrainingSet.get_series(indices[j]));
//		}

		NodeSplitterResult result =  super.fitByIndices(stTrainingSet, trainIndices);
	
		TIntObjectMap<Dataset> splits = new TIntObjectHashMap<Dataset>();
		Dataset fullTrainSet = options.getTrainingSet();
		for (int k : result.splitIndices.keys()) {
			TIntArrayList ind = result.splitIndices.get(k);
			MTSDataset temp = new MTSDataset(ind.size());
			for (int i = 0; i < ind.size(); i++) {
				temp.add(fullTrainSet.getSeries(ind.get(i)));
			}
			splits.putIfAbsent(k, temp);
		}
				
		//store the shapelet
		//get shapelet record
		ShapeletTransformDataStore.STInfoRecord rec = this.node.tree.getForest().stDataStore.stInfoLines.get(getBestAttribute());
		TimeSeries s = fullTrainSet.getSeries(rec.seriesId);
		best_shapelet = new ShapeletEx(s, rec.startPos, rec.startPos + rec.length, true, options.st_normalize);
		
		//measure time before split function as time is measured separately for split function as it can be called separately --to prevent double counting 
		this.node.tree.result.st_splitter_train_time += (System.nanoTime() - startTime);
		return result;
	}

	@Override
	public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		setNumFeatures(options.it_m, nodeTrainData.length(), options.it_feature_selection);
		
		Dataset stTrainingSet = this.node.tree.getForest().stDataStore.getTrainingSet();
		trainIndices.setDataset(stTrainingSet); //TODO
		NodeSplitterResult result =  super.fitByIndices(nodeTrainData, trainIndices);
		
		//measure time before split function as time is measured separately for split function as it can be called separately --to prevent double counting 
		this.node.tree.result.st_splitter_train_time += (System.nanoTime() - startTime);
 		return result;
	}

	@Override
	public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
	
		Dataset stTrainingSet = this.node.tree.getForest().stDataStore.getTrainingSet();
//		MTSDataset nodeDataset = new MTSDataset(indices.length, stTrainingSet.length());
//		for (int j = 0; j < indices.length; j++) {
//			nodeDataset.add(stTrainingSet.get_series(indices[j]));
//		}		

		NodeSplitterResult result = super.splitByIndices(stTrainingSet, trainIndices);
		result.splitter = this;

		TIntObjectMap<Dataset> branches = new TIntObjectHashMap<Dataset>();
		Dataset fullTrainSet = options.getTrainingSet();
		for (int k : result.splitIndices.keys()) {
			TIntArrayList ind = result.splitIndices.get(k);
			Dataset temp = new MTSDataset(ind.size());
			for (int i = 0; i < ind.size(); i++) {
				temp.add(fullTrainSet.getSeries(ind.get(i)));
			}
			branches.putIfAbsent(k, temp);
		}
		
		//measure time before split function as time is measured separately for split function as it can be called separately --to prevent double counting 
		this.node.tree.result.st_splitter_train_time += (System.nanoTime() - startTime);
		return result;
	}

	@Override
	public NodeSplitterResult splitByIndices(Dataset allTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();

		Dataset stTrainingSet = this.node.tree.getForest().stDataStore.getTrainingSet();
		NodeSplitterResult result = super.splitByIndices(stTrainingSet, trainIndices); //TODO  train or split??
		result.splitter = this;

		//measure time before split function as time is measured separately for split function as it can be called separately --to prevent double counting 
		this.node.tree.result.st_splitter_train_time += (System.nanoTime() - startTime);
		
		return result;
	}

	@Override
	public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
		double dist = best_shapelet.distance(query);

		if (dist < getBestThreshold()) {
			return LEFT_BRANCH;
		}else {
			return RIGHT_BRANCH;
		}		
	}

}

















