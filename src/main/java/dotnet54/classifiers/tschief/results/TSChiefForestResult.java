package dotnet54.classifiers.tschief.results;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.TSCheifForest;
import dotnet54.classifiers.ClassifierResult;
import dotnet54.classifiers.tschief.TSCheifTree;
import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.tscore.data.Dataset;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.ArrayUtils;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class TSChiefForestResult extends ClassifierResult {

    protected transient TSCheifForest model;
    protected transient TSChiefOptions options;
    public transient Table treesTable;
    public transient Table forestTable;
    public transient Table predTable;

//    @JsonAdapter(JsonHelper.ObjectTypeAdapter.class)
    protected TSChiefTreeResult _baseModelResults[];

    public boolean _resultsPrepared = false;


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

    public double max_mem_usage;

    public int num_trees;
    public String num_candidates_str;
    public String voting_scheme;

    // mean across the trees
    public double num_nodes;
    public double num_internal_nodes;
    public double num_leaves;
    public double depth; // max height
    public double depth_std;
    public double balance_factor;   // height(longest branch) - height(shortest branch)
    public double weighted_balance_factor; // balance factor with weight as node size

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

    // mean
    public double ee_count;
    public double boss_count;
    public double rif_count;
    public double cif_count;
    public double randf_count;
    public double rotf_count;
    public double st_count;
    public double tsf_count;
    public double it_count;
    public double rt_count;

    public double ee_win;
    public double boss_win;
    public double rif_win;
    public double cif_win;
    public double randf_win;
    public double rotf_win;
    public double st_win;
    public double tsf_win;
    public double it_win;
    public double rt_win;

    public int mv_lpi;
    public int mv_lpd;
    public String mv_dependency;
    public String mv_dim_selection;
    public int dim_dep_count;
    public int dim_indep_count;
    public int dim_dep_win;
    public int dim_indep_win;

    public int boss_transformations;

    public double euc_count;
    public double dtwf_count;
    public double dtw_count;
    public double ddtwf_count;
    public double ddtw_count;
    public double wdtw_count;
    public double wddtw_count;
    public double lcss_count;
    public double twe_count;
    public double erp_count;
    public double msm_count;

    public double euc_win;
    public double dtwf_win;
    public double dtw_win;
    public double ddtwf_win;
    public double ddtw_win;
    public double wdtw_win;
    public double wddtw_win;
    public double lcss_win;
    public double twe_win;
    public double erp_win;
    public double msm_win;

    public double ee_time;
    public double boss_time;
    public double rif_time;
    public double cif_time;
    public double randf_time;
    public double rotf_time;
    public double st_time;
    public double tsf_time;
    public double it_time;
    public double rt_time;

    public double euc_time;
    public double dtwf_time;
    public double dtw_time;
    public double ddtwf_time;
    public double ddtw_time;
    public double wdtw_time;
    public double wddtw_time;
    public double lcss_time;
    public double twe_time;
    public double erp_time;
    public double msm_time;

    public double rif_acf_count;
    public double rif_pacf_count;
    public double rif_arma_count;
    public double rif_ps_count;
    public double rif_dft_count;

    public double rif_acf_win;
    public double rif_pacf_win;
    public double rif_arma_win;
    public double rif_ps_win;
    public double rif_dft_win;

    public double rif_acf_time;
    public double rif_pacf_time;
    public double rif_arma_time;
    public double rif_ps_time;
    public double rif_dft_time;

//    public long ee_splitter_train_time;
//    public long boss_splitter_train_time;
//    public long rise_splitter_train_time;
//    public long st_splitter_train_time;
//    public long it_splitter_train_time;
//    public long rt_splitter_train_time;
//    public long randf_splitter_train_time;
//    public long cif_splitter_train_time;

//    public long data_fetch_time;
//    public long boss_data_fetch_time;
//    public long rise_data_fetch_time;
//    public long st_data_fetch_time;
//    public long it_data_fetch_time;
//    public long rt_data_fetch_time;
//    public long cif_data_fetch_time;

//    public long split_evaluator_train_time;

    public double tree_accuracy_mean;
    public double tree_accuracy_std;
    public double tree_total_time_s_mean;
    public double tree_total_time_s_std;

    public String args;

    //splitters
    public double boss_transform_time;
    public double st_transform_time;
    public double it_transform_time;
    public double rt_transform_time;

    //dev TODO DEV
    public transient List<String> allShapelets;
    public transient List<String> winShapelets;

    public TSChiefForestResult(TSCheifForest forest){
        super(forest);
        this.model = forest; // override
        this.options = (TSChiefOptions) forest.getOptions();
        _baseModelResults = new TSChiefTreeResult[options.numTrees];
    }

    public TSChiefTreeResult[] getBaseModelResults() {
        return _baseModelResults;
    }

    public void setBaseModelResults(TSChiefTreeResult[] _baseModelResults) {
        this._baseModelResults = _baseModelResults;
    }

    public void addBaseModelResult(int index, TSChiefTreeResult result) {
        this._baseModelResults[index] = result;
    }

    private void prepareForestData(){

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
        mv_dim_selection = options.dimensionsToUseAsString;

        num_nodes = treesTable.intColumn("num_nodes").mean();
        num_internal_nodes = treesTable.intColumn("num_internal_nodes").mean();
        num_leaves = treesTable.intColumn("num_leaves").mean();
        depth = treesTable.intColumn("depth").mean();
        depth_std = treesTable.intColumn("depth").standardDeviation();
//        balance_factor = treesTable.intColumn("balance_factor").mean();
//        weighted_balance_factor = treesTable.intColumn("weighted_balance_factor").mean();


        tree_accuracy_mean = treesTable.doubleColumn("accuracy").mean();
        tree_accuracy_std = treesTable.doubleColumn("accuracy").standardDeviation();
        tree_total_time_s_mean = treesTable.doubleColumn("total_time_s").mean();
        tree_total_time_s_std = treesTable.doubleColumn("total_time_s").standardDeviation();

        max_mem_usage = treesTable.longColumn("alloc_memory").max();
        max_mem_usage = Math.max(max_mem_usage, treesTable.longColumn("alloc_memory_after_init").max());
        max_mem_usage = Math.max(max_mem_usage, treesTable.longColumn("alloc_memory_after_train").max());
        max_mem_usage = Math.max(max_mem_usage, treesTable.longColumn("alloc_memory_after_test").max());

        appendCounterColumns();

        args = String.join(" ", options.cmdArgs);

    }

    private void appendCounterColumns(){
        try{
            String[] columns = ("ee_count,boss_count,rif_count,cif_count,randf_count,rotf_count,st_count,tsf_count,it_count," +
                    "rt_count,ee_win,boss_win,rif_win,cif_win,randf_win,rotf_win,st_win,tsf_win,it_win,rt_win,euc_count,dtwf_count," +
                    "dtw_count,ddtwf_count,ddtw_count,wdtw_count,wddtw_count,lcss_count,twe_count,erp_count,msm_count,euc_win," +
                    "dtwf_win,dtw_win,ddtwf_win,ddtw_win,wdtw_win,wddtw_win,lcss_win,twe_win,erp_win,msm_win,ee_time,boss_time," +
                    "rif_time,cif_time,randf_time,rotf_time,st_time,tsf_time,it_time,rt_time,euc_time,dtwf_time,dtw_time," +
                    "ddtwf_time,ddtw_time,wdtw_time,wddtw_time,lcss_time,twe_time,erp_time,msm_time,mv_lpi,mv_lpd,mv_dependency," +
                    "rif_acf_count,rif_pacf_count,rif_arma_count,rif_ps_count," +
                    "rif_dft_count,rif_acf_win,rif_pacf_win,rif_arma_win,rif_ps_win,rif_dft_win," +
                    "rif_acf_time,rif_pacf_time,rif_arma_time,rif_ps_time,rif_dft_time").split(",");

            for (int i = 0; i < columns.length; i++) {
                String colName = columns[i];
                Column column = treesTable.column(colName);

                Field field = this.getClass().getField(column.name());
                Class<?> fieldClass = field.getType();

                if (column instanceof DoubleColumn && fieldClass.equals(double.class)){
                    field.setDouble(this, treesTable.doubleColumn(colName).sum());
                }else if (column instanceof IntColumn && fieldClass.equals(double.class)){
                    field.setDouble(this, treesTable.intColumn(colName).sum());
                }else if (column instanceof LongColumn && fieldClass.equals(double.class)){
                    field.setDouble(this, treesTable.longColumn(colName).sum());
                }else{
                    //
                }
            }


        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeInit() {
        super.beforeInit();

        index = model.getForestID();
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
    public void afterTest() {
        super.afterTest();

        prepareTreesTable();
        preparePredTable();

        // forest results
        forestTable = Table.create("forest");
        TSChiefForestResult.addColumns(forestTable);
        prepareForestData();
        addRow(forestTable);

        _resultsPrepared = true;
    }

    public static void addColumns(Table table){

        Field[] fields =  TSChiefForestResult.class.getDeclaredFields();
        Field[] parentFields = TSChiefForestResult.class.getSuperclass().getFields();
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

    private void prepareTreesTable(){
        // trees results
        treesTable = Table.create("trees");
        TSChiefTreeResult.addColumns(treesTable);
        // add data
        TSCheifTree[] trees = model.getTrees();
        TSChiefTreeResult treeResult;

        // pass 1 - populate basic data
        for (int i = 0; i < trees.length; i++) {
            treeResult = trees[i].result;
            treeResult.addRow(treesTable);
        }

        DoubleColumn nc = (DoubleColumn) treesTable.column("accuracy_mean");
        nc.set((x) -> { return true; }, treesTable.doubleColumn("accuracy").mean());
        nc = (DoubleColumn) treesTable.column("accuracy_std");
        nc.set((x) -> { return true; }, treesTable.doubleColumn("accuracy").standardDeviation());
    }

    private void preparePredTable(){
        // pred results
        predTable = Table.create("pred");

        predTable.addColumns(IntColumn.create("index"));
        predTable.addColumns(IntColumn.create("actual_label"));
        predTable.addColumns(IntColumn.create("predicted_label"));
        predTable.addColumns(IntColumn.create("correct"));
        for (int i = 0; i < _baseModelResults.length; i++) {
            predTable.addColumns(IntColumn.create("tree_" + i));
        }

        for (int i = 0; i < predictedLabels.length; i++) {
            ((IntColumn) predTable.column("index")).append(i);
            ((IntColumn) predTable.column("actual_label")).append(options.getTestingSet().getClass(i));
            ((IntColumn) predTable.column("predicted_label")).append(predictedLabels[i]);
            int correct = options.getTestingSet().getClass(i) == predictedLabels[i] ? 1: 0 ;
            ((IntColumn) predTable.column("correct")).append(correct);

            TSCheifTree[] trees = model.getTrees();
            for (int j = 0; j < trees.length; j++) {
                int[] basePredictedLabels = trees[j].getTestResults().getPredictedLabels();
                ((IntColumn) predTable.column("tree_" + j)).append(basePredictedLabels[i]);
            }

        }
    }

    public void exportForestCsv() throws IOException {

        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".forest.csv";

        // export
        File fileObj = new File(fileName);
        File pf = fileObj.getParentFile();
        if (pf != null) {
            fileObj.getParentFile().mkdirs();
        }

        forestTable.write().csv(fileName);

    }

    public void exportTreeCsv() throws IOException {

        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".tree.csv";

        File fileObj = new File(fileName);
        File pf = fileObj.getParentFile();
        if (pf != null) {
            fileObj.getParentFile().mkdirs();
        }

        treesTable.write().csv(fileName);

    }

    public void exportPredCsv(Dataset testData) throws IOException {

        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".pred.csv";

        File fileObj = new File(fileName);
        File pf = fileObj.getParentFile();
        if (pf != null) {
            fileObj.getParentFile().mkdirs();
        }

        predTable.write().csv(fileName);
    }

    public String exportConfigJson() throws Exception {

        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".config.json";

        File fileObj = new File(fileName);
        File pf = fileObj.getParentFile();
        if (pf != null) {
            fileObj.getParentFile().mkdirs();
        }
        fileObj.createNewFile();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false))){

            Gson gson;
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeSpecialFloatingPointValues();
            gsonBuilder.serializeNulls();

            gsonBuilder.setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    if (fieldAttributes.getName().equals("_baseModelResults")){
                        return true;
                    }
                    return false;
                }
                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            });

//            gsonBuilder.registerTypeAdapter(TSChiefTreeResult.class, new JsonHelper.ObjectTypeAdapter());

            Type intObjectMapType = new TypeToken<TIntObjectMap<TSChiefNode>>(){}.getType();
            gsonBuilder.registerTypeAdapter(intObjectMapType, new JsonHelper.TIntObjectMapSerializer());

            Type intintMapType = new TypeToken<TIntIntMap>(){}.getType();
            gsonBuilder.registerTypeAdapter(intintMapType, new JsonHelper.TIntIntMapSerializer());
            gson = gsonBuilder.create();

//			SerializableResultSet object = new SerializableResultSet(this.forests);

            StringBuilder buffer = new StringBuilder();
            buffer.append('{');

            buffer.append("\"options\": ");
            buffer.append(gson.toJson(this.options));
            buffer.append(',');

            buffer.append("\"forest\" : ");
            buffer.append(gson.toJson(this));

            buffer.append('}');

            bw.write(buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
//			bw.close();
        }

        return fileName;
    }

    public String exportForestJson() throws Exception {

        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".forest.json";

        File fileObj = new File(fileName);
        File pf = fileObj.getParentFile();
        if (pf != null) {
            fileObj.getParentFile().mkdirs();
        }

//        fileObj.createNewFile();

        Gson gson;

        GsonBuilder gsonBuilder = new GsonBuilder()
                .setVersion(1.0)
                .serializeNulls()
//                .serializeSpecialFloatingPointValues()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
//            gsonBuilder.setPrettyPrinting();

        gsonBuilder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                if (fieldAttributes.getName().equals("_baseModelResults")){
                    return true;
                }
                return false;
            }
            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        });


//            gsonBuilder.registerTypeAdapter(Class.class, new JsonHelper.ClassTypeAdapter());
//            gsonBuilder.registerTypeAdapter(TSChiefOptions.class, new JsonHelper.ObjectTypeAdapter());
        gsonBuilder.registerTypeAdapter(TIntObjectMap.class, new JsonHelper.ObjectTypeAdapter());
        gsonBuilder.registerTypeAdapter(TIntObjectHashMap.class, new JsonHelper.ObjectTypeAdapter());

        Type intObjectMapType = new TypeToken<TIntObjectMap<TSChiefNode>>(){}.getType();
        gsonBuilder.registerTypeAdapter(intObjectMapType, new JsonHelper.TIntObjectMapSerializer());

        Type intintMapType = new TypeToken<TIntIntMap>(){}.getType();
        gsonBuilder.registerTypeAdapter(intintMapType, new JsonHelper.TIntIntMapSerializer());

        gsonBuilder.registerTypeAdapter(double.class, JsonHelper.doubleAdapterWithSpecialCharSupport());
        gsonBuilder.registerTypeAdapter(Double.class, JsonHelper.doubleAdapterWithSpecialCharSupport());

        gson = gsonBuilder.create();

        StringBuilder buffer = new StringBuilder();
        buffer.append(gson.toJson(this.model));


        if (options.exportFiles.contains("z")){
            // output a compressed file
            compressStringToGzip(buffer.toString(), new File(fileName + ".gz"));
        }else{
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false))){
                bw.write(buffer.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
//			bw.close();
            }
        }

        return fileName;
    }

    public static void compressStringToGzip(String data, File target) throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(
                new FileOutputStream(target))) {
            gos.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String exportSplitsJson() throws Exception {

        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".splits.json";

        File fileObj = new File(fileName);
        File pf = fileObj.getParentFile();
        if (pf != null) {
            fileObj.getParentFile().mkdirs();
        }
        fileObj.createNewFile();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false))){

            Gson gson;
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeSpecialFloatingPointValues();
            gsonBuilder.serializeNulls();

//            gsonBuilder.registerTypeAdapter(TSChiefTreeResult.class, new JsonHelper.ObjectTypeAdapter());

            Type intObjectMapType = new TypeToken<TIntObjectMap<TSChiefNode>>(){}.getType();
            gsonBuilder.registerTypeAdapter(intObjectMapType, new JsonHelper.TIntObjectMapSerializer());

            Type intintMapType = new TypeToken<TIntIntMap>(){}.getType();
            gsonBuilder.registerTypeAdapter(intintMapType, new JsonHelper.TIntIntMapSerializer());
            gson = gsonBuilder.create();

//			SerializableResultSet object = new SerializableResultSet(this.forests);

            StringBuilder buffer = new StringBuilder();
            buffer.append('{');

            //write appcontext
//            buffer.append("\"settings\": ");
//            buffer.append(gson.toJson(TSChiefArgs.class));
//            buffer.append(',');

            buffer.append("\"forest\" : ");
            buffer.append(gson.toJson(this));

            buffer.append('}');

            bw.write(buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
//			bw.close();
        }

        return fileName;
    }

    public void exportTreeGraphs() throws IOException {
        TSCheifTree[] trees = model.getTrees();
        for (int i = 0; i < trees.length; i++) {
            GraphVizExporter.exportTree(trees[i]);
        }
    }

    public void exportAGSampleSizes() throws IOException {
        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".agSizes.debug.csv";

        for (TSChiefTreeResult baseModelResult : _baseModelResults) {
            baseModelResult._agSampleSizes.write(fileName, true);
        }
    }

    public void exportDims() throws IOException {
        String fileName =  options.currentOutputPath + "/" +
                options.datasetName + "/" +
                options.datasetName + ".dims.debug.csv";

        for (TSChiefTreeResult baseModelResult : _baseModelResults) {
            baseModelResult._dimSizes.write(fileName, true);
        }
    }

}
