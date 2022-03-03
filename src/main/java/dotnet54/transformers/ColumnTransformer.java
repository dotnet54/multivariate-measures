package dotnet54.transformers;

import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.TimeSeries;

public class ColumnTransformer implements Transformer{
    @Override
    public void fit(Dataset inputData, Options params) {

    }

    @Override
    public Dataset transform(Dataset inputData, Options params) {
        return null;
    }

    @Override
    public TimeSeries transform(TimeSeries inputSeries, Options params) {
        return null;
    }

    @Override
    public double[] transform(double[] inputData, Options params) {
        return new double[0];
    }

    @Override
    public Dataset fitTransform(Dataset inputData, Options params) {
        return null;
    }

    @Override
    public Dataset inverseTransform(Dataset inputData, Options params) {
        return null;
    }

    @Override
    public double[] inverseTransform(double[] inputData, Options params) {
        return new double[0];
    }

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public void setOptions(Options params) {

    }

    @Override
    public <T> T getOption(String optionName) {
        return null;
    }

    @Override
    public <T> void setOption(String optionName, T optionValue) {

    }
}
