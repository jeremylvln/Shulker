/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"
	"strings"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/packages/shulker-crds/v1alpha1"
	config "github.com/iamblueslime/shulker/packages/shulker-operator/src/resources/minecraftserver/config"
)

type MinecraftServerResourceConfigMapBuilder struct {
	*MinecraftServerResourceBuilder
}

func (b *MinecraftServerResourceBuilder) MinecraftServerConfigMap() *MinecraftServerResourceConfigMapBuilder {
	return &MinecraftServerResourceConfigMapBuilder{b}
}

func (b *MinecraftServerResourceConfigMapBuilder) Build() (client.Object, error) {
	return &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetConfigMapName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerResourceConfigMapBuilder) Update(object client.Object) error {
	configMap := object.(*corev1.ConfigMap)

	configMapData, err := GetConfigMapDataFromConfigSpec(&b.Instance.Spec.Configuration)
	if err != nil {
		return err
	}
	configMap.Data = configMapData

	if err := controllerutil.SetControllerReference(b.Instance, configMap, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for ConfigMap: %v", err)
	}

	return nil
}

func (b *MinecraftServerResourceConfigMapBuilder) CanBeUpdated() bool {
	return true
}

func GetConfigMapDataFromConfigSpec(spec *shulkermciov1alpha1.MinecraftServerConfigurationSpec) (map[string]string, error) {
	configMapData := make(map[string]string)

	configMapData["init-fs.sh"] = trimScript(`
		#!/bin/sh
		set -euo pipefail
		set -o xtrace

		cp "${SHULKER_CONFIG_DIR}/server.properties" "${SERVER_CONFIG_DIR}/server.properties"
		if [ "${TYPE}" == "BUKKIT" ]; then
			cp "${SHULKER_CONFIG_DIR}/bukkit-config.yml" "${SERVER_CONFIG_DIR}/bukkit.yml"
		elif [ "${TYPE}" == "SPIGOT" ]; then
			cp "${SHULKER_CONFIG_DIR}/bukkit-config.yml" "${SERVER_CONFIG_DIR}/bukkit.yml"
			cp "${SHULKER_CONFIG_DIR}/spigot-config.yml" "${SERVER_CONFIG_DIR}/spigot.yml"
		elif [ "${TYPE}" == "PAPER" ]; then
			cp "${SHULKER_CONFIG_DIR}/bukkit-config.yml" "${SERVER_CONFIG_DIR}/bukkit.yml"
			cp "${SHULKER_CONFIG_DIR}/spigot-config.yml" "${SERVER_CONFIG_DIR}/spigot.yml"
			mkdir -p "${SERVER_CONFIG_DIR}/config"
			cp "${SHULKER_CONFIG_DIR}/paper-global-config.yml" "${SERVER_CONFIG_DIR}/config/paper-global.yml"
		fi

		if [ "${SERVER_WORLD_URL}" != "" ]; then
			(cd "${SERVER_CONFIG_DIR}" && wget "${SERVER_WORLD_URL}" -O - | tar -xzv)
		fi

		if [ "${SERVER_PLUGIN_URLS}" != "" ]; then
			mkdir -p "${SERVER_CONFIG_DIR}/plugins"
			for plugin_url in ${SERVER_PLUGIN_URLS//;/ }; do
				(cd "${SERVER_CONFIG_DIR}/plugins" && wget "${plugin_url}")
			done
		fi

		if [ "${SERVER_PATCH_URLS}" != "" ]; then
			for patch_url in ${SERVER_PATCH_URLS//;/ }; do
				(cd "${SERVER_CONFIG_DIR}" && wget "${patch_url}" -O - | tar -xzv)
			done
		fi
	`)

	configMapData["server.properties"] = config.GetServerProperties(spec)

	bukkitConfigYml, err := config.GetBukkitYml(spec)
	if err != nil {
		return configMapData, err
	}
	configMapData["bukkit-config.yml"] = bukkitConfigYml

	spigotConfigYml, err := config.GetSpigotYml(spec)
	if err != nil {
		return configMapData, err
	}
	configMapData["spigot-config.yml"] = spigotConfigYml

	paperGlobalConfigYml, err := config.GetPaperGlobalYml(spec)
	if err != nil {
		return configMapData, err
	}
	configMapData["paper-global-config.yml"] = paperGlobalConfigYml

	return configMapData, nil
}

func trimScript(script string) string {
	lines := strings.Split(strings.TrimSpace(script), "\n")
	for i := range lines {
		lines[i] = strings.TrimSpace(lines[i])
	}
	return strings.Join(lines, "\n")
}
