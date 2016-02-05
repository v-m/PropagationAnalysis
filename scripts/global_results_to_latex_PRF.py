#!/bin/python

#Generate one document showing precision/recall/fscores in a latex table.
#Pass as arguments CSV files generated using 'pminer-global-perf' command with -c ";" option.
#Can take as many files as required but ensure that there are only five graphs to avoid bugs (indeed, this script is made to handle 5 graphs).
#Files can be softminer graph based or java pdg based (-j option)
#
#Autor: Vincenzo Musco (http://www.vmusco.com)

import sys
import utils

#_NAMEPROJ = 0
#_GRAPHTYP = 1
#_OPERATOR = 2

_TYPE = "med"   #Can be med (median) or avg (average)

#Columns are identified starting from the 4rd column (do -3 to remove the three columns above)
display = [-1,4,5,0,2,3,11,12,13]

if len(sys.argv) < 2:
    print("Please specify a csv file as parameter.")
    sys.exit(1)

items = utils.parseGlobalCsvFiles(sys.argv[1:], type=_TYPE)

header = open(".templates/page2.tex", "r")

#Print the header
for l in header:
    sys.stdout.write(l);

sys.stdout.write("\\begin{tabular}{@{}lrr");

for i in range(0,len(items)):
    sys.stdout.write("ccccccccc");

sys.stdout.write("@{}}\n\\toprule\n&&");

for gt in sorted(items.keys()):
    sys.stdout.write("& \multicolumn{8}{c}{%s} &"%gt)

sys.stdout.write("\\\\\n")

i=4
for gt in sorted(items.keys()):
    sys.stdout.write("\cmidrule{%d-%d}\n"%(i, i+7))
    i = i + 9

# Print items

lastproj = None

sys.stdout.write("Project & Ope. &\phantom{}")
for gt in sorted(items.keys()):
    sys.stdout.write("& N & E & K & U & I & P & R & F & \phantom{}")

sys.stdout.write("\\\\\n")



k = list(items)[0]

for kk in sorted(items[k].keys()):
    print("\t\t\t\\midrule")
    for kkk in sorted(items[k][kk]):
        sys.stdout.write("\t\t\t%s & %s "%(utils.shortToFullProjectName(kk) if lastproj != kk else "", kkk))
        lastproj = kk

        for k in sorted(items.keys()):
            line = items[k][kk][kkk]
            for pos in display:
                if pos == -1:
                    sys.stdout.write("& ")
                else:
                    if "." in line[pos]:
                        val = float(line[pos])
                        sys.stdout.write("& %.2f "%val)
                    else:
                        val = int(line[pos])
                        sys.stdout.write("& %d "%val)

        print("\\\\")





sys.stdout.write("\\bottomrule\n\\end{tabular}\n\\end{table}\n\\end{document}")
