package dotnet54.classifiers.tschief.splitters.ee;

import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.results.NodeSplitterResult;
import dotnet54.transformers.DerivativeTransformer;
import dotnet54.tscore.data.Dataset;
import dotnet54.tscore.data.DatasetIndex;
import dotnet54.tscore.data.MTSDataset;
import dotnet54.tscore.data.TimeSeries;
import dotnet54.measures.multivariate.MultivariateMeasure;
import dotnet54.tscore.exceptions.NotSupportedException;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import dotnet54.classifiers.tschief.splitters.NodeSplitter;
import dotnet54.measures.Measure;
import dotnet54.util.Sampler;
import dotnet54.util.Util;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author shifaz
 * @email ahmed.shifaz@monash.edu
 *
 */

public class MultivariateElasticDistanceSplitter extends NodeSplitter {

	protected Random rand;
	protected MultivariateMeasure measure;
	protected TIntObjectMap<double[][]> exemplars;
	protected Measure measureName;
	protected boolean dimensionDependency;
	protected int[] dimensionsToUse;

	//just to reuse this  structure -- used during testing
	private List<Integer> closestNodes = new ArrayList<Integer>();

	public MultivariateElasticDistanceSplitter(TSChiefNode node){
		super(node);
		splitterType = SplitterType.ElasticDistanceSplitter;
		splitterClass = getClassName();

		if (node!=null){
			this.node.result.ee_count++;
			this.rand = options.getRand();
		}else{
			rand = new Random();
		}
	}
	
	public NodeSplitterResult fit(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();
		TIntObjectMap<Dataset> dataPerClass = nodeTrainData.splitByClass();	//TODO can be done once at node

		int seriesLength = nodeTrainData.length();
		int numDimensions = nodeTrainData.dimensions();

		if (options.dimensionDependencyMethod == TSChiefOptions.DimensionDependencyMethod.independent){
			dimensionDependency = false;
		}else if (options.dimensionDependencyMethod == TSChiefOptions.DimensionDependencyMethod.dependent){
			dimensionDependency = false;
		}else if (options.dimensionDependencyMethod == TSChiefOptions.DimensionDependencyMethod.random){
			dimensionDependency = rand.nextBoolean();
		}else{
			throw new NotSupportedException();
		}

		if (dimensionDependency){
			this.node.result.dim_dep_count++;
		}else{
			this.node.result.dim_indep_count++;
		}

		int subsetSize = 0;
		if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.set){
			// a fixed set of values are already set during the command line parsing in TSChiefOptions::parseDimenstionsToUse
			dimensionsToUse = options.dimensionsToUse;
		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.range){
			// a fixed set of values are already set during the command line parsing in TSChiefOptions::parseDimenstionsToUse
			dimensionsToUse = options.dimensionsToUse;
		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.all){
			// a fixed set of values are already set during the command line parsing in TSChiefOptions::parseDimenstionsToUse
			dimensionsToUse = options.dimensionsToUse;
		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.sqrt){
			// subsetSize = options.maxNumDimensions = (int) ceil(sqrt(numDimensions)) // already set in TSChiefOptions::parseDimenstionsToUse

			// sampling without replacement
			dimensionsToUse = Sampler.sampleNRandomIntsFromRange(0, numDimensions, options.maxNumDimensions);
		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.uniform){
			// dims ~ U(1, D)  => range is an inclusive interval [1, D]

//			// using ApacheCommon implementation which includes lower and upper bound
//			// ApacheCommon doesn't support the seed set in cmd args
//			UniformIntegerDistribution uniform = new UniformIntegerDistribution(options.minNumDimensions, options.maxNumDimensions);
//			subsetSize = uniform.sample();

			// using Java Random
			subsetSize = Util.getRandomNumberInRange(options.minNumDimensions, options.maxNumDimensions, true);

			// sampling without replacement
			dimensionsToUse = Sampler.sampleNRandomIntsFromRange(0, numDimensions, subsetSize);
		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.beta){
			// if subset size is <= dimThreshold then use uniform distribution
			// this helps to flatten the beta distribution if a small subset is sampled
			// the distribution is modified because beta gives the highest probability to subset size 1

			// ApacheCommon doesn't support the seed set in cmd args
			// TODO inefficient to reinstantiate rng every time. move it up to do it once.
			BetaDistribution betaDistribution = new BetaDistribution(options.betaDistributionAlpha, options.betaDistributionBeta);

			// beta distribution supports the range 0 <= rand <= 1 (when a,b >= 1)
			// ApacheCommon implementation is expected to return the range [0,1] -- checked
			// scale the range to [0, D]
//			double randomValue = betaDistribution.sample() * numDimensions;
			double randomValue = betaDistribution.sample();
			randomValue *= numDimensions;


			// TODO refactor this to do once
			double dimThreshold;
			String[] dimArgs =  options.dimensionsToUseAsString.split(":");
			// dimThreshold can be set using cmd args
			if (dimArgs.length > 3){
				if (dimArgs[3].startsWith("sqrt")){
					dimThreshold = Math.sqrt(numDimensions);
				} else if (dimArgs[3].equals("D")){
					dimThreshold = numDimensions;
				}else {
					dimThreshold = Integer.parseInt(dimArgs[3]);
				}
			}else{
				dimThreshold = numDimensions; // if no min threshold is given, use uniform/beta(1,1)
			}

			// using <= instead of < -- to prevent a higher probability peak at dimThreshold -- sqrt(D)
			if (randomValue <= dimThreshold){
				subsetSize = Util.getRandomNumberInRange(1, (int) Math.ceil(dimThreshold), true);;
			}else{
				subsetSize = (int) Math.ceil(randomValue);
			}

			// assert subsetSize <= numDimensions

			// sampling without replacement
			dimensionsToUse = Sampler.sampleNRandomIntsFromRange(0, numDimensions, subsetSize);
		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.two_uniform){
			final double LOWER_BOUND = 1e-9; // quick way to set the lower bound > 0
			// we need a randomValue of range 0 < rand <= 1 -- i.e ~ U(0,1]

			// Using ApacheCommons which returns [0, 1] by default
			UniformRealDistribution uniformRealDistribution = new UniformRealDistribution(LOWER_BOUND, 1);
			double randomValue = uniformRealDistribution.sample();

			// using Java Random which returns ~U[0, 1)
//			double randomValue = rand.nextDouble() * -- TODO fix bound adjustment

			if (randomValue < 0.5){
				subsetSize = (int) Math.ceil(randomValue * 2 * Math.sqrt(numDimensions));
			}else{
				subsetSize = (int) Math.ceil(Math.sqrt(numDimensions)
								+ (randomValue-0.5) * 2 * (numDimensions - Math.sqrt(numDimensions)));
			}
			/*
			TODO
			1.0 record subset size and look at the distribution
			do a for loop and from 0.01 to 1 in steps of 1/1000

			 */

			// sampling without replacement
			dimensionsToUse = Sampler.sampleNRandomIntsFromRange(0, numDimensions, subsetSize);
		}else{
			throw new NotSupportedException("DimensionSelectionMethod not supported");
		}

		this.node.result._dimSizes.add(dimensionsToUse.length);

//		if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.uniform){
//			subsetSize = Util.getRandomNumberInRange(options.minNumDimensions, options.maxNumDimensions, true);
//			dimensionsToUse = Sampler.sampleNRandomIntsFromRange(0, numDimensions, subsetSize);
//		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.beta){
//			BetaDistribution betaDistribution = new BetaDistribution(options.betaDistributionAlpha, options.betaDistributionBeta);
//			// beta is defined on 0 <= rand <= 1
//			// ApacheCommon is upper bound exclusive //CHECK
//			double rand = betaDistribution.sample();
////			int subsetSize = (int) (rand * numDimensions + 1);
//			subsetSize = (int) (rand * numDimensions);
//			if (subsetSize < options.minNumDimensions){
//				// if subset size is < min then use uniform distribution, this flattens the curve for lower dimensions
//				subsetSize = Util.getRandomNumberInRange(1, options.minNumDimensions, true);;
//			}
//			if (subsetSize > numDimensions){ // double check --  should not happen
//				subsetSize = numDimensions;
//			}
//			dimensionsToUse = Sampler.sampleNRandomIntsFromRange(0, numDimensions, subsetSize);
//		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.two_uniform){
// 			// TODO
// 			// rand ~ U(0,1] --  range 0 < rand <= 1
// 			// rand.nextInt((max - min) + 1) + min  //random number with inclusive upper bound
// 			double rand = this.rand.nextInt((int) ((1.0-1e9) + 1)) + 1e9; // CHECK excludes 0
//			// CHECK
//			if (rand < 0.5){
//				subsetSize = (int) Math.ceil(rand * 2 * Math.sqrt(numDimensions));
//			}else{
//				subsetSize = (int) Math.ceil(Math.sqrt(numDimensions) + (rand-0.5) * 2 * Math.sqrt(numDimensions));
//			}
//
//			dimensionsToUse = Sampler.sampleNRandomIntsFromRange(0, numDimensions, subsetSize);
//		}else if (options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.set ||
//				options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.range ||
//				options.dimensionSelectionMethod == TSChiefOptions.DimensionSelectionMethod.all){
//			dimensionsToUse = options.dimensionsToUse;
//		}else{
//			throw new NotSupportedException("DimensionSelectionMethod not supported");
//		}

		int r = rand.nextInt(options.eeEnabledDistanceMeasures.size());
		measureName = options.eeEnabledDistanceMeasures.get(r);

		switch (measureName){
			case euc:
				this.node.result.euc_count++;
				break;
			case dtwf:
				this.node.result.dtwf_count++;
				break;
			case dtw:
				this.node.result.dtw_count++;
				break;
			case ddtwf:
				this.node.result.ddtwf_count++;
				break;
			case ddtw:
				this.node.result.ddtw_count++;
				break;
			case wdtw:
				this.node.result.wdtw_count++;
				break;
			case wddtw:
				this.node.result.wddtw_count++;
				break;
			case lcss:
				this.node.result.lcss_count++;
				break;
			case msm:
				this.node.result.msm_count++;
				break;
			case erp:
				this.node.result.erp_count++;
				break;
			case twe:
				this.node.result.twe_count++;
				break;
		}

		measure = MultivariateMeasure.createSimilarityMeasure(
				measureName, dimensionDependency, dimensionsToUse);
		measure.setUseSquaredDiff(true);
		measure.setAdjustSquaredDiff(true);

//		if (measure.useDerivativeData){
//			measure.setTrainDataset(AppConfig.trainDataDerivative);   //set both to train and test dataset as during training phase
//			measure.setTestDataset(AppConfig.testDataDerivative);    //set both to train and test dataset as during training phase
//		}

		Dataset trainDataDerivative = null;
		if (measure.useDerivativeData){
			DerivativeTransformer dt = new DerivativeTransformer();
			trainDataDerivative = dt.transform(nodeTrainData, null);
//			measure.setTrainDataset(trainDataDerivative);
//			measure.setTestDataset(trainDataDerivative); //TODO fix
		}

		// params;
		if (measure.useDerivativeData){
			measure.initParamsByID(trainDataDerivative, rand);
			measure.setRandomParamByID(trainDataDerivative, rand);
		}else{
			measure.initParamsByID(nodeTrainData, rand);
			measure.setRandomParamByID(nodeTrainData, rand);
		}

		this.exemplars = new TIntObjectHashMap<double[][]>(nodeTrainData.getNumClasses());

		int branch = 0;
		for (int key : dataPerClass.keys()) {
			r = rand.nextInt(dataPerClass.get(key).size());
			this.exemplars.put(branch, dataPerClass.get(key).getSeries(r).data());
			branch++;
		}

		// time is measured separately for fit and split since they can be called independently
		this.node.tree.result.ee_time += (System.nanoTime() - startTime);
		this.isFitted = true;
		return split(nodeTrainData, trainIndices);
	}

//	@Override
	public NodeSplitterResult  split(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		long startTime = System.nanoTime();

		NodeSplitterResult result = new NodeSplitterResult(this, nodeTrainData.getNumClasses());
		int data_size = nodeTrainData.size();
		int closest_branch;

		for (int j = 0; j < data_size; j++) {
			closest_branch = findNearestExemplar(nodeTrainData.getSeries(j).data());

			if (! result.splits.containsKey(closest_branch)){
				// initial capacity based on class distributions, this may be an over or an underestimate, but better than 0 initial capacity
				result.splits.put(closest_branch, new MTSDataset(nodeTrainData.getClassDistribution().get(closest_branch)));
			}

			result.splits.get(closest_branch).add(nodeTrainData.getSeries(j));
		}

		result.weightedGini = node.weighted_gini(nodeTrainData.size(), result.splits);
		weightedGini = result.weightedGini;
		this.node.weightedGiniPerSplitter.add(new Pair<>(this.getClassName(), weightedGini));
//		this.node.meanWeightedGini.increment(weightedGini);
//		this.node.sdtvWeightedGini.increment(weightedGini);

		// time is measured separately for fit and split since they can be called independently
		this.node.tree.result.ee_time += (System.nanoTime() - startTime);
		return result;
	}

//	@Override
	public int predict(TimeSeries query, Dataset testData, int queryIndex) throws Exception {
		return findNearestExemplar(query.data());
	}

//	@Override
	public NodeSplitterResult  fitByIndices(Dataset nodeTrainData, DatasetIndex trainIndices) throws Exception {
		throw new NotSupportedException();
	}

//	@Override
	public NodeSplitterResult  splitByIndices(Dataset allTrainData, DatasetIndex trainIndices) throws Exception {
		throw new NotSupportedException();
	}

	private int findNearestExemplar(double[][] query) throws Exception{
		closestNodes.clear();
		double dist;
		double min_dist = Double.POSITIVE_INFINITY;

		for (int key : exemplars.keys()) {
			double[][] exemplar = exemplars.get(key);

			if (options.config_skip_distance_when_exemplar_matches_query && exemplar == query) {
				return key;
			}

			dist = measure.distance(query, exemplar, Double.POSITIVE_INFINITY);

			if (dist < min_dist) {
				min_dist = dist;
				closestNodes.clear();
				closestNodes.add(key);
			}else if (dist == min_dist) {
//				if (distance == min_distance) {
//					System.out.println("min distances are same " + distance + ":" + min_distance);
//				}
//				min_dist = dist;
				closestNodes.add(key);
			}
		}

		int r = rand.nextInt(closestNodes.size());
		return closestNodes.get(r);
	}

//	//TODO move this function to dotnet54.knn.fit()
//	public static void initParamIDs(Dataset trainData, int seriesLength) {
//		double stdTrain = trainData.getStdv();
//		double[] stdPerDim = trainData.getStdvPerDimension();
//
//		// note if any param depends on derivative , use derivative datasets, currently its not needed
//		if (args.paramIDs.size() > 0){
//			args.windowSize.clear();
//			IntStream.range(0, MultivarSimMeasure.MAX_PARAMID).forEach(i -> args.windowSize.add(i / 100.0));
//
//			args.gWDTW.clear();
//			IntStream.range(0, MultivarSimMeasure.MAX_PARAMID).forEach(i -> args.gWDTW.add(i / 100.0));
//
//			double[] epsilons = Util.linspaceDbl(stdTrain * 0.2, stdTrain, 10,true);
//			double[] deltas = Util.linspaceDbl(0, 0.25, 10,true);
//			args.eLCSS = Arrays.stream(epsilons).boxed().collect(Collectors.toList());
//			args.wLCSS =  Arrays.stream(deltas).boxed().collect(Collectors.toList());
//
//			// DEV - NOTE  args.eLCSS is used dependent LCSS, and args.epsilonsPerDim is use dby independent LCSS
//			args.epsilonsPerDim = new double[stdPerDim.length][];
//			for (int dim = 0; dim < args.epsilonsPerDim.length; dim++) {
//				args.epsilonsPerDim[dim] = Util.linspaceDbl(stdPerDim[dim] * 0.2, stdPerDim[dim], 10,true);
//			}
//
//			double[] erpG = Util.linspaceDbl(stdTrain * 0.2, stdTrain, 10,true);
//			double[] erpWindows = Util.linspaceDbl(0, 0.25, 10,true);
//			args.gERP = Arrays.stream(erpG).boxed().collect(Collectors.toList());
//			args.wERP = Arrays.stream(erpWindows).boxed().collect(Collectors.toList());
//
//			//DEV
//			args.erpGPerDim = new double[stdPerDim.length][];
//			for (int dim = 0; dim < args.epsilonsPerDim.length; dim++) {
//				args.erpGPerDim[dim] = Util.linspaceDbl(stdPerDim[dim] * 0.2, stdPerDim[dim], 10,true);
//			}
//
//			args.cMSM.clear();
//			IntStream.range(0, MultivarSimMeasure.MAX_PARAMID).forEach(i -> args.cMSM.add(MSM.msmParams[i]));
//
//			args.nTWE.clear();
//			IntStream.range(0, TWE.twe_nuParams.length).forEach(i -> args.nTWE.add(TWE.twe_nuParams[i]));
//
//			args.lTWE.clear();
//			IntStream.range(0, TWE.twe_lamdaParams.length).forEach(i -> args.lTWE.add(TWE.twe_lamdaParams[i]));
//
//		}
//	}
	
	public MultivariateMeasure getMeasure() {
		return this.measure;
	}

	public boolean isUsingDependentDimensions() {
		return dimensionDependency;
	}

	public String toString() {
		if (measure == null) {
			return "EE[untrained]";
		}else {
			return "EE[" + measure.toString() + "]"; //,e={" + "..." + "}
		}		
	}

}
