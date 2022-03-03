package dotnet54.classifiers.tschief.splitters.cif;

import dotnet54.tscore.data.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntervalList {

    protected static final int MAX_ITERATIONS = 1000;
    protected static final int DIM = 0;
    protected static final int START = 1;
    protected static final int END = 2;
    protected static final int LENGTH = 3;

    protected Random rand;
    protected IntervalGenerationMethod method = IntervalGenerationMethod.startThenEnd;
    protected int minLength = 3; // 0 <= minLength <= maxLength <= seriesLength
    protected int maxLength = -1; // 0 <= minLength <= maxLength <= seriesLength, maxLength == -1 => maxLength == seriesLength
    protected int seriesLength;   // 0 <= seriesLength, length of the series
    protected int dimension = -1; // -1 == interval applies to all
    protected int numIntervals = 1;

    // just storing length even though its not necessary
    // [numIntervals][dimension,startIndex,endIndex,length]
    protected List<int[]> intervals;

    // different methods may produce different distributions of interval lengths, and start points,
    // which introduces a bias to the generated random intervals,
    // e.g. some methods may favour longer random intervals, while others may favor shorter random intervals
    protected enum IntervalGenerationMethod{
        startThenEnd,
        startThenLength,
        lengthThenStart,
        twoPointsThenSwap,
    }

    public IntervalList(Random rand){
        this.rand = rand;
    }

    public IntervalList(Random rand, int minLength, int maxLength, IntervalGenerationMethod method){
        this.rand = rand;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.method = method;
    }

    public int getRandomNumberInRangeInclusive(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        return rand.nextInt((max - min) + 1) + min;
    }

    public List<int[]> generateIntervals(int numIntervals, int seriesLength) {
        switch (method){
            case startThenEnd:
                return generateNIntervalsUsingStartThenEnd(numIntervals, seriesLength);
            case startThenLength:
                return generateNIntervalsUsingStartThenLength(numIntervals, seriesLength);
            case lengthThenStart:
                return generateNIntervalsUsingLengthThenStart(numIntervals, seriesLength);
            case twoPointsThenSwap:
                return generateNIntervalsUsingTwoPointsThenSwap(numIntervals, seriesLength);
            default:
                throw new IllegalArgumentException("Unknown interval generation method");
        }
    }

    public List<int[]> generateNIntervalsUsingStartThenEnd(int numIntervals, int seriesLength){
        //[numIntervals][dimension,startIndex,endIndex,length]
        this.intervals = new ArrayList<>(numIntervals);

        if (this.minLength < 0){
            this.minLength = 3;
        }else if(minLength > seriesLength){
            this.minLength = 3;
        }

        if (this.maxLength < this.minLength){
            this.maxLength = seriesLength;
        }else if(maxLength > seriesLength){
            this.maxLength = seriesLength;
        }

        int minIndex, maxIndex;
        for (int i = 0; i < numIntervals; i++) {
            int[] interval = new int[4];
            interval[DIM] = this.dimension;
            // start = range(0, seriesLength - minLength)
            minIndex = 0;
            maxIndex = seriesLength -1 - minLength;
            interval[START] = rand.nextInt((maxIndex - minIndex) + 1) + minIndex;
            // end = range(start + minLength, seriesLength)
            minIndex = interval[1] + minLength;
            maxIndex = seriesLength - 1;
            interval[END] = rand.nextInt((maxIndex - minIndex) + 1) + minIndex;
            // length = end - start + 1
            interval[LENGTH] = interval[END] - interval[START] + 1;

            this.intervals.add(interval);
        }

        return intervals;
    }

    public List<int[]> generateNIntervalsUsingStartThenLength(int numIntervals, int seriesLength){
        //[numIntervals][dimension,startIndex,endIndex,length]
        this.intervals = new ArrayList<>(numIntervals);

        if (this.minLength < 0){
            this.minLength = 3;
        }else if(minLength > seriesLength){
            this.minLength = 3;
        }

        if (this.maxLength < this.minLength){
            this.maxLength = seriesLength;
        }else if(maxLength > seriesLength){
            this.maxLength = seriesLength;
        }

        for (int i = 0; i < numIntervals; i++) {
            int[] interval = new int[4];
            interval[DIM] = this.dimension;
            //TODO
//            // start = range(0, seriesLength - minLength)
//            interval[1] = rand.nextInt((seriesLength - minLength) + 1) + minLength;
//            // length = range(minLength, maxLength)
//            interval[3] = rand.nextInt((seriesLength - minLength) + 1) + minLength;
//            // end =
//            interval[2] = rand.nextInt();
            this.intervals.add(interval);
        }

        return intervals;
    }

    public List<int[]> generateNIntervalsUsingLengthThenStart(int numIntervals, int seriesLength){
        //[numIntervals][dimension,startIndex,endIndex,length]
        this.intervals = new ArrayList<>(numIntervals);

        if (this.minLength < 0){
            this.minLength = 3;
        }else if(minLength > seriesLength){
            this.minLength = 3;
        }

        if (this.maxLength < this.minLength){
            this.maxLength = seriesLength;
        }else if(maxLength > seriesLength){
            this.maxLength = seriesLength;
        }

        for (int i = 0; i < numIntervals; i++) {
            int[] interval = new int[4];
            interval[DIM] = this.dimension;
            //TODO
//            //startIndex = rangeInclusive(0, seriesLength - minLength)
//            interval[1] = rand.nextInt((seriesLength - minLength) + 1) + minLength;
//            //endIndex = rangeInclusive(startIndex, seriesLength)
//            interval[2] = rand.nextInt();
//            interval[3] = interval[2] - interval[1] + 1; // length = end - start + 1

            this.intervals.add(interval);
        }

        return intervals;
    }

    public List<int[]> generateNIntervalsUsingTwoPointsThenSwap(int numIntervals, int seriesLength){
        //[numIntervals][dimension,startIndex,endIndex,length]
        this.intervals = new ArrayList<>(numIntervals);

        if (this.minLength < 0){
            this.minLength = 3;
        }else if(minLength > seriesLength){
            this.minLength = 3;
        }

        if (this.maxLength < this.minLength){
            this.maxLength = seriesLength;
        }else if(maxLength > seriesLength){
            this.maxLength = seriesLength;
        }

        for (int i = 0; i < numIntervals; i++) {
            int[] interval = new int[4];
            interval[DIM] = this.dimension;
            //TODO
//            //startIndex = rangeInclusive(0, seriesLength - minLength)
//            interval[1] = rand.nextInt((seriesLength - minLength) + 1) + minLength;
//            //endIndex = rangeInclusive(startIndex, seriesLength)
//            interval[2] = rand.nextInt();
//            interval[3] = interval[2] - interval[1] + 1; // length = end - start + 1

            this.intervals.add(interval);
        }

        return intervals;
    }

    public double[][][] copyIntervalData(int intervalIndex, Dataset dataset){
        int[] interval = intervals.get(intervalIndex);
        int dataSize = dataset.size();
        double[][][] intervalData = new double[dataSize][dataset.dimensions()][interval[LENGTH]];
        double[][] seriesData;

        for (int i = 0; i < dataSize; i++) {
            seriesData = dataset.getSeriesData(i);
            for (int d = 0; d < seriesData.length; d++) {
                System.arraycopy(seriesData[d], interval[START], intervalData[i][d], 0, interval[LENGTH]);
            }
        }

        return intervalData;
    }

    public void toFile(String fileName){
        //TODO
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getSeriesLength() {
        return seriesLength;
    }

    public void setSeriesLength(int seriesLength) {
        this.seriesLength = seriesLength;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getNumIntervals() {
        return numIntervals;
    }

    public void setNumIntervals(int numIntervals) {
        this.numIntervals = numIntervals;
    }

}
