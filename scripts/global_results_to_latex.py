#!/bin/python

#Generate n documents (one for each graph type) showing scores in latex tables.
#Pass as arguments CSV files generated using 'pminer-global-perf' command with -c ";" option.
#Can take as many files as required. Files can be softminer graph based or java pdg based (-j option)
#
#Autor: Vincenzo Musco (http://www.vmusco.com)

import sys
import utils

#_NAMEPROJ = 0
#_GRAPHTYP = 1
# _TYPE = 2
#_OPERATOR = 3

#_NBMUTANT = 4      0
#_NBALIVES = 5      1
#_NBUNBOUN = 6      2
#_NBISOLAT = 7      3

# _NBNODES = 8      4
# _NBEDGES = 9      5

#_CIS      = 10      6
#_AIS      = 11      7
#_CAIS     = 12      8
#_FPIS     = 13     9
#_DIS      = 14     10
#_prec     = 15     11
#_recall   = 16     12
#_fscore   = 17     13
#_S        = 18     14
#_C        = 19     15
#_O        = 20     16
#_U        = 21     17
#_D        = 22     18

display = [-1,0,1,2,-1,3,4,5,6,7,-1,8,9,10,-1,11,12,13,14,15]

if len(sys.argv) < 2:
    print("Please specify a csv file as parameter.")
    sys.exit(1)

items = utils.parseGlobalCsvFiles(sys.argv[1:], type="med")

header, body, footer = utils.parseLatexFile(".templates/page1.tex")

#Print the header
for l in header:
    sys.stdout.write(l)

# Print items
for k in sorted(items.keys()):
    proj = items[k]

    lastproj = None

    for l in body:
        if l[0] == "%" and l[1:-1] == "captionhere":
            print("\t\t\\caption{%s}"%k)
        elif l[0] == "%" and l[1:-1] == "tablehere":

            for kk in sorted(items[k].keys()):
                print("\t\t\t\\midrule")
                for kkk in sorted(items[k][kk]):
                    line = items[k][kk][kkk]

                    sys.stdout.write("\t\t\t%s & %s "%(utils.shortToFullProjectName(kk) if lastproj != kk else "", kkk))
                    lastproj = kk

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
        else:
            sys.stdout.write(l)

#Print the footer
for l in footer:
    sys.stdout.write(l)
