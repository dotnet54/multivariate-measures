#!/usr/bin/env bash

#SBATCH --job-name=ts0
#SBATCH --time=00:30:00
#SBATCH --partition=comp
##SBATCH --constraint=Xeon-Gold
#SBATCH --ntasks=1
##SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=1
#SBATCH --mem-per-cpu=2000
#SBATCH --output=out/%j.out
#SBATCH --error=err/%j.err
##SBATCH --qos=normal

module load java/1.8.0_77

JUCLEANED=${JUCMD//;/,}


echo "start-------------------------------------------------------------------------------------------------------------"
date +%Y-%m-%d-%H:%M:%S
hostname
pwd
java -version
echo "=================================================================================================================="
echo $JUJAVACMD -jar $JUJAR $JUCLEANED 
echo "------------------------------------------------------------------------------------------------------------------"

java $JUJAVACMD -jar $JUJAR $JUCLEANED

