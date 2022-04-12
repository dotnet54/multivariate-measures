import sys
import os
import glob
import re
import datetime
import math
import random
from pathlib import Path
import importlib
import shutil

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import scipy as sp
import statsmodels.api as sm
import sklearn as sk
from sktime.utils.data_io import load_from_tsfile_to_dataframe
from IPython.display import display, HTML

import loading
import plotting

pd.set_option("display.max_rows", 100)
pd.set_option("display.max_columns", 100)
pd.set_option('display.width', 2000)
pd.set_option('display.float_format', lambda x: '%.4f' % x)
pd.set_option("display.max_seq_items", 100)

PROJECT_DIR = "E:/git/dotnet54/TS-CHIEF-DEV/"
SCRIPTS_DIR = f"{PROJECT_DIR}/experiments/python/"

INPUT = f"E:/git/experiments/thesis/out/0105/"
OUTPUT = f"E:/git/dotnet54/TS-CHIEF-DEV/experiments/thesis/data/tmp/"

if __name__ == '__main__':
    # tmp = loading.read_and_merge_csv("k500b100.all_forest", "k500b100_bv1.all_forest", input_dir=OUTPUT)[0]
    # plotting.scatterxy(tmp['accuracy_x'], tmp['accuracy_y'])

    tmp = loading.read_and_merge_csv("k500b100.all_forest", "k500b100_bv1.all_forest", input_dir=OUTPUT)[0]
    df = plotting.to_barchart_data(tmp)
    plotting.barchart(df)

    # plotting.stacked_barchart()
