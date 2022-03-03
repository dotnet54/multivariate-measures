package dotnet54.classifiers.tschief.results;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.TSCheifTree;
import dotnet54.classifiers.ClassifierResult;
import dotnet54.tscore.dev.WritableThreadSafeList;
import org.apache.commons.lang3.ArrayUtils;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeFormatter;

public class TSChiefTreeResult extends ClassifierResult {

    // TODO use AtomicInteger to support multithreading.
    // some of these counters are not accurate when used with multiple threads

    protected transient TSCheifTree model;
    protected transient TSChiefOptions options;
//    protected transient int[] predictedLabels; // supports a matrix of predicted labels (for ensembles)

//	@column { name="testColumn"}
//	protected int _testColumn;

    public int _forest_id;
    public int _tree_id;
    protected boolean _resultsPrepared = false;

    // TODO annotate GSON + exclusion strategy
    public transient SplitInfoCollector _splitInfoCollector;

//    public int index;
//    public String timestamp;
//    public String dataset;
//    public int train_size;
//    public int test_size;
//    public int dimensions;
//    public int length;
//    public int classes;

//    public double accuracy;
//    public double error_rate;
//    public int correct;
//    public int errors;
//    public long total_time_hr;
//    public long total_time_s;
//    public String total_time_formatted;
//    public String train_time_formatted;
//    public String test_time_formatted;
//    public long train_time_ns;
//    public long test_time_ns;
//    public long init_time_ns;
//    public transient AtomicLong startTrainTime;
//    public transient AtomicLong endTrainTime;
//    public transient AtomicLong startTestTime;
//    public transient AtomicLong endTestTime;

//    public long max_memory;
//    public long total_memory;
//    public long free_memory;
//    public long free_memory_after_init;
//    public long free_memory_after_train;
//    public long free_memory_after_test;

    public int num_trees;
    public String num_candidates_str;
    public String voting_scheme;
    public int num_nodes;
    public int num_internal_nodes;
    public int num_leaves;
    public int depth;
    //	public double weighted_depth;

    public int num_candidates;
    public int c_ee;
    public int c_boss;
    public int c_rise;
    public int c_randf;
    public int c_rotf;
    public int c_tsf;
    public int c_st;
    public int c_rt;
    public int c_it;
    public int c_cif;

    public int ee_count;
    public int boss_count;
    public int rif_count;
    public int cif_count;
    public int randf_count;
    public int rotf_count;
    public int st_count;
    public int tsf_count;
    public int it_count;
    public int rt_count;

    public int ee_win;
    public int boss_win;
    public int rif_win;
    public int cif_win;
    public int randf_win;
    public int rotf_win;
    public int st_win;
    public int tsf_win;
    public int it_win;
    public int rt_win;

    public int mv_lpi;
    public int mv_lpd;
    public String mv_dependency;
    public String mv_dims;
    public int mv_subset_size;

    public int dim_dep_count;
    public int dim_indep_count;
    public int dim_dep_win;
    public int dim_indep_win;

    public int boss_transformations;
    public int num_intervals; // total # intervals use by all interval splitters across all nodes

    public int euc_count;
    public int dtwf_count;
    public int dtw_count;
    public int ddtwf_count;
    public int ddtw_count;
    public int wdtw_count;
    public int wddtw_count;
    public int lcss_count;
    public int twe_count;
    public int erp_count;
    public int msm_count;

    public int euc_win;
    public int dtwf_win;
    public int dtw_win;
    public int ddtwf_win;
    public int ddtw_win;
    public int wdtw_win;
    public int wddtw_win;
    public int lcss_win;
    public int twe_win;
    public int erp_win;
    public int msm_win;

    public long ee_time;
    public long boss_time;
    public long rif_time;
    public long cif_time;
    public long randf_time;
    public long rotf_time;
    public long st_time;
    public long tsf_time;
    public long it_time;
    public long rt_time;

    public long euc_time;
    public long dtwf_time;
    public long dtw_time;
    public long ddtwf_time;
    public long ddtw_time;
    public long wdtw_time;
    public long wddtw_time;
    public long lcss_time;
    public long twe_time;
    public long erp_time;
    public long msm_time;

    public int rif_acf_count;
    public int rif_pacf_count;
    public int rif_arma_count;
    public int rif_ps_count;
    public int rif_dft_count;

    public int rif_acf_win;
    public int rif_pacf_win;
    public int rif_arma_win;
    public int rif_ps_win;
    public int rif_dft_win;

    public long rif_acf_time;
    public long rif_pacf_time;
    public long rif_arma_time;
    public long rif_ps_time;
    public long rif_dft_time;

    public long ee_splitter_train_time;
    public long boss_splitter_train_time;
    public long rise_splitter_train_time;
    public long st_splitter_train_time;
    public long it_splitter_train_time;
    public long rt_splitter_train_time;
    public long randf_splitter_train_time;
    public long cif_splitter_train_time;

    public long data_fetch_time;
    public long boss_data_fetch_time;
    public long rise_data_fetch_time;
    public long st_data_fetch_time;
    public long it_data_fetch_time;
    public long rt_data_fetch_time;
    public long cif_data_fetch_time;

    public long split_evaluator_train_time;

    public double accuracy_mean;
    public double accuracy_std;
//    public double correct_mean;
//    public double correct_std;
//    public double total_time_s_mean;
//    public double total_time_s_std;
//    public double num_nodes_mean;
//    public double num_nodes_std;
//    public double depth_mean;
//    public double depth_std;

    public double accuracy_forest;
    public double total_time_str_forest;
    public double max_memory_forest;

    public String args;

    // experimental
    public WritableThreadSafeList<Integer> _agSampleSizes;
    public WritableThreadSafeList<Integer> _dimSizes;


    public TSChiefTreeResult(TSCheifTree model){
        super(model);
        this.model = model;
        this.options = this.model.tsChiefOptions;
        this._splitInfoCollector = new SplitInfoCollector(options);

        //experimental
        _agSampleSizes = new WritableThreadSafeList<>(options.numTrees * options.num_splitters_per_node);
        _dimSizes = new WritableThreadSafeList<>(options.numTrees * options.num_splitters_per_node);

    }

    @Override
    public void beforeInit() {
        super.beforeInit();

        index = model.getTreeID();
        timestamp = options.experiment_timestamp.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        dataset = options.datasetName;
        train_size = options.getTrainingSet().size();
        test_size = options.getTestingSet().size();
        dimensions = options.getTrainingSet().dimensions();
        length = options.getTrainingSet().length();
        classes = options.getTrainingSet().getNumClasses();
    }

    @Override
    public void afterTrain() {
        super.afterTrain();
    }

    @Override
    public void afterTest() {
        super.afterTest();

        index = model.getTreeID();
        timestamp = options.experiment_timestamp.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        dataset = options.datasetName;
        train_size = options.getTrainingSet().size();
        test_size = options.getTestingSet().size();
        dimensions = options.getTrainingSet().dimensions();
        length = options.getTrainingSet().length();
        classes = options.getTrainingSet().getNumClasses();

        rand_seed = options.randSeed;
        threads = options.numThreads;
        cpus = options.numAvailableCPUs;
        hostname = options.hostName;
        version = options.version;
        version_tag = options.versionTag;
        build_date = options.buildDate;

        experiment_name = "";
        experiment_id = options.current_experiment_id;
        normalize = options.normalize;
        shuffle = options.shuffleData;
        repeat_no = options.currentRepetition;
        fold_no = options.currentFold;
        train_classes = options.getTrainingSet().getClassDistribution().size();
        test_classes = options.getTestingSet().getClassDistribution().size();
        voting_scheme = options.ensembleVotingScheme;

        num_trees = options.numTrees;
        num_candidates_str = options.numCandidateSplitsAsString;
        num_candidates = options.num_splitters_per_node;
        c_ee = options.ee_splitters_per_node;
        c_boss = options.boss_splitters_per_node;
        c_rise = options.rif_splitters_per_node;
        c_cif = options.cif_splitters_per_node;
        c_randf = options.randf_splitters_per_node;
        c_rotf = options.rotf_splitters_per_node;
        c_tsf = options.tsf_splitters_per_node;
        c_st = options.st_splitters_per_node;
        c_it = options.it_splitters_per_node;
        c_rt = options.rt_splitters_per_node;

        boss_transformations = options.bossNumTransformations;

        mv_lpi = options.lpIndependent;
        mv_lpd = options.lpDepependent;
        mv_dependency = options.dimensionDependencyMethod.toString();
        mv_dims = options.dimensionsToUseAsString;
        if (options.dimensionsToUse != null){
            mv_subset_size = options.dimensionsToUse.length;
        }

        num_nodes = model.getNumNodes();
        num_leaves = model.getNumLeaves();
        num_internal_nodes = num_nodes - num_leaves;
        depth = model.getDepth();
//		weighted_depth = -1; // not implemented tree.get_weighted_depth();

        args = String.join(" ", options.cmdArgs);
        _resultsPrepared = true;

    }

    public static void addColumns(Table table){

        Field[] fields =  TSChiefTreeResult.class.getDeclaredFields();
        Field[] parentFields = TSChiefTreeResult.class.getSuperclass().getFields();
        // reorder the fields with super class fields first
        Field[] allFields = ArrayUtils.addAll(parentFields, fields);

        for (int i = 0; i < allFields.length; i++) {
            Field field = allFields[i];
            field.setAccessible(true);
            int modifier = field.getModifiers();

            if (Modifier.isTransient(modifier)){
                continue;
            } else if (Modifier.isPrivate(modifier)){
                continue;
            } else if (field.getName().startsWith("_")){
                continue;
            }
//			else if (field.isAnnotationPresent(exclude.class))

            String columnName;
            columnName = field.getName();

            Class<?> fieldClass = field.getType();

            if (fieldClass.equals(Boolean.class) || fieldClass.equals(boolean.class)){
                table.addColumns(BooleanColumn.create(columnName));
            }else if (fieldClass.equals(Integer.class) || fieldClass.equals(int.class)){
                table.addColumns(IntColumn.create(columnName));
            }else if (fieldClass.equals(Long.class) || fieldClass.equals(long.class)){
                table.addColumns(LongColumn.create(columnName));
            }else if (fieldClass.equals(Double.class) || fieldClass.equals(double.class)){
                table.addColumns(DoubleColumn.create(columnName));
            }else{
                table.addColumns(StringColumn.create(columnName));
            }
        }

    }

    public void addRow(Table table) {
        int numColumns = table.columnCount();
        int columnsAdded = 0;

        try{
            for (int i = 0; i < numColumns; i++) {
                Column<?> column = table.column(i);

                // no support for annotation based columns
                Field field = this.getClass().getField(column.name());
                Class<?> fieldClass = field.getType();

                if (fieldClass.equals(Boolean.class) || fieldClass.equals(boolean.class)){
                    boolean value = field.getBoolean(this);
                    ((BooleanColumn) column).append(value);
                }else if (fieldClass.equals(Integer.class) || fieldClass.equals(int.class)){
                    int value = field.getInt(this);
                    ((IntColumn) column).append(value);
                }else if (fieldClass.equals(Long.class) || fieldClass.equals(long.class)){
                    long value = field.getLong(this);
                    ((LongColumn) column).append(value);
                }else if (fieldClass.equals(Double.class) || fieldClass.equals(double.class)){
                    double value = field.getDouble(this);
                    ((DoubleColumn) column).append(value);
                }else{
                    Object value = field.get(this);
                    if (value != null){
                        String str = value.toString();
                        ((StringColumn) column).append(str);
                    }else{
                        ((StringColumn) column).append("");
//						throw new RuntimeException("Value for " + column.name() + " is null ");
                    }
                }
                columnsAdded++;
            }
        }catch (NoSuchFieldException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

}
