package dotnet54.transformers;


import dotnet54.tscore.data.Dataset;

public interface TransformContainer {

	public void fit(Dataset train) throws Exception;
	public Dataset transform(Dataset train) throws Exception;
//	public Dataset transform(TSDataset train, ParameterBag params);

}
