import sys
import os
import glob
import re
import datetime
import math
import random
import pathlib
import importlib

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from scipy import stats

from IPython.display import display, HTML

VERSION = "29-4-2021.1"
print('utils.py ' + VERSION)
PROJECT_DIR = "E:/git/dotnet54/TS-CHIEF-DEV/"
print('PROJECT_DIR: ' + PROJECT_DIR)


# print('________________________________________________________________________\n')

def get_subdirs(path, exclude="."):
    dirs = [d for d in os.listdir(path) if os.path.isdir(os.path.join(path, d)) and not d.startswith(exclude)]
    return dirs


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


# https://stackoverflow.com/questions/480214/
# how-do-you-remove-duplicates-from-a-list-whilst
# -preserving-order?page=1&tab=oldest#tab-top
def unique_with_order(seq):
    seen = set()
    seen_add = seen.add
    return [x for x in seq if not (x in seen or seen_add(x))]


def get_mts2018(input_dir=PROJECT_DIR + "/data/", filename='mts2018.csv'):
    fullname = f"{input_dir}/{filename}"
    df = pd.read_csv(fullname, index_col=0)
    return df


def get_uts2018(input_dir=PROJECT_DIR + "/data/", filename='uts2018.csv'):
    fullname = f"{input_dir}/{filename}"
    df = pd.read_csv(fullname, index_col=0)
    return df


def get_ucr2015(input_dir=PROJECT_DIR + "/data/", filename='ucr2015.csv'):
    fullname = f"{input_dir}/{filename}"
    df = pd.read_csv(fullname, index_col=0)
    return df


# load some useful variables

if 'df_ucr2015' not in globals():
    df_ucr2015 = get_ucr2015()
if 'df_uts2018' not in globals():
    df_uts2018 = get_uts2018()
if 'df_mts2018' not in globals():
    df_mts2018 = get_mts2018()
