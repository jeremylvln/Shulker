#!/bin/bash

CRDS_IN=../../config/crd/bases
CRDS_OUT=./src/main/java

mkdir -p $CRDS_OUT
CRDS=$(find $CRDS_IN -type f -name "*.yaml")

function generateCrd() {
  CRD_PATH="$1"
  CRD_FILE_NAME=$(basename $CRD_PATH)

  docker run \
    --rm \
    -v "$(realpath $CRD_PATH)":"/tmp/${CRD_FILE_NAME}" \
    -v "$(realpath $CRDS_OUT)":"/tmp/out" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --network host \
    docker.pkg.github.com/kubernetes-client/java/crd-model-gen:v1.0.6 \
    /generate.sh \
    -u /tmp/${CRD_FILE_NAME} \
    -n io.shulkermc \
    -p io.shulkermc \
    -o "/tmp/out"
}

for crd in $CRDS; do
  generateCrd $crd
done
