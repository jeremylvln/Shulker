#!/bin/sh
set -euo pipefail
set -o xtrace

cp "${SHULKER_CONFIG_DIR}/server.properties" "${SHULKER_SERVER_CONFIG_DIR}/server.properties"
if [ "${SHULKER_VERSION_CHANNEL}" == "Paper" ] || [ "${SHULKER_VERSION_CHANNEL}" == "Folia" ] || [ "${SHULKER_VERSION_CHANNEL}" == "Minestom" ]; then
  cp "${SHULKER_CONFIG_DIR}/bukkit-config.yml" "${SHULKER_SERVER_CONFIG_DIR}/bukkit.yml"
  cp "${SHULKER_CONFIG_DIR}/spigot-config.yml" "${SHULKER_SERVER_CONFIG_DIR}/spigot.yml"
  mkdir -p "${SHULKER_SERVER_CONFIG_DIR}/config"
  cp "${SHULKER_CONFIG_DIR}/paper-global-config.yml" "${SHULKER_SERVER_CONFIG_DIR}/config/paper-global.yml"
fi

if [ ! -z "${SERVER_WORLD_URL:-}" ]; then
  (cd "${SHULKER_SERVER_CONFIG_DIR}" && wget "${SERVER_WORLD_URL}" -O - | tar -xzv)
fi

if [ ! -z "${SHULKER_SERVER_PLUGIN_URLS:-}" ]; then
  mkdir -p "${SHULKER_SERVER_CONFIG_DIR}/plugins"
  for plugin_url in ${SHULKER_SERVER_PLUGIN_URLS//;/ }; do
    (cd "${SHULKER_SERVER_CONFIG_DIR}/plugins" && wget "${plugin_url}")
  done
fi

if [ ! -z "${SHULKER_SERVER_PATCH_URLS:-}" ]; then
  for patch_url in ${SHULKER_SERVER_PATCH_URLS//;/ }; do
    (cd "${SHULKER_SERVER_CONFIG_DIR}" && wget "${patch_url}" -O - | tar -xzv)
  done
fi
