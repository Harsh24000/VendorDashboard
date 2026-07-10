#!/bin/bash
# Compiles the app and the tests into ./out
set -e
rm -rf out
mkdir out
echo "Compiling main sources..."
javac -d out $(find src -name "*.java")
echo "Compiling tests..."
javac -cp out -d out $(find test -name "*.java")
echo "Build OK -> ./out"
