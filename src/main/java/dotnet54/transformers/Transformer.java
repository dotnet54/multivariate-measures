package dotnet54.transformers;

import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.TimeSeries;

public interface Transformer {

    public void fit(Dataset inputData, Options options);

    public Dataset transform(Dataset inputData, Options options);

    public TimeSeries transform(TimeSeries inputSeries, Options options);

    public double[] transform(double[] inputData, Options options);

    public Dataset fitTransform(Dataset inputData, Options options);

    public Dataset inverseTransform(Dataset inputData, Options options);

    public double[] inverseTransform(double[] inputData, Options options);

    public Options getOptions();

    public void setOptions(Options options);

    public <T> T getOption(String optionName);

    public <T> void setOption(String optionName, T optionValue);

}
