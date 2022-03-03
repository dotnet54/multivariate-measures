package dotnet54.classifiers;

public interface Ensemble {

    public int getSize();

    public Classifier[] getModels();

    public Classifier getModel(int i);

}
