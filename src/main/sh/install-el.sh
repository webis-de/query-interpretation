#!/bin/bash

SCRIPT_PATH=$(dirname "$(realpath -s "$0")")
PROJECT_HOME=$(realpath "$SCRIPT_PATH/../../..")
DATA_PATH="$PROJECT_HOME/data/persistent"


mkdir -p "$DATA_PATH"
pushd "$DATA_PATH" || exit
if [ ! -d "$DATA_PATH/entity-commonness" ]
then
  wget -nc https://files.webis.de/wsdm22-query-interpretation-data/entity-commonness.zip
  unzip 'entity-commonness.zip'
fi

if [ ! -d "$DATA_PATH/wiki-entity-index" ]
then
  wget -nc https://files.webis.de/wsdm22-query-interpretation-data/wiki-entity-index.zip
  unzip 'wiki-entity-index.zip'
fi

rm -rf ./*.zip
popd || exit
