# Scripts folder

This folder contains scripts used to compute statistics on results produced with programs in the framework.
Those results are intended to be embedded in scientific papers.

## Fault Localization

Here is how the fault localization scores table are generated:

* Check out the dataset (steimann one);
* Compute the set sizes using (the primer without learning the latter with - run those commands from the `root` folder):

```
$ ./run pminer faultloc --graph --steimann-dataset <path_to_project>/smf/mutations/main/STEIMANN-1/mutations.xml <path_to_project>/graphs/cg_cha.xml > <path_to_project>/results/pminer.csv
$ ./run softwearn faultloc --graph --steimann-dataset <path_to_project>/smf/mutations/main/STEIMANN-1/mutations.xml <path_to_project>/graphs/cg_cha.xml > <path_to_project>/results/pminer.csv
```

* Compute the values for all projects using (run this command from the `scripts` folder):

```
$ ./faultloc.py $(ls <pathtoprojectsroot>/*/results/pminer.csv)
```


* If needed, change options in the script:
 * `fallback`: possible values are `True` or `False`. Defines whether the fallback values should be taken into consideration;
  * `metric`: possible values are `avg_we`, `zero` and `one`. Set the computed metric: the first one return the average wasted effort size. The second and last return the number of wasted effort =0 and <=1 respectively;
  * `sep`: separator for values -- can be any character. Recommended values are : **;** for CSV and **&** for LaTeX table separator.
 * `eol`: end of line symbol -- can be any string. Recommended value are: (empty string) for CSV and **\\\\** for LaTeX tables.
 * `summary`: possible values are `avg_we`, `zero` and `one`. If True, only the vanilla metrics and the new metric is displayer, otherwise all metrics are displayed.
 * `intersect`: possible value is any string or `None`. If none, values are simply read and analyzed, otherwise, the file `intersect` is read in the same folder as the current one and an intersection is applied on input data before computing statistics. This option is used to take into consideration the fact that learning can produce less results.

* Import the data in a CSV compatible program (such as LibreOffice calc) or in a new LaTeX document (just put the generated code in a `	tabular` element). A nice terminal version can be obtained with:

```
./faultloc.py $(ls /home/vince/Experiments/steimann/*/results/softwearn.csv) | column -s ";" -t
```
