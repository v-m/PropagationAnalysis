#!/bin/bash

for d in $@
do
    ( ./smf-stats $d )
done