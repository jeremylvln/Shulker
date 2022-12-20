#!/bin/bash

set -xe

app="$1"
tags_path="$2"
labels_path="$3"

while IFS= read -r tag; do
  tag_with_image="$(echo "${tag}" | sed "s/placeholder_app_name/${app}/g")"
  tags_params="${tags_params} --tag ${tag_with_image}"
done < "$tags_path"
tags_params="$(echo "${tags_params}" | xargs)"

while IFS= read -r label; do
  labels_params="${labels_params} --label ${label}"
done < "$labels_path"
labels_params="$(echo "${labels_params}" | xargs)"

docker buildx build \
  --file "apps/${app}/Dockerfile" \
  --push \
  ${tags_params} \
  ${labels_params} \
  .
