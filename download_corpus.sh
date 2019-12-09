#!/bin/bash

mkdir -p "./data/corpora/webis-qspell-17"
pushd "./data/corpora/webis-qspell-17"

wget -O "webis-qspell-17.zip" https://zenodo.org/record/3256201/files/corpus-webis-qspell-17.zip?download=1
unzip -uq "webis-qspell-17.zip"
rm "webis-qspell-17.zip"

popd