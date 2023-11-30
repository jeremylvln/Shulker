#!/bin/sh
set -euo pipefail
set -o xtrace

cp "${SHULKER_CONFIG_DIR}/probe-readiness.sh" "${SHULKER_PROXY_DATA_DIR}/probe-readiness.sh"
cat "${SHULKER_CONFIG_DIR}/server-icon.png" | base64 -d > "${SHULKER_PROXY_DATA_DIR}/server-icon.png"

if [ "${SHULKER_VERSION_CHANNEL}" == "Velocity" ]; then
  cp "${SHULKER_CONFIG_DIR}/velocity-config.toml" "${SHULKER_PROXY_DATA_DIR}/velocity.toml"
  echo "dummy" > "${SHULKER_PROXY_DATA_DIR}/forwarding.secret"
else
  cp "${SHULKER_CONFIG_DIR}/bungeecord-config.yml" "${SHULKER_PROXY_DATA_DIR}/config.yml"
fi

if [ ! -z "${SHULKER_PROXY_PLUGIN_URLS+x}" ]; then
  mkdir -p "${SHULKER_PROXY_DATA_DIR}/plugins"
  for plugin_url in ${SHULKER_PROXY_PLUGIN_URLS//;/ }; do
    (cd "${SHULKER_PROXY_DATA_DIR}/plugins" && wget "${plugin_url}")
  done
fi

if [ ! -z "${SHULKER_PROXY_PATCH_URLS+x}" ]; then
  for patch_url in ${SHULKER_PROXY_PATCH_URLS//;/ }; do
    (cd "${SHULKER_PROXY_DATA_DIR}" && wget "${patch_url}" -O - | tar -xzv)
  done
fi
