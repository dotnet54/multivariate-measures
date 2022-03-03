package dotnet54.classifiers.tschief.splitters;

import dotnet54.applications.tschief.TSChiefOptions;

public class SplitCriterion {
	
	public int attribute = -1; //-1 = unknown attribute, may store index of an item from a list (eg. index of a shapelet), or index of column from 2d array
	public double threshold;
	public double wgini;
	public TSChiefOptions.ThresholdSelectionMethod selection_method;
	
	public SplitCriterion(double t, double wg, TSChiefOptions.ThresholdSelectionMethod selection_method) {
		this.threshold = t;
		this.wgini = wg;
		this.selection_method = selection_method;
	}
	
	public String toString() {
		return this.attribute + ":" + this.threshold + " = " + this.wgini + " : " + selection_method;
	}
}
