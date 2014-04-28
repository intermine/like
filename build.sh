#!/bin/bash

set -e

# Clean previous artifacts
rm -rf build lib like.jar

# Create a list java files to compile.
find src -name '*.java' > sources.list
find $HOME/git/intermine/flymine/dbmodel/build/gen/src -name '*.java' >> sources.list

mkdir build
mkdir lib

cp $HOME/git/intermine/intermine/objectstore/main/dist/intermine-objectstore.jar \
   $HOME/git/intermine/intermine/pathquery/main/dist/intermine-pathquery.jar \
   $HOME/git/intermine/intermine/objectstore/main/lib/* \
   $HOME/git/intermine/intermine/api/main/dist/intermine-api.jar \
   lib

JAR_FILES=$(find lib -type f)

javac -d build -cp 'lib/*' @sources.list

cp -r resources/* build/
cp -r lib build

echo -n Class-Path: >> build/Manifest.txt
for jar in $JAR_FILES; do
  echo " $jar " >> build/Manifest.txt
done
echo Main-Class: org.intermine.like.precalculation.tasks/Precalculate >> build/Manifest.txt

cd build
jar -cfm ../like.jar Manifest.txt .
