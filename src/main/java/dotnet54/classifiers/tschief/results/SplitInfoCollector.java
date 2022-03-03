package dotnet54.classifiers.tschief.results;

import dotnet54.applications.tschief.TSChiefOptions;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SplitInfoCollector {
    // TODO reduce memory usage

    public ConcurrentLinkedQueue<NodeSplitterResult> splitInfo;
    public Table splitStatTable;
    public transient TSChiefOptions options;

    public SplitInfoCollector(TSChiefOptions options){
        splitInfo = new ConcurrentLinkedQueue<>();
        splitStatTable = Table.create("splits");
        this.options = options;
    }


    public void exportSplitStatCsvFile() throws IOException {

        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".splits.csv";

        File fileObj = new File(fileName);
        File pf = fileObj.getParentFile();
        if (pf != null) {
            fileObj.getParentFile().mkdirs();
        }

        // convert concurrent queue to table
        //TODO

        splitStatTable.write().csv(fileName);
    }

    public void exportSplitStatJsonFile() throws IOException {

        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".splits.json";

        File fileObj = new File(fileName);
        File pf = fileObj.getParentFile();
        if (pf != null) {
            fileObj.getParentFile().mkdirs();
        }


    }

}
