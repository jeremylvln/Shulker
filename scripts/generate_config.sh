#!/bin/bash

SCRIPTPATH="$(cd -- "$(dirname "$0")" >/dev/null 2>&1; pwd -P)"
ROOTDIR="$(realpath "${SCRIPTPATH}/..")"

controller-gen rbac:roleName=manager-role crd webhook paths="${ROOTDIR}/..." output:crd:artifacts:config=${ROOTDIR}/config/crd/bases
controller-gen object:headerFile="${SCRIPTPATH}/hack/boilerplate.go.txt" paths="${ROOTDIR}/..."
