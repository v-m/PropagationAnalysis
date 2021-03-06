#! /usr/bin/env python

import sys
import csv
import os

fallback = True
random = True
metric = "avg_we" # avg_we, zero or one
sep = ";" #use & for LaTeX
eol = "" #use \\ for LaTeX
summary = False
intersect = None #"softwearn.csv" # None for no intersection

softwares = ["codec", "lang", "daikon", "draw2d", "eventbus", "htmlparser", "jaxen", "jester", "jexel", "jparsec"]

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

def guessTheSoft(filepath):
    for part in filepath.split("/"):
        for soft in softwares:
            if part == soft:
                return soft

    return "?"


def analyzeProject(head, lines):
    header = None
    islearning = None

    nbmax = -1
    nbshift = -1
    accumulators = []
    iszero = []
    isone = []
    predictime = 0
    learntime = 0

    nb = 0

    #header
    islearning = (not (len(head)==17))

    nbshift = 8 if islearning else 5
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

         if islearning:
             predictime = predictime + int(row[6])
             if learntime <= 0:
                 learntime = int(row[4])
         else:
             predictime = predictime + int(row[4])

         c = -1;
         for col in row[nbshift:]:
            c=c+1

            value = int(col)
            if(value == -1):
                continue

            accumulators[c] = accumulators[c] + value
            if(value == 0):
                iszero[c] = iszero[c] + 1

            if(value == 1):
                isone[c] = isone[c] + 1

    c = 0
    ret = {"max": nbmax, "considered": nb, "avg_predict": predictime/nb, "avg_learn": -1, "totaltime": predictime}

    if islearning:
        # Time per fold - divided by 10 as there is 10-fold !
        ret["avg_learn"] = learntime / 10
        ret["totaltime"] = ret["totaltime"] + (learntime * 10)

    for entry in header:
        ret[entry] = {
            "nb": nb,
            "avg_we": float(accumulators[c])/nb,
            "zero": iszero[c],
            "one": isone[c]
        }

        c=c+1

    return ret

mystring = "proband"
if(not summary):
    mystring = "%s%s%s%s%s"%(mystring, sep, "considered", sep, "discared")
    mystring = "%s%s%s"%(mystring, sep, "Avg Predict")
    mystring = "%s%s%s"%(mystring, sep, "Tot Learning")
    mystring = "%s%s%s"%(mystring, sep, "Total time")
    mystring = "%s%s%s%s%s"%(mystring, sep, "#inter", sep, "Graph")

if(random):
    if(not summary):
        mystring = "%s%s%s"%(mystring, sep, "Random")
    mystring = "%s%s%s"%(mystring, sep, "Random")

if(not summary):
    mystring = "%s%s%s"%(mystring, sep, "Tarantula")
mystring = "%s%s%s"%(mystring, sep, "Tarantula")

if(not summary):
    mystring = "%s%s%s"%(mystring, sep, "Ochiai")
mystring = "%s%s%s"%(mystring, sep, "Ochiai")

if(not summary):
    mystring = "%s%s%s"%(mystring, sep, "Zoltar")
mystring = "%s%s%s"%(mystring, sep, "Zoltar")

if(not summary):
    mystring = "%s%s%s"%(mystring, sep, "Naish")
mystring = "%s%s%s"%(mystring, sep, "Naish")
if(not summary):
    mystring = "%s%s%s"%(mystring, sep, "Steimann")
mystring = "%s%s%s"%(mystring, sep, "Steimann")
mystring = "%s%s%s"%(mystring, sep, "Vautrin")

print("%s%s"%(mystring, eol))

if(not summary):
    mystring = "%s%s%s%s%s"%("", sep, "", sep, "")
    mystring = "%s%s%s%s%s"%(mystring, sep, "", sep, "")
    mystring = "%s%s%s"%(mystring, sep, "")
    mystring = "%s%s%s"%(mystring, sep, "")
    mystring = "%s%s%s"%(mystring, sep, "")
    if(random):
        mystring = "%s%s%s%s%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%s%s%s%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%s%s%s%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%s%s%s%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%s%s%s%s"%(mystring, sep, "Vanilla", sep, "Graph")
    mystring = "%s%s%s%s%s"%(mystring, sep, "Vanilla", sep, "Graph")
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

    mystring="%s"%guessTheSoft(afile)

    if(not summary):
        mystring = "%s%s%d%s%d"%(mystring, sep, analyzed["considered"], sep, totaldiscared)
        mystring = "%s%s%.2f"%(mystring, sep, analyzed["avg_predict"])
        mystring = "%s%s%.2f"%(mystring, sep, analyzed["avg_learn"])
        mystring = "%s%s%d"%(mystring, sep, analyzed["totaltime"])
        mystring = "%s%s%.2f%s%.2f"%(mystring, sep, analyzed["#inter"][metric], sep, analyzed["+WG"][metric])

    if(random):
        mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WR"][metric])

        if(not(summary)):
            mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WRG"][metric])

    mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WT"][metric])
    if(not summary):
        mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WTG"][metric])

    mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WO"][metric])
    if(not summary):
        mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WOG"][metric])



    mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WZ"][metric])
    if(not summary):
        mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WZG"][metric])

    mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WN"][metric])
    if(not summary):
        mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WNG"][metric])

    mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WT*"][metric])
    if(not summary):
        mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WT*G"][metric])

    mystring = "%s%s%.2f"%(mystring, sep, analyzed["+WT*G"][metric])
    print("%s%s"%(mystring, eol))
