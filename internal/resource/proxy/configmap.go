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
set -x
cp -H -r $SHULKER_CONFIG_DIR/* $SHULKER_DATA_DIR/
if [ -e "$SHULKER_CONFIG_DIR/server-icon.png" ]; then cat $SHULKER_CONFIG_DIR/server-icon.png | base64 -d > $SHULKER_DATA_DIR/server-icon.png; fi
	`, "\n ")

	configMapData["init-plugins.sh"] = strings.Trim(`
set -x
mkdir -p $SHULKER_DATA_DIR/plugins
plugins=$(echo "$SHULKER_PLUGINS_URL" | tr ';' ' ')
for plugin in $plugins; do cd $SHULKER_DATA_DIR/plugins && curl -L -O $plugin; done
if [ ! -z "$SHULKER_REDIS_SYNC_ENABLED" ]; then mkdir -p $SHULKER_DATA_DIR/plugins/RedisBungee && cp $SHULKER_CONFIG_DIR/redisbungee-config.yml $SHULKER_DATA_DIR/plugins/RedisBungee/config.yml; fi
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

	if b.Cluster.Spec.RedisSync.Enabled {
		redisBungeeConfigYml, err := b.getRedisBungeeConfigYmlFile()
		if err != nil {
			return err
		}
		configMapData["redisbungee-config.yml"] = redisBungeeConfigYml
	}

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
	Listeners               []configListenerYml    `yaml:"listeners"`
	Groups                  map[string]interface{} `yaml:"groups"`
	IpForward               bool                   `yaml:"ip_forward"`
	PreventProxyConnections bool                   `yaml:"prevent_proxy_connections"`
}

type configListenerYml struct {
	Host               string   `yaml:"host"`
	QueryPort          int16    `yaml:"query_port"`
	Motd               string   `yaml:"motd"`
	MaxPlayers         int64    `yaml:"max_players"`
	Priorities         []string `yaml:"priorities"`
	ForceDefaultServer bool     `yaml:"force_default_server"`
}

func (b *ProxyDeploymentConfigMapBuilder) getConfigYmlFile() (string, error) {
	configYml := configYml{
		Listeners: []configListenerYml{{
			Host:               "0.0.0.0:25577",
			QueryPort:          int16(25577),
			Motd:               b.Instance.Spec.Motd,
			MaxPlayers:         *b.Instance.Spec.MaxPlayers,
			Priorities:         []string{"lobby"},
			ForceDefaultServer: true,
		}},
		Groups:                  map[string]interface{}{},
		IpForward:               true,
		PreventProxyConnections: true,
	}

	out, err := yaml.Marshal(&configYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}

type redisBungeeConfigYml struct {
	RedisServer   string `yaml:"redis-server"`
	RedisPort     string `yaml:"redis-port"`
	RedisPassword string `yaml:"redis-password"`
	ServerId      string `yaml:"server-id"`
}

func (b *ProxyDeploymentConfigMapBuilder) getRedisBungeeConfigYmlFile() (string, error) {
	redisBungeeConfigYml := redisBungeeConfigYml{
		RedisServer:   "${CFG_REDISBUNGEE_REDIS_HOST}",
		RedisPort:     "${CFG_REDISBUNGEE_REDIS_PORT}",
		RedisPassword: "${CFG_REDISBUNGEE_REDIS_PASSWORD}",
		ServerId:      "${CFG_SERVER_ID}",
	}

	out, err := yaml.Marshal(&redisBungeeConfigYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
