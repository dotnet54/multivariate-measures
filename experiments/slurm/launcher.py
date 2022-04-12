#! /usr/bin/env python
import os
import sys
import subprocess
from argparse import ArgumentParser
import platform
import re
from pathlib import Path

RED = "\033[1;31m"
BLUE = "\033[1;34m"
CYAN = "\033[1;36m"
GREEN = "\033[0;32m"
RESET = "\033[0;0m"
BOLD = "\033[;1m"
REVERSE = "\033[;7m"

# to convert windows to linux new lines
# sed -i.bak 's/\r$//' launcher.py
launcher_version = "12-10-2019-v2-termcolor"

# module load python/3.7.2-gcc6
# print(platform.sys.version)

def cprint(str):
    print(str + RESET)

def get_subdirs(path, exclude_prefix=None, include_prefix=None):
    filtered_dirs = None
    if exclude_prefix is None:
        exclude_prefix = [".", "_"]
    if os.path.exists(path):
        # get all sub dirs
        dirs = [d for d in os.listdir(path) if os.path.isdir(os.path.join(path, d))]
        # filter ~ just separating steps for readability
        filtered_dirs = [d for d in dirs if not any(d.startswith(p) for p in exclude_prefix)]
        if include_prefix is not None:
            filtered_dirs = [d for d in filtered_dirs if any(d.startswith(p) for p in include_prefix)]
    return filtered_dirs


def unique_with_order(seq):
    seen = set()
    seen_add = seen.add
    return [x for x in seq if not (x in seen or seen_add(x))]


def get_value_in_args(key, default=None):
    """
    extracts the value for the key from command line args for the jar file
    """
    regex = re.compile(r'\s[-](?P<key>' + key + r')=(?P<value>[^\s]*?)\s').search(args.args)
    if regex is None:
        return default
    else:
        return regex.group('value')


def is_complete(exp_dir, dataset):
    # check forest file
    forest = f"{exp_dir}/{dataset}/{dataset}.forest.csv"
    if os.path.exists(forest):
        stats = Path(forest).stat()
        # check file size
        if stats.st_size != 0:
            return True

    # check tree file
    forest = f"{exp_dir}/{dataset}/{dataset}.tree.csv"
    if os.path.exists(forest):
        stats = Path(forest).stat()
        # check file size
        if stats.st_size != 0:
            return True

    # check pred file
    forest = f"{exp_dir}/{dataset}/{dataset}.pred.csv"
    if os.path.exists(forest):
        stats = Path(forest).stat()
        # check file size
        if stats.st_size != 0:
            return True

    return False


def find_finished_datasets(exp_dir, datasets_todo):
    datasets_todo_list = datasets_todo.split(",")
    dirs = get_subdirs(exp_dir)

    if not os.path.exists(exp_dir):
        print(f"Path does not exists {exp_dir}, cwd:({os.getcwd()})")
        return datasets_todo, []

    _incomplete = []
    # check folder structure, is it nested deep or shallow
    if len(dirs) > 0 and dirs[0][0:4] == 'fold':
        for _fold in dirs:
            _repeats = get_subdirs(f"{exp_dir}/{_fold}")
            for _repeat in _repeats:
                for _dataset in datasets_todo_list:
                    if not is_complete(f"{exp_dir}/{_fold}/{_repeat}", _dataset):
                        _incomplete.append(_dataset)
    else:
        for _dataset in dirs:
            if not is_complete(exp_dir, _dataset):
                _incomplete.append(_dataset)
    # filter distinct and convert to a string
    _incomplete = unique_with_order(_incomplete)
    _complete = [d for d in datasets_todo_list if d not in _incomplete]
    _incomplete = ",".join(_incomplete)
    _complete = ",".join(_complete)
    return _incomplete, _complete


def get_job_name(out_dir, datasets):
    if args.job == 'auto':
        p = out_dir.rfind('/')
        if p > 0:
            _job_name = out_dir[p + 1:p + 1 + 10]
        else:
            _job_name = re.sub(r'[^\w]', '', out_dir[:-10])
        _job_name = _job_name + '_' + datasets[0:4].lower()
    else:
        _job_name = args.job[:10] + '_' + datasets[0:4].lower()
    return _job_name


cprint(BLUE + '''===============================================================================================''')
cprint(BLUE + f"Python {platform.sys.version[0:5]}, launcher version: {launcher_version}")


parser = ArgumentParser()
parser.add_argument("--job", "-j", help="slurm job name", type=str, default="auto")
parser.add_argument("--cpu", help="--slurm cpus-per-task", type=str, default="16")
parser.add_argument("--mem", help="--slurm mem", type=str, default="500")
parser.add_argument("--time", help="--slurm cpu", type=str, default="500")
parser.add_argument("--partition", help="--slurm partition", type=str, default="comp")
parser.add_argument("--qos", help="--slurm qos", type=str, default="normal")
parser.add_argument("--check", help="checks if the results files already exist before running jar file",
                    action='store_true')
parser.add_argument("--dry", help="--dry run, just print the sbatch command", action='store_true')
parser.add_argument("--app", help="jar file name", type=str, default="ts-chief-0.0.0.jar")
parser.add_argument("--cwd", help="", type=str, default=None)
parser.add_argument("--datasets"
                    , help="list of datasets. use a colon to partition datasets into slurm jobs."
                           " e.g. Beef,Car:Plane sends Beef and Car to job1 and Plane to job2"
                    , type=str, default="")
parser.add_argument("--args", help="jar file args", type=str, default="")

args, unknown_args = parser.parse_known_args(sys.argv)
print(args)
print(unknown_args)

if args.cwd is not None:
    os.chdir(args.cwd)
cprint(f"{BLUE}CWD:{RESET} {os.getcwd()}")

# get values we need from jar file args
out_dir = get_value_in_args("out")
cprint(f"{BLUE}Output Dir:{RESET} {out_dir}")
overwrite_results = True if get_value_in_args("overwriteResults") == 'true' else False
num_folds = get_value_in_args("folds", 0)
num_repeats = get_value_in_args("numRepeats", 1)
repeat_no = get_value_in_args("repeatNo", 0)

print('''===============================================================================================''')

# os.makedirs("out/" + out_dir, exist_ok=True)
os.makedirs("slurm/" + out_dir, exist_ok=True)

# JAVA args
xmx = int(args.mem) * int(args.cpu)

dataset_groups = args.datasets.split(':')

for job_num, datasets in enumerate(dataset_groups):
    datasets = datasets.strip()

    if not overwrite_results and args.check:
        incomplete, complete = find_finished_datasets(out_dir, datasets)
        if len(incomplete) == 0:
            cprint(RED + f"{job_num}: results exist: {complete}")
            continue
        else:
            cprint(RED + f"{job_num}: results exist: {complete}")
        datasets = incomplete

    # remove and replace -datasets
    new_args, num_subs = re.compile(r'-datasets=[^ ]*? ').subn("-datasets=" + datasets + " ", args.args)
    if num_subs == 0:
        new_args = args.args + " -datasets=" + datasets

    # new_args, num_subs = re.compile(r':').subn("#", new_args)
    new_args, num_subs = re.compile(r',').subn("#", new_args)
    # print(new_args)

    # set an automatic job name
    job_name = get_job_name(out_dir, datasets)

    exports = '--export=XMX=%s,APP=%s,APPARGS="%s"' \
              % (xmx, args.app, new_args)

    cmd = f"sbatch --job-name={job_name} --time={args.time} --mem-per-cpu={args.mem} --cpus-per-task={args.cpu}" \
          + f" --partition={args.partition} --qos={args.qos}" \
          + f" --output=slurm/{out_dir}/%j_{job_name}.out --error=slurm/{out_dir}/%j_{job_name}.err {exports} job.slurm.sh"

    # cmd = "sbatch --time=" + args.time + " --mem-per-cpu=" + args.mem + " --cpus-per-task=" + args.cpu \
    #       + " --output=slurm/" + out_dir + "/%j.out --error=slurm/" + out_dir + "/%j.err --job-name=" + args.job \
    #       + " " + exports + " job.slurm.sh"

    cprint(GREEN + "=> " + str(job_num) + ": " + RESET+ cmd)
    if not args.dry:
        subprocess.call(cmd, shell=True)
        pass
