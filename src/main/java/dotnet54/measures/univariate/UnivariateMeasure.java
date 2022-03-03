package dotnet54.measures.univariate;

import java.util.Random;

import dotnet54.measures.Measure;
import dotnet54.tscore.data.Dataset;
import dotnet54.classifiers.tschief.TSChiefNode;

public abstract class UnivariateMeasure {
	
	public Measure measureName;
	public boolean enableEarlyAbandoning = false; // TODO
	public boolean enableLowerBounding = false; // TODO

	// If true, reuse arrays from MemorySpaceProvider
	// NOT THREAD SAFE unless each thread have its own MemorySpaceProvider
	// However, current PF and TS-CHIEF's default configuration uses one thread per tree making this thread-safe
	// If in the future, multiple threads are used to train a single node, this might create issues.
	public boolean useSharedMemoryObjects = true;

	//DEV
	public boolean debug = false;
//    public boolean refreshParamCache = true;

	public UnivariateMeasure(Measure measureName){
		this.measureName = measureName;
	}

	public double distance(double[] series1, double[] series2){
		return this.distance(series1, series2, Double.POSITIVE_INFINITY);
	}

	public abstract double distance(double[] series1, double[] series2, double bsf);

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

	public static UnivariateMeasure createUnivariateMeasure(Measure measureName){
		UnivariateMeasure measure = null;
		switch (measureName) {
			case euc:
			case euclidean:
				measure = new Euclidean();
				break;
			case dtwf:
				measure = new DTW(measureName);
				break;
			case dtw:
			case dtwr:
				measure = new DTW(measureName);
				break;
			case ddtwf:
				measure = new DDTW(measureName);
				break;
			case ddtw:
			case ddtwr:
				measure = new DDTW(measureName);
				break;
			case wdtw:
				measure = new WDTW();
				break;
			case wddtw:
				measure = new WDDTW();
				break;
			case lcss:
				measure = new LCSS();
				break;
			case msm:
				measure = new MSM();
				break;
			case erp:
				measure = new ERP();
				break;
			case twe:
				measure = new TWE();
				break;
			default:
				throw new RuntimeException("Unknown univariate similarity measure");
		}

		return measure;
	}

}
