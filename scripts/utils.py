'''
Utility functions used for generating latex material

Autor: Vincenzo Musco (http://www.vmusco.com)
'''

def shortToFullProjectName(projrect):
    projrect = projrect.replace("ChangePropagation-dataset-", "")
    if "codec" in projrect:
        return "Codec"
    elif "collections" in projrect:
        return "Collections"
    elif "io" in projrect:
        return "IO"
    elif "lang" in projrect:
        return "Lang"
    elif "gson" in projrect:
        return "Gson"
    elif "jgit" in projrect:
        return "JGit"
    elif "joda" in projrect:
        return "JodaTime"
    elif "shindig" in projrect:
        return "Shindig"
    elif "sonar" in projrect:
        return "SonarQube"
    elif "spojo" in projrect:
        return "Spojo"
    else:
        return "???"

def parseGlobalCsvFiles(files, sep = ";", type = "avg"):
    items = dict()

    for fpath in files:
        f = open(fpath, "r")

        for line in f:
            line_parts = line.replace("\n","").split(sep)

            if type in line_parts[2]:
                continue;

            projrect = line_parts[0].replace("ChangePropagation.full-", "").replace("\"", "")

            graph = line_parts[1].replace("full-", "").replace("\"", "").replace("callgraph.xml", "CG").replace("callgraph_f.xml", "F-CG").replace("callgraph_cha.xml", "CHA-CG").replace("callgraph_f_cha.xml", "F-CHA-CG").replace("callgraph_m.xml", "M-CG")

            if "pdg" in graph:
                graph = "PDG"

            if projrect == "Project":
                continue

            operator = line_parts[3].replace("\"", "")

            if graph not in items:
                items[graph] = dict()

            if projrect not in items[graph]:
                items[graph][projrect] = dict()

            if operator not in items[graph][projrect]:
                items[graph][projrect][operator] = line_parts[4:]

    return items

def parseLatexFile(afile):
    ftex = open(afile, "r")

    header = list()
    footer = None
    body = list()
    phead, pfoot = -1, -1

    part = 0 # 0: header, 1: body, 2:footer
    cpt = 0
    for l in ftex:
        if phead == -1 and pfoot == -1:
            parts = l[1: -1].split(",")
            phead = int(parts[0])
            pfoot = int(parts[1])
        elif part == 0:
            header.append(l)
            cpt = cpt + 1
            if(cpt >= phead):
                part = 1
                cpt = 0
        else:
            body.append(l)

    footer = body[len(body) - pfoot:]
    body = body[:-1 * pfoot]

    return header, body, footer
