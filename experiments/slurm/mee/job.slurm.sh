#!/usr/bin/env bash

#MEASURE=$1
#SBATCH --job-name=j0-a0
#SBATCH --time=23:00:00
#SBATCH --partition=comp
##SBATCH --constraint=Xeon-Gold
#SBATCH --ntasks=1
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=16
#SBATCH --mem-per-cpu=1000
#SBATCH --output=slurm/a0/%j.out
#SBATCH --error=slurm/a0/%j.err
#SBATCH --qos=normal

#module load java/1.8.0_77
module load java/openjdk-1.14.02

JAVA_VER=$(java -version 2>&1)
echo "start-------------------------------------------------------------------------------------------------------------"
date +%Y-%m-%d-%H:%M:%S
hostname
cd "$WDIR" || exit
echo `pwd`
echo "$JAVA_VER"
echo "=================================================================================================================="
DATASETS=${DATASETSESCAPED//./,}
MEASURES=${MEASURESESCAPED//./,}
DEPENDENCY=${DEPESCAPED//./,}
PARAMS=${PARAMSESCAPED//./,}
echo $DATASETS
echo $MEASURES
echo $DEPENDENCY
echo $PARAMS
echo $XMX
echo $APPARGS
echo "------------------------------------------------------------------------------------------------------------------"

CMDARGS="-data=../data/ -a=Multivariate2018_ts -seed=6463564 -t=0 -vb=1 -useTLRandom=false -tf=yyyy-MM-dd-HHmmSS\
 -fileOutput=overwrite -exportTrainQueryFile=true -exportTestQueryFile=true -saveTransformedDatasets=false\
 -testDir=out/$OUTPUT/test/ -testParamFileSuffixes=i,d,id,b -generateBestParamFiles=false -python=venv/Scripts/python\
 -dep=$DEPENDENCY -dims=AllDims $APPARGS -adjustSquaredDiff=true -useSquaredDiff=true\
 -d=$DATASETS -o=out/$OUTPUT/train/ -sf=summary-$OUTPUT.csv\
 -m=$MEASURES\
 -params=list:$PARAMS"

java -Xmx${XMX}m -cp "MEE_v300122.jar:lib/*" dotnet54.applications.mee.MultivariateEEApp $CMDARGS
