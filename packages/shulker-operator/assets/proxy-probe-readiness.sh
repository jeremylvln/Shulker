#!/bin/sh
set -euo pipefail
set -o xtrace

if [ -f "/tmp/drain-lock" ]; then
  echo "Drain lock found" && exit 1
elif [ -f "/tmp/readiness-lock" ]; then
  echo "Readiness lock found" && exit 1
fi

bash /usr/bin/health.sh
