package dotnet54.tscore.exceptions;

public class MultivariateDataNotSupportedException extends NotSupportedException{

    public MultivariateDataNotSupportedException(){
        super("This class or method does not support using Multivariate time series");
    }
}
