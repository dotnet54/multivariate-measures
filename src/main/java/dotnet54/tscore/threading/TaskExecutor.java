package dotnet54.tscore.threading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskExecutor {

    private ExecutorService executor;
    private int numThreads;
    private int numRequestedThreads;
    private int numAvailableProcessors;
    private int maxPoolSize;

    /**
     * new class to replace MultiThreadedTasks and ParallelTasks class
     * @2021
     *
     */

     public TaskExecutor(int numThreads){
         int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
         this.numRequestedThreads = numThreads;

         if (numThreads == 0 || numThreads > numAvailableProcessors) {
             this.numThreads = Runtime.getRuntime().availableProcessors();
         }else{
             this.numThreads = numThreads;
         }

         this.executor = Executors.newFixedThreadPool(this.numThreads);

         // this is important for slurm jobs because
         // Runtime.getRuntime().availableProcessors() does not equal SBATCH argument
         // cpus-per-task
         if (executor instanceof ThreadPoolExecutor) {
             maxPoolSize = ((ThreadPoolExecutor) executor).getMaximumPoolSize();
         }
     }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ThreadPoolExecutor getThreadPool() {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor);
        } else {
            return null;
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            //just wait forever
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    public class AsyncTaskResult{
        protected AtomicInteger taskID;

        public AsyncTaskResult(int taskID){
            this.taskID = new AtomicInteger(taskID);
        }

        public AtomicInteger getTaskID() {
            return taskID;
        }

    }

    public class AsyncTask implements Callable<AsyncTaskResult> {
        protected AsyncTaskResult result;

        public AsyncTask(int taskID){
            this.result = new AsyncTaskResult(taskID);
        }

        public AsyncTaskResult getTaskID() {
            return result;
        }

        @Override
        public AsyncTaskResult call() throws Exception {
            return this.result;
        }
    }

    /**
     * Executes an array of tasks in parallel and waits until all tasks are completed
     * Returns when all given tasks are completed
     *
     * @param tasks
     * @param <T>
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public <T> List<T> runTasks(Callable<T>[] tasks) throws InterruptedException, ExecutionException {
        List<Callable<T>> taskList = Arrays.asList(tasks);
        List<Future<T>> futures;
        List<T> results = new ArrayList();

        futures = this.executor.invokeAll(taskList);

        for (int i = 0; i < futures.size(); i++) {
            Future<T> future = futures.get(i);
            results.add(future.get()); // block until complete
        }

        return results;
    }

}
