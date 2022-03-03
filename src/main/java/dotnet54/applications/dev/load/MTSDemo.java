package dotnet54.applications.dev.load;

import dotnet54.tscore.data.MTSDataset;
import dotnet54.tscore.io.TSReader;

public class MTSDemo {

	public static void main(String[] args) {
		try {
			
			testMTSDatasetReading();
			
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void testMTSDatasetReading() {
		TSReader reader = new TSReader();
		MTSDataset train = reader.readFile("E://Multivariate2018_ts/AtrialFibrillation/AtrialFibrillation_TRAIN.ts", null);

		System.out.println(train.size());		
	}

}
