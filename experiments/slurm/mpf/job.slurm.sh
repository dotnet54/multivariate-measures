#!/usr/bin/env bash

#SBATCH --job-name=test
#SBATCH --time=23:00:00
#SBATCH --partition=comp
##SBATCH --constraint=Xeon-Gold
#SBATCH --ntasks=1
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=32
#SBATCH --mem-per-cpu=1000
#SBATCH --output=slurm/%j.out
#SBATCH --error=slurm/%j.err
#SBATCH --qos=normal

#module load java/1.8.0_77
module load java/openjdk-1.14.02

JAVA_VER=$(java -version 2>&1)
echo "START -------------------------------------------------------------------------------------------------------------"
echo `date +%Y-%m-%d-%H:%M:%S`
echo `hostname`
echo `pwd`
echo "$JAVA_VER"
echo "------------------------------------------------------------------------------------------------------------------"
echo $XMX
echo $APP
CLEAN_APPARGS=${APPARGS//#/,}
echo ${CLEAN_APPARGS}
echo "------------------------------------------------------------------------------------------------------------------"


java -Xmx${XMX}m -cp "${APP}:lib/*" dotnet54.applications.tschief.TSChiefApp ${CLEAN_APPARGS}

echo "END =================================================================================================================="
