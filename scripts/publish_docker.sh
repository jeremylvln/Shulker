#!/bin/bash

set -xe

app="$1"
tags_path="$2"
labels_path="$3"

tags_params=()
while IFS= read -r tag; do
  tag_with_image="$(echo "${tag}" | sed "s/placeholder_app_name/${app}/g")"
  tags_params+=("--tag" "${tag_with_image}")
done < "$tags_path"

labels_params=()
while IFS= read -r label; do
  labels_params+=("--label" "${label}")
done < "$labels_path"

docker buildx build \
  --file "packages/${app}/Dockerfile" \
  --push \
  --platform linux/amd64,linux/arm64/v8 \
  "${tags_params[@]}" \
  "${labels_params[@]}" \
  .
