#! /usr/bin/env python

# sed -i.bak 's/\r$//' pyshell.py

# imports
#import os
import sys
import subprocess


# script

print("hello,  world " + str(sys.version_info[0]))


cmd = 'ls -all'

JUJAVACMD=""
JUJAR="app02601.jar"
JUCLEANED="-out=dev/ -results=results.txt -trees=10 -threads=0 -export=3 -s=ee:5 -ucr=Coffee"

jcmd = '--export=JUJAVACMD="%s",JUJAR=%s,JUCMD="%s"' % (JUJAVACMD,JUJAR,JUCLEANED)
cmd = "sbatch --job-name=test --time=0:00:10 --output=out/test-%j.out --error=err/test-%j.err --partition=comp --mem-per-cpu=100 --qos=normal --cpus-per-task=1 "+jcmd+" jobm3.slurm"

print(cmd)

subprocess.call(cmd, shell=True)

#sbatch --job-name=$JSJOB$JSID --time=$JSTIME --output=out/$JSTIMESTAMP-%j.out --error=err/$JSTIMESTAMP-%j.err 
#--export=JUJAVACMD=$JUJAVACMD,JUJAR=$JUJAR,JUCMD="$JUCLEANED" --partition=$JSPART --mem-per-cpu=$JSMEM_PER_CPU --qos=$JSQOS --cpus-per-task=$JSCPU_PER_TASK jobm3.slurm


