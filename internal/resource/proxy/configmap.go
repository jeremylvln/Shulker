package resource

import (
	"fmt"
	"strings"

	"gopkg.in/yaml.v3"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

const defaultServerIcon = "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAG1BMVEUAAABSNGCrhKqXaZdsSGtDJlCEUomPY4/////HT7OpAAAACXRSTlMA//////////83ApvUAAAAh0lEQVRYhe3Qyw6AIAxEURTQ//9jmYQmE1IeWwbvytD2LAzhb9JFnQTw0U0tIxsD3lGkUmmIbA7wYRwEJJdUgac0A7qIEDBCEqUKYMlDkpMqgENG8MZHb00ZQBgYYoAdYmZvqoA94tsOsNzOVAHUA3hZHfCW2uVc6x4fACwlABiy9MOEgaP6APk1HDGFXeaaAAAAAElFTkSuQmCC"

type ProxyDeploymentConfigMapBuilder struct {
	*ProxyDeploymentResourceBuilder
}

func (b *ProxyDeploymentResourceBuilder) ProxyDeploymentConfigMap() *ProxyDeploymentConfigMapBuilder {
	return &ProxyDeploymentConfigMapBuilder{b}
}

func (b *ProxyDeploymentConfigMapBuilder) Build() (client.Object, error) {
	return &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getConfigMapName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *ProxyDeploymentConfigMapBuilder) Update(object client.Object) error {
	configMap := object.(*corev1.ConfigMap)
	configMapData := make(map[string]string)

	configMapData["init-fs.sh"] = strings.Trim(`
cp -H -r $SHULKER_CONFIG_DIR/* $SHULKER_DATA_DIR/;
if [ -e "$SHULKER_CONFIG_DIR/server-icon.png" ]; then cat $SHULKER_CONFIG_DIR/server-icon.png | base64 -d > $SHULKER_DATA_DIR/server-icon.png; fi
	`, "\n ")

	configMapData["init-plugins.sh"] = strings.Trim(`
mkdir -p $SHULKER_DATA_DIR/plugins
function plugin { curl -L -o "$SHULKER_DATA_DIR/plugins/$2-$3.jar" -u "${SHULKER_MAVEN_USERNAME}:${SHULKER_MAVEN_PASSWORD}" https://maven.pkg.github.com/IamBlueSlime/Shulker/$1/$2/$3/$2-$3.jar; }
if [ ! -z "$SHULKER_PROXY_DIRECTORY_VERSION" ]; then plugin io.shulkermc shulker-proxy-directory "$SHULKER_PROXY_DIRECTORY_VERSION"; fi
	`, "\n ")

	if b.Instance.Spec.ServerIcon != "" {
		configMapData["server-icon.png"] = b.Instance.Spec.ServerIcon
	} else {
		configMapData["server-icon.png"] = defaultServerIcon
	}

	configYml, err := b.getConfigYmlFile()
	if err != nil {
		return err
	}
	configMapData["config.yml"] = configYml

	configMap.Data = configMapData

	if err := controllerutil.SetControllerReference(b.Instance, configMap, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for ConfigMap: %v", err)
	}

	return nil
}

func (b *ProxyDeploymentConfigMapBuilder) CanBeUpdated() bool {
	return true
}

type configYml struct {
	Listeners []configListenerYml    `yaml:"listeners"`
	Groups    map[string]interface{} `yaml:"groups"`
}

type configListenerYml struct {
	Host               string   `yaml:"host"`
	QueryPort          int16    `yaml:"query_port"`
	Motd               string   `yaml:"motd"`
	MaxPlayers         int64    `yaml:"max_players"`
	ForceDefaultServer bool     `yaml:"force_default_server"`
	Priorities         []string `yaml:"priorities"`
}

func (b *ProxyDeploymentConfigMapBuilder) getConfigYmlFile() (string, error) {
	configYml := configYml{
		Listeners: []configListenerYml{{
			Host:               "0.0.0.0:25577",
			QueryPort:          int16(25577),
			Motd:               b.Instance.Spec.Motd,
			MaxPlayers:         *b.Instance.Spec.MaxPlayers,
			ForceDefaultServer: true,
			Priorities:         []string{"lobby"},
		}},
		Groups: map[string]interface{}{},
	}

	out, err := yaml.Marshal(&configYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
