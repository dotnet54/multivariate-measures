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

from utils import get_subdirs

VERSION = "18-6-2021.1"
print('loading.py ' + VERSION)


# print('________________________________________________________________________\n')

def detect_folder_structure(input_dir):
    folders = {}
    folds = get_subdirs(input_dir)
    for fold in folds:
        repeats = get_subdirs(f"{input_dir}/{fold}/")
        folders[fold] = repeats
    return folders


def read_csv(file, input_dir, index_col=0, header=0, reset_index=True):
    df = pd.read_csv(f"{input_dir}/{file}.csv", index_col=index_col, header=header)
    if reset_index:
        df.reset_index(inplace=True)
    return df


def read_and_merge_csv(filex, filey, input_dir, merge_on='dataset', index_col=0, header=0, reset_index=True):
    dfx = pd.read_csv(f"{input_dir}/{filex}.csv", index_col=index_col, header=header)
    dfy = pd.read_csv(f"{input_dir}/{filey}.csv", index_col=index_col, header=header)
    merged = dfx.merge(dfy, on=merge_on)
    if reset_index:
        merged.reset_index(inplace=True, drop=True)
    return merged, dfx, dfy


def read_files_into_df(input_files, index_col=0, header=0, reset_index=True):
    li = []
    # if len(li) == 0:
    #     raise Exception("read_files_into_df:: No input files to concatenate")
    for filename in input_files:
        df = pd.read_csv(filename, index_col=index_col, header=header)
        li.append(df)
    df = pd.concat(li, axis=0, ignore_index=True)
    if reset_index:
        df.reset_index(inplace=True)
    return df


def load_tree_csv(experiment_dir, input_dir, output_dir="out/",
                  num_folds='*', num_repeats='*',
                  save_intermediate=False,
                  save=False, verbosity=0):
    input_dir = f"{input_dir}/{experiment_dir}/"

    if output_dir is None:
        output_dir = input_dir

    # detect folders
    dirs = detect_folder_structure(input_dir)

    if len(dirs) > 0:
        if list(dirs.keys())[0][0:4] != 'fold':
            # if results are not nested, load everything
            input_files = glob.glob(input_dir + "/**/*.tree.csv")
            df = read_files_into_df(input_files)
            if save_intermediate:
                df.to_csv(input_dir + f"/{experiment_dir}.fold_all.repeat_all.all_tree.csv", index=False)
        else:
            li = []
            for fold, repeats in dirs.items():
                if num_folds == '*' or int(fold[4:]) < int(num_folds):
                    for r, repeat in enumerate(repeats):
                        if num_repeats == '*' or int(repeat[6:]) < int(num_repeats):
                            input_files = glob.glob(input_dir + f"/{fold}/{repeat}/**/*.tree.csv")
                            if len(input_files) > 0:
                                tmp = read_files_into_df(input_files)
                                if save_intermediate:
                                    tmp.to_csv(input_dir +
                                               f"/{fold}/{experiment_dir}.{fold}.{repeat}.all_tree.csv", index=False)
                                li.append(tmp)
                    df = pd.concat(li, axis=0, ignore_index=True)
                    df.reset_index(inplace=True)
                    if save_intermediate:
                        df.to_csv(input_dir + f"/{experiment_dir}.{fold}.repeat_all.all_tree.csv", index=False)
    else:
        print(f"\tload_tree_csv:: no result folders found in the input directory: {input_dir}")
        return None

    output_file = f"{output_dir}/{experiment_dir}.all_tree.csv"
    if save:
        df.to_csv(output_file, index=False)
    print(f"\tload_tree_csv:: output generated {df.shape} for file {output_file}")
    return df


def load_forest_csv(experiment_dir, input_dir, output_dir="out/",
                    num_folds='*', num_repeats='*',
                    save_intermediate=False,
                    save=False, verbosity=0):
    input_dir = f"{input_dir}/{experiment_dir}/"

    if output_dir is None:
        output_dir = input_dir

    # detect folders
    dirs = detect_folder_structure(input_dir)

    if len(dirs) > 0:
        if list(dirs.keys())[0][0:4] != 'fold':
            # if results are not nested, load everything
            input_files = glob.glob(input_dir + "/**/*.forest.csv")
            df = read_files_into_df(input_files)
            if save_intermediate:
                df.to_csv(input_dir + f"/{experiment_dir}.fold_all.repeat_all.all_forest.csv", index=False)
        else:
            li = []
            for fold, repeats in dirs.items():
                if num_folds == '*' or int(fold[4:]) < int(num_folds):
                    for r, repeat in enumerate(repeats):
                        if num_repeats == '*' or int(repeat[6:]) < int(num_repeats):
                            input_files = glob.glob(input_dir + f"/{fold}/{repeat}/**/*.forest.csv")
                            if len(input_files) > 0:
                                tmp = read_files_into_df(input_files)
                                if save_intermediate:
                                    tmp.to_csv(input_dir +
                                               f"/{fold}/{experiment_dir}.{fold}.{repeat}.all_forest.csv", index=False)
                                li.append(tmp)
                    df = pd.concat(li, axis=0, ignore_index=True)
                    df.reset_index(inplace=True, drop=True)
                    if save_intermediate:
                        df.to_csv(input_dir + f"/{experiment_dir}.{fold}.repeat_all.all_forest.csv", index=False)
    else:
        print(f"\tload_forest_csv:: no result folders found in the input directory: {input_dir}")
        return None

    # clean the df and add some metadata
    df.insert(1, 'exp_name', experiment_dir)
    df = df.rename(columns={'index': 'batch_index'})
    df.reset_index(inplace=True, drop=True)
    # df.index.name = 'index'

    output_file = f"{output_dir}/{experiment_dir}.all_forest.csv"
    if save:
        pathlib.Path(output_file).parent.mkdir(parents=True, exist_ok=True)
        df.to_csv(output_file)
    print(f"load_forest_csv:: output generated {df.shape} for file {output_file}")
    return df


def load_experiments(input_dir, output_dir, include=None, exclude = None,
                     use_cache=True, results=None,
                     # fold='*', repeat='*',
                     save_intermediate=False,
                     save=True, verbosity=0):
    '''
    loads experiments from each folder in the input_dir

    use_cache = try,always,never
    '''

    folders = get_subdirs(input_dir)
    # print(folders)

    if results is None:
        results = {}

    for i, folder in enumerate(folders):
        if exclude is not None and folder in exclude:
            continue
        if include is not None and folder not in include:
            continue
        if use_cache:
            print(f"load_experiments:: {i}. loading {folder} from cache")
            results[folder] = pd.read_csv(f"{output_dir}/{folder}.all_forest.csv", index_col=0)
        else:
            print(f"load_experiments:: {i}. loading {folder} from forest.csv files")
            results[folder] = load_forest_csv(folder, input_dir=input_dir, output_dir=output_dir,
                                          save_intermediate=save_intermediate,
                                          save=save, verbosity=verbosity)

    return results


def get_exp_status_report(input_dir, df_archive, max_repeats = 5):
    print('----Report generated at ' + datetime.datetime.now().strftime("%d-%m-%Y %H:%M:%S"))
    print(colored(f'Input folder: {input_dir}\n', 'green'))

    # if df_archive == uea2018mts, remove variable length and long datasets
    exclude_datasets = ('CharacterTrajectories,EigenWorms,FaceDetection' \
                        ',InsectWingbeat,JapaneseVowels,MotorImagery,SpokenArabicDigits').split(',')
    df_archive = df_archive[~df_archive.dataset.isin(exclude_datasets)]

    experiments = get_subdirs(input_dir)
    experiments.sort()
    for i, exp in enumerate(experiments):
        print(str(i) + ". " + exp)
        folds = get_subdirs(input_dir + '/' + exp)
        for fold in folds:
            print('   ' + fold)
            repeats = get_subdirs(input_dir + '/' + exp + '/' + fold)
            for r, repeat in enumerate(repeats):
                if r >= max_repeats:
                    break
                datasets = get_subdirs(input_dir + '/' + exp + '/' + fold + '/' + repeat)
                print('\t' + repeat + ' - ' + str(len(datasets)) + ' datasets\n\t', end = '')
                unfinished = df_archive[~df_archive.dataset.isin(datasets)]['dataset']
                unfinished = ','.join(list(unfinished))
                print(colored(unfinished, 'red'))

    #                 for dataset in datasets:
    #                     print('\t' + dataset)

    print('\n-------end-------')