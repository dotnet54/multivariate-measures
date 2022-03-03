package dotnet54.measures.univariate;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import dotnet54.measures.Measure;
import dotnet54.tscore.data.Dataset;
import dotnet54.util.Util;

/**
 * Some classes in this package may contain borrowed code from the timeseriesweka project (Bagnall, 2017), 
 * we might have modified (bug fixes, and improvements for efficiency) the original classes.
 * 
 */

public class ERP extends UnivariateMeasure {
	public static final String windowParam = "w";
	public static final String gapParam = "g";
	//	protected double[] currentRow, prevRow; //not thread safe
	protected int windowSize = -1;
	protected double windowPercentage = -1;
	protected double g;

	// TODO each instance of distance function reallocates memory for this -- refactor to reuse the existing arrays where possible
	protected double[] windowCache;
	protected double[] gCache;
	private transient double[] curr, prev;

	public ERP() {
		super(Measure.erp);
	}

	@Override
	public double distance(double[] series1, double[] series2, double bsf) {
		return distance(series1, series2, bsf, windowSize, g);
	}

	public synchronized double distance(double[] first, double[] second, double bsf, int windowSize, double gValue) {
		// base case - we're assuming class val is last. If this is
		// true, this method is fine,
		// if not, we'll default to the DTW class

		int m = first.length;
		int n = second.length;

		if (curr == null || curr.length < m) {
			curr = new double[m];
			prev = new double[m];
		} else {
			// FPH: init to 0 just in case, didn't check if
			// important
			for (int i = 0; i < curr.length; i++) {
				curr[i] = 0.0;
				prev[i] = 0.0;
			}
		}

		// size of edit distance band
		// bandsize is the maximum allowed distance to the diagonal
		// int band = (int) Math.ceil(v2.getDimensionality() *
		// bandSize);
//		int band = (int) Math.ceil(m * bandSize);
		int band = windowSize;

		// g parameter for local usage
		for (int i = 0; i < m; i++) {
			// Swap current and prev arrays. We'll just overwrite
			// the new curr.
			{
				double[] temp = prev;
				prev = curr;
				curr = temp;
			}
			int l = i - (band + 1);
			if (l < 0) {
				l = 0;
			}
			int r = i + (band + 1);
			if (r > (m - 1)) {
				r = (m - 1);
			}

			for (int j = l; j <= r; j++) {
				if (Math.abs(i - j) <= band) {
					// compute squared distance of feature
					// vectors
					double val1 = first[i];
					double val2 = gValue;
					double diff = (val1 - val2);
//					final double d1 = Math.sqrt(diff * diff);
					final double d1 = diff;//FPH simplificaiton

					val1 = gValue;
					val2 = second[j];
					diff = (val1 - val2);
//					final double d2 = Math.sqrt(diff * diff);
					final double d2 = diff;

					val1 = first[i];
					val2 = second[j];
					diff = (val1 - val2);
//					final double d12 = Math.sqrt(diff * diff);
					final double d12 = diff;

					final double dist1 = d1 * d1;
					final double dist2 = d2 * d2;
					final double dist12 = d12 * d12;

					final double cost;

					if ((i + j) != 0) {
						if ((i == 0) || ((j != 0) && (((prev[j - 1] + dist12) > (curr[j - 1] + dist2))
										&& ((curr[j - 1] + dist2) < (prev[j] + dist1))))) {
							// del
							cost = curr[j - 1] + dist2;
						} else if ((j == 0) || ((i != 0) && (((prev[j - 1] + dist12) > (prev[j] + dist1))
										&& ((prev[j] + dist1) < (curr[j - 1] + dist2))))) {
							// ins
							cost = prev[j] + dist1;
						} else {
							// match
							cost = prev[j - 1] + dist12;
						}
					} else {
						cost = 0;
					}

					curr[j] = cost;
					// steps[i][j] = step;
				} else {
					curr[j] = Double.POSITIVE_INFINITY; // outside
									    // band
				}
			}
		}

		return Math.sqrt(curr[m - 1]);	//TODO do we need sqrt here
	}
	
	public int get_random_window(Dataset d, Random r) {
//		int x = (d.length() +1) / 4;
		int w = r.nextInt(d.length()/ 4+1);
		return w;
	} 	
	
	public double get_random_g(Dataset d, Random r) {
		double stdv = DistanceTools.stdv_p(d);		
		double g = r.nextDouble()*.8*stdv+0.2*stdv; //[0.2*stdv,stdv]
		return g;
	}

	@Override
	public void initParamsByID(Dataset trainData, Random rand) {
		double stdTrain = trainData.getStdv();
		double[] stdPerDim = trainData.getStdvPerDimension(); // TODO check getStdvPerDimension

		windowCache = Util.linspaceDbl(0, 0.25, 10,true);
		gCache = Util.linspaceDbl(stdPerDim[0] * 0.2, stdPerDim[0], 10,true);
	}

	@Override
	public void setParamsByID(int paramID, int seriesLength, Random rand) {
		setWindowSizeDbl(windowCache[paramID / 10], seriesLength);
		setG(gCache[paramID % 10]);
	}

	@Override
	public void setRandomParamByID(Dataset trainData, Random rand) {
		int paramID = rand.nextInt(100 );
		setParamsByID(paramID, trainData.length(), rand);
	}

	@Override
	public void setRandomParams(Dataset trainData, Random rand) {
		setWindowSizeInt(getRandomWindowSize(trainData, rand));
		setG(get_random_g(trainData, rand));
	}


	public static int getRandomWindowSize(Dataset dataset, Random rand) {
		return rand.nextInt(dataset.length() / 4); // w ~ U[0, L/4]
	}

	public int getWindowSize() {
		return windowSize;
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

	public void setWindowSizeInt(int windowSize){
		this.windowSize = windowSize;
	}

	public double getG() {
		return g;
	}

	public void setG(double g) {
		this.g = g;
	}

	@Override
	public String toString(){
		return "ERP[,w="+windowSize+",wp="+windowPercentage+",g="+g+","
				+super.toString()+",]";
	}

}
