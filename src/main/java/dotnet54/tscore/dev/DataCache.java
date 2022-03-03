package dotnet54.tscore.dev;

import dotnet54.tscore.data.MTSDataset;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DataCache {

	//key format:
	//transform:train:params,params,..hash
	
	protected static int datasetID;	//TODO atomic int??
	protected Map<String, MTSDataset> dataStore;
	protected AtomicInteger datasetHash;
	
	public DataCache() {
		dataStore = new HashMap<String, MTSDataset>();
	}

	public Map<String, MTSDataset> getDataStore() {
		return dataStore;
	}
	

	
	
}
