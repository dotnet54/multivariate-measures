package dotnet54.measures.univariate_pea;

import dotnet54.measures.Measure;
import dotnet54.measures.univariate.UnivariateMeasure;
import dotnet54.tscore.data.Dataset;

import java.util.Random;

public abstract class UnivariateMeasureWithPEA extends UnivariateMeasure {

	public Measure measureName;
//	public boolean enableEarlyAbandoning = false;
//	public boolean enableLowerBounding = false;

	// If true, reuse arrays from MemorySpaceProvider
	// NOT THREAD SAFE unless each thread have its own MemorySpaceProvider
	// However, current PF and TS-CHIEF's default configuration uses one thread per tree making this thread-safe
	// If in the future, multiple threads are used to train a single node, this might create issues.
	public boolean useSharedMemoryObjects = false;

	//DEV
	public boolean debug = false;
//    public boolean refreshParamCache = true;
	public boolean transformByFirstDerivative = false;
	private transient double[] deriv1, deriv2;

	public UnivariateMeasureWithPEA(Measure measureName){
		super(measureName);
		this.measureName = measureName;
	}

	public double distance(double[] series1, double[] series2){
		return this.distance(series1, series2, Double.POSITIVE_INFINITY);
	}

	public abstract double distance_wrapper(double[] series1, double[] series2, double cutoff);

	public double distance(double[] series1, double[] series2, double cutoff){
		if (transformByFirstDerivative){
			if (deriv1 == null || deriv1.length != series1.length) {
				deriv1 = new double[series1.length];
			}
			getDeriv(deriv1,series1);

			if (deriv2 == null || deriv2.length != series2.length) {
				deriv2 = new double[series2.length];
			}
			getDeriv(deriv2,series2);

			return distance_wrapper(deriv1, deriv2, cutoff);
		}else{
			return distance_wrapper(series1, series2, cutoff);
		}
	}

	public double distance(Dataset dataset, int ixSeries1, int ixSeries2){

		// fetch dimension 0
		double[] series1 = dataset.getSeriesData(ixSeries1)[0];
		double[] series2 = dataset.getSeriesData(ixSeries2)[0];

		return this.distance(series1, series2, Double.POSITIVE_INFINITY);
	}

	public double distance(Dataset trainDataset, Dataset testDataset, int ixSeries1, int ixSeries2){

		// fetch dimension 0
		double[] series1 = trainDataset.getSeriesData(ixSeries1)[0];
		double[] series2 = testDataset.getSeriesData(ixSeries2)[0];

		return this.distance(series1, series2, Double.POSITIVE_INFINITY);
	}

	protected static void getDeriv(double[]d, double[] series) {
		for (int i = 1; i < series.length - 1 ; i++) {
			d[i] = ((series[i] - series[i - 1]) + ((series[i + 1] - series[i - 1]) / 2.0)) / 2.0;
		}
		d[0] = d[1];
		d[d.length - 1] = d[d.length - 2];
	}

	public abstract void initParamsByID(Dataset trainData, Random rand);

	public abstract void setParamsByID(int paramID, int seriesLength, Random rand);

	public abstract void setRandomParamByID(Dataset trainData, Random rand);

	public abstract void setRandomParams(Dataset trainData, Random rand);

	public double geDoubleParam(String name)  {
		throw new RuntimeException("Invalid parameter name");
	}

	public void setDoubleParam(String name, double value)  {
		throw new RuntimeException("Invalid parameter");

	}

	public int getIntParam(String name)  {
		throw new RuntimeException("Invalid parameter");

	}

	public void setIntParam(String name, int value)  {
		throw new RuntimeException("Invalid parameter name");
	}

	public String getClassName(){
		return this.getClass().getSimpleName();
	}

	public Measure getMeasureName(){
		return measureName;
	}

	public String toString() {
		return this.measureName.toString(); //+ " [" + dm.toString() + "]";
	}

	public static UnivariateMeasureWithPEA createUnivariateMeasure(Measure measureName){
		UnivariateMeasureWithPEA measure = null;
		switch (measureName) {
			case euc:
			case euclidean:
				measure = new Euclidean();
				break;
			case dtwf:
				measure = new DTW(measureName, false);
				break;
			case dtw:
			case dtwr:
				measure = new CDTW(measureName, false);
				break;
			case ddtwf:
				measure = new DTW(measureName, true); // TODO derivatives
//				measure = new DTW(measureName, false); // TODO derivatives
				break;
			case ddtw:
			case ddtwr:
				measure = new CDTW(measureName, true);  // TODO derivatives
//				measure = new CDTW(measureName, false);  // TODO derivatives
				break;
			case wdtw:
				measure = new WDTW(measureName, false);
				break;
			case wddtw:
				measure = new WDTW(measureName, true);  // TODO derivatives
//				measure = new WDTW(measureName, false);  // TODO derivatives
				break;
			case lcss:
				measure = new LCSS(measureName);
				break;
			case msm:
				measure = new MSM(measureName);
				break;
			case erp:
				measure = new ERP(measureName);
				break;
			case twe:
				measure = new TWE(measureName);
				break;
			default:
				throw new RuntimeException("Unknown univariate similarity measure");
		}

		return measure;
	}

}
