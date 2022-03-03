package dotnet54.util;

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;

import dotnet54.applications.tschief.TSChiefApp;
import dotnet54.tscore.TSCore;
import dotnet54.tscore.data.Dataset;
import org.apache.commons.lang3.StringUtils;
import gnu.trove.map.TIntObjectMap;

public class PrintUtilities {

	public static void abort(Exception e) {
		System.err.println("[ERROR] ------------------------------------------------------------------------");
//        System.out.println("\nFatal Error::" + e.getMessage() + "\n\n");
        System.err.println("\nException:: " + e.getMessage() + "\n");
		System.err.println("-----------------------------------------------------------------------------");
        e.printStackTrace();

		Util.logger.info("Terminating Logger");
		System.exit(-1);
	}
	
	public static void printMemoryUsage() {
		PrintUtilities.printMemoryUsage(false);
	}	
	
	public static void printMemoryUsage(boolean minimal) {
		long avail_mem, free_mem, used_mem;
		avail_mem = TSChiefApp.tsChiefOptions.runtime.totalMemory() / TSChiefApp.tsChiefOptions.ONE_MB;
		free_mem = TSChiefApp.tsChiefOptions.runtime.freeMemory() / TSChiefApp.tsChiefOptions.ONE_MB;
		used_mem = avail_mem - free_mem;
		if (minimal) {
			System.out.print("(" + used_mem + "/" + avail_mem + "MB) ");
		}else {
			System.out.println("Using: " + used_mem + " MB, Free: " + free_mem 
					+ " MB, Allocated Pool: " + avail_mem+ " MB, Max Available: " 
					+ TSChiefApp.tsChiefOptions.runtime.maxMemory()/ TSChiefApp.tsChiefOptions.ONE_MB + " MB");
		}

	}

	public static void printConfiguration() {
		System.out.println("Repeats: " + TSChiefApp.tsChiefOptions.numRepeats + " , Trees: " + TSChiefApp.tsChiefOptions.numTrees + " , Shuffle Data: " + TSChiefApp.tsChiefOptions.shuffleData + ", JVM WarmUp: " + TSChiefApp.tsChiefOptions.warmup_java);
		System.out.println("OutputDir: " + TSChiefApp.tsChiefOptions.currentOutputPath + ", Export: " + TSChiefApp.tsChiefOptions.exportFiles + ", Verbosity: " + TSChiefApp.tsChiefOptions.verbosity);

		//splitter settings
		String prefix;
		System.out.println("Enabled Splitters: " + StringUtils.join(TSChiefApp.tsChiefOptions.enabledSplitters, ","));

		//TODO if using probabilities to choose splitters print them here


		System.out.println("Candidate Splits Per Node(s): " + TSChiefApp.tsChiefOptions.num_splitters_per_node + " ("
				+ "ee:" + TSChiefApp.tsChiefOptions.ee_splitters_per_node
				+ ",randf:" + TSChiefApp.tsChiefOptions.randf_splitters_per_node
				+ ",rotf:" + TSChiefApp.tsChiefOptions.rotf_splitters_per_node
				+ ",st:" + TSChiefApp.tsChiefOptions.st_splitters_per_node
				+ ",boss:" + TSChiefApp.tsChiefOptions.boss_splitters_per_node
				+ ",tsf:" + TSChiefApp.tsChiefOptions.tsf_splitters_per_node
				+ ",rise:" + TSChiefApp.tsChiefOptions.rif_splitters_per_node
				+ ",it:" + TSChiefApp.tsChiefOptions.it_splitters_per_node
				+ ",rt:" + TSChiefApp.tsChiefOptions.rt_splitters_per_node
				+ ")");

		System.out.println("---------------------------------------------------------------------------------------------------------------------");
	}
	
	public static void printDatasetInfo() {
		System.out.println("Dataset: " + TSChiefApp.tsChiefOptions.getDatasetName()
		+ ", Training Data (size x length, classes): " + TSChiefApp.tsChiefOptions.getTrainingSet().size() + "x" + TSChiefApp.tsChiefOptions.getTrainingSet().length()
			+ ", " + TSChiefApp.tsChiefOptions.getTrainingSet().getNumClasses()
		+ " , Testing Data: " + TSChiefApp.tsChiefOptions.getTestingSet().size() + "x" + TSChiefApp.tsChiefOptions.getTestingSet().length()
			+ ", " + TSChiefApp.tsChiefOptions.getTestingSet().getNumClasses()
			);
	}
	
	
	public static String print_split(TIntObjectMap<Dataset> splits) {
		StringBuilder sb = new StringBuilder();
		DecimalFormat df = new DecimalFormat(TSCore.DECIMAL_PRINT_FORMAT);

		for (int key : splits.keys()) {
			sb.append(key + "=" +  splits.get(key).getClassDistribution().toString() + "=" + df.format(splits.get(key).gini())  + ", ");
			
		}
		
		sb.append("wgini = " + df.format(Util.weighted_gini(splits)));

		return sb.toString();
	}

	public static String sprintf(String format, Object... strings){
		StringBuilder sb = new StringBuilder();
		String out;
		try (Formatter ft = new Formatter(sb, Locale.UK)) {
			ft.format(format, strings);
			out = ft.toString();
		}
		return out;
	}

}
