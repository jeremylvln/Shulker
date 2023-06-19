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

	shulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/v1alpha1"
	config "github.com/jeremylvln/shulker/packages/shulker-operator/src/resources/proxyfleet/config"
)

const defaultServerIcon = "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAG1BMVEUAAABSNGCrhKqXaZdsSGtDJlCEUomPY4/////HT7OpAAAACXRSTlMA//////////83ApvUAAAAh0lEQVRYhe3Qyw6AIAxEURTQ//9jmYQmE1IeWwbvytD2LAzhb9JFnQTw0U0tIxsD3lGkUmmIbA7wYRwEJJdUgac0A7qIEDBCEqUKYMlDkpMqgENG8MZHb00ZQBgYYoAdYmZvqoA94tsOsNzOVAHUA3hZHfCW2uVc6x4fACwlABiy9MOEgaP6APk1HDGFXeaaAAAAAElFTkSuQmCC"

type ProxyFleetResourceConfigMapBuilder struct {
	*ProxyFleetResourceBuilder
}

func (b *ProxyFleetResourceBuilder) ProxyFleetConfigMap() *ProxyFleetResourceConfigMapBuilder {
	return &ProxyFleetResourceConfigMapBuilder{b}
}

func (b *ProxyFleetResourceConfigMapBuilder) Build() (client.Object, error) {
	return &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetConfigMapName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *ProxyFleetResourceConfigMapBuilder) Update(object client.Object) error {
	configMap := object.(*corev1.ConfigMap)

	configMapData, err := GetConfigMapDataFromConfigSpec(&b.Instance.Spec.Template.Spec.Configuration)
	if err != nil {
		return err
	}
	configMap.Data = configMapData

	if err := controllerutil.SetControllerReference(b.Instance, configMap, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for ConfigMap: %v", err)
	}

	return nil
}

func (b *ProxyFleetResourceConfigMapBuilder) CanBeUpdated() bool {
	return true
}

func GetConfigMapDataFromConfigSpec(spec *shulkermciov1alpha1.ProxyConfigurationSpec) (map[string]string, error) {
	configMapData := make(map[string]string)

	configMapData["init-fs.sh"] = trimScript(`
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
			(cd "${PROXY_DATA_DIR}/plugins" && wget https://maven.jeremylvln.fr/artifactory/shulker/io/shulkermc/shulker-proxy-agent/${SHULKER_PROXY_AGENT_VERSION}/shulker-proxy-agent-${SHULKER_PROXY_AGENT_VERSION}-velocity.jar)
		else
			(cd "${PROXY_DATA_DIR}/plugins" && wget https://maven.jeremylvln.fr/artifactory/shulker/io/shulkermc/shulker-proxy-agent/${SHULKER_PROXY_AGENT_VERSION}/shulker-proxy-agent-${SHULKER_PROXY_AGENT_VERSION}-bungeecord.jar)
		fi

		if [ "${PROXY_PLUGIN_URLS}" != "" ]; then
			for plugin_url in ${PROXY_PLUGIN_URLS//;/ }; do
				(cd "${PROXY_DATA_DIR}/plugins" && wget "${plugin_url}")
			done
		fi

		if [ "${PROXY_PATCH_URLS}" != "" ]; then
			for patch_url in ${PROXY_PATCH_URLS//;/ }; do
				(cd "${PROXY_DATA_DIR}" && wget "${patch_url}" -O - | tar -xzv)
			done
		fi
	`)

	configMapData["probe-readiness.sh"] = trimScript(`
		#!/bin/sh
		set -euo pipefail
		set -o xtrace

		if [ -f "/tmp/drain-lock" ]; then
			echo "Drain lock found" && exit 1
		fi

		bash /health.sh
	`)

	if spec.ServerIcon != "" {
		configMapData["server-icon.png"] = spec.ServerIcon
	} else {
		configMapData["server-icon.png"] = defaultServerIcon
	}

	bungeeCordConfigYml, err := config.GetBungeeCordYml(spec)
	if err != nil {
		return configMapData, err
	}
	configMapData["bungeecord-config.yml"] = bungeeCordConfigYml

	velocityConfigToml, err := config.GetVelocityToml(spec)
	if err != nil {
		return configMapData, err
	}
	configMapData["velocity-config.toml"] = velocityConfigToml

	return configMapData, nil
}

func trimScript(script string) string {
	lines := strings.Split(strings.TrimSpace(script), "\n")
	for i := range lines {
		lines[i] = strings.TrimSpace(lines[i])
	}
	return strings.Join(lines, "\n")
}
