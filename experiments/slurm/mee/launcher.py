#! /usr/bin/env python

# last modified on 30-01-2022, resample experiments for KAIS R1

# test python launcher.py -j=t0 -o=t0 -p=10:0,100,1 -m=euc:dtwf:dtwr:ddtwf:ddtwr:wdtw:wddtw:lcss:msm:erp:twe
# -d=BasicMotions,ERing:RacketSports

# to convert windows to linux new lines
# sed -i.bak 's/\r$//' launcher.py

launcher_version = "31-01-2022"

import os
import sys
import subprocess
from argparse import ArgumentParser, REMAINDER
import platform

RED = "\033[1;31m"
BLUE = "\033[1;34m"
CYAN = "\033[1;36m"
GREEN = "\033[0;32m"
RESET = "\033[0;0m"
BOLD = "\033[;1m"
REVERSE = "\033[;7m"


# module load python/3.7.2-gcc6
# print(platform.sys.version)

def cprint(str):
    print(str + RESET)


cprint(BLUE + platform.sys.version)
# module load python/3.7.2-gcc6
# print(platform.sys.version)

parser = ArgumentParser()
parser.add_argument("-job", "-j", help="slurm job name", type=str, default="test")
parser.add_argument("-datasets", "-d", help="datasets", type=str, default="BasicMotions,ERing:RacketSports")
parser.add_argument("-measures", "-m", help="measures", type=str, default="euc,dtwf:ddtwf")
parser.add_argument("-dependency", "-dp", help="dependency", type=str, default="false,true")
parser.add_argument("-params", "-p", help="params_per_job:start,end,step", type=str, default="100:0,100,1")
parser.add_argument("-out", "-o", help="output folder", type=str, default="train")
parser.add_argument("-norm", "-n", help="normalize", type=str, default="true")
parser.add_argument("-fold", help="fold no", type=str, default="0")
parser.add_argument("-pi", help="p for indep", type=str, default="2")
parser.add_argument("-pd", help="p for dep", type=str, default="2")
parser.add_argument("-train", help="run train loocv", type=str, default="true")
parser.add_argument("-test", help="run testing", type=str, default="false")
parser.add_argument("-cpu", help="--slurm cpus-per-task", type=str, default="16")
parser.add_argument("-mem", help="--slurm mem", type=str, default="500")
parser.add_argument("-time", help="--slurm cpu", type=str, default="500")
parser.add_argument("-part", help="--slurm partition {comp,rtqp}", type=str, default="comp")
parser.add_argument("-qos", help="--slurm qos {normal,rtq}", type=str, default="normal")
parser.add_argument("-dry", help="--dry run", action='store_true')

args, unknown_args = parser.parse_known_args(sys.argv)
cprint(GREEN + str(args))
cprint(RED + str(unknown_args))

dataset_groups = args.datasets.split(':')
measure_groups = args.measures.split(':')
dependency_groups = args.dependency.split(':')

param_args = args.params.split(':')
threads = int(param_args[0])
prange = param_args[1].split(",")
start = int(prange[0])
end = int(prange[1])
step = int(prange[2])
param_ids = [i for i in range(0, 100, 1)]
param_id_groups = [param_ids[i:i + threads] for i in range(0, len(param_ids), threads)]
param_id_groups = [[str(integer) for integer in group] for group in param_id_groups]  # change to strings
# print(param_ids)
# print(param_id_groups)

# 16 cpus
xmx = int(args.mem) * 16

# extra args to pass
app_args = "-lpIndep=" + args.pi + " -lpDep=" + args.pd + " -norm=" + args.norm + " -runLOOCV=" + args.train \
           + " -runTesting=" + args.test + " -fold=" + args.fold

os.makedirs("out/" + args.out, exist_ok=True)
os.makedirs("slurm/" + args.out, exist_ok=True)

job_count = 0
for d, datasets in enumerate(dataset_groups):
    for dp, dependencies in enumerate(dependency_groups):
        for m, measures in enumerate(measure_groups):
            for p, params in enumerate(param_id_groups):
                datasets = datasets.strip()
                dependencies = dependencies.strip()
                measures = measures.strip()
                datasets_escaped = datasets.replace(",", ".")
                dependencies_escaped = dependencies.replace(",", ".")
                measures_escaped = measures.replace(",", ".")
                params_escaped = ",".join(params)
                params_escaped = params_escaped.replace(",", ".")

                exports = ' --export=XMX=%s,DATASETSESCAPED="%s",DEPESCAPED="%s",MEASURESESCAPED="%s",' \
                          'PARAMSESCAPED="%s",OUTPUT="%s",APPARGS="%s"' \
                          % (xmx, datasets_escaped, dependencies_escaped, measures_escaped, params_escaped, args.out,
                             app_args)
                cmd = f"sbatch --time={args.time} --mem-per-cpu={args.mem} --cpus-per-task={args.cpu}" \
                      + f" --partition={args.part} --qos={args.qos}" \
                      + f" --output=slurm/{args.out}/%j.out --error=slurm/{args.out}/%j.err" \
                      + f" --job-name={args.job}-{str(measures)[:3]}-{str(len(dependencies.split(',')))} " \
                      + exports + " job.slurm.sh"

                job_count += 1
                print(str(job_count) + ": " + cmd)
                if not args.dry:
                    subprocess.call(cmd, shell=True)
