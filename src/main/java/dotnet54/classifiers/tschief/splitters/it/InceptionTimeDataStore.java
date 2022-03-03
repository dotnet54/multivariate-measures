package dotnet54.classifiers.tschief.splitters.it;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.tscore.data.DataStore;
import dotnet54.tscore.io.DataLoader;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.MTSDataset;
import dotnet54.tscore.data.MTimeSeries;

public class InceptionTimeDataStore extends DataStore {

	protected TSChiefOptions options;

	public InceptionTimeDataStore(TSChiefOptions options) {
		super();
		this.options = options;
		this.trainingSetKey = "it_train";
		this.testingSetKey = "it_test";
	}
	
	@Override
	public void initBeforeTrain(Dataset train) throws Exception {
		double[][][] transformedData = loadTransformedData(options.getDatasetName(), "train");
		setTrainingSet(toDataset(train, transformedData[0]));
	}

	@Override
	public void initBeforeTest(Dataset test) throws Exception {
		double[][][] transformedData = loadTransformedData(options.getDatasetName(), "test");
		setTestingSet(toDataset(test, transformedData[1]));
	}
	
	private double[][][] loadTransformedData(String datasetName, String load) {
		double[][][] transformedData = new double[2][][];
		String fileName;
		
		if (load.equals("train") || load.equals("both") ) {
			fileName = options.it_cache_dir + "/" + options.ucr2015.to2015Name(datasetName) + "/x_train_vec.csv";
			transformedData[0] = DataLoader.loadToArray(fileName, false);
		}

		if (load.equals("test")  || load.equals("both") ) {
			fileName = options.it_cache_dir + "/" + options.ucr2015.to2015Name(datasetName) + "/x_test_vec.csv";
			transformedData[1] = DataLoader.loadToArray(fileName, false);
		}

		return transformedData;
	}
	
	private Dataset toDataset(Dataset originalData, double[][] transformedData) throws Exception {
		Dataset transformedDataset = new MTSDataset(originalData.size());
		
		for (int i = 0; i < originalData.size(); i++) {
			double[][] data = new double[1][];
			data[0] = transformedData[i];
			MTimeSeries series = new MTimeSeries(data, originalData.getClass(i));
			transformedDataset.add(series);
		}
		
		return transformedDataset;
	}

	
}
