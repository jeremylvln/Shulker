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

if [ ! -z "${SHULKER_SERVER_WORLD_URL:-}" ]; then
  (cd "${SHULKER_SERVER_CONFIG_DIR}" && wget "${SHULKER_SERVER_WORLD_URL}" -O - | tar -xzv)
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

if [ ! -z "${SHULKER_SERVER_FILES:-}" ]; then
  # Format expected: "location1;url1;location2;url2" (pairs of entries)
  IFS=';' read -ra FILE_ENTRIES <<< "${SHULKER_SERVER_FILES}"
  i=0
  while [ $i -lt ${#FILE_ENTRIES[@]} ]; do
    if [ $((i+1)) -lt ${#FILE_ENTRIES[@]} ]; then
      location="${FILE_ENTRIES[$i]}"
      url="${FILE_ENTRIES[$((i+1))]}"
      target_path="${SHULKER_SERVER_CONFIG_DIR}/${location}"
      mkdir -p "$(dirname "${target_path}")"
      wget "${url}" -O "${target_path}"
    fi
    i=$((i+2))
  done
fi

if [ ! -z "${SHULKER_PROXY_FILES:-}" ]; then
  # Format expected: "location1;url1;location2;url2" (pairs of entries)
  IFS=';' read -ra FILE_ENTRIES <<< "${SHULKER_PROXY_FILES}"
  i=0
  while [ $i -lt ${#FILE_ENTRIES[@]} ]; do
    if [ $((i+1)) -lt ${#FILE_ENTRIES[@]} ]; then
      location="${FILE_ENTRIES[$i]}"
      url="${FILE_ENTRIES[$((i+1))]}"
      target_path="${SHULKER_PROXY_DATA_DIR}/${location}"
      mkdir -p "$(dirname "${target_path}")"
      wget "${url}" -O "${target_path}"
    fi
    i=$((i+2))
  done
fi
