#!/bin/bash

SCRIPTPATH="$(cd -- "$(dirname "$0")" >/dev/null 2>&1; pwd -P)"
TOOLSDIR="${SCRIPTPATH}/.tools"
mkdir -p "${TOOLSDIR}"

## Tool Binaries
KUSTOMIZE="${TOOLSDIR}/kustomize"
KUSTOMIZE_VERSION="v3.8.7"
KUSTOMIZE_INSTALL_SCRIPT="https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh"

CONTROLLER_GEN="${TOOLSDIR}/controller-gen"
CONTROLLER_GEN_VERSION="v0.9.2"

ENVTEST="${TOOLSDIR}/setup-envtest"
ENVTEST_VERSION="latest"

if ! test -f "${KUSTOMIZE}"; then
  echo "[#] Downloading kustomize..."
  curl -Ss "${KUSTOMIZE_INSTALL_SCRIPT}" | bash -s -- "$(echo $KUSTOMIZE_VERSION | sed "s/v//")" "${TOOLSDIR}"
fi

if ! test -f "${CONTROLLER_GEN}"; then
  echo "[#] Downloading controller-gen..."
  GOBIN="${TOOLSDIR}" go install "sigs.k8s.io/controller-tools/cmd/controller-gen@${CONTROLLER_GEN_VERSION}"
fi

if ! test -f "${ENVTEST}"; then
  echo "[#] Downloading setup-envtest..."
  GOBIN="${TOOLSDIR}" go install "sigs.k8s.io/controller-runtime/tools/setup-envtest@${ENVTEST_VERSION}"
fi
