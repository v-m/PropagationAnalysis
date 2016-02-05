#!/bin/python

#Generate one document showing precision/recall/fscores in a latex table.
#Pass as arguments CSV files generated using 'pminer-global-perf' command with -c ";" option.
#Can take as many files as required but ensure that there are only five graphs to avoid bugs (indeed, this script is made to handle 5 graphs).
#Files can be softminer graph based or java pdg based (-j option)
#
#Autor: Vincenzo Musco (http://www.vmusco.com)

import sys
import utils
import cmath

#_NAMEPROJ = 0
#_GRAPHTYP = 1
#_OPERATOR = 2

#Columns are identified starting from the 4rd column (do -3 to remove the three columns above)
display = [-1,2,14,15,11,12,13]
cols = ["PDG", "CG", "CHA-CG", "F-CHA-CG"]

if len(sys.argv) < 2:
    print("Please specify a csv file as parameter.")
    sys.exit(1)

items = utils.parseGlobalCsvFiles(sys.argv[1:], type="med")

header = open(".templates/page2.tex", "r")

#Print the header
for l in header:
    sys.stdout.write(l);

sys.stdout.write("\n")

sys.stdout.write("\t\t\\begin{tabular}{@{}lcc");

for i in range(0,len(cols)):
    sys.stdout.write("ccccccc");

sys.stdout.write("@{}}\n\t\t\t\\toprule\n\t\t\t&&&");

for gt in cols:
    if gt != cols[0]:
        sys.stdout.write(" &")
    sys.stdout.write("& \multicolumn{6}{c}{%s}"%gt)

sys.stdout.write("\\\\\n")

i=5
for gt in cols:
    sys.stdout.write("\t\t\t\cmidrule{%d-%d}\n"%(i, i+5))
    i = i + 7

# Print items

lastproj = None

sys.stdout.write("\t\t\tProject & Ope. & K &\phantom{}")
for gt in cols:
    if gt != cols[0]:
        sys.stdout.write(" & \phantom{}")
    sys.stdout.write("& U & S & C & P & R & F")

sys.stdout.write("\\\\\n")



k = list(items)[0]

for kk in sorted(items[k].keys()):

    print("\t\t\t\\midrule")
    for kkk in sorted(items[k][kk]):
        killed = int(items[k][kk][kkk][0])

        sys.stdout.write("\t\t\t%s & %s & %d "%(utils.shortToFullProjectName(kk) if lastproj != kk else "", kkk, killed))

        lastproj = kk

        maxv = -1

        for k in cols:
            if float("%.2f"%float(items[k][kk][kkk][13])) > maxv:
                maxv = float("%.2f"%float(items[k][kk][kkk][13]))

        for k in cols:
            #print(">%s"%k)
            #print(">>%s"%kk)
            #print(">>>%s"%kkk)
            line = items[k][kk][kkk]
            #print(">>>> %s"%line)
            for pos in display:
                if pos == -1:
                    sys.stdout.write("& ")
                else:
                    if pos == 2:
                        line[pos] = int(line[pos]) + int(line[pos+1])
                        line[pos] = "%d"%line[pos]

                    if "." in line[pos]:
                        val = float("%.2f"%float(line[pos]))

                        if pos == 14 or pos == 15:
                            val = val * 100
                            sys.stdout.write("& %d\\%%"%val)
                        elif pos == 13 and maxv == val:
                            sys.stdout.write("& \\textbf{%.2f} "%val)
                        else:
                            sys.stdout.write("& %.2f "%val)
                    elif "NaN" in line[pos]:
                        sys.stdout.write("& ? ")
                    else:
                        val = int(line[pos])
                        sys.stdout.write("& %d "%val)

        print("\\\\")





sys.stdout.write("\t\t\t\\bottomrule\n\t\t\\end{tabular}\n\t\\end{table}\n\\end{document}")
