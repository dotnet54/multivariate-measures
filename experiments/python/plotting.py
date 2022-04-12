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

VERSION = "07-06-2021.1"
print('plotting.py ' + VERSION)


# print('________________________________________________________________________\n')

# use this function to check data before plotting, eg. does x and y align by datasets?
# are num repeats the same?
def check_data():
    pass

def scatterxy(x, y, xlabel='x', ylabel='y', figsize=(6, 6),
              output_dir="./out/", file=None, format="pdf",
              color='coral', size=25, annotate_wdl=True,
              xlim=(0, 1), ylim=(0, 1)):

    if x.shape[0] != y.shape[0]:
        raise Exception(f"Scatter: Shape of x {x.shape} and y {y.shape} doesnt match")

    fig, ax = plt.subplots(figsize=figsize)

    marker_style = dict(facecolors="coral", edgecolors="k")
    cm = plt.cm.get_cmap('autumn_r')

    sc = ax.scatter(x, y, s=size, lw=0.5, c=color, edgecolor='k')

    # diagonal line
    ax.plot([0, 1], [0, 1], transform=ax.transAxes, ls='--', color='k', lw=.5, label='_nolegend_')

    xywins = (x < y).sum()
    xyloss = (x > y).sum()
    xyties = (x == y).sum()
    wdl = f"W/D/L for y-axis = {xywins}/{xyties}/{xyloss}, Total: {len(x)} \n"
    print(wdl)
    ax.set_title(wdl)
    if annotate_wdl:
        ax.annotate(f"x wins = {xyties}", (xlim[0]+0.7, 0.05), fontsize=12,rotation=0)
        ax.annotate(f"draws = {xyties}", (0.05, ylim[1]-0.05-0.05), fontsize=12,rotation=0)
        ax.annotate(f"y wins = {xywins}", (0.05, ylim[1]-0.05), fontsize=12,rotation=0)

    ax.set_xlim(xlim)
    ax.set_ylim(ylim)
    ax.set_xlabel(f"{xlabel}")
    ax.set_ylabel(f"{ylabel}")

    plt.show()
    if file is not None:
        if file == 'auto':
            file = xlabel + " vs " + ylabel
        fig.savefig(output_dir + file + "." + format, format=format, dpi=600, bbox_inches="tight")
    return ax

def scatterdf(xdf, ydf, xcol='accuracy', ycol='accuracy',
               xlabel='x', ylabel='y', match_on='dataset',
               label_datasets=False, figsize=(6, 6),
               output_dir="./fig1/", file=None, format="pdf",
               archive_info=None,
               color='coral', size=25,
               annotate=False, ant_delta=0.02, ant_max_len=10, ant_col='code',
               xlim=(0, 1), ylim=(0, 1)):

    df_merged = xdf[[match_on, xcol]].merge(ydf[[match_on, ycol]], on=match_on)
    #     display(df_merged.head(26))

    # TODO infer archive based on columns
    df_mts2018 = pd.read_csv(f"E:/git/experiments/reference/uae2018mv-f0-exp.csv")
    df_ucr2015 = pd.read_csv(f"E:/git/experiments/reference/ucr2015-f0-exp.csv")

    if xdf['dimensions'].max() > 1:
        df_datasets = df_mts2018[['dataset', 'code', 'trainsize', 'testsize',
                                  'dimensions', 'length', 'classes']]
    else:
        df_datasets = df_ucr2015[['dataset', 'code', 'trainsize', 'testsize',
                                  'dimensions', 'length', 'classes']]
    df_merged = df_merged.merge(df_datasets, on=match_on)
    #     display(df_merged.head(26))

    # TODO - handle nan

    if xcol == ycol:
        x = df_merged[xcol + "_x"]
        y = df_merged[ycol + "_y"]
    else:
        x = df_merged[xcol]
        y = df_merged[ycol]

    df_merged['acc_diff_x-y'] = x - y

    fig, ax = plt.subplots(figsize=figsize)

    marker_style = dict(facecolors="coral", edgecolors="k")
    cm = plt.cm.get_cmap('autumn_r')

    #     size = np.sqrt(df_merged['NumDimensions'].values) * 10
    #     colors = df_merged['TestSize'].values / 100
    #     colors = np.sqrt(df_merged['NumDimensions'].values)
    #     colors = 'coral'
    #     size = 25 # df_merged['TestSize'].values / 10
    #     print(colors)

    sc = ax.scatter(x, y, s=size, lw=0.5, c=color, edgecolor='k')

    #     ax.scatter(x, y, s=size, lw=0.5, **marker_style)

    # diagonal line
    ax.plot([0, 1], [0, 1], transform=ax.transAxes, ls='--', color='k', lw=.5, label='_nolegend_')

    xywins = (x < y).sum()
    xyloss = (x > y).sum()
    xyties = (x == y).sum()
    wdl = f"W/D/L for y-axis = {xywins}/{xyties}/{xyloss}, Total: {len(x)} \n"
    print(wdl)
    ax.set_title(wdl)
    #     ax.annotate(f"x wins = {xyties}", (xlim[0]+0.7, 0.05), fontsize=12,rotation=0)
    #     ax.annotate(f"draws = {xyties}", (0.05, ylim[1]-0.05-0.05), fontsize=12,rotation=0)
    #     ax.annotate(f"y wins = {xywins}", (0.05, ylim[1]-0.05), fontsize=12,rotation=0)

    ax.set_xlim(xlim)
    ax.set_ylim(ylim)
    ax.set_xlabel(f"{xlabel}")
    ax.set_ylabel(f"{ylabel}")

    # annotate
    if annotate:
        for i in range(df_merged.shape[0]):
            if (x[i] - y[i] > ant_delta):  # xwins
                ax.annotate(df_merged.iloc[i][ant_col][0:ant_max_len], (x[i], y[i]), fontsize=8, rotation=0)
            elif (y[i] - x[i] > ant_delta):  # ywins
                ax.annotate(df_merged.iloc[i][ant_col][0:ant_max_len], (x[i], y[i]), fontsize=8, rotation=90)

    #     plt.colorbar(sc)
    plt.show()
    if file is not None:
        if file == 'auto':
            file = xlabel + " vs " + ylabel
        fig.savefig(output_dir + file + "." + format, format=format, dpi=600, bbox_inches="tight")
    return df_merged, xdf, ydf


def scatterexp(xdf, ydf, exp=None, yexp=None, xcol='accuracy', ycol='accuracy',
            xlabel='x', ylabel='y', match_on='dataset',
            label_datasets=False, figsize=(6, 6),
            output_dir="./fig1/", file=None, format="pdf",
            archive_info=None,
            color='coral', size=25,
            annotate=False, ant_delta=0.02, ant_max_len=10, ant_col='code',
            xlim=(0, 1), ylim=(0, 1)):
    if isinstance(xdf, pd.DataFrame):
        xkey = 'x'
    elif isinstance(xdf, pd.Series):
        xkey = 'x'
    else:
        xkey = xdf
        xdf = exp[xdf]
        xlabel = xkey

    if isinstance(ydf, pd.DataFrame):
        ykey = 'y'
    elif isinstance(xdf, pd.Series):
        xkey = 'y'
    else:
        ykey = ydf
        ydf = exp[ydf]
        ylabel = ykey

    xcolumns = xdf.columns
    ycolumns = ydf.columns

    df_merged = xdf[[match_on, xcol]].merge(ydf[[match_on, ycol]], on=match_on)
    #     display(df_merged.head(26))

    # TODO infer archive based on columns
    df_mts2018 = pd.read_csv(f"E:/git/experiments/reference/uae2018mv-f0-exp.csv")
    df_ucr2015 = pd.read_csv(f"E:/git/experiments/reference/ucr2015-f0-exp.csv")

    if xdf['dimensions'].max() > 1:
        df_datasets = df_mts2018[['dataset', 'code', 'trainsize', 'testsize',
                                  'dimensions', 'length', 'classes']]
    else:
        df_datasets = df_ucr2015[['dataset', 'code', 'trainsize', 'testsize',
                                  'dimensions', 'length', 'classes']]
    df_merged = df_merged.merge(df_datasets, on=match_on)
    #     display(df_merged.head(26))

    # TODO - handle nan

    if xcol == ycol:
        x = df_merged[xcol + "_x"]
        y = df_merged[ycol + "_y"]
    else:
        x = df_merged[xcol]
        y = df_merged[ycol]

    df_merged['acc_diff_x-y'] = x - y

    fig, ax = plt.subplots(figsize=figsize)

    marker_style = dict(facecolors="coral", edgecolors="k")
    cm = plt.cm.get_cmap('autumn_r')

    #     size = np.sqrt(df_merged['NumDimensions'].values) * 10
    #     colors = df_merged['TestSize'].values / 100
    #     colors = np.sqrt(df_merged['NumDimensions'].values)
    #     colors = 'coral'
    #     size = 25 # df_merged['TestSize'].values / 10
    #     print(colors)

    sc = ax.scatter(x, y, s=size, lw=0.5, c=color, edgecolor='k')

    #     ax.scatter(x, y, s=size, lw=0.5, **marker_style)

    # diagonal line
    ax.plot([0, 1], [0, 1], transform=ax.transAxes, ls='--', color='k', lw=.5, label='_nolegend_')

    xywins = (x < y).sum()
    xyloss = (x > y).sum()
    xyties = (x == y).sum()
    wdl = f"W/D/L for y-axis = {xywins}/{xyties}/{xyloss}, Total: {len(x)} \n"
    print(wdl)
    ax.set_title(wdl)
    #     ax.annotate(f"x wins = {xyties}", (xlim[0]+0.7, 0.05), fontsize=12,rotation=0)
    #     ax.annotate(f"draws = {xyties}", (0.05, ylim[1]-0.05-0.05), fontsize=12,rotation=0)
    #     ax.annotate(f"y wins = {xywins}", (0.05, ylim[1]-0.05), fontsize=12,rotation=0)

    ax.set_xlim(xlim)
    ax.set_ylim(ylim)
    ax.set_xlabel(f"{xlabel}")
    ax.set_ylabel(f"{ylabel}")

    # annotate
    if annotate:
        for i in range(df_merged.shape[0]):
            if (x[i] - y[i] > ant_delta):  # xwins
                ax.annotate(df_merged.iloc[i][ant_col][0:ant_max_len], (x[i], y[i]), fontsize=8, rotation=0)
            elif (y[i] - x[i] > ant_delta):  # ywins
                ax.annotate(df_merged.iloc[i][ant_col][0:ant_max_len], (x[i], y[i]), fontsize=8, rotation=90)

    #     plt.colorbar(sc)
    plt.show()
    if file is not None:
        if file == 'auto':
            file = xlabel + " vs " + ylabel
        fig.savefig(output_dir + file + "." + format, format=format, dpi=600, bbox_inches="tight")
    return df_merged, xdf, ydf


def to_barchart_data(df):
    chart_data = df.copy()
    chart_data['labels'] = df['dataset']
    chart_data['y1'] = df['train_size_x']
    chart_data['y2'] = df['test_size_x']
    return chart_data.iloc[0:10]

def barchart(df,
    width = 0.35,
    figsize=(6, 6)):

    labels = df['labels'].values
    y1 = df['y1'].values
    y2 = df['y2'].values

    x = np.arange(len(labels))  # the label locations

    fig, ax = plt.subplots(figsize=(6, 6))

    rects1 = ax.bar(x - width/2, y1, width, label='y1')
    rects2 = ax.bar(x + width/2, y2, width, label='y2')

    # Add some text for labels, title and custom x-axis tick labels, etc.
    ax.set_ylabel('Scores')
    ax.set_title('Scores by group and gender')
    ax.set_xticks(x)
    ax.set_xticklabels(labels, rotation = 90)
    ax.legend()

    # ax.bar_label(rects1, padding=3)
    # ax.bar_label(rects2, padding=3)

    fig.tight_layout()

    plt.show()
    return None

def stacked_barchart(figsize=(6, 6)):
    fig, ax = plt.subplots()

    N = 5
    menMeans = (20, 35, 30, 35, -27)
    womenMeans = (25, 32, 34, 20, -25)
    menStd = (2, 3, 4, 1, 2)
    womenStd = (3, 5, 2, 3, 3)
    ind = np.arange(N)    # the x locations for the groups
    width = 0.35       # the width of the bars: can also be len(x) sequence

    p1 = ax.bar(ind, menMeans, width, yerr=menStd, label='Men')
    p2 = ax.bar(ind, womenMeans, width,
                bottom=menMeans, yerr=womenStd, label='Women')

    ax.axhline(0, color='grey', linewidth=0.8)
    ax.set_ylabel('Scores')
    ax.set_title('Scores by group and gender')
    ax.set_xticks(ind)
    ax.set_xticklabels(('G1', 'G2', 'G3', 'G4', 'G5'))
    ax.legend()

    # Label with label_type 'center' instead of the default 'edge'
    # ax.bar_label(p1, label_type='center')
    # ax.bar_label(p2, label_type='center')
    # ax.bar_label(p2)

    plt.show()
    return None