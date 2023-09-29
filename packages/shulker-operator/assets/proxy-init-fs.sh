#!/bin/sh
set -euo pipefail
set -o xtrace

cp "${SHULKER_CONFIG_DIR}/probe-readiness.sh" "${PROXY_DATA_DIR}/probe-readiness.sh"
cat "${SHULKER_CONFIG_DIR}/server-icon.png" | base64 -d > "${PROXY_DATA_DIR}/server-icon.png"

if [ "${TYPE}" == "VELOCITY" ]; then
  cp "${SHULKER_CONFIG_DIR}/velocity-config.toml" "${PROXY_DATA_DIR}/velocity.toml"
  echo "dummy" > "${PROXY_DATA_DIR}/forwarding.secret"
else
  cp "${SHULKER_CONFIG_DIR}/bungeecord-config.yml" "${PROXY_DATA_DIR}/config.yml"
fi

mkdir -p "${PROXY_DATA_DIR}/plugins"
if [ "${TYPE}" == "VELOCITY" ]; then
  (cd "${PROXY_DATA_DIR}/plugins" && wget "${SHULKER_MAVEN_REPOSITORY}/io/shulkermc/shulker-proxy-agent/${SHULKER_PROXY_AGENT_VERSION}/shulker-proxy-agent-${SHULKER_PROXY_AGENT_VERSION}-velocity.jar")
else
  (cd "${PROXY_DATA_DIR}/plugins" && wget "${SHULKER_MAVEN_REPOSITORY}/io/shulkermc/shulker-proxy-agent/${SHULKER_PROXY_AGENT_VERSION}/shulker-proxy-agent-${SHULKER_PROXY_AGENT_VERSION}-bungeecord.jar")
fi

if [ ! -z "${PROXY_PLUGIN_URLS+x}" ]; then
  for plugin_url in ${PROXY_PLUGIN_URLS//;/ }; do
    (cd "${PROXY_DATA_DIR}/plugins" && wget "${plugin_url}")
  done
fi

if [ ! -z "${PROXY_PATCH_URLS+x}" ]; then
  for patch_url in ${PROXY_PATCH_URLS//;/ }; do
    (cd "${PROXY_DATA_DIR}" && wget "${patch_url}" -O - | tar -xzv)
  done
fi
