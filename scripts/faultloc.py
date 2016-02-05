#! /usr/bin/env python

import sys
import csv
import os

fallback = False
metric = "avg_we" # avg_we, zero or one
sep = ";" #use & for LaTeX
eol = ""# "\\\\" #use \\ for LaTeX
summary = False
intersect = None#"softwearn.csv" # None for no intersection

def readMap(afile, fb):
    amap = {}
    skip = 0
    header = None
    discared = 0

    with open(afile, 'r') as csvfile:
        csvreader = csv.reader(csvfile, delimiter=';', quotechar='"')

        for row in csvreader:
            if(len(row) == 0):
                continue

            if header == None:
                header = row
                continue

            if fb or (not fb and not isThereAtLeastOneNegative(row)):
                #if(not isThereAtLeastOneNegative(row)):
                    #print("already there %s"%row)
                #    pass
                #else:
                #    print("added %s"%row)
                key = row[2]
                amap[key] = row
            else:
                discared = discared+1

    return (header, amap, discared)

def intersectMaps(map1, map2):
    #removeFromMap = []
    ret = {}
    discared = 0

    for key in map1:
        if key in map2:
            if(not isThereAtLeastOneNegative(map1[key]) and not isThereAtLeastOneNegative(map2[key])):
                ret[key] = map1[key]
            else:
                discared = discared + 1
        else:
            discared = discared+1

    return (ret, discared)

def isThereAtLeastOneNegative(fields):
    for f in fields:
        if f == "-1":
            return True

    return False

def analyzeProject(head, lines):

    header = None
    islearning = None

    nbmax = -1
    nbshift = -1
    accumulators = []
    iszero = []
    isone = []

    nb = 0

    #header
    islearning = (not (len(head)==16))
    nbshift = 6 if islearning else 4
    header = head[nbshift:]

    for rowkey in lines:
         row = lines[rowkey]

         if(len(row)==0):
             continue

         if(len(accumulators) == 0):
             nbmax = int(row[1])
             for i in range(len(row) - nbshift):
                 accumulators.append(0)
                 iszero.append(0)
                 isone.append(0)

         nb = nb + 1
         c = -1;
         for col in row[nbshift:]:
            c=c+1

            value = int(col)
            if(value == -1):
                continue

            accumulators[c] = accumulators[c]+ value
            if(value == 0):
                iszero[c] = iszero[c] + 1

            if(value == 1):
                isone[c] = isone[c] + 1

    c = 0
    ret = {"max": nbmax, "considered": nb}

    for entry in header:
        ret[entry] = {
            "nb": nb,
            "avg_we": accumulators[c]/nb,
            "zero": iszero[c],
            "one": isone[c]
        }
        c=c+1

    return ret

mystring = "proband"
if(not summary):
    mystring = "%s%c%s%c%s"%(mystring, sep, "considered", sep, "discared")
    mystring = "%s%c%s%c%s"%(mystring, sep, "#inter", sep, "Graph")
    mystring = "%s%c%s"%(mystring, sep, "Tarantula")

mystring = "%s%c%s"%(mystring, sep, "Tarantula")
if(not summary):
    mystring = "%s%c%s"%(mystring, sep, "Ochiai")
mystring = "%s%c%s"%(mystring, sep, "Ochiai")
if(not summary):
    mystring = "%s%c%s"%(mystring, sep, "Naish")
mystring = "%s%c%s"%(mystring, sep, "Naish")
if(not summary):
    mystring = "%s%c%s"%(mystring, sep, "Tarantula*")
mystring = "%s%c%s"%(mystring, sep, "Tarantula*")
if(not summary):
    mystring = "%s%c%s"%(mystring, sep, "Zoltar")
mystring = "%s%c%s"%(mystring, sep, "Zoltar")

print("%s%s"%(mystring, eol))

if(not summary):
    mystring = "%s%c%s%c%s"%("", sep, "", sep, "")
    mystring = "%s%c%s%c%s"%(mystring, sep, "", sep, "")
    mystring = "%s%c%s%c%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%c%s%c%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%c%s%c%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%c%s%c%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%c%s%c%s"%(mystring, sep, "Vanilla", sep, "Graph")
    print("%s%s"%(mystring, eol))

for afile in sys.argv[1:]:
    if intersect is not None:
        anotherfile = ""
        parts = afile.split("/")
        i = 0
        for part in parts:
            if(i == len(parts) - 1):
                break
            anotherfile="%s/%s"%(anotherfile, part)
            i=i+1
        anotherfile="%s/%s"%(anotherfile, intersect)

        map1 = readMap(afile, fallback)
        map2 = readMap(anotherfile, fallback)
        mymap = intersectMaps(map1[1], map2[1])
        totaldiscared = map1[2]+mymap[1]
        analyzed = analyzeProject(map1[0], mymap[0])
    else:
        map1 = readMap(afile, fallback)
        totaldiscared = map1[2]
        analyzed = analyzeProject(map1[0], map1[1])

    mystring="%s"%afile.split("/")[5]

    if(not summary):
        mystring = "%s%c%d%c%d"%(mystring, sep, analyzed["considered"], sep, totaldiscared)
        mystring = "%s%c%.2f%c%.2f"%(mystring, sep, analyzed["#inter"][metric], sep, analyzed["+WG"][metric])

    mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WT"][metric])
    if(not summary):
        mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WTG"][metric])

    mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WO"][metric])
    if(not summary):
        mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WOG"][metric])

    mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WN"][metric])
    if(not summary):
        mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WNG"][metric])

    mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WT*"][metric])
    if(not summary):

        mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WT*G"][metric])
    mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WZ"][metric])
    if(not summary):
        mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WZG"][metric])

    if(summary):
        mystring = "%s%c%.2f"%(mystring, sep, analyzed["+WT*G"][metric])

    print("%s%s"%(mystring, eol))
