package dotnet54.tscore.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ContinuousLabelEncoder {

//    Encode target labels with value between 0 and nClasses-1.

    private Map<String, Integer> encodings;
    private String[] uniqueClasses;
    private boolean isFitted = false;

    public ContinuousLabelEncoder(){
        encodings = new HashMap<>();
    }

    public void fit(String[] y){
        uniqueClasses = Arrays.stream(y).distinct().toArray(String[]::new);
        Arrays.sort(uniqueClasses);

        for (int i = 0; i < uniqueClasses.length; i++) {
            encodings.put(uniqueClasses[i], i);
        }

        isFitted = true;
    }

    public int[] fitTransform(String[] y){
        fit(y);
        return encodings.values().stream().mapToInt(i->i).toArray();
    }

    public int[] transform(String[] y){
        if (!isFitted){
            throw new RuntimeException("LabelEncoder must be fitted before calling the transform function.");
        }

        int[] testEncodings = new int[y.length];

        for (int i = 0; i < y.length ; i++) {
            if (encodings.containsKey(y[i])){
                testEncodings[i] = encodings.get(y[i]);
            }else{
                throw new RuntimeException("Argument y contains labels unknown during the fitting process");
            }
        }

        return testEncodings;
    }

    public String[] inverseTransform(int[] y) throws Exception {
        if (!isFitted){
            throw new RuntimeException("LabelEncoder must be fitted before calling the inverse_transform function.");
        }

        String[] testDecodings = new String[y.length];

        for (int i = 0; i < y.length ; i++) {

            if (y[i] > 0 && y[i] < uniqueClasses.length){
                testDecodings[i] = uniqueClasses[i];
            }else{
                throw new RuntimeException("Argument y contains labels unknown during the fitting process");
            }
        }

        return testDecodings;
    }

    public int getEncodedLabel(String label){
        return encodings.get(label);
    }
}
