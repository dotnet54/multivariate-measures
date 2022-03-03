package dotnet54.tscore.data;

import java.util.List;
import java.util.Random;

import dotnet54.tscore.Options;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;

/**
 * 
 * @author shifaz
 * @email ahmed.shifaz@monash.edu
 *
 */

public interface Dataset extends Iterable<TimeSeries>{

	public String getName();

	public void setName(String name);

	public String getTags();

	public void setTags(String tags);

	public boolean isMultivariate();

	public boolean isVariableLength();

	public boolean isNormalized();

	public boolean hasMissingValues();

	public boolean hasTimestamps();

	public int size();
	
	public int length();

	public int minLength();

	public int maxLength();

	public int dimensions();

	public TimeSeries add(TimeSeries series);
	
	public void remove(int i);

	public void clear();

	public TimeSeries getSeries(int i);

	public double[][] getSeriesData(int i);

	public Integer getClass(int i);

	public TIntIntMap getClassDistribution();

	public int getNumClasses();
	
	public int getClassSize(Integer classLabel);

	public int[] getUniqueClasses();

	public LabelEncoder getLabelEncoder();

	public TIntObjectMap<Dataset> splitByClass();

	public TIntObjectMap<List<Integer>> splitByClassIdx();

	public double gini();

//	TODO TEMPORARY FIXES

	public TimeSeries[] toArray();

	public List<double[][]> _data_as_list();
	
	public List<Integer> _class_as_list();
	
	public double[][] _data_as_array();
	
	public int[] _class_as_array();
	
	public void shuffle(Random rand);
	
	public void shuffle(long seed);

	public void sort(int attribute);
	
	public Dataset shallowClone();
	
	public Dataset deepClone();

	public double getMean();

	public double getStdv();

	public double[] getMeanPerDimension();

	public double[] getStdvPerDimension();

	public double[] getMinPerDimension();

	public double[] getMaxPerDimension();

	public double[][] getMeanPerSeriesPerDimension();

	public double[][] getStdvPerSeriesPerDimension();

	public double[][] getMinPerSeriesPerDimension();

	public double[][] getMaxPerSeriesPerDimension();

	public void zNormalize(boolean perSeries);

	public void meanNormalize(boolean perSeries);

	public void featureScale(boolean perSeries);

}
