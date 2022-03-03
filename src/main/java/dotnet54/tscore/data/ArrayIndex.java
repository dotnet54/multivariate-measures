package dotnet54.tscore.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayIndex implements DatasetIndex {

    /**
     * This is the full dataset we want to index, all indices should be indexed with respect to this dataset
     * an index can be built without attaching a dataset, in such cases user can manually manage dataset
     *
     */
    private Dataset dataset;

    /**
     * This is the index either equal to or less than the size of the dataset,
     * making it possible to index subsets from the dataset
     *
     * size of index is <= dataset.size()
     */
    public int[] indices;

    public ArrayIndex(Dataset dataset){
        this.dataset =  dataset;
        this.indices = new int[dataset.size()];
    }

    public ArrayIndex(Dataset dataset, boolean init){
        this.dataset =  dataset;
        this.indices = new int[dataset.size()];
        if (init){
            for (int i = 0; i < indices.length; i++) {
                indices[i] = i; // just add everything
            }
        }
    }

    public ArrayIndex(int size){
        this.dataset =  null;
        this.indices = new int[size];
    }

    public ArrayIndex(int[] indices){
        this.dataset =  null;
        this.indices = indices;
    }

    public ArrayIndex(Dataset dataset, int size){
        this.dataset =  dataset;
        this.indices = new int[size];
    }

    public ArrayIndex(Dataset dataset, int[] indices){
        this.dataset =  dataset;
        this.indices = indices;
    }

    public ArrayIndex(Dataset dataset, Dataset subset){
        this.dataset =  dataset;
        this.syncByReplacing(subset);
    }

    @Override
    public int size() {
        return indices.length;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    @Override
    public int getIndex(int i) {
        return indices[i];
    }

    @Override
    public void setIndex(int i, int index) {
        this.indices[i] = index;
    }

    @Override
    public TimeSeries getSeries(int i) {
        if (dataset != null){
            return dataset.getSeries(indices[i]);
        }else{
            throw new RuntimeException("Attach a dataset to this index before calling this method");
        }
    }

    public boolean hasDataset(){
        return dataset != null;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void addAll(Dataset dataset){
        this.indices = new int[dataset.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i; // just add everything
        }
    }

    public void sampleRandomly(int sampleSize){
        if (sampleSize > dataset.size()){
            sampleSize = dataset.size();
        }
        // NOTE: this is a slow way to shuffle an array, faster way would be to use Yates-Fisher algorithm
        // TODO refactor to use Yates-Fisher implementation in util class
        List<Integer> sample = new ArrayList<Integer>(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            sample.add(i);
        }
        // TODO support seeding, test Random vs ThreadLocalRandom -- called in tree classes when bagging, which may have its own thread
        Collections.shuffle(sample);
        this.indices =  sample.stream().mapToInt(i->i).toArray();
    }

    public void syncByReplacing(Dataset targetDataset){
        if (hasDataset()){
            int datasetSize = this.dataset.size();
            int targetSize = targetDataset.size();

            if (indices == null || targetSize != indices.length){
                this.indices = new int[targetSize];
            }

            for (int i = 0; i < datasetSize; i++) {
                for (int j = 0; j < targetSize; j++) {
                    //TODO implement equals method in TimeSeries instead of using ==
                    if (this.dataset.getSeries(i) == targetDataset.getSeries(j)) {
                        indices[j] = i;
                    }
                }
            }
        }else{
            throw new RuntimeException("Attach a dataset to this index before calling this method");
        }
    }

    public void syncByAppending(Dataset targetDataset){
        //TODO
    }

}
