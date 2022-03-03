package dotnet54.measures.univariate;

import java.util.Random;

import dotnet54.measures.Measure;
import dotnet54.tscore.data.Dataset;

/**
 * Some classes in this package may contain borrowed code from the timeseriesweka project (Bagnall, 2017), 
 * we might have modified (bug fixes, and improvements for efficiency) the original classes.
 * 
 */

public class WDTW extends UnivariateMeasure {
	
	private static final double WEIGHT_MAX = 1;
	private double g; // "empirical constant that controls the curvature (slope) of the function
	private transient double[] weightVector; // initilised on first distance call
	
	public WDTW() {
		super(Measure.wdtw);
	}

	public WDTW(Measure measureName) {
		super(measureName);
	}

	@Override
	public double distance(double[] series1, double[] series2, double bsf) {
		return distance(series1, series2, bsf, g);
	}

	//fast WDTW implemented by Geoff Webb
	public synchronized double distance(double[] first, double[] second, double bsf, double g) {
		this.setG(g, Math.max(first.length,second.length));

		double[] prevRow = new double[second.length];
		double[] curRow = new double[second.length];
		double second0 = second[0];
		double thisDiff;
		double prevVal = 0.0;
		
		// put the first row into prevRow to save swapping before moving to the second row
		
		{	double first0 = first[0];
		
			// first value
			thisDiff = first0 - second0;
			prevVal = prevRow[0] = this.weightVector[0] * thisDiff * thisDiff;
	
			// top row
			for (int j = 1; j < second.length; j++) {
				thisDiff = first0 - second[j];
				prevVal = prevRow[j] = prevVal + this.weightVector[j] * thisDiff * thisDiff;
			}
		}
		
		double minDistance;
		double firsti = first[1];
		
		// second row is a special case because path can't go through prevRow[j]
		thisDiff = firsti - second0;
		prevVal = curRow[0] = prevRow[0] + this.weightVector[1] * thisDiff * thisDiff;
		
		for (int j = 1; j < second.length; j++) {
			// calculate distances
			minDistance = Math.min(prevVal, prevRow[j - 1]);
			thisDiff = firsti - second[j];
			prevVal = curRow[j] = minDistance + this.weightVector[j-1] * thisDiff * thisDiff;
		}

		// warp rest
		for (int i = 2; i < first.length; i++) {
			// make the old current row into the current previous row and set current row to use the old prev row
			double [] tmp = curRow;
			curRow = prevRow;
			prevRow = tmp;
			firsti = first[i];
			
			thisDiff = firsti - second0;
			prevVal = curRow[0] = prevRow[0] + this.weightVector[i] * thisDiff * thisDiff;
			
			for (int j = 1; j < second.length; j++) {
				// calculate distances
				minDistance = min(prevVal, prevRow[j], prevRow[j - 1]);
				thisDiff = firsti - second[j];
				prevVal = curRow[j] = minDistance + this.weightVector[Math.abs(i - j)] * thisDiff * thisDiff;
			}

		}

		double res = prevVal;
		return res;
	}
	
	public static final double min(double A, double B, double C) {
		if (A < B) {
			if (A < C) {
				// A < B and A < C
				return A;
			} else {
				// C < A < B
				return C;
			}
		} else {
			if (B < C) {
				// B < A and B < C
				return B;
			} else {
				// C < B < A
				return C;
			}
		}
	}


	@Override
	public void initParamsByID(Dataset trainData, Random rand) {
		initWeights(trainData.length());
	}

	@Override
	public void setParamsByID(int paramID, int seriesLength, Random rand) {
		setG(paramID / 100.0, seriesLength);
	}

	@Override
	public void setRandomParamByID(Dataset trainData, Random rand) {
		int paramID = rand.nextInt(100 );
		setParamsByID(paramID, trainData.length(), rand);
	}

	@Override
	public void setRandomParams(Dataset trainData, Random rand) {
		setG(getRandomG(trainData, rand), trainData.length());
	}

	public static double getRandomG(Dataset dataset, Random rand) {
		return rand.nextDouble();
	}


	public void setG(double g,int length){
		if(this.g!=g || this.weightVector==null){
			this.g= g;
			this.initWeights(length);
		}

	}

	private void initWeights(int seriesLength) {
		this.weightVector = new double[seriesLength];
		double halfLength = (double) seriesLength / 2;

		for (int i = 0; i < seriesLength; i++) {
			weightVector[i] = WEIGHT_MAX / (1 + Math.exp(-g * (i - halfLength)));
		}
	}
	
	public double get_random_g(Dataset d, Random r) {
		return r.nextDouble();
	}

	@Override
	public String toString(){
		return "WDTW[,g="+g+","
				+super.toString()+",]";
	}

}
