#!/usr/bin/env bash

#example command line

#./m3.sh app02601.jar "-machine=m3 -out=k100e5b100r100s100/ -trees=100 -s=st:100,ee:5,boss:100,rif:100 -threads=0 -results=res/k100e5sbr100.txt" Coffee 5 5000 10

# sed -i.bak 's/\r$//' m4.sh 
JUJAR=./$1
#JUCMD=${2:-"-out=dev/ -results=res/results.txt -s=ee:5,boss:100,rif:100"}


JSTIMESTAMP=`date '+%Y-%m-%d-%H%M%S'`
JSJOB=${7:-job}
JSID=0
JSTIME=${4:-"1:00:00"}
JSOUT=
JSPART=comp
JSMEM_PER_CPU=${5:-5000}
JSCPU_PER_TASK=${6:-10}
JTOTALMEM=`expr $JSMEM_PER_CPU \* $JSCPU_PER_TASK`
JUJAVACMD=-Xmx${JTOTALMEM}m
JSQOS=normal

#splitting datasets into 4 sets to make it easy to send in batches; set resource requirements different for each batch to optimize queuing 
#--- 16 cpu shared119 timings
#set10 -- for ts-chief k5000 total 1hr; each < 1hr
declare -a set10=(SonyAIBORobotSurface1 SonyAIBORobotSurface2 ItalyPowerDemand TwoLeadECG MoteStrain ECGFiveDays CBF GunPoint Coffee 
	DiatomSizeReduction ArrowHead ECG200 FaceFour BirdChicken BeetleFly ToeSegmentation1 ToeSegmentation2 Symbols Wine ShapeletSim 
	Plane SyntheticControl OliveOil Beef Trace Meat DistalPhalanxTW ProximalPhalanxOutlineAgeGroup DistalPhalanxOutlineAgeGroup 
	MiddlePhalanxOutlineAgeGroup ProximalPhalanxTW MiddlePhalanxTW Lightning7 FacesUCR MedicalImages Herring Car MiddlePhalanxOutlineCorrect 
	DistalPhalanxOutlineCorrect ProximalPhalanxOutlineCorrect)
#set11 -- for ts-chief k500 total 9hr; each < 1hr
declare -a set11=(Ham ECG5000 Lightning2 SwedishLeaf InsectWingbeatSound TwoPatterns Wafer 
	FaceAll Fish Mallat OSULeaf ChlorineConcentration Adiac Strawberry WordSynonyms Yoga CinCECGtorso PhalangesOutlinesCorrect 
	CricketX CricketY CricketZ Computers Earthquakes)
#set12 -- for ts-chief k500 total 21hr; each < 5hr
declare -a set12=(UWaveGestureLibraryX UWaveGestureLibraryZ UWaveGestureLibraryY WormsTwoClass FiftyWords Worms SmallKitchenAppliances 
	Haptics LargeKitchenAppliances ScreenType RefrigerationDevices ElectricDevices InlineSkate ShapesAll)
#set13 -- for ts-chief k500  total 218hr - 9days  -- 5:26:50 5:31:14 6:38:20 16:37:31 18:17:23 28:13:22 28:43:02 108:35:15 (4.5days)
declare -a set13=(StarlightCurves Phoneme UWaveGestureLibraryAll FordA FordB NonInvasiveFetalECGThorax2 NonInvasiveFetalECGThorax1 HandOutlines)

declare -a stest=(ItalyPowerDemand Coffee)

declare -a sexp1=(SonyAIBORobotSurface1 TwoLeadECG CBF DiatomSizeReduction FaceFour ECG200 ToeSegmentation2 Wine Beef DistalPhalanxTW 
	MiddlePhalanxTW ProximalPhalanxTW Lightning7 FacesUCR MedicalImages Car ProximalPhalanxOutlineCorrect ECG5000 Mallat OSULeaf 
	Adiac Strawberry ChlorineConcentration PhalangesOutlinesCorrect CricketX CricketY CricketZ FiftyWords WormsTwoClass Worms UWaveGestureLibraryY 
	LargeKitchenAppliances Haptics ScreenType RefrigerationDevices InlineSkate Phoneme UWaveGestureLibraryAll FordB NonInvasiveFetalECGThorax2)


#JCMD parsing
if [ "${2:-none}" == "none" ]; then
	#assume -ucr is handled by jar file	and all datasets run under one job
    JUCMD="-out=dev/ -results=results.txt -trees=100 -threads=0 -export=3 -s=ee:5,boss:100,rif:100"
elif [ "${2}" == "-default" ]; then
	#send one dataset per job
    JUCMD="-out=dev/ -results=results.txt -trees=100 -threads=0 -export=3 -s=ee:5,boss:100,rif:100"

elif [ "${2}" == "-append" ]; then
	#send one dataset per job
    JUCMD="-out=dev/ -results=results.txt -trees=100 -threads=0 -export=3 ${2}" 
else
    JUCMD=$2
fi


#JUCR parsing

if [ "${3:-none}" == "none" ]; then
	#assume -ucr is handled by jar file	and all datasets run under one job
	echo "using test dataset"
    JUCR=-ucr=ItalyPowerDemand
elif [ "$3" == "s10" ]; then
	#send one dataset per job
    declare -a datasets=("${set10[@]}")
	LOOP=1
elif [ "$3" == "s11" ]; then
	#send one dataset per job
    declare -a datasets=("${set11[@]}")
	LOOP=1
elif [ "$3" == "s12" ]; then
	#send one dataset per job
    declare -a datasets=("${set12[@]}")
	LOOP=1
elif [ "$3" == "s13" ]; then
	#send one dataset per job
    declare -a datasets=("${set13[@]}")
	LOOP=1
elif [ "$3" == "stest" ]; then
	#send one dataset per job
    declare -a datasets=("${stest[@]}")
	LOOP=1
elif [ "$3" == "sexp1" ]; then
	#send one dataset per job
    declare -a datasets=("${sexp1[@]}")
	LOOP=1
elif [ "${3:0:5}" == "-ucr=" ]; then
	#send one dataset per job
	DATASETS_ENCODED="${3:5}" 
	IFS=', ' read -r -a datasets <<< "$DATASETS_ENCODED"
	LOOP=1
else
    JUCR=-ucr=$3
fi

#some init work
mkdir -p out
mkdir -p err


if [ "$LOOP" == 1 ]; then
	for i in "${datasets[@]}"
	do
		JUCLEANED=${JUCMD//,/;}
		JUCLEANED="${JUCLEANED} -ucr=${i}"
		echo "-----sending job $i ($JSTIME, ${JSMEM_PER_CPU}MB, ${JSCPU_PER_TASK}CPU): $JUJAVACMD $JUJAR $JUCLEANED"
		sbatch --job-name=$JSJOB$JSID --time=$JSTIME --output=out/$JSTIMESTAMP-%j.out --error=err/$JSTIMESTAMP-%j.err --export=JUJAVACMD=$JUJAVACMD,JUJAR=$JUJAR,JUCMD="$JUCLEANED" --partition=$JSPART --mem-per-cpu=$JSMEM_PER_CPU --qos=$JSQOS --cpus-per-task=$JSCPU_PER_TASK job.slurm
	done
else
	JTMP="${JUCMD} ${JUCR}"
	JUCLEANED=${JTMP//,/;} 
	# echo " **********************   $JUCLEANED"
	# JUCLEANED="${JUCLEANED} ${JUCR}"
	echo "-----sending----- job $i ($JSTIME, ${JSMEM_PER_CPU}MB, ${JSCPU_PER_TASK}CPU): $JUJAVACMD $JUJAR $JUCLEANED"
	sbatch --job-name=$JSJOB$JSID --time=$JSTIME --output=out/$JSTIMESTAMP-%j.out --error=err/$JSTIMESTAMP-%j.err --export=JUJAVACMD=$JUJAVACMD,JUJAR=$JUJAR,JUCMD="$JUCLEANED" --partition=$JSPART --mem-per-cpu=$JSMEM_PER_CPU --qos=$JSQOS --cpus-per-task=$JSCPU_PER_TASK job.slurm
fi


## now loop through the above array


