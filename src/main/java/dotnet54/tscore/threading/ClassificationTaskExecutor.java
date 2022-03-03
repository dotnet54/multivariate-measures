package dotnet54.tscore.threading;

import dotnet54.classifiers.Classifier;
import dotnet54.classifiers.ClassifierResult;
import dotnet54.tscore.DebugInfo;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassificationTaskExecutor extends TaskExecutor {

    public ClassificationTaskExecutor(int numThreads){
        super(numThreads);
    }

    public class TrainingTask implements Callable<ClassifierResult> {
        protected Classifier model;
        protected Dataset trainData;
        protected DatasetIndex trainIndices;
        protected DebugInfo debugInfo;
        protected AtomicInteger taskID;

        //dev
        public int verbosity;

        public TrainingTask(Classifier model, Dataset trainData, int taskID) {
            this.model = model;
            this.trainData = trainData;
            this.taskID = new AtomicInteger(taskID);
        }

        public TrainingTask(Classifier model, Dataset trainData, DatasetIndex trainIndices, DebugInfo debugInfo, int taskID) {
            this.model = model;
            this.trainData = trainData;
            this.trainIndices = trainIndices;
            this.debugInfo = debugInfo;
            this.taskID = new AtomicInteger(taskID);
        }

        @Override
        public ClassifierResult call() throws Exception {
            ClassifierResult result;

            if (trainIndices == null){
                result = model.fit(trainData);
            }else{
                result = model.fit(trainData, trainIndices, debugInfo);
            }

            System.out.print(this.taskID + ".");
            return result;
        }

    }

    public class PredictionTask implements Callable<ClassifierResult> {
        protected Classifier model;
        protected Dataset testData;
        protected DatasetIndex testIndices;
        protected DebugInfo debugInfo;
        protected AtomicInteger taskID;
        protected boolean getProbability;
        protected int startIndex = 0;
        protected int endIndex = -1;
        protected boolean useRange;

        public PredictionTask(Classifier model, Dataset testData, int taskID) {
            this.model = model;
            this.testData = testData;
            this.taskID = new AtomicInteger(taskID);
            this.getProbability = false;
            this.endIndex = testData.size() - 1;
        }

        public PredictionTask(Classifier model, Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo,
                              boolean getProbability, int taskID) {
            this.model = model;
            this.testData = testData;
            this.testIndices = testIndices;
            this.debugInfo = debugInfo;
            this.taskID = new AtomicInteger(taskID);
            this.getProbability = getProbability;
            this.endIndex = testData.size() - 1;
        }

        public PredictionTask(Classifier model, Dataset testData, DatasetIndex testIndices, DebugInfo debugInfo,
                              boolean getProbability, int taskID, int startIndex, int endIndex) {
            this.model = model;
            this.testData = testData;
            this.testIndices = testIndices;
            this.debugInfo = debugInfo;
            this.taskID = new AtomicInteger(taskID);
            this.getProbability = getProbability;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.useRange = true;
        }


        @Override
        public ClassifierResult call() throws Exception {
            ClassifierResult result = model.getTrainResults();

            if (startIndex > endIndex){
                throw new RuntimeException("PredictionTask: Prediction starting index "+startIndex
                    +" should be less than the end index " +endIndex);
            }

            if (useRange){
                long startTime = System.nanoTime();
                // TODO timings not work here, needs to change time counters to AtomicLong
                for (int i = startIndex; i <= endIndex; i++) {
//					predictedClasses[i] = model.predict(testData.getSeries(testIndices.getIndex()[i]));
                    int[] predictedLabels = result.getPredictedLabels();
                    predictedLabels[i] = model.predict(i);
                }
                long endTime = System.nanoTime();
                if (startIndex != 0){
                    model.getTestResults().test_time_ns += startTime - endTime;
                }
            }else{
                if (testIndices == null){
                    if (getProbability){
                        result = model.predictProba(testData);
                    }else{
                        result = model.predict(testData);
                    }
                }else{
                    if (getProbability){
                        result = model.predictProba(testData, testIndices, debugInfo);
                    }else{
                        result = model.predict(testData, testIndices, debugInfo);
                    }
                }
            }
            System.out.print(this.taskID + ".");
            return result;
        }

    }

}
