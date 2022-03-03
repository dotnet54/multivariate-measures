package dotnet54.classifiers;

import dotnet54.tscore.Options;
import dotnet54.tscore.data.Dataset;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class ClassifierResult {
    // times are stored using System.nanoSeconds() unless specified

    protected transient Classifier model;
    protected transient Options options;

    protected transient boolean hasInitialized;
    protected transient boolean isTrainingComplete;
    protected transient boolean isPredictionComplete;

    protected transient int[] predictedLabels;

    public int index;
    public String timestamp;
    public String dataset;
    public int train_size;
    public int test_size;
    public int dimensions;
    public int length;
    public int classes;

    public double accuracy;
    public double error_rate;
    public int correct;
    public int errors;
    public double total_time_s;
    public double total_time_hr;
    public double train_time_hr;
    public double test_time_hr;
    public String total_time_formatted;
    public String train_time_formatted;
    public String test_time_formatted;
    public long train_time_ns;
    public long test_time_ns;
    public long init_time_ns;
    public transient AtomicLong startInitTime;
    public transient AtomicLong endInitTime;

    public transient AtomicLong startTrainTime;
    public transient AtomicLong endTrainTime;
    public transient AtomicLong startTestTime;
    public transient AtomicLong endTestTime;

    public long rand_seed;
    public int threads;
    public int cpus;
    public String hostname;
    public String version;
    public String build_date;
    public String version_tag;

    public String experiment_name;
    public String experiment_id;
    public boolean normalize;
    public boolean shuffle;
    public int fold_no;
    public int repeat_no;
    public int train_classes;
    public int test_classes;

    public long max_memory;
    public long total_memory;
    public long free_memory;
    public long free_memory_after_init;
    public long free_memory_after_train;
    public long free_memory_after_test;
    public long alloc_memory;
    public long alloc_memory_after_init;
    public long alloc_memory_after_train;
    public long alloc_memory_after_test;

    public ClassifierResult(Classifier model){
        startInitTime = new AtomicLong();
        endInitTime = new AtomicLong();
        startTrainTime = new AtomicLong();
        endTrainTime = new AtomicLong();
        startTestTime = new AtomicLong();
        endTestTime = new AtomicLong();
        this.model = model;
        this.options = model.getOptions();
    }

    public void beforeInit() {
        startInitTime.set(System.nanoTime());
        max_memory = (Runtime.getRuntime().maxMemory())  / (1024*1024);
        total_memory = (Runtime.getRuntime().totalMemory())  / (1024*1024);
        free_memory = Runtime.getRuntime().freeMemory() / (1024*1024);
        alloc_memory = total_memory - free_memory;
    }

    public void afterInit() {
        endInitTime.set(System.nanoTime());
        init_time_ns = endInitTime.get() - startInitTime.get();
        total_memory = (Runtime.getRuntime().totalMemory())  / (1024*1024);
        free_memory_after_init = Runtime.getRuntime().freeMemory() / (1024*1024);
        alloc_memory_after_init = total_memory - free_memory_after_init;
        hasInitialized = true;
    }

    public void beforeTrain(Dataset trainData){
        if (!hasInitialized){
            beforeInit();
            afterInit();
        }

        startTrainTime.set(System.nanoTime());
    }

    public void afterTrain(){
        endTrainTime.set(System.nanoTime());
        train_time_ns = endTrainTime.get() - startTrainTime.get();
        train_time_formatted = DurationFormatUtils.formatDuration((long) (train_time_ns / 1e6), "d-H:m:s.SSS");
        total_memory = (Runtime.getRuntime().totalMemory())  / (1024*1024);
        free_memory_after_train = Runtime.getRuntime().freeMemory() / (1024*1024);
        alloc_memory_after_train = total_memory - free_memory_after_train;
        isTrainingComplete = true;
    }

    public void beforeTest(Dataset testData){

        if (!hasInitialized){
            throw new RuntimeException("Call model.fit before calling predict");
        }

        startTestTime.set(System.nanoTime());
        predictedLabels = new int[testData.size()];
    }

    public void afterTest(){

        if (!hasInitialized){
            throw new RuntimeException("Call model.fit before calling predict");
        }

        endTestTime.set(System.nanoTime());
        test_time_ns = endTestTime.get() - startTestTime.get();
        total_time_s = ((train_time_ns + test_time_ns) / 1e9);
        total_time_hr = total_time_s / 3600;
        train_time_hr = train_time_ns / 1e9 / 3600;
        test_time_hr = test_time_ns / 1e9 / 3600;
        test_time_formatted = DurationFormatUtils.formatDuration((long) (test_time_ns / 1e6), "d-H:m:s.SSS");
        total_time_formatted = DurationFormatUtils.formatDuration((long) (total_time_s * 1e3), "d-H:m:s.SSS");
        total_memory = (Runtime.getRuntime().totalMemory())  / (1024*1024);
        free_memory_after_test = Runtime.getRuntime().freeMemory() / (1024*1024);
        alloc_memory_after_test = total_memory - free_memory_after_test;

        accuracy = (double) correct / test_size;
        errors = test_size - correct;
        error_rate = 1 - accuracy;

        isPredictionComplete = true;
    }

    public int[] getPredictedLabels(){
        return this.predictedLabels;
    }


    public void setPredictedLabels(int[] predictedLabels) {
        this.predictedLabels = predictedLabels;
    }

//    public double getAccuracyScore(){
//        return ((double) correct) / this.predictedLabels.length;
//    }

}
