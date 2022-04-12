# last modified on 31-3-2022
import pandas as pd
import numpy as np
import glob
import os
import sys
from termcolor import colored, cprint

"""

Main:

If there are training files
 make a test folder
 combine them to .train.all.csv files
 generate pred and count files
 copy relevant files to test folder
 run java testing code
 generate pred files
 generate ee files

"""


def get_datasets_in_path(path, exclude="."):
    """
    Get a list of datasets in the fiven folder based on subdir names

    :param path:
    :param exclude:
    :return:
    :version: 28-2-2022
    :author: shifaz
    """
    datasets = [os.path.basename(os.path.dirname(p)) for p in glob.glob(path + "*/") if
                not os.path.basename(os.path.dirname(p)).startswith(exclude)]
    return datasets


def combine_training_files(input_dir, output_dir, datasets,
                           measures=['euc', 'dtwf', 'dtwr', 'ddtwf', 'ddtwr',
                                     'wdtw', 'wddtw', 'lcss', 'msm', 'erp', 'twe'],
                           dependency=[True, False],
                           folds=list(range(0, 9)),
                           num_dimensions=None,
                           verify=True,
                           output_suffix=""):
    '''
    Creates a *.train.all.csv for each dataset in output_dir

        noDims is same for each file
        noDims should be useful for all datasets in the list
    '''

    if not isinstance(datasets, list):
        datasets = [datasets]

    for fold in folds:
        for dataset in datasets:
            print(f"{dataset}.fold{fold}: combine_training_files")

            search_pattern = f"{input_dir}/fold{fold}/{dataset}/{dataset}*.train.exp.csv"
            files = glob.glob(search_pattern)
            print(f"   INFO {dataset}: {len(files)} files found, searched: ({search_pattern})")

            if len(files) == 0:
                print(colored(f"  WARN {dataset}: skipping {dataset}"), 'red')
                continue

            # read all files and combine into one dataframe
            tmp_df_list = []
            for file in files:
                # print(file)
                tmp_df = pd.read_csv(file, index_col=False)

                # clean dataframe
                tmp_df.rename(columns=lambda x: x.strip(), inplace=True)
                tmp_df = tmp_df.reset_index(drop=True)

                exp_path, exp_file = os.path.split(file)
                tmp_df['expPath'] = exp_path
                tmp_df['expFile'] = exp_file

                tmp_df['queryFile'] = exp_file.replace('.exp', '.query')
                tmp_df['countFile'] = exp_file.replace('.exp', '.count')

                tmp_df_list.append(tmp_df)
            df_all = pd.concat(tmp_df_list)
            _num_rows = df_all.shape[0]
            print(f"   INFO {dataset}: df_all.shape before filter {df_all.shape}")
            # display(df_all.head())

            # apply the filters -- uses one of the noDims
            if num_dimensions is None:
                num_dimensions_to_select = df_all.iloc[0]['dimensions']
            else:
                num_dimensions_to_select = num_dimensions
            df_all = df_all[(df_all['noDims'] == num_dimensions_to_select) &
                            (df_all['useDependentDims'].isin(dependency)) &
                            (df_all['name'].isin(measures))].copy()
            if df_all.shape[0] != _num_rows:
                cprint(f"   ERROR {dataset}: df_all.shape after filter {df_all.shape}, rows != {_num_rows}", 'red')
                raise Exception("Terminating....")

            # display(df_all.head())

            # verify that all paramIDs have been explored for each group (dependency, measure)
            if verify:
                for m in measures:
                    for d in dependency:
                        filtered_df = df_all[(df_all['useDependentDims'] == d) & (df_all['name'] == m)]
                        tested_params = filtered_df['paramID'].unique()

                        if m in ['euc', 'dtwf', 'ddtwf'] and len(tested_params) != 1:
                            print(
                                colored(f"  WARN:{dataset} {len(tested_params)}/1 paramID was found for: {m}, dep: {d}",
                                        'red'))
                            # raise Exception(f"  ERROR:{dataset} {len(tested_params)}/1 paramID was found for: {m}, dep: {d}")
                        elif m not in ['euc', 'dtwf', 'ddtwf'] and len(tested_params) != 100:
                            print(colored(
                                f"  WARN:{dataset} {len(tested_params)}/100 paramID was found for: {m}, dep: {d}",
                                'red'))
                            # raise Exception(f"  ERROR:{dataset} {len(tested_params)}/1 paramID was found for: {m}, dep: {d}")

            if output_suffix is not None:
                os.makedirs(f"{output_dir}/fold{fold}/{dataset}/", exist_ok=True)
                df_all.to_csv(
                    f"{output_dir}/fold{fold}/{dataset}/{dataset}-train{output_suffix}-all-fold{fold}.all.csv",
                    index=False)
                print(f"   INFO:saved: {output_dir}/{dataset}/{dataset}-train{output_suffix}-all-fold{fold}.all.csv")
            else:
                print(
                    f"   INFO: NOT SAVING: {output_dir}/{dataset}/{dataset}-train{output_suffix}-all-fold{fold}.all.csv")


def make_best_param_file(input_dir, output_dir, dataset, group_by, fold=0,
                         measures=['euc', 'dtwf', 'dtwr', 'ddtwf', 'ddtwr',
                                   'wdtw', 'wddtw', 'lcss', 'msm', 'erp', 'twe'],
                         input_suffix="",
                         output_suffix="",
                         copy_best_query_data=True,
                         matching_key='iterationKey',
                         verify=True,
                         tie_break=True,
                         random_state=None,
                         ):
    '''
    For a given dataset and dependency group; filter, group and aggregate *.train.all.csv to make one of the {-i,-d,-id,-b} *.train.best.csv file

        can apply additional filters on the *.all file
        copies *.query data of best experiments to output_dir
    '''
    print(f"{dataset}: make_best_param_file {group_by}")

    df_all = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-train{input_suffix}-all-fold{fold}.all.csv")

    # detect grouping method for dependency
    if group_by == "i":
        dependency = [False]
    elif group_by == "d":
        dependency = [True]
    elif group_by == "id":
        dependency = [False, True]
    elif group_by == "b":
        dependency = [False, True]
    else:
        raise Exception(f"ERROR: {dataset}, unknown group_by method: {group_by}")

    # # filters the results, selects rows where all dimensions are used
    # no_dimensions = df_all.iloc[0]['dimensions']
    # if group_by == "b":
    #     df_all = df_all[(df_all['noDims'] == no_dimensions) &
    #                     (df_all['name'].isin(measures))]
    # else:
    #     df_all = df_all[(df_all['noDims'] == no_dimensions) &
    #                     (df_all['useDependentDims'].isin(dependency)) &
    #                     (df_all['name'].isin(measures))]
    # filters the results, selects rows where all dimensions are used
    # no_dimensions = df_all.iloc[0]['dimensions']

    if group_by == "b":
        df_all = df_all[(df_all['name'].isin(measures))]
    else:
        df_all = df_all[(df_all['useDependentDims'].isin(dependency)) &
                        (df_all['name'].isin(measures))]

    # add any derived columns -- doing this before filtering to keep the final column order as defined below
    df_all['groupBy'] = group_by
    df_all['dependency'] = df_all['useDependentDims'].replace({True: 'd', False: 'i'})
    #     df_all['classifier'] = df_all['name'] + "_" + group_by
    df_all['classifier'] = df_all['name'] + "_" + df_all['dependency'] + "_" + df_all['groupBy']

    # filter and reorder the relevant columns
    df_all = df_all[
        ['iterationKey', 'dataset', 'classifier', 'trainAccuracy', 'name', 'dependency', 'groupBy', 'measure',
         'noDims', 'useDependentDims', 'dimensionsToUse', 'paramID', 'trainTime', 'seed', 'lpIndep', 'lpDep',
         'normalize',
         'expPath', 'expFile', 'queryFile', 'countFile']]
    df_all.reset_index(inplace=True, drop=False)
    df_all.rename(columns={"index": "index_all"}, inplace=True)

    # verify that all paramIDs have been explored for each group (dependency, measure)
    if verify:
        for m in measures:
            for d in dependency:
                filtered_df = df_all[(df_all['useDependentDims'] == d) & (df_all['name'] == m)]
                tested_params = filtered_df['paramID'].unique()

                if m in ['euc', 'dtwf', 'ddtwf'] and len(tested_params) != 1:
                    cprint(f"  WARN {dataset}: {len(tested_params)}/1 paramID was found for: {m}, dep: {d}", 'yellow')
                elif m not in ['euc', 'dtwf', 'ddtwf'] and len(tested_params) != 100:
                    cprint(f"  WARN {dataset}: {len(tested_params)}/100 paramID was found for: {m}, dep: {d}", 'yellow')

    # apply the groupby and aggregations
    if group_by == "b":
        grp = df_all.groupby(['dataset', 'name', 'noDims'])
    else:
        grp = df_all.groupby(['dataset', 'name', 'noDims', 'useDependentDims'])

    # find max accuracy per group WITH random tie break if specified
    def idxmax(group, tie_break=True, n=1, random_state=None):
        if tie_break:
            max_value = group.max()  # find the max
            max_rows = group[group == max_value]  # filter all values that matches the max
            max_rand_sample = max_rows.sample(n=n, random_state=random_state)  # sample one of the max
            #     return max_rand_sample.values[0] # return the max value as float
            return max_rand_sample.index.values[0]  # return the max id as int
        else:
            return group.idxmax()  # selects the index of the first max

    max_indices = grp['trainAccuracy'].apply(idxmax, tie_break=tie_break, n=1, random_state=random_state)
    df_max_accuracy = df_all.iloc[max_indices]

    df_max_accuracy.reset_index(inplace=True, drop=True)

    if verify:
        # verify that all measures are present in the experiment files
        if group_by == "i" and df_max_accuracy.shape[0] != 11:
            print(
                f"  WARN {dataset}: 11 measures not found in the experiments for output {group_by}: {df_max_accuracy.shape[0]}")
        elif group_by == "d" and df_max_accuracy.shape[0] != 11:
            print(
                f"  WARN {dataset}: 11 measures not found in the experiments for output {group_by}: {df_max_accuracy.shape[0]}")
        elif group_by == "id" and df_max_accuracy.shape[0] != 22:
            print(
                f"  WARN {dataset}: 22 measures not found in the experiments for output {group_by}: {df_max_accuracy.shape[0]}")
        elif group_by == "b" and df_max_accuracy.shape[0] != 11:
            print(
                f"  WARN {dataset}: 11 measures not found in the experiments for output {group_by}: {df_max_accuracy.shape[0]}")

    if output_suffix is not None:
        df_max_accuracy.to_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-train-{group_by}-fold{fold}.best.exp.csv",
                               index=True)
        print(f"   INFO:saved: {output_dir}/fold{fold}/{dataset}/{dataset}-train-{group_by}-fold{fold}.best.exp.csv")

    # memory and time intensive operation - move to another function
    if copy_best_query_data:

        # query file copying
        df_list = []
        for i in range(df_max_accuracy.shape[0]):
            query_file = df_max_accuracy.iloc[i]['queryFile']
            query_file_fullname = f"{input_dir}/fold{fold}/{dataset}/{query_file}"
            df_query = pd.read_csv(query_file_fullname, index_col=0)

            #             print(f"{i} : {df_max_accuracy.iloc[i][matching_key]}") # generates warning for Libras #b1e5b8df

            df_query_filtered = df_query[
                df_query[matching_key].astype('str') == str(df_max_accuracy.iloc[i][matching_key])].copy()
            df_list.append(df_query_filtered)

        df_query = pd.concat(df_list)
        df_query.to_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-train-{group_by}-fold{fold}.best.query.csv",
                        index=True)
        print(f"   INFO:saved: {output_dir}/fold{fold}/{dataset}/{dataset}-train-{group_by}-fold{fold}.best.query.csv")

        # count file copying
        df_list = []
        for i in range(df_max_accuracy.shape[0]):
            query_file = df_max_accuracy.iloc[i]['countFile']
            query_file_fullname = f"{input_dir}/fold{fold}/{dataset}/{query_file}"
            df_query = pd.read_csv(query_file_fullname, index_col=False)

            #             print(f"{i} : {df_max_accuracy.iloc[i][matching_key]} = {df_max_accuracy.iloc[i]['classifier']}")

            df_query_filtered = df_query[
                df_query[matching_key].astype('str') == str(df_max_accuracy.iloc[i][matching_key])].copy()
            df_list.append(df_query_filtered)

        df_query = pd.concat(df_list)
        df_query.to_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-train-{group_by}-fold{fold}.best.count.csv",
                        index=True)
        print(f"   INFO:saved: {output_dir}/fold{fold}/{dataset}/{dataset}-train-{group_by}-fold{fold}.best.count.csv")

    return df_max_accuracy


def make_best_param_files(input_dir, output_dir, datasets,
                          folds=list(range(0, 10)),
                          measures=['euc', 'dtwf', 'dtwr', 'ddtwf', 'ddtwr',
                                    'wdtw', 'wddtw', 'lcss', 'msm', 'erp', 'twe'],
                          input_suffix="",
                          output_suffix="",
                          copy_best_query_data=True,
                          verify=True,
                          tie_break=True,
                          random_state=None,
                          save_output=True
                          ):
    list_indep = []
    list_dep = []
    list_both = []
    list_best = []

    for i, dataset in enumerate(datasets):
        print(f"{i}: make_best_param_files : {dataset}")

        # combine_training_files(input_dir, output_dir, dataset, verify=True, folds=folds)

        for fold in folds:
            df_best_per_dataset_i = make_best_param_file(input_dir, output_dir, dataset, fold=fold, group_by="i",
                                                         measures=measures,
                                                         random_state=random_state, tie_break=True,
                                                         copy_best_query_data=True)
            df_best_per_dataset_d = make_best_param_file(input_dir, output_dir, dataset, fold=fold, group_by="d",
                                                         measures=measures,
                                                         random_state=random_state, tie_break=True,
                                                         copy_best_query_data=True)
            df_best_per_dataset_id = make_best_param_file(input_dir, output_dir, dataset, fold=fold, group_by="id",
                                                          measures=measures,
                                                          random_state=random_state, tie_break=True,
                                                          copy_best_query_data=True)
            df_best_per_dataset_b = make_best_param_file(input_dir, output_dir, dataset, fold=fold, group_by="b",
                                                         measures=measures,
                                                         random_state=random_state, tie_break=True,
                                                         copy_best_query_data=True)

            list_indep.append(df_best_per_dataset_i)
            list_dep.append(df_best_per_dataset_d)
            list_both.append(df_best_per_dataset_id)
            list_best.append(df_best_per_dataset_b)

    pd_best_i = pd.concat(list_indep)
    pd_best_d = pd.concat(list_dep)
    pd_best_id = pd.concat(list_both)
    pd_best_b = pd.concat(list_best)

    if save_output:
        pd_best_i.to_csv(f"{output_dir}/knn-i.train.all.best.csv", index=True)
        pd_best_d.to_csv(f"{output_dir}/knn-d.train.all.best.csv", index=True)
        pd_best_id.to_csv(f"{output_dir}/knn-id.train.all.best.csv", index=True)
        pd_best_b.to_csv(f"{output_dir}/knn-b.train.all.best.csv", index=True)

    return pd_best_i, pd_best_d, pd_best_b


def make_prob_pred_file(input_dir, output_dir, dataset, group_by, train_test = 'train', fold = 0,
                        output_suffix = "", redo_voting = False, random_state = None, use_copied_count_file=True):

    if train_test == 'train':
        full_input_file = f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-{group_by}-fold{fold}.best.exp.csv"
        matching_key = 'iterationKey'
        accuracy_key = 'trainAccuracy'
        class_columns_start_index = 6
    elif train_test == 'test':
        full_input_file = f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-{group_by}.test.exp.csv"
        matching_key = 'testKey'
        accuracy_key = 'testAccuracy'
        class_columns_start_index = 5

    # read experiment result file
    df_exp = pd.read_csv(full_input_file)
    #     display(df_exp.head())

    # for each experiment
    prob_dataframes = []
    df_prob = None
    sum_weights = 0;
    for i in range(df_exp.shape[0]):
        #         print(full_input_file)
        #         display(df_exp.head())
        #         print(df_exp.iloc[i]['name'])
        classifier = df_exp.iloc[i]['name'] + ('_d' if df_exp.iloc[i]['useDependentDims'] else'_i')
        #         print(f'classifier {i}: {classifier}')
        accuracy = df_exp.iloc[i][accuracy_key]
        sum_weights += accuracy

        if train_test == 'train':
            if (use_copied_count_file): # TODO debug - some keys missing from copied file 1.9.2020
                count_file = f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-{group_by}-fold{fold}.best.count.csv" # filtered and copied file
            else:
                full_count_filename = glob.glob(f"{input_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-{df_exp.iloc[i]['name']}-{group_by}-*.train.count.csv")
                print(full_count_filename)
                count_file = full_count_filename[0]
        elif train_test == 'test':
            dir_name, base_name = os.path.split(full_input_file)
            count_file = f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-{group_by}.test.count.csv"

        # print("reading count file: " + count_file + f" {classifier} for key: " + df_exp.iloc[i][matching_key])
        df_count = pd.read_csv(count_file, index_col=False)

        # ------------- VERIFICATION
        # count file should have 1 row per test set for each experiment
        # all keys in best file must match keys in count file




        # filter the rows we need
        key = str(df_exp.iloc[i][matching_key])
        df_count_per_classifier = df_count[df_count[matching_key].astype('str') == key].copy()
        if df_count_per_classifier.shape[0] == 0:
            display(df_count_per_classifier)
            display(df_count_per_classifier[matching_key].unique())
            raise Exception(f"ERROR: Failed to fetch rows for key {key} from {count_file}")
        #         display(df_count_per_classifier.head())
        #         print(df_query['iterationKey'].unique())
        #         print(df_exp.iloc[i])


        # one the first run, initialize the result df by making a deep copy of initial columns
        if i == 0:
            df_prob = df_count_per_classifier[['queryIndex', 'queryLabel']].copy()
            classes = list(df_count_per_classifier.columns[class_columns_start_index:]) # assumes class columns start from 5th column
            #             print(classes)
            df_prob['eePredictedLabel'] = np.nan
            df_prob['eeCorrect'] = np.nan
            df_prob['eeAccuracy'] = np.nan
            df_prob['classes'] = ",".join(classes)
            ee_classes = [c for c in classes]
            df_prob[ee_classes] = float(0)
            df_prob.set_index('queryIndex', inplace=True,drop=True)
            df_prob.sort_values(by='queryIndex')
            prob_dataframes.append(df_prob)
        #             display(df_prob)

        # TODO doesnt support revoting using nearestIndices
        if redo_voting:
            # overwrite predictedLabel column with new voting using the given random_seed and nearestIndices column
            pass

        #         print(df_count_per_key.dtypes)
        df_count_per_classifier[classes].astype('float')
        df_count_per_classifier[classes] = df_count_per_classifier[classes].div(df_count_per_classifier['numNeighbours'], axis=0)
        #         print(df_count_per_key.dtypes)

        # voting # TODO tie_break

        #         # find max accuracy per group WITH random tie break if specified
        #         def idxmax(group, tie_break = True, n = 1, random_state = None):
        #             if tie_break:
        #                 max_value = group.max() # find the max
        #                 max_rows = group[group == max_value] # filter all values that matches the max
        #                 max_rand_sample = max_rows.sample(n=n, random_state=random_state) # sample one of the max
        #             #     return max_rand_sample.values[0] # return the max value as float
        #                 return max_rand_sample.index.values[0] # return the max id as int
        #             else:
        #                 return group.idxmax() # selects the index of the first max
        #         max_indices = grp['trainAccuracy'].apply(idxmax, tie_break = tie_break, n = 1, random_state = random_state)
        #         df_max_accuracy = df_all.iloc[max_indices]

        maxProbLabel = df_count_per_classifier[classes].idxmax(axis=1)

        df_count_per_classifier = df_count_per_classifier[['queryIndex'] + classes] # remove extra columns+ ['numNeighbours']
        classifier_class_columns = {c:classifier +'_'+ str(c) for c in classes}
        df_count_per_classifier.rename(columns= classifier_class_columns, inplace=True) # classifier_class
        #         df_count_per_classifier.rename(columns={'numNeighbours':classifier + '_numNeighbours'}, inplace=True)
        df_count_per_classifier[classifier + '_' + accuracy_key] = accuracy
        df_count_per_classifier[classifier + '_predictedLabel'] = maxProbLabel
        df_count_per_classifier.set_index('queryIndex', drop=True, inplace=True)
        df_count_per_classifier.sort_values(by='queryIndex')
        #         display(df_count_per_classifier)

        #         print(df_prob.columns)
        #         print(classifier_class_columns)
        for k,classifier_class in classifier_class_columns.items():
            c = classifier_class.split("_")[-1]
            #             print(f"{c} == {classifier_class} = {accuracy} -> ")
            df_prob[c] = df_prob[c] + (df_count_per_classifier[classifier_class].mul(accuracy))

        #         print(f"{classes} == {classifier_class_columns.values()}")
        #         print(f"{df_prob[classes].columns} == {df_count_per_classifier[classifier_class_columns.values()].columns}")
        #         df_prob[classes] = df_prob[classes] + (df_count_per_classifier[classifier_class_columns.values()].mul(accuracy))
        #         df_prob[classes] = 0 + (df_count_per_classifier[classifier_class_columns.values()].mul(accuracy))
        #         df_prob[classes] = df_prob[classes] + (1 * 0.7)

        #         display(df_prob.head())
        #         display(df_count_per_classifier[classes].isna().values.any())
        prob_dataframes.append(df_count_per_classifier)


    # concatenate the dataframes
    df_prob = pd.concat(prob_dataframes, sort=False, axis=1, ignore_index=False)
    #     display(df_prob[classes])

    df_prob[classes] = df_prob[classes] / sum_weights
    # TODO tie_break
    df_prob['eePredictedLabel'] = df_prob[classes].idxmax(axis=1).astype('int')

    df_prob['eeCorrect'] = (df_prob['queryLabel'].astype('int') == df_prob['eePredictedLabel'].astype('int')).astype('int')
    df_prob['eeAccuracy'] = df_prob['eeCorrect'].sum() / df_prob.shape[0]

    if output_suffix is not None:
        df_prob.to_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-{group_by}-prob.{train_test}.prob.csv",index=True)
        cprint(f"   INFO:saved: {output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-{group_by}-prob.{train_test}.prob.csv", 'blue')

    return df_prob


def make_prob_pred_files(input_dir, output_dir, datasets, train_test = ['train', 'test'], groups = ["i", "d", "id", "b"]
                         , folds = list(range(0,1)), random_state = None):

    for t in train_test:
        for d in datasets:
            for f in folds:
                for g in groups:
                    try:
                        print(f"Generating EE prob vote for: {d}-{f}-{g}")
                        make_prob_pred_file(input_dir, output_dir, d,
                                            group_by=g, train_test = t, fold = f, random_state=random_state)
                    except Exception:
                        print("An exception occurred")
                        raise


def make_ee(input_dir, output_dir, dataset, train_test = 'test', fold = 0, voting_scheme='majority', random_state = None, output_suffix = ""):

    if train_test == 'train':
        extension = "train.best.exp"
    elif train_test == 'test':
        extension = "test.exp"

    df_best_i = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-i.{extension}.csv", index_col=0)
    df_best_d = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-d.{extension}.csv", index_col=0)
    df_best_id = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-id.{extension}.csv", index_col=0)
    df_best_b = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-b.{extension}.csv", index_col=0)
    #     display(df_best_i)
    #     display(df_best_d.shape)
    #     display(df_best_id.shape)
    #     display(df_best_b.shape)

    #     // verification?? check if measures are present in best files

    df_best = pd.concat([df_best_i, df_best_d, df_best_id, df_best_b])
    df_best = df_best.sort_values(by=['groupBy', 'dependency', 'name'])
    #     display(df_best)

    # TODO TEMP filter msm out
    #     print(' WARN: removing msm')
    #     df_best = df_best[df_best['name'] != 'msm'].copy()

    # pivot the table
    df_ee_stacked = df_best[['dataset','classifier', f'{train_test}Accuracy']].copy()
    #     df_ee_stacked = df_ee_stacked.set_index('dataset')
    df_ee_stacked = df_ee_stacked.reset_index(drop=True)

    # NOTE: we need a new unique column because classifier column is not unique when i, d is used with id - refer to value counts
    #     display(df_ee_stacked)
    #     print(df_ee_stacked['dataset'].value_counts())
    #     print(df_ee_stacked['classifier'].value_counts())
    #     display(df_ee_stacked.pivot(index=['dataset'], columns=['classifier'], values=f'{train_test}Accuracy'))

    df_ee = df_ee_stacked.pivot(index=['dataset'], columns=['classifier'], values=f'{train_test}Accuracy')
    df_ee.columns.name = None
    df_ee['accuracy'] = train_test
    #     display(df_ee)

    # reorder columns, move last column ['accuracy'] as the first column
    _measures_by_order = ['euc', 'dtwf', 'dtwr', 'ddtwf', 'ddtwr', 'wdtw', 'wddtw', 'lcss', 'msm', 'erp', 'twe']
    #     _measures = ['euc', 'dtwf', 'dtwr', 'ddtwf', 'ddtwr', 'wdtw', 'wddtw', 'lcss', 'erp', 'twe'] # TEMP no MSM
    _dependency_by_order = ['i', 'd']
    _groups_by_order = ['i', 'd', 'id', 'b']
    _columns_by_order = []
    _columns_by_order.append('accuracy')
    for g in _groups_by_order:
        for m in _measures_by_order:
            for d in _dependency_by_order:

                _columns_by_order.append(m + "_" + d + "_" + g)
    # filter _columns_by_order not in df_ee.columns
    _columns_by_order_filtered = [c for c in _columns_by_order if c in  df_ee.columns]
    _extra_columns = [ c for c in list(df_ee.columns) if c not in _columns_by_order_filtered]
    _new_column_order = _extra_columns + _columns_by_order_filtered
    #     print(_new_column_order)
    #     print(df_ee.columns)
    #     print([c for c in _new_column_order if c not in  df_ee.columns])
    df_ee = df_ee[_new_column_order]
    df_ee.reset_index(inplace=True)
    #     display(df_ee)

    # Assemble EE results
    if voting_scheme == 'majority':
        # pred_dataframes = make_majority_pred_files(input_dir, output_dir, dataset, train_test = [train_test], random_state=random_state)
        # df_correct = pred_dataframes[train_test + "-i"][1] # 0 = pred, 1 = correct
        # df_ee['ee_i'] = df_correct['accuracy']
        #
        # df_correct = pred_dataframes[train_test + "-d"][1] # 0 = pred, 1 = correct
        # df_ee['ee_d'] = df_correct['accuracy']
        #
        # df_correct = pred_dataframes[train_test + "-id"][1] # 0 = pred, 1 = correct
        # df_ee['ee_id'] = df_correct['accuracy']
        #
        # df_correct = pred_dataframes[train_test + "-b"][1] # 0 = pred, 1 = correct
        # df_ee['ee_b'] = df_correct['accuracy']

        pass

    elif voting_scheme == 'prob':

        df_prob = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-i-prob.{train_test}.prob.csv", index_col=0)
        df_ee['ee_i'] = df_prob['eeAccuracy']

        df_prob = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-d-prob.{train_test}.prob.csv", index_col=0)
        df_ee['ee_d'] = df_prob['eeAccuracy']

        df_prob = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-id-prob.{train_test}.prob.csv", index_col=0)
        df_ee['ee_id'] = df_prob['eeAccuracy']

        df_prob = pd.read_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{train_test}-b-prob.{train_test}.prob.csv", index_col=0)
        df_ee['ee_b'] = df_prob['eeAccuracy']
    else:
        raise Exception("Unknown voting scheme for EE")

    if (output_suffix is not None):
        df_ee.to_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{voting_scheme}{output_suffix}.{train_test}.ee.csv",index=False)
        df_ee_stacked.to_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{voting_scheme}-fold{fold}{output_suffix}-stacked.{train_test}.see.csv",index=False)
        cprint(f"   INFO:saved: {output_dir}/fold{fold}/{dataset}/{dataset}-{voting_scheme}-fold{fold}{output_suffix}.{train_test}.ee.csv", 'blue')

    return df_ee, df_ee_stacked


def make_ee_files(input_dir, output_dir, dataset, voting_scheme='majority', fold = 0, random_state = None, output_suffix = None):
    df_ee_train, df_ee_stacked_train  = make_ee(input_dir, output_dir, dataset, train_test = 'train', output_suffix="",
                                                voting_scheme=voting_scheme, random_state=random_state)
    df_ee_test, df_ee_stacked_test  = make_ee(input_dir, output_dir, dataset, train_test = 'test', output_suffix="",
                                              voting_scheme=voting_scheme, random_state=random_state)

    df_ee_stacked = df_ee_stacked_train.merge(df_ee_stacked_test, on=['dataset', 'classifier'], how='left')
    # df_ee_stacked.to_csv(f"{output_dir}/fold{fold}/{dataset}/{dataset}-{voting_scheme}-stacked.see.csv",index=False)
    return df_ee_train, df_ee_test, df_ee_stacked


def main(argv):
    print("Python Start: " + " ".join(argv))

    # # -seed=6463564
    # seed = argv[0]
    # results_dir = argv[1]
    # dataset = argv[2]
    #
    # df_all_per_exp = combine_results_into_one_dataframe(results_dir, dataset, output_file='-train')
    # df_best_per_dataset = find_best_experiments(results_dir, dataset, dependency=[True, False], output_file="-b",
    #                                             seed=seed)

    random_state = 6463564
    # inputdir = f"E:/git/experiments/knn/10-2-2022/m3/i1d2-norm/temp/subset/dim5/i1d2-norm/train/"
    # outputdir = f"E:/git/experiments/knn/10-2-2022/m3/i1d2-norm/temp/subset/dim5/i1d2-norm/test/"
    # datasets = get_datasets_in_path(f"E:/git/experiments/knn/10-2-2022/m3/i1d2-norm/train//fold0/")
    # datasets = ['BasicMotions', 'ERing', 'Handwriting']
    # datasets = ['BasicMotions', 'ERing']
    # datasets = ['BasicMotions']

    inputdir = f"E:/git/experiments/knn/10-2-2022/m3/i1d2+norm/train/"
    outputdir = f"E:/git/experiments/knn/10-2-2022/m3/i1d2+norm/test/"
    datasets = get_datasets_in_path(f"E:/git/experiments/knn/10-2-2022/m3/i1d2-norm/train//fold0/")

    # combine_training_files(inputdir, outputdir, datasets, output_suffix="", verify=True,
    #                        num_dimensions=None,
    #                        folds=["0"])

    # pd_best_i, pd_best_d, pd_best_b = make_best_param_files(inputdir, outputdir, datasets, folds=[0],
    #                                                         random_state=random_state, tie_break=True,
    #                                                         copy_best_query_data=True)

    make_prob_pred_files(inputdir, outputdir, datasets, train_test = ['test'],
                         groups = ["i", "d", "id", "b"], folds = [0], random_state=random_state)



    # for i, d in enumerate(datasets):
    #     try:
    #         print(f"{i} EE: {d}")
    #         _df_ee, _df_ee_stacked = make_ee(inputdir, outputdir, d, train_test = 'test', output_suffix="",
    #                                           voting_scheme='prob', fold = 0, random_state=random_state)
    #         print(_df_ee.shape)
    #         # display(_df_ee)
    #         print(_df_ee_stacked.shape)
    #         # display(_df_ee_stacked.head())
    #     except Exception as e:
    #         print(f"ERROR: while generating EE for {d} + {e}")
    #         raise

    print("Python End:--------------------------------------------")


if __name__ == "__main__":
    # sys.argv = "6463564 E:/git/TS-CHIEF-DEV/out/dotnet54.knn/e1/ BasicMotions".split();
    main(sys.argv)
