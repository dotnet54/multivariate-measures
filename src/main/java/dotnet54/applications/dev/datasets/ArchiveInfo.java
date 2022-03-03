package dotnet54.applications.dev.datasets;

import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.MTSDataset;
import dotnet54.tscore.io.DataLoader;
import dotnet54.tscore.io.TSReader;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

public class ArchiveInfo {

    public static void main(String[] args) throws IOException {
        Options options = new Options()
                .set("dataPath", "E:/data/")
                .set("archiveName", "Multivariate2018_ts")
                .set("extension", ".ts")
                .set("trainSuffix", "_TRAIN")
                .set("testSuffix", "_TEST")
                .set("csvFileHeader", false)
                .set("csvTargetColumn", 0);

        File file = new File(options.getString("dataPath") + options.getString("archiveName"));
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        System.out.println(Arrays.toString(directories));

        Table table = Table.create("archive");
        table.addColumns(StringColumn.create("dataset"));
        table.addColumns(IntColumn.create("trainsize"));
        table.addColumns(IntColumn.create("testsize"));
        table.addColumns(IntColumn.create("dimensions"));
        table.addColumns(IntColumn.create("length"));
        table.addColumns(IntColumn.create("minlength"));
        table.addColumns(IntColumn.create("maxlength"));
        table.addColumns(IntColumn.create("classes"));
        table.addColumns(IntColumn.create("variable"));
        table.addColumns(IntColumn.create("missing"));

        assert directories != null;
        for (String datasetName : directories) {
            String trainFile = options.getString("dataPath") + options.getString("archiveName")
                    + "/" + datasetName + "/" + datasetName + "_TRAIN.ts";
            String testFile = options.getString("dataPath") + options.getString("archiveName")
                    + "/" + datasetName + "/" + datasetName + "_TEST.ts";

            //we assume no header in the csv files, and that class label is in the first column, modify if necessary
            DataLoader dataLoader = new DataLoader(options);
            Dataset train =  dataLoader.loadTrainingSet(trainFile);
            //its important to pass training  reference to the test  loader so that it will use same label encoder
            Dataset test =  dataLoader.loadTestingSet(testFile, train);

//            System.out.println(datasetName + " " + train.size() + "x" + test.size() + "x"
//                    + train.dimensions() + "x" + train.length() + "x" + train.getNumClasses() );

            StringColumn cDataset =((StringColumn) table.column("dataset")).append(datasetName);
            IntColumn column1 =((IntColumn) table.column("trainsize")).append(train.size());
            IntColumn column2 =((IntColumn) table.column("testsize")).append(test.size());
            IntColumn column3 =((IntColumn) table.column("dimensions")).append(train.dimensions());
            IntColumn column4 =((IntColumn) table.column("length")).append(train.length());
            IntColumn column41 =((IntColumn) table.column("minlength")).append(train.minLength());
            IntColumn column42 =((IntColumn) table.column("maxlength")).append(train.maxLength());
            // check if train and test classes match
            IntColumn column5 =((IntColumn) table.column("classes")).append(train.getNumClasses());
            //check both train and test
            IntColumn column6 =((IntColumn) table.column("variable")).append(train.isVariableLength() ? 1 : 0);
            IntColumn column7 =((IntColumn) table.column("missing")).append(train.hasMissingValues()? 1 : 0);

        }

        table.write().csv("out/archive.csv");


    }


}
