package dotnet54.tscore.dev;

@CsvExportAnnotations
public class CsvExportFileDev {

    @CsvExportAnnotations.excludeColumn
    String fileName;

    // annotation based csv exporter
    public CsvExportFileDev(){

    }

}
