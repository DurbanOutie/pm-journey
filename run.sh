#! /bin/bash
java -verbose:gc -Xlog:gc* -cp build/classes:build/classes/lib/jar/sira.jar Main
