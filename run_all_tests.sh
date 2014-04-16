#!/bin/bash
set -e -x
rm -rf target
grails clean
grails compile
grails maven-install
grails test-app :integration
grails test-app :cli
cd testapp
./run_test_app.sh