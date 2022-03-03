package dotnet54.util.math.doubles;

public class Normalization {

    public static double[] zNormalize(double[] vector, double mean, double std) {
        double[] normalizedVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalizedVector[i] = (vector[i] - mean)/std;
        }
        return normalizedVector;
    }

    public static void zNormalise(double[] seriesData){
        double meanSum = 0;

        for (int i = 0; i < seriesData.length; i++){
            meanSum += seriesData[i];
        }

        double mean = meanSum / seriesData.length;

        double squareSum = 0;

        for (int i = 0; i < seriesData.length; i++){
            double temp = seriesData[i] - mean;
            squareSum += temp * temp;
        }

        double stdev = Math.sqrt(squareSum/(seriesData.length-1));

        if (stdev == 0){
            stdev = 1;
        }

        for (int i = 0; i < seriesData.length; i++){
            seriesData[i] = (seriesData[i] - mean) / stdev;
        }
    }

    public static double[] meanNormalize(double[] vector, double mean) {
        double[] normalizedVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalizedVector[i] = vector[i] - mean;
        }
        return normalizedVector;
    }

    //scales between 0 and 1
    public static double[] featureScale(double[] vector, double min, double max) {
        double[] rescaledVector = new double[vector.length];
        double scale = max - min;
        for (int i = 0; i < vector.length; i++) {
            rescaledVector[i] = (vector[i] - min)/scale;
        }
        return rescaledVector;
    }

    //scales between lower bound lb and upper bound ub
    public static double[] featureScale(double[] vector, double min, double max, double lb, double ub) {
        double[] rescaledVector = new double[vector.length];
        double scale = max - min;
        double bound = ub-lb;
        for (int i = 0; i < vector.length; i++) {
            rescaledVector[i] = lb + ((vector[i] - min) * (bound))/scale;
        }
        return rescaledVector;
    }

}