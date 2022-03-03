package dotnet54.tscore.data;

import dotnet54.util.Util;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class ClassDistribution {
	private TIntIntMap distribution;
	
	public ClassDistribution(boolean deferredInitialization) {
		//risky but allowed if setting map using _set_internal_map
		if (!deferredInitialization) {
			distribution = new TIntIntHashMap();
		}
	}
	
	public ClassDistribution() {
		distribution = new TIntIntHashMap();
	}
	
	public ClassDistribution(int size) {
		distribution = new TIntIntHashMap(size);
	}
	
	public int size() {
		return distribution.size();
	}
	
	public void clear() {
		distribution.clear();
	}
	
	public int put(int v, int k) {
		return distribution.put(k, k);
	}
	
	public int inc(int k) {
//		return distribution.increment(k);
		return distribution.adjustOrPutValue(k, 1, 1);
	}
	
	public int dec(int k) {
		return distribution.adjustOrPutValue(k, -1, 0);
	}
	
	public int get(int k) {
		return distribution.get(k);
	}
	
	public int[] keys() {
		return distribution.keys();
	}
	
	public int[] values() {
		return distribution.values();
	}
	
	public int sum() {
		int sum = 0;
		int[] val = distribution.values();
		for (int i = 0; i < val.length; i++) {
			sum += val[i];
		}
		return sum;
	}
	
	public TIntIntMap _get_internal_map() {
		return distribution;
	}
	
	public void _set_internal_map(TIntIntMap map) {
		this.distribution = map;
	}
	
	public double gini() {
		return Util.gini(distribution);
	}
	
	public double gini(int total) {
		return Util.gini(distribution, total);
	}

	public int[][] toArray(){
		return ClassDistribution.mapToArray(this.distribution);
	}

	public static int[][] mapToArray(TIntIntMap distribution){
		int[][] dist = new int[distribution.size()][2];

		int i = 0;
		for (int key : distribution.keys()) {
			dist[i][0] = key;
			dist[i][1] = distribution.get(key);
			i++;
		}

		return dist;
	}

//	public double[][] normalize(){
//		int[][] dist = this.toArray();
//		double[][] prob;
//
//		return null; // TODO
//	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int key: distribution.keys()) {
			sb.append(key + ":" + distribution.get(key) + ","); 
		}
		sb.append("=");
		sb.append(this.gini());
		
		return sb.toString();
	}
	
}
