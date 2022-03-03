package dotnet54.measures.univariate_pea;

import dotnet54.measures.Measure;
import dotnet54.tscore.data.Dataset;

import java.util.Random;

public class Euclidean extends UnivariateMeasureWithPEA {

	public Euclidean() {
		super(Measure.euc);
	}

	@Override
	public double distance_wrapper(double[] series1, double[] series2, double bsf){
		double total = 0;
		int length = Math.min(series1.length, series2.length);

		for (int i = 0; i < length; i++){
			total += (series1[i] - series2[i]) * (series1[i] - series2[i]);
		}

//		return Math.sqrt(total);
		return total;
	}

	@Override
	public void initParamsByID(Dataset trainData, Random rand) {

	}

	@Override
	public void setParamsByID(int paramID, int seriesLength, Random rand) {

	}

	@Override
	public void setRandomParamByID(Dataset trainData, Random rand) {

	}

	@Override
	public void setRandomParams(Dataset trainData, Random rand) {

	}

}
