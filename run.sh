#!/bin/bash
# Runs the dashboard. Data (snapshot/CSV/log) is written to ./data
set -e
java -cp out com.festival.vendor.Main
