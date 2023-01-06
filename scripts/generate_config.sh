#!/bin/bash

set -xe

SCRIPTPATH="$(cd -- "$(dirname "$0")" >/dev/null 2>&1; pwd -P)"
ROOTDIR="$(realpath "${SCRIPTPATH}/..")"

# controller-gen rbac:roleName=shulker-operator-role crd webhook paths="${ROOTDIR}/..." output:crd:artifacts:config=${ROOTDIR}/config/crd/bases
controller-gen crd webhook paths="${ROOTDIR}/..." output:crd:artifacts:config=${ROOTDIR}/config/crd/bases

controller-gen object:headerFile="${SCRIPTPATH}/hack/boilerplate.go.txt" paths="${ROOTDIR}/..."
