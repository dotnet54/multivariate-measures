package dotnet54.classifiers.tschief.splitters.cif;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.splitters.rise.ACF;
import dotnet54.classifiers.tschief.splitters.rise.ARMA;
import dotnet54.classifiers.tschief.splitters.rise.PACF;
import dotnet54.transformers.Transformer;
import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.TimeSeries;
import org.jtransforms.fft.DoubleFFT_1D;

public class RiseFeatureTransformer implements Transformer {

    protected int maxLag;
    protected transient DoubleFFT_1D fft = null;

    public RiseFeatureTransformer(){

    }
    @Override
    public void fit(Dataset inputData, Options options) {
        this.fft = new DoubleFFT_1D(inputData.length());
    }

    @Override
    public Dataset transform(Dataset inputData, Options options) {
        return null;
    }

    @Override
    public TimeSeries transform(TimeSeries inputSeries, Options options) {
        return null;
    }

    @Override
    public double[] transform(double[] inputData, Options options) {
        double[] out = null;
        int num_attribs = 4;

        double[] acf = ACF.transformArray(inputData, maxLag);
        double[] pacf = PACF.transformArray(acf, maxLag);
        double[] arma = ARMA.transformArray(inputData, acf, pacf, maxLag, false);

        double[] spectral = transformSpectral(inputData, TSChiefOptions.RiseFeatures.PS);

        int len = acf.length + pacf.length + arma.length + spectral.length;

        out = new double[num_attribs * 4];	//TODO check

        //new changes in v1.1.17 adding num_attribs_per_component
        System.arraycopy(acf, 0, out, 0, Math.min(num_attribs, acf.length));
        System.arraycopy(pacf, 0, out, acf.length, Math.min(num_attribs,pacf.length));
        System.arraycopy(arma, 0, out, acf.length + pacf.length, Math.min(num_attribs,arma.length));
        System.arraycopy(spectral, 0, out, acf.length + pacf.length + arma.length, Math.min(num_attribs,spectral.length));

        for (int i = 0; i < out.length; i++) {
            if (out[i] == Double.NaN) {
                out[i] = 0;
            }
        }

        return out;
    }

    //just a temp function -- move this code to a filter
    private double[] transformSpectral(double[] input, TSChiefOptions.RiseFeatures subFilter) {
        //extract this code to a clean PS filter in future, for now, just keeping here to prevent unnecessarily creating lots of objects
        //

        if (subFilter == TSChiefOptions.RiseFeatures.DFT) {
            double[] dft = new double[2 * input.length];
            System.arraycopy(input, 0, dft, 0, input.length);
            //        this.fft.realForward(dft);
            this.fft.complexForward(dft);
            //        ps[1] = 0; // DC-coefficient imaginary part

            return dft;

        }else{ // use PS
            double[] dft = new double[2 * input.length];
            System.arraycopy(input, 0, dft, 0, input.length);
            //        this.fft.realForward(dft);
            this.fft.complexForward(dft);
            //        ps[1] = 0; // DC-coefficient imaginary part

            //TODO using PS only because HiveCOTE uses it, we could just use DFT here, right?

            double[] ps = new double[input.length];

            for(int j=0;j<input.length;j++){
                ps[j] = dft[j*2] * dft[j*2] + dft[j*2+1] * dft[j*2+1];
            }

            return ps;
        }

    }

    @Override
    public Dataset fitTransform(Dataset inputData, Options options) {
        return null;
    }

    @Override
    public Dataset inverseTransform(Dataset inputData, Options options) {
        return null;
    }

    @Override
    public double[] inverseTransform(double[] inputData, Options options) {
        return new double[0];
    }

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public void setOptions(Options options) {

    }

    @Override
    public <T> T getOption(String optionName) {
        return null;
    }

    @Override
    public <T> void setOption(String optionName, T optionValue) {

    }
}
