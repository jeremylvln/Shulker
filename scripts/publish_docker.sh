#!/bin/bash

app="$1"
tags="$2"
labels="$3"

tags="$(echo "${tags}" | sed "s/shulker_app/${app}/g")"
if [ "${tags}" != "" ]; then
  tags_params="--tag $(echo "${tags}" | sed "s/;;;/ --tag /g")"
fi

if [ "${labels}" != "" ]; then
  labels_params="--label $(echo "${labels}" | sed "s/;;;/ --label /g")"
fi

docker build \
  --file "apps/${app}/Dockerfile" \
  --push \
  ${tags_params} \
  ${labels_params} \
  .
