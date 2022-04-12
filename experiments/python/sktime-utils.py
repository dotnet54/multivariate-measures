import os
import itertools
import textwrap

import numpy as np
import pandas as pd
import sktime

from sktime.utils.data_processing import _make_column_names, from_long_to_nested

from sktime.utils.data_io import (
    load_from_tsfile_to_dataframe,
    write_dataframe_to_tsfile,
    make_multi_index_dataframe
)
from sktime.utils.data_processing import (
    is_nested_dataframe,
    from_nested_to_2d_array,
    from_2d_array_to_nested,
    from_nested_to_3d_numpy,
    from_3d_numpy_to_nested,
    from_nested_to_multi_index,
    from_multi_index_to_nested,
    from_multi_index_to_3d_numpy,
)



def write_dataframe_to_tsfile(
        data,
        path,
        problem_name="sample_data",
        file_suffix=".ts",
        timestamp=False,
        univariate=True,
        class_label=None,
        class_value_list=None,
        equal_length=False,
        series_length=-1,
        missing_values="NaN",
        comment=None,
):
    """
    Output a dataset in dataframe format to .ts file
    Parameters
    ----------
    data: pandas dataframe
        the dataset in a dataframe to be written as a ts file
        which must be of the structure specified in the documentation
        https://github.com/whackteachers/sktime/blob/master/examples/loading_data.ipynb
        index |   dim_0   |   dim_1   |    ...    |  dim_c-1
           0  | pd.Series | pd.Series | pd.Series | pd.Series
           1  | pd.Series | pd.Series | pd.Series | pd.Series
          ... |    ...    |    ...    |    ...    |    ...
           n  | pd.Series | pd.Series | pd.Series | pd.Series
    path: str
        The full path to output the ts file
    problem_name: str
        The problemName to print in the header of the ts file
        and also the name of the file.
    timestamp: {False, bool}, optional
        Indicate whether the data contains timestamps in the header.
    univariate: {True, bool}, optional
        Indicate whether the data is univariate or multivariate in the header.
        If univariate, only the first dimension will be written to file
    class_label: {list, None}, optional
        Provide class label to show the possible class values
        for classification problems in the header.
    class_value_list: {list/ndarray, []}, optional
        ndarray containing the class values for each case in classification problems
    equal_length: {False, bool}, optional
        Indicate whether each series has equal length. It only write to file if true.
    series_length: {-1, int}, optional
        Indicate each series length if they are of equal length.
        It only write to file if true.
    missing_values: {NaN, str}, optional
        Representation for missing value, default is NaN.
    comment: {None, str}, optional
        Comment text to be inserted before the header in a block.
    Returns
    -------
    None
    Notes
    -----
    This version currently does not support writing timestamp data.
    References
    ----------
    The code for writing series data into file is adopted from
    https://stackoverflow.com/questions/37877708/
    how-to-turn-a-pandas-dataframe-row-into-a-comma-separated-string
    """
    if class_value_list is None:
        class_value_list = []
    # ensure data provided is a dataframe
    if not isinstance(data, pd.DataFrame):
        raise ValueError("Data provided must be a DataFrame")
    # ensure number of cases is same as the class value list
    if len(data.index) != len(class_value_list) and len(class_value_list) > 0:
        raise IndexError(
            "The number of cases is not the same as the number of given " "class values"
        )

    if equal_length and series_length == -1:
        raise ValueError(
            "Please specify the series length for equal length time series data."
        )

    # create path if not exist
    dirt = f"{str(path)}/{str(problem_name)}/"
    try:
        os.makedirs(dirt)
    except os.error:
        pass  # raises os.error if path already exists

    # create ts file in the path
    file = open(f"{dirt}{str(problem_name)}{file_suffix}", "w")

    # write comment if any as a block at start of file
    if comment:
        file.write("\n# ".join(textwrap.wrap("# " + comment)))
        file.write("\n")
    # begin writing header information
    file.write(f"@problemName {problem_name}\n")
    file.write(f"@timeStamps {str(timestamp).lower()}\n")
    file.write(f"@univariate {str(univariate).lower()}\n")
    dim = data.iloc[0].shape[0]
    file.write(f"@dimensions {dim}\n")

    # write equal length or series length if provided
    if equal_length:
        file.write(f"@equalLength {str(equal_length).lower()}\n")
    if series_length > 0:
        file.write(f"@seriesLength {series_length}\n")

    # write class label line
    if class_label:
        space_separated_class_label = " ".join(str(label) for label in class_label)
        file.write(f"@classLabel true {space_separated_class_label}\n")
    else:
        file.write("@class_label false\n")

    # begin writing the core data for each case
    # which are the series and the class value list if there is any
    file.write("@data\n")
    for case, value in itertools.zip_longest(data.iterrows(), class_value_list):
        case_id = case[0]
        case_series = case[1]
        #         print(case_series.shape)
        #         print(case[1][5])
        #         for dimension in range(case_series.shape[0]):
        #             print(f'{dimension} : {case_series[dimension]}')
        #             print("----")
        #         print("=================")
        for dimension in range(case_series.shape[0]):
            # split the series observation into separate token
            # ignoring the header and index
            series = (
                case_series[dimension]
                    .to_string(index=False, header=False, na_rep=missing_values, float_format = lambda x: '%.6f' % x)
                    .split("\n")
            )
            # turn series into comma-separated row
            series = ",".join(str(obsv).strip() for obsv in series)
            file.write(str(series))
            # continue with another dimension for multivariate case
            if not univariate:
                file.write(":")
        if value is not None:
            file.write(f"{value}")  # write the case value if any
        file.write("\n")  # open a new line

    file.close()
#     print(f"Saved: {file.name}")

def test_write_dataframe_to_tsfile(
        dataset_name = "BasicMotions",
        DATA_PATH = "E:/data/Multivariate2018_ts/",
        OUT_PATH = "data/_transformed/"):

    X, y = load_from_tsfile_to_dataframe(os.path.join(DATA_PATH, f"{dataset_name}/{dataset_name}_TRAIN.ts"))
    length =  X.iloc[0].iloc[0].shape[0]

    # write output
    write_dataframe_to_tsfile(X, OUT_PATH + f"/_tmp/",
                              problem_name = dataset_name, file_suffix= "_TRAIN.ts",
                              timestamp=False, univariate=False,
                              class_label= np.unique(y).tolist(),
                              class_value_list=y,
                              equal_length=True, series_length= length,missing_values= "NaN",
                              comment=f" test output file")
