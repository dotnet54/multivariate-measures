package dotnet54.tscore.data;

public interface DatasetIndex {

    public int size();

    public int[] getIndices();

    public void setIndices(int[] indices);

    public int getIndex(int i);

    public void setIndex(int i, int index);

    public TimeSeries getSeries(int i);

    public boolean hasDataset();

    public Dataset getDataset();

    public void setDataset(Dataset dataset);

    public void syncByReplacing(Dataset targetDataset);

    // validate

}
