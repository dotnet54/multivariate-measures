package dotnet54.util;

import dotnet54.applications.tschief.TSChiefOptions;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class InputParser {


    /**
     *
     * Checks if a value exceeds the given bounds, if so, its set to max or min bound
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static int clamp(int value, int min, int max){
        if (value > max){
            value = max;
        }
        if (value < min){
            value = min;
        }
        return value;
    }

    public static int[] clamp(int[] values, int min, int max){
        return Arrays.stream(values).map(v-> clamp(v, min , max)).toArray();
    }

    public static IntStream clamp(IntStream values, int min, int max){
        return values.map(v-> clamp(v, min , max));
    }

    public static double clamp(double value, double min, double max){
        if (value > max){
            value = max;
        }
        if (value < min){
            value = min;
        }
        return value;
    }

    public static double[] clamp(double[] values, int min, int max){
        return Arrays.stream(values).map(v-> clamp(v, min , max)).toArray();
    }

    public static DoubleStream clamp(DoubleStream values, double min, double max){
        return values.map( v-> clamp(v, min , max));
    }

    /**
     *
     * Clamps a value with converting negative values going reverse from max value
     *
     * @param value
     * @param max
     * @return
     */
    public static int clampAround(int value, int max){
        if (value > max){
            value = max;
        }
        if (value < 0){
            value = max + value;
        }
        return value;
    }

    public static int[] clampAround(int[] values, int max){
        return Arrays.stream(values).map( v-> clampAround(v , max)).toArray();
    }

    public static IntStream clampAround(IntStream values, int max){
        return values.map( v-> clampAround(v , max));
    }

    public static int toInt(String str, int min, int max){
        int value = Integer.parseInt(str);
        if (value > max){
            value = max;
        }
        if (value < min){
            value = min;
        }
        return value;
    }

    public static int[] toInt(String[] strArray, int min, int max){
        return clamp(Arrays.stream(strArray).mapToInt(Integer::parseInt), min, max).toArray();
    }

    public static double toDouble(String str, double min, double max){
        double value = Double.parseDouble(str);
        if (value > max){
            value = max;
        }
        if (value < min){
            value = min;
        }
        return value;
    }

    public static double[] toDouble(String[] strArray, int min, int max){
        return clamp(Arrays.stream(strArray).mapToDouble(Double::parseDouble), min, max).toArray();

    }

    public static String getStrIfExists(String[] args, int pos, String defaultVal){
        if (args.length > pos){
            return args[pos];
        }else {
            return defaultVal;
        }
    }

    public static boolean getBooleanIfExists(String[] args, int pos, boolean defaultVal){
        if (args.length > pos){
            return Boolean.parseBoolean(args[pos]);
        }else {
            return defaultVal;
        }
    }

    public static int getIntIfExists(String[] args, int pos, int defaultVal){
        if (args.length > pos){
            return Integer.parseInt(args[pos]);
        }else {
            return defaultVal;
        }
    }

    public static double getDoubleIfExists(String[] args, int pos, double defaultVal){
        if (args.length > pos){
            return Double.parseDouble(args[pos]);
        }else {
            return defaultVal;
        }
    }

    public int getIntIfExists(String[] args, int pos, int defaultVal, int min, int max){
        if (args.length > pos){
            return toInt(args[pos], min, max);
        }else {
            return clamp(defaultVal, min, max);
        }
    }

    public static double getDoubleIfExists(String[] args, int pos, double defaultVal, double min, double max){
        if (args.length > pos){
            return toDouble(args[pos], min, max);
        }else {
            return clamp(defaultVal, min, max);
        }
    }

    public static int[] parseIntSet(String arg, String separator){
        String cleanedString = arg.replaceAll("^\\{|\\}$", "");
        String[] split = cleanedString.split(separator);
        return Arrays.stream(split).mapToInt(Integer::parseInt).distinct().toArray();
    }

    public static double[] parseDoubleSet(String arg, String separator){
        String cleanedString = arg.replaceAll("^\\{|\\}$", "");
        String[] split = cleanedString.split(separator);
        return Arrays.stream(split).mapToDouble(Double::parseDouble).distinct().toArray();
    }

    public static IntStream parseIntList(String arg, String separator){
        String cleanedString = arg.replaceAll("^\\[|\\]$", "");
        String[] split = cleanedString.split(separator);
        return Arrays.stream(split).mapToInt(Integer::parseInt);
    }

    public static DoubleStream parseDoubleList(String arg, String separator){
        String cleanedString = arg.replaceAll("^\\[|\\]$", "");
        String[] split = cleanedString.split(separator);
        return Arrays.stream(split).mapToDouble(Double::parseDouble);
    }

    public static int parseIntArg(String arg, TSChiefOptions options){
        switch (arg) {
            case "size":
                return options.getTrainingSet().size();
            case "length":
                return options.getTrainingSet().length();
            case "dims":
                return options.getTrainingSet().dimensions();
            default:
                return Integer.parseInt(arg);
        }
    }

    public static double parseDoubleArg(String arg, TSChiefOptions options){
        switch (arg) {
            case "size":
                return options.getTrainingSet().size();
            case "length":
                return options.getTrainingSet().length();
            case "dims":
                return options.getTrainingSet().dimensions();
            default:
                return Double.parseDouble(arg);
        }
    }

    public static double parseSqrt(String[] args, double defaultVal, TSChiefOptions options){
        if (args.length > 1) {
            return Math.sqrt(parseDoubleArg(args[1], options));
        }
        return defaultVal;
    }

    public static double parseLoge(String[] args, double defaultVal, TSChiefOptions options){
        if (args.length > 1) {
            return Math.log(parseDoubleArg(args[1], options));
        }
        return defaultVal;
    }

    public static double parseLog2(String[] args, double defaultVal, TSChiefOptions options){
        if (args.length > 1) {
            return Math.log(parseDoubleArg(args[1], options)) / Math.log(2);
        }
        return defaultVal;
    }

    public static double parseLog10(String[] args, double defaultVal, TSChiefOptions options){
        if (args.length > 1) {
            return Math.log10(parseDoubleArg(args[1], options)) / Math.log(2);
        }
        return defaultVal;
    }

    public static double parseSigmoid(String[] args, double defaultVal, TSChiefOptions options){
        if (args.length > 1) {
            return 1 / (1 + Math.pow(Math.E, -parseDoubleArg(args[1], options)));
        }
        return defaultVal;
    }

    public static double[] parseUniform(String[] args, TSChiefOptions options){
        double[] params = new double[2]; // min, max
        if (args.length > 2) {
            params[0] = parseDoubleArg(args[1], options);
            params[1] = parseDoubleArg(args[2], options);
        }else {
            throw new IllegalArgumentException();
        }
        if (params[0] < params[1]){
            throw new IllegalArgumentException();
        }
        return params;
    }

    public static double[] parseNormal(String[] args, TSChiefOptions options){
        double[] params = new double[2]; // mean, std
        if (args.length > 2) {
            params[0] = Double.parseDouble(args[1]);
            params[1] = Double.parseDouble(args[2]);
        }else {
            throw new IllegalArgumentException();
        }
        if (params[0] < params[1]){
            throw new IllegalArgumentException();
        }
        return params;
    }

    public static double[] parseFlatBeta(String[] args, TSChiefOptions options){
        double[] params = new double[3]; // alpha, beta, min
        if (args.length > 3) {
            params[0] = Double.parseDouble(args[1]);
            params[1] = Double.parseDouble(args[2]);
            params[2] = parseDoubleArg(args[3], options);
        }if (args.length > 2) {
            params[0] = Double.parseDouble(args[1]);
            params[1] = Double.parseDouble(args[2]);
            params[2] = 1;
        }else {
            throw new IllegalArgumentException();
        }
        if (params[0] < params[1]){
            throw new IllegalArgumentException();
        }
        return params;
    }


}
