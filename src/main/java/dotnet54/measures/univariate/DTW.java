package dotnet54.measures.univariate;

import static java.lang.Math.sqrt;

import java.util.Random;

import dotnet54.measures.Measure;
import dotnet54.tscore.data.Dataset;

/**
 * Some classes in this package may contain borrowed code from the timeseriesweka project (Bagnall, 2017), 
 * we might have modified (bug fixes, and improvements for efficiency) the original classes.
 * 
 */

public class DTW extends UnivariateMeasure {
	public static final String windowParam = "w";
	protected int windowSize = -1;
	protected double windowPercentage = -1;
	protected String randomParamStrategy = "ts-chief"; //{ts-chief,default,uniform,normal,beta}

	public DTW(Measure measureName) {
		super(measureName);
	}

	@Override
	public double distance(double[] series1, double[] series2, double bsf) {
		return distance(series1, series2, bsf, windowSize);
	}

	//fast DTW implemented by Geoff Webb
	public synchronized double distance(double[] series1, double[] series2,double bsf, int windowSize) {
		if (windowSize == -1) {
			windowSize = series1.length;
		}

		int length1 = series1.length;
		int length2 = series2.length;

		int maxLength = Math.max(length1, length2);
		
		double[] prevRow = new double[maxLength];
		double[] currentRow = new double[maxLength];
		
		if (prevRow == null || prevRow.length < maxLength) {
			prevRow = new double[maxLength];
		}
		
		if (currentRow == null || currentRow.length < maxLength) {
			currentRow = new double[maxLength];
		}

		int i, j;
		double prevVal;
		double thisSeries1Val = series1[0];
		
		// initialising the first row - do this in prevRow so as to save swapping rows before next row
		prevVal = prevRow[0] = squaredDistance(thisSeries1Val, series2[0]);

		for (j = 1; j < Math.min(length2, 1 + windowSize); j++) {
			prevVal = prevRow[j] = prevVal + squaredDistance(thisSeries1Val, series2[j]);
		}

		// the second row is a special case
		if (length1 >= 2){
			thisSeries1Val = series1[1];
			
			if (windowSize>0){
				currentRow[0] = prevRow[0]+squaredDistance(thisSeries1Val, series2[0]);
			}
			
			// in this special case, neither matrix[1][0] nor matrix[0][1] can be on the (shortest) minimum path
			prevVal = currentRow[1]=prevRow[0]+squaredDistance(thisSeries1Val, series2[1]);
			int jStop = (windowSize + 2 > length2) ? length2 : windowSize + 2;

				for (j = 2; j < jStop; j++) {
					// for the second row, matrix[0][j - 1] cannot be on a (shortest) minimum path
					prevVal = currentRow[j] = Math.min(prevVal, prevRow[j - 1]) + squaredDistance(thisSeries1Val, series2[j]);
				}
		}
		
		// third and subsequent rows
		for (i = 2; i < length1; i++) {
			int jStart;
			int jStop = (i + windowSize >= length2) ? length2-1 : i + windowSize;
			
			// the old currentRow becomes this prevRow and so the currentRow needs to use the old prevRow
			double[] tmp = prevRow;
			prevRow = currentRow;
			currentRow = tmp;
			
			thisSeries1Val = series1[i];

			if (i - windowSize < 1) {
				jStart = 1;
				currentRow[0] = prevRow[0] + squaredDistance(thisSeries1Val, series2[0]);
			}
			else {
				jStart = i - windowSize;
			}
			
			if (jStart <= jStop){
				// If jStart is the start of the window, [i][jStart-1] is outside the window.
				// Otherwise jStart-1 must be 0 and the path through [i][0] can never be less than the path directly from [i-1][0]

				//bugfix 1-4-2020 - when window=0, this line should not look at  prevRow[jStart] -- Shifaz
				if (windowSize == 0){
					prevVal = currentRow[jStart] = prevVal + squaredDistance(thisSeries1Val, series2[jStart]);
//					prevVal = currentRow[jStart] = prevRow[jStart - 1] + squaredDistance(thisSeries1Val, series2[jStart]);
				}else{
					prevVal = currentRow[jStart] = Math.min(prevRow[jStart - 1], prevRow[jStart])+ squaredDistance(thisSeries1Val, series2[jStart]);
					for (j = jStart+1; j < jStop; j++) {
						prevVal = currentRow[j] = min(prevRow[j - 1], prevVal, prevRow[j])
								+ squaredDistance(thisSeries1Val, series2[j]);
					}

					if (i + windowSize >= length2) {
						// the window overruns the end of the sequence so can have a path through prevRow[jStop]
						currentRow[jStop] = min(prevRow[jStop - 1], prevRow[jStop], prevVal) + squaredDistance(thisSeries1Val, series2[jStop]);
					}
					else {
						currentRow[jStop] = Math.min(prevRow[jStop - 1], prevVal) + squaredDistance(thisSeries1Val, series2[jStop]);
					}
				}


			}
		}
		
		double res = sqrt(currentRow[length2 - 1]);
		
		return res;
	}

	public final double min(double A, double B, double C) {
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

	public final double squaredDistance(double A, double B) {
		double x = A - B;
		return x * x;
	}

	@Override
	public void initParamsByID(Dataset trainData, Random rand) {

	}

	@Override
	public void setParamsByID(int paramID, int seriesLength, Random rand) {
		setWindowSizeDbl(paramID / 100.0, seriesLength);
	}

	@Override
	public void setRandomParamByID(Dataset trainData, Random rand) {
		int paramID = rand.nextInt(100 );
		setParamsByID(paramID, trainData.length(), rand);
	}

	@Override
	public void setRandomParams(Dataset trainData, Random rand) {
		if (randomParamStrategy.equals("ts-chief")){
			setWindowSizeInt(get_random_window(trainData, rand), trainData.length());
		}else{
			setWindowSizeInt(getRandomWindowUsingDefaultStrategy(trainData, rand), trainData.length());
		}
	}

	public double getWindowSize(){
		return this.windowSize;
	}

	public void setWindowSizeDbl(double windowPercentage, int seriesLength){
		int window;
		if(0 <= windowPercentage && windowPercentage <= 1){
			this.windowPercentage = windowPercentage;
			window  = (int) Math.ceil(seriesLength  * windowPercentage);
		}else {
			window = seriesLength;
		}
		setWindowSizeInt(window, seriesLength);
	}

	public void setWindowSizeInt(int windowSize, int seriesLength){
		if (windowSize < 0 || windowSize > seriesLength) {
			windowSize = seriesLength;
		}
		this.windowSize = windowSize;
	}

	public void setWindowSize(int windowSize){
		this.windowSize = windowSize;
	}

	@Override
	public String toString() {
		return "DTW[,w="+windowSize+",wp="+windowPercentage+","
				+super.toString()+",]";
	}

	// function used in ts-chief paper
	public int get_random_window(Dataset d, Random r) {
		return r.nextInt(Math.max(((d.length() +1) / 4), 4));
	}

	public int getRandomWindowUsingDefaultStrategy(Dataset trainData, Random rand){
		// w = uniform(1, L/4), min = 1 because Euclidean already use w=0
		return rand.nextInt( trainData.length() / 4) + 1;
	}

}