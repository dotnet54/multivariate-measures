package dotnet54.applications.dev;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.io.CsvWriter;
import dotnet54.tscore.io.DataLoader;

public class Test {


    public static void main(String[] args) {
        try {
            String dataPath = "E://";
            String archive = "Multivariate2018_ts";
            String fileType = ".ts";
            String[] datsets = "Heartbeat"
                    .split(",");

//            TSChiefOptions.initialize();

            for (String datasetName:datsets) {
                DataLoader loader = new DataLoader(DataLoader.multivariateUAE2018);
                Dataset[] trainAndTestSet = loader.loadTrainAndTestSet(datasetName);

                // --- for temporary tests
                CsvWriter file = new CsvWriter("out/test.csv", CsvWriter.WriteMode.append, 5);
                file.addColumns("dataset,accuracy,traintime");
                file.addColumns("testtime", "trees");
                file.add("Beef", "0.90", "1544", "657");
                file.addColumnValue("trees","Coffee");
                file.addRowAsString("Fish,0.8,2,5,10");
                file.addRowAsString("Italy,0.8,2,5,10");

                file.appendWithoutHeaderIfExists();

                file.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
