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

VERSION = "3-11-2021.1"
print('aggregating.py ' + VERSION)


# print('________________________________________________________________________\n')


def get_agg_matrix(exp_dict, experiments, col='accuracy', agg='mean'):
    df_result = exp_dict[experiments[0]][['dataset', col]]
    df_result = df_result.rename(columns={col: experiments[0]})
    df_result = df_result.groupby(by=["dataset"]).agg(agg)

    for exp in range(1, len(experiments)):
        df_tmp = exp_dict[experiments[exp]][['dataset', col]]
        df_tmp = df_tmp.rename(columns={col: experiments[exp]})
        df_tmp = df_tmp.groupby(by=["dataset"]).agg(agg)
        df_result = df_result.merge(df_tmp, on='dataset', how='inner')

    df_result = df_result.reset_index()
    return df_result


def exp_groupby_dataset(df_exp, max_repeats=5):
    """
    groups the rows of df_exp by dataset and produces a summary report
    df_exp is usually the result from load_experiments
    """

    # apply func for groupby
    def _func_dataset_group(df_group, max_repeats=5):
        df_group_out = df_group.head(max_repeats)
        df_group_out = df_group_out[['dataset', 'exp_name', 'fold_no', 'repeat_no',
                                     'version', 'build_date', 'version_tag',
                                     'cpus', 'total_time_hr', 'max_memory', 'total_memory',
                                     'accuracy', 'num_trees', 'num_candidates_str']]
        df_group_out = df_group_out.reset_index(drop=True)
        # new repeat no fixes any non unique repeat_no issue in output files
        df_group_out.index.name = 'new_repeat_no'
        # add new information columns
        df_group_out.insert(1, 'num_repeats', df_group_out.shape[0])
        df_group_out.insert(2, 'is_complete', df_group_out.shape[0] == max_repeats)
        df_group_out['is_complete'] = df_group_out['is_complete'].astype(int)
        return df_group_out

    df_result = df_exp.groupby('dataset').apply(_func_dataset_group, max_repeats=max_repeats)
    df_result = df_result.reset_index(drop=True, level=0)  # drop level 0 dataset index only
    df_result = df_result.reset_index(drop=False)  # add new int index, keep new_repeat_no as a column
    return df_result
