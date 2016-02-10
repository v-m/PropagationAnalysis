# PropagationAnalysis

## Overview
Software graphs, mutation testing and propagation estimer tools.
This framework is split into four tools.
Those tools are used for my PhD research purposes.

## Prerequisites

To run programs in this framework: install **Java** and **Maven**.
Moreover, install git or svn in order to obtain projects *with tests*.
Note that this framework is intended to run on Linux operating systems.

### First step
Checkout ChangePropagation and package with maven (skip tests to speed up the package process):

```
$ git clone https://github.com/v-m/PropagationAnalysis.git
$ mvn package
```

(optional) Create eclipse projects:

```
$ mvn eclipse:eclipse
```

## General running

As this project is made of several entry points, a unique script is proposed to ease the running. To run any tool, just invoke ``./run`` in the root folder. Pass as a first parameter the the desired tool (pass no argument to list available tools). Then pass as a second parameter the desired function in the tool. For example, please read the rest of this file.

## Tools

### Simple Mutation Framework (smf)
Tool performing Java Software Mutations. This software produces new versions of a software, based on specific mutations operator.
Mutation operators are defined (extendable easily with new ones).

### Software Miner (softminer)
Tool used for producing Java Software graphs. Currently support Call Graphs including or not:
- Class Hierarchy Analysis (CHA);
- Global fields access;

It proposes a total of four types of different graphs. It is based on the Spoon library for producing exploring the code.

### Propagation Miner (pminer)

Tool used to compute some statistics regarding the produces mutants and the software graphs produced with smf and softminer.
Including: precisions, recall and f-scores.

### Software learning (softwearn)

Tools used to do some basic learning on propagation.

## Research Papers

This framework is used in the following papers:

- __Vincenzo Musco__, Martin Monperrus, Philippe Preux. An Experimental Protocol for Analyzing the Accuracy of Software Error Impact Analysis. Tenth IEEE/ACM International Workshop on Automation of Software Test, May 2015, Florence, Italy.

## How to use

All commands can take the ``-h`` or ``--help`` option which will give the list of possible options for the command.

### Working with projects

#### Create project

This framework work in its local folder. Thus the first step is to create create all folders and a configuration file used by the framework tools. Note the directory is built to work on the computer which created it. Some parameters should require manual updating if the user want to run some tools on a working directory that has been moved to another environment.

##### Start from a maven project
In a new terminal, checkout a project and switch to the desired version (here we consider the Apache Common Lang rev. 6965455):

```
$ cd /tmp
$ git clone http://git-wip-us.apache.org/repos/asf/commons-lang.git
$ cd commons-lang
$ git reset --hard 6965455
```

Create a new project

```
$ ./run smf newproject /tmp/myproject commons-lang /tmp/commons-lang/
```

##### Start from a maven project - old Java version

If the project require an old version of Java, you need to install the required jre and run the command using `--compliance <nr>` and `--jre <path_to_bin>`. As an example, if my project require Java 1.6 and I have installed the jre in `/usr/lib/jvm/java-6-jre/jre`, i can use (note the addition of `bin` to the jre path!):

```
$ ./run smf newproject /tmp/myproject commons-lang /tmp/commons-lang --compliance 6 --jre /usr/lib/jvm/java-6-jre/jre/bin/
```

##### Start from any other project

If the classpath dependencies cannot be automatized using maven, you can manually specify the dependencies requires to build and run the project. Let imagine we checkout a non maven project in `/tmp/asoftware`. This project structure is:

- `/tmp/asoftware`: our software
 - `source`: folder containing sources files
 - `tests`: folder containing tests files
 - `lib`: folder containing external requires jars
  - `dep1.jar`: a first dependency
  - `dep2.jar`: a second dependency
 - `ress`: folder containing resources for testing

A working project can be created using:

```
./run smf newproject /tmp/myproject software_name /tmp/asoftware -s source -t tests -c /tmp/asoftware/lib/dep1.jar:/tmp/asoftware/lib/dep2.jar -r ress --compliance 6 --jre /usr/lib/jvm/java-6-jre/jre/bin/
```
Using the `-c` flag will copy all the dependencies in the working directory. To link external dependencies without copying it use `-C` instead. Moreover, if there is no dependencies, use the `-no-classpath` flag to skip any class path definition.

#### Create mutants

Choose and create a mutation for a specific operator:

```
$ ./run smf createmutation -o
$ ./run smf createmutation /tmp/myproject ABS
```

#### Run tests on mutants

Simply use:

```
$ ./run smf runmutants /tmp/myproject/mutations/main/ABS/
```

### Generate graphs

This tool can simply use projects created with smf to generate graphs from it. To do so, just invoke:

```
$ ./run softminer smfgengraph /tmp/myproject/
```

To generate other type of graphs and more options, just add the ``--help`` parameter to the program.

### Visualize the propagation on a graph

```
$ ./run pminer displayvisu mut_result/soft1/mutations/main/ABS/ mutant_52 mut_result/soft1/graph_cha.xml
```

Add the ``-c`` option to display the sets on terminal.

### Obtain propagation statistics

Let consider the following hierarchy:

- mut_result
 - soft1
   - mutations
     - main
        - ABS
          - mutations.xml
          - exec
            - mutant_52.xml
            - mutant_1045.xml
            - (...)
        - AOR
        - (...)
   - smf.run.xml
   - graph.xml
   - graph_cha.xml
 - soft2
   - (...)

#### Detailed performances for one project/mutation operator

To get the performances of mutation using the CHA graph for soft1 software and the ABS mutation operator:

```
./run pminer mutopperf mut_result/soft1/graph_cha.xml mut_result/soft1/mutations/main/ABS/mutations.xml
```

Following options are available:

- ``-a``: include also alive mutants (instead of considering only killed ones) in the analysis;
- ``-o``: exclude nulls from the medians calculation;
- ``-u``: include unbounded mutants in the computation (an unbounded mutant is a mutant which the mutation point occurs on a node which is not declared in the graph (can occurs in some methods not directly used such as ``compare``));
- ``-n <nb>``: filter out if more than <nb> mutants are present;
- ``-c <sep>``: export in csv format with <sep> separator.

#### Global performances for all projects mutation operators

To get the performances of mutation with and without CHA graph for all soft present in mut_result and all mutation operator:

```
./run pminer globalperf mut_result graph.xml:graph_cha.xml
```

If there is only the two softwares listed above the following command is equivalent:

```
./run pminer globalperf mut_result/soft1:mut_result/soft2 graph.xml:graph_cha.xml
```

Similar options than for ``mutopperf`` can be used. Moreover, the ``-v`` options allows to compute averages instead of medians.

## Use JavaPDG as a graph data source

Both commands (``mutopperf`` and ``globalperf``) accepts as option ``--javapdg`` (or ``-j``). Those enable to pass as an argument the path to a javapdg database. For ``globalperf``, pass the path to a folder containing subfolders for databases, one for each project (named similarly as the project folders itself). For ``mutopperf``, pass the path to the software database.

## Import Steimann dataset

We used Steimann dataset as part of our research. Here is an easy way to convert [Steimann data](http://www.feu.de/ps/prjs/EzUnit/eval/ISSTA13/) into data usable by PropagationAnalysis tools. Here is an execution example for Jester:

```
$ wget http://www.feu.de/ps/prjs/EzUnit/eval/ISSTA13/matrices/Jester1.37b%20\(+tests\).7z -O jester.7z
$ 7z x jester.7z
$ mv Jester1.37b\ \(+tests\)/ jester
$ ./run smf importsteimann /tmp/mysoft /tmp/jester/reference.txt /tmp/jester/1-fault/ jparsec
```
## Fault Localization

To do fault localization with Steimann dataset:

```
./run pminer faultloc --graph --steimann-dataset /tmp/mysoft/smf/mutations/main/STEIMANN-1/mutations.xml /tmp/mysoft/graphs/cg_cha.xml
```
To obtain a new file with such values, use the stdout redirection (`>`).
Parameter `--graph` compute also the score using graphs alone.
Parameter `--steimann-dataset` convert the input data to conform with steimann signatures which are not the sames as ones used in PropagationAnalysis.
Use `-h` to list all possible options. However, note that this is the expected output of [script](scripts/README.md) related to this part.

### With learning

To apply learning on top of fault localization (learn on a part of the dataset to change the graph weight), use:

```
./run softwearn faultloc --graph --steimann-dataset /tmp/mysoft/smf/mutations/main/STEIMANN-1/mutations.xml /tmp/mysoft/graphs/cg_cha.xml
```

## Dependencies

 - junit
 - commons-cli
 - commons-io
 - jdom 2
 - log4j
 - spoon


## Contact

See: http://www.vmusco.com or http://www.vincenzomusco.com
