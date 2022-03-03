package dotnet54.transformers;

import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.MTSDataset;
import dotnet54.tscore.data.MTimeSeries;
import dotnet54.tscore.data.TimeSeries;
import dotnet54.tscore.exceptions.NotSupportedException;

public class DerivativeTransformer implements Transformer {

    @Override
    public void fit(Dataset inputData, Options options) {

    }

    @Override
    public Dataset transform(Dataset inputData, Options options) {
        Dataset transformedDataset = new MTSDataset(inputData.size());
        transformedDataset.setName(inputData.getName());
        transformedDataset.setTags(inputData.getTags());
        int numDimensions = inputData.dimensions();
        int seriesLength = inputData.length();

        for (int i = 0; i < inputData.size(); i++) {
            TimeSeries series = inputData.getSeries(i);
            TimeSeries transformedSeries;

            double[][] transformedData = new double[numDimensions][];
            for (int dimension = 0; dimension < numDimensions; dimension++) {
                transformedData[dimension] = new double[seriesLength]; // change to length - 1? derivative series has -1 length
                getDerivative(transformedData[dimension], series.data(dimension));
            }
            transformedSeries = new MTimeSeries(series, transformedData);
            transformedDataset.add(transformedSeries);
        }
        transformedDataset.setTags(transformedDataset.getTags() + ",deriv1");

        return transformedDataset;
    }

    @Override
    public TimeSeries transform(TimeSeries inputSeries, Options options) {
        TimeSeries transformedSeries;
        int numDimensions = inputSeries.dimensions();
        int seriesLength = inputSeries.length();

        double[][] transformedData = new double[numDimensions][];
        for (int dimension = 0; dimension < numDimensions; dimension++) {
            transformedData[dimension] = new double[seriesLength]; // change to length - 1? derivative series has -1 length
            getDerivative(transformedData[dimension], inputSeries.data(dimension));
        }

        transformedSeries = new MTimeSeries(inputSeries, transformedData);
        return transformedSeries;
    }

    @Override
    public double[] transform(double[] inputData, Options options) {
        return null;
    }

    @Override
    public Dataset fitTransform(Dataset inputData, Options options) {
        return null;
    }

    @Override
    public Dataset inverseTransform(Dataset inputData, Options options) {
        throw new NotSupportedException();
    }

    @Override
    public double[] inverseTransform(double[] inputData, Options options) {
        return null;
    }

    public Options getOptions() {
        return null;
    }

    public void setOptions(Options options) {
    }

    public <T> T getOption(String optionName) {
        return null;
    }

    public <T> void setOption(String optionName, T optionValue) {
    }

    private void getDerivative(double[] outputArray, double[] vector) {
        for (int i = 1; i < vector.length - 1; i++) {
            outputArray[i] = ((vector[i] - vector[i - 1]) + ((vector[i + 1] - vector[i - 1]) / 2.0)) / 2.0;
        }
        outputArray[0] = outputArray[1];
        outputArray[outputArray.length - 1] = outputArray[outputArray.length - 2];
    }

}
