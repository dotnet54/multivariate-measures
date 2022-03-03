package dotnet54.util;

import java.util.*;

public class Statistics {

	public static double sum(double[] list) {
		double sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i];
		}
		return sum;
	}	
	
	public static long sum(long[] list) {
		long sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i];
		}
		return sum;
	}	
	
	public static int sum(int[] list) {
		int sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i];
		}
		return sum;
	}		
	
	public static int sumIntList(List<Integer> list) {
		int sum = 0;
		int length = list.size();
		for (int i = 0; i < length; i++) {
			sum +=list.get(i);
		}
		return sum;
	}

	public static double sumDoubleList(List<Double> list) {
		double sum = 0;
		int length = list.size();
		for (int i = 0; i < length; i++) {
			sum += list.get(i);
		}
		return sum;
	}
	
	public static double meanIntList(List<Integer> list) {
		return Statistics.sumIntList(list) / list.size();
	}	
	
	public static double meanDoubleList(List<Double> list) {
		return Statistics.sumDoubleList(list) / list.size();
	}	
	
	public static double mean(double[] list) {
		double sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i];
		}
		return sum/list.length;
	}
	
	public static double mean(int[] list) {
		int sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i];
		}
		return sum/list.length;
	}
	
	public static double mean(long[] list) {
		long sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i];
		}
		return sum/list.length;
	}
	
	public static double standard_deviation_population(double[] list) {
		double mean = Statistics.mean(list);
		double sq_sum = 0;
		
		for (int i = 0; i < list.length; i++) {
			sq_sum += (list[i] - mean) * (list[i] - mean);
		}
		
		return Math.sqrt(sq_sum/list.length);
	}
	
	public static double standard_deviation_population(long[] list) {
		double mean = Statistics.mean(list);
		long sq_sum = 0;
		
		for (int i = 0; i < list.length; i++) {
			sq_sum += (list[i] - mean) * (list[i] - mean);
		}
		
		return Math.sqrt((double)sq_sum/list.length);
	}

	public static double standard_deviation_population(int[] list) {
		double mean = Statistics.mean(list);
		long sq_sum = 0;
		
		for (int i = 0; i < list.length; i++) {
			sq_sum += (list[i] - mean) * (list[i] - mean);
		}
		
		return Math.sqrt((double)sq_sum/list.length);
	}
	
	public static double median(double[] numArray) {
		Arrays.sort(numArray);
		double median;
		if (numArray.length % 2 == 0)
		    median = ((double)numArray[numArray.length/2] + (double)numArray[numArray.length/2 - 1])/2;
		else
		    median = (double) numArray[numArray.length/2];		
		return median;
	}

	public static double median(List<Double> values, boolean copyArr) {
		List<Double> copy;
		if (copyArr) copy = new ArrayList<>(values); else copy = values;
		Collections.sort(copy);
		if (copy.size() % 2 == 1)
			return copy.get(copy.size()/2);
		else
			return (copy.get(copy.size()/2 - 1) + copy.get(copy.size()/2)) / 2;
	}

	public static double indexOfMin(double[] dist) {
		double min = dist[0];
		int minInd = 0;

		for (int i = 1; i < dist.length; ++i) {
			if (dist[i] < min) {
				min = dist[i];
				minInd = i;
			}
		}
		return minInd;
	}


	public static double indexOfMax(double[] dist) {
		double max = dist[0];
		int maxInd = 0;

		for (int i = 1; i < dist.length; ++i) {
			if (dist[i] > max) {
				max = dist[i];
				maxInd = i;
			}
		}
		return maxInd;
	}

	public static class SortIndexDescending implements Comparator<Integer> {
		private double[] values;

		public SortIndexDescending(double[] values){
			this.values = values;
		}

		public Integer[] getIndicies(){
			Integer[] indicies = new Integer[values.length];
			for (int i = 0; i < values.length; i++) {
				indicies[i] = i;
			}
			return indicies;
		}

		@Override
		public int compare(Integer index1, Integer index2) {
			if (values[index2] < values[index1]){
				return -1;
			}
			else if (values[index2] > values[index1]){
				return 1;
			}
			else{
				return 0;
			}
		}
	}

	public static class SortIndexAscending implements Comparator<Integer> {
		private double[] values;

		public SortIndexAscending(double[] values){
			this.values = values;
		}

		public Integer[] getIndicies(){
			Integer[] indicies = new Integer[values.length];
			for (int i = 0; i < values.length; i++) {
				indicies[i] = i;
			}
			return indicies;
		}

		@Override
		public int compare(Integer index1, Integer index2) {
			if (values[index1] < values[index2]){
				return -1;
			}
			else if (values[index1] > values[index2]){
				return 1;
			}
			else{
				return 0;
			}
		}
	}

//	/**
//	 *  commenting this method -- USE new implementation in class StandardDeviation 13/10/2020
//	 *
//	 * Welford's online algoirithm for standard deviation
//	 *
//	 * Reference: https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
//	 *
//	 * @param aggregate array of 5 values for existing values[count, mean, sqDistanceFromMean, varPopulation. stdPopulation]
//	 * @param newValue new value to add to the aggregates
//	 * @return array of 3 values for the next call [count, mean, sqDistanceFromMean, varPopulation. stdPopulation]
//	 */
//	public static double[] stdvPopulationOnline(double[] aggregate, double newValue, boolean updateAggregates){
//		if (aggregate == null || aggregate.length != 5){
//			aggregate = new double[5];
//		}
//
//		if (updateAggregates){
//			aggregate[0] += 1; //count += 1
//			double delta = newValue - aggregate[1]; //delta = newValue - mean
//			aggregate[1] += delta / aggregate[0]; //mean += delta / count
//			double delta2 = newValue - aggregate[1]; //delta2 = newValue - mean
//			aggregate[2] += delta * delta2; //sqDistanceFromMean += delta * delta2
//		}else{
//			if (aggregate[0] < 2){
//				return new double[]{1,newValue,0,0,0};
//			}else{
//				aggregate[3] = aggregate[2] / aggregate[0];  //sqDistanceFromMean / count
//				aggregate[4] = Math.sqrt(aggregate[2] / aggregate[0]); //sqDistanceFromMean / count
////				if (Double.isNaN(aggregate[3])){
////					throw new RuntimeException("stdvPopulationOnline: sqDistanceFromMean aggregate[3]" + aggregate[3]);
////				}
////				if (Double.isNaN(aggregate[4])){
////					throw new RuntimeException("stdvPopulationOnline: varPopulation aggregate[4]" + aggregate[4]);
////				}
//			}
//		}
//		return aggregate;
//	}
//
}
