package dotnet54.util.math.doubles;

import java.util.Random;

/**
 * some functions are copied from weka.core.Util class
 */
public class Compare {

    public static final double THRESHOLD = 1e-6;

    public static double min(double a, double b){
        return (a <= b) ? a : b;
    }

//    // if equal return the diagonal element (a)
//    public static double min(double a, double b, double c){
//        return (a <= b) ? a : b;
//    }



    /** The natural logarithm of 2. */
    public static double log2 = Math.log(2);

    /**
     * Tests if a is equal to b.
     *
     * @param a a double
     * @param b a double
     */
    public static /*@pure@*/ boolean eq(double a, double b){

        return (a == b) || ((a - b < THRESHOLD) && (b - a < THRESHOLD));
    }

    /**
     * Tests if a is smaller or equal to b.
     *
     * @param a a double
     * @param b a double
     */
    public static /*@pure@*/ boolean smOrEq(double a,double b) {

        return (a-b < THRESHOLD) || (a <= b);
    }

    /**
     * Tests if a is greater or equal to b.
     *
     * @param a a double
     * @param b a double
     */
    public static /*@pure@*/ boolean grOrEq(double a,double b) {

        return (b-a < THRESHOLD) || (a >= b);
    }

    /**
     * Tests if a is smaller than b.
     *
     * @param a a double
     * @param b a double
     */
    public static /*@pure@*/ boolean sm(double a,double b) {

        return (b-a > THRESHOLD);
    }

    /**
     * Tests if a is greater than b.
     *
     * @param a a double
     * @param b a double 
     */
    public static /*@pure@*/ boolean gr(double a,double b) {

        return (a-b > THRESHOLD);
    }


    /**
     * Returns the logarithm of a for base 2.
     *
     * @param a 	a double
     * @return	the logarithm for base 2
     */
    public static /*@pure@*/ double log2(double a) {

        return Math.log(a) / log2;
    }

    /**
     * Returns index of maximum element in a given
     * array of doubles. First maximum is returned.
     *
     * @param doubles the array of doubles
     * @return the index of the maximum element
     */
    public static /*@pure@*/ int maxIndex(double[] doubles) {

        double maximum = 0;
        int maxIndex = 0;

        for (int i = 0; i < doubles.length; i++) {
            if ((i == 0) || (doubles[i] > maximum)) {
                maxIndex = i;
                maximum = doubles[i];
            }
        }

        return maxIndex;
    }

    /**
     * Returns index of maximum element in a given
     * array of integers. First maximum is returned.
     *
     * @param ints the array of integers
     * @return the index of the maximum element
     */
    public static /*@pure@*/ int maxIndex(int[] ints) {

        int maximum = 0;
        int maxIndex = 0;

        for (int i = 0; i < ints.length; i++) {
            if ((i == 0) || (ints[i] > maximum)) {
                maxIndex = i;
                maximum = ints[i];
            }
        }

        return maxIndex;
    }

    /**
     * Computes the mean for an array of doubles.
     *
     * @param vector the array
     * @return the mean
     */
    public static /*@pure@*/ double mean(double[] vector) {

        double sum = 0;

        if (vector.length == 0) {
            return 0;
        }
        for (int i = 0; i < vector.length; i++) {
            sum += vector[i];
        }
        return sum / (double) vector.length;
    }

    /**
     * Returns index of minimum element in a given
     * array of integers. First minimum is returned.
     *
     * @param ints the array of integers
     * @return the index of the minimum element
     */
    public static /*@pure@*/ int minIndex(int[] ints) {

        int minimum = 0;
        int minIndex = 0;

        for (int i = 0; i < ints.length; i++) {
            if ((i == 0) || (ints[i] < minimum)) {
                minIndex = i;
                minimum = ints[i];
            }
        }

        return minIndex;
    }

    /**
     * Returns index of minimum element in a given
     * array of doubles. First minimum is returned.
     *
     * @param doubles the array of doubles
     * @return the index of the minimum element
     */
    public static /*@pure@*/ int minIndex(double[] doubles) {

        double minimum = 0;
        int minIndex = 0;

        for (int i = 0; i < doubles.length; i++) {
            if ((i == 0) || (doubles[i] < minimum)) {
                minIndex = i;
                minimum = doubles[i];
            }
        }

        return minIndex;
    }

    /**
     * Normalizes the doubles in the array by their sum.
     *
     * @param doubles the array of double
     * @exception IllegalArgumentException if sum is Zero or NaN
     */
    public static void normalize(double[] doubles) {

        double sum = 0;
        for (int i = 0; i < doubles.length; i++) {
            sum += doubles[i];
        }
        normalize(doubles, sum);
    }

    /**
     * Normalizes the doubles in the array using the given value.
     *
     * @param doubles the array of double
     * @param sum the value by which the doubles are to be normalized
     * @exception IllegalArgumentException if sum is zero or NaN
     */
    public static void normalize(double[] doubles, double sum) {

        if (Double.isNaN(sum)) {
            throw new IllegalArgumentException("Can't normalize array. Sum is NaN.");
        }
        if (sum == 0) {
            // Maybe this should just be a return.
            throw new IllegalArgumentException("Can't normalize array. Sum is zero.");
        }
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] /= sum;
        }
    }

    /**
     * Converts an array containing the natural logarithms of
     * probabilities stored in a vector back into probabilities.
     * The probabilities are assumed to sum to one.
     *
     * @param a an array holding the natural logarithms of the probabilities
     * @return the converted array 
     */
    public static double[] logs2probs(double[] a) {

        double max = a[maxIndex(a)];
        double sum = 0.0;

        double[] result = new double[a.length];
        for(int i = 0; i < a.length; i++) {
            result[i] = Math.exp(a[i] - max);
            sum += result[i];
        }

        normalize(result, sum);

        return result;
    }

    /**
     * Returns the log-odds for a given probabilitiy.
     *
     * @param prob the probabilitiy
     *
     * @return the log-odds after the probability has been mapped to
     * [Utils.THRESHOLD, 1-Utils.THRESHOLD]
     */
    public static /*@pure@*/ double probToLogOdds(double prob) {

        if (gr(prob, 1) || (sm(prob, 0))) {
            throw new IllegalArgumentException("probToLogOdds: probability must " +
                    "be in [0,1] "+prob);
        }
        double p = THRESHOLD + (1.0 - 2 * THRESHOLD) * prob;
        return Math.log(p / (1 - p));
    }

    /**
     * Rounds a double to the next nearest integer value. The JDK version
     * of it doesn't work properly.
     *
     * @param value the double value
     * @return the resulting integer value
     */
    public static /*@pure@*/ int round(double value) {

        int roundedValue = value > 0
                ? (int)(value + 0.5)
                : -(int)(Math.abs(value) + 0.5);

        return roundedValue;
    }

    /**
     * Rounds a double to the next nearest integer value in a probabilistic
     * fashion (e.g. 0.8 has a 20% chance of being rounded down to 0 and a
     * 80% chance of being rounded up to 1). In the limit, the average of
     * the rounded numbers generated by this procedure should converge to
     * the original double.
     *
     * @param value the double value
     * @param rand the random number generator
     * @return the resulting integer value
     */
    public static int probRound(double value, Random rand) {

        if (value >= 0) {
            double lower = Math.floor(value);
            double prob = value - lower;
            if (rand.nextDouble() < prob) {
                return (int)lower + 1;
            } else {
                return (int)lower;
            }
        } else {
            double lower = Math.floor(Math.abs(value));
            double prob = Math.abs(value) - lower;
            if (rand.nextDouble() < prob) {
                return -((int)lower + 1);
            } else {
                return -(int)lower;
            }
        }
    }
    
}
