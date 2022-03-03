package dotnet54.classifiers.tschief.splitters.it;

import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.classifiers.tschief.splitters.RandomTreeOnIndexSplitter;
import dotnet54.tscore.data.*;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class InceptionTimeSplitter extends RandomTreeOnIndexSplitter {

	protected DataStore dataStore;

	public InceptionTimeSplitter(TSChiefNode node) throws Exception {
		super(node);
		super.featureSelectionMethod = options.it_feature_selection;
		splitterType = SplitterType.InceptionTimeSplitter;
		splitterClass = getClassName();
		this.dataStore = node.tree.getForest().itDataStore;
		this.node.tree.result.it_count++;
	}
	
	public String toString() {
		return "InceptionTime[gsplit=" + super.toString()  +"]";
	}
	
	@Override
	public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		setNumFeatures(options.it_m, nodeTrainData.length(), options.it_feature_selection);
		
		Dataset itTrainingSet = this.node.tree.getForest().itDataStore.getTrainingSet();
//		MTSDataset nodeDataset = new MTSDataset(indices.length, itTrainingSet.length());
//		for (int j = 0; j < indices.length; j++) {
//			nodeDataset.add(itTrainingSet.get_series(indices[j]));
//		}

		NodeSplitterResult result = super.fitByIndices(itTrainingSet, trainIndices);
		if (result.splitIndices == null) {
			return null; //cant find a sensible split point for this data.
		}

		Dataset fullTrainSet = options.getTrainingSet();
		TIntObjectMap<Dataset> splits = new TIntObjectHashMap<Dataset>();
		for (int k : result.splitIndices.keys()) {
			TIntArrayList ind = result.splitIndices.get(k);
			MTSDataset temp = new MTSDataset(ind.size());
			for (int i = 0; i < ind.size(); i++) {
				temp.add(fullTrainSet.getSeries(ind.get(i)));
			}
			splits.putIfAbsent(k, temp);
		}
				
		//measure time before split function as time is measured separately for split function as it can be called separately --to prevent double counting 
		this.node.tree.result.it_splitter_train_time += (System.nanoTime() - startTime);
 		return result;
	}

	@Override
	public NodeSplitterResult fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		setNumFeatures(options.it_m, nodeTrainData.length(), options.it_feature_selection);
		
		Dataset itTrainingSet = this.node.tree.getForest().itDataStore.getTrainingSet();
		trainIndices.setDataset(itTrainingSet); //TODO refactor
		NodeSplitterResult result = super.fitByIndices(nodeTrainData, trainIndices);
		
		//measure time before split function as time is measured separately for split function as it can be called separately --to prevent double counting 
		this.node.tree.result.it_splitter_train_time += (System.nanoTime() - startTime);
 		return result;
	}

	@Override
	public NodeSplitterResult split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		
		Dataset itTrainingSet = this.node.tree.getForest().itDataStore.getTrainingSet();
//		MTSDataset nodeDataset = new MTSDataset(indices.length, itTrainingSet.length());
//		for (int j = 0; j < indices.length; j++) {
//			nodeDataset.add(itTrainingSet.get_series(indices[j]));
//		}		

		NodeSplitterResult result = super.splitByIndices(itTrainingSet, trainIndices);
		result.splitter = this;

		Dataset fullTrainSet = options.getTrainingSet();
		TIntObjectMap<Dataset> splits = new TIntObjectHashMap<Dataset>();
		for (int k : result.splitIndices.keys()) {
			TIntArrayList ind = result.splitIndices.get(k);
			Dataset temp = new MTSDataset(ind.size());
			for (int i = 0; i < ind.size(); i++) {
				temp.add(fullTrainSet.getSeries(ind.get(i)));
			}
			splits.putIfAbsent(k, temp);
		}
		
		//measure time before split function as time is measured separately for split function as it can be called separately --to prevent double counting 
		this.node.tree.result.it_splitter_train_time += (System.nanoTime() - startTime);
		return result;
	}

	@Override
	public NodeSplitterResult splitByIndices(Dataset allTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();

		Dataset itTrainingSet = this.node.tree.getForest().itDataStore.getTrainingSet();
		NodeSplitterResult result = super.splitByIndices(itTrainingSet, trainIndices);
		result.splitter = this;
		
		//measure time before split function as time is measured separately for split function as it can be called separately --to prevent double counting 
		this.node.tree.result.it_splitter_train_time += (System.nanoTime() - startTime);
		return result;
	}

	@Override
	public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
		
		Dataset itTestingSet = dataStore.getTestingSet();
		TimeSeries transformed_query = itTestingSet.getSeries(queryIndex);
		
		return super.predict(transformed_query, itTestingSet, queryIndex);
	}

}
