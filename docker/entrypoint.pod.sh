#!/bin/sh

set -x

if [ -z "$SHULKER_TEMPLATE_URL" ]; then
  echo "Missing SHULKER_TEMPLATE_URL environment variable" >&2
  exit 1
fi

curl --silent $SHULKER_TEMPLATE_URL | tar -xz
echo "eula=true" > eula.txt

java -jar server.jar
