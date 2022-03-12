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

type MinecraftServerConfigMapBuilder struct {
	*MinecraftServerResourceBuilder
}

func (b *MinecraftServerResourceBuilder) MinecraftServerConfigMap() *MinecraftServerConfigMapBuilder {
	return &MinecraftServerConfigMapBuilder{b}
}

func (b *MinecraftServerConfigMapBuilder) Build() (client.Object, error) {
	return &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getConfigMapName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerConfigMapBuilder) Update(object client.Object) error {
	configMap := object.(*corev1.ConfigMap)
	configMapData := make(map[string]string)

	configMapData["init-fs.sh"] = strings.Trim(`
cp -H -r $SHULKER_CONFIG_DIR/* $SHULKER_DATA_DIR/;
if [ -e "$SHULKER_CONFIG_DIR/server-icon.png" ]; then cat $SHULKER_CONFIG_DIR/server-icon.png | base64 -d > $SHULKER_DATA_DIR/server-icon.png; fi
if [ ! -z "$SHULKER_LIMBO_SCHEMATIC_URL" ]; then wget -O $SHULKER_DATA_DIR/limbo.schematic $SHULKER_LIMBO_SCHEMATIC_URL; fi
	`, "\n ")

	if b.Instance.Spec.World != nil && b.Instance.Spec.World.SchematicUrl != "" {
		configMapData["init-limbo-schematic.sh"] = strings.Trim(`
wget -O $SHULKER_DATA_DIR/limbo.schematic $SHULKER_LIMBO_SCHEMATIC_URL
	`, "\n ")
	}

	if b.Instance.Spec.ServerIcon != "" {
		configMapData["server-icon.png"] = b.Instance.Spec.ServerIcon
	} else {
		configMapData["server-icon.png"] = defaultServerIcon
	}

	bukkitYml, err := b.getBukkitYmlFile()
	if err != nil {
		return err
	}
	configMapData["bukkit.yml"] = bukkitYml

	configMap.Data = configMapData

	if err := controllerutil.SetControllerReference(b.Instance, configMap, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for ConfigMap: %v", err)
	}

	return nil
}

func (b *MinecraftServerConfigMapBuilder) CanBeUpdated() bool {
	return true
}

type bukkitYml struct {
	Settings bukkitYmlSettings `yaml:"settings"`
}

type bukkitYmlSettings struct {
	AllowEnd bool `yaml:"allow-end"`
}

func (b *MinecraftServerConfigMapBuilder) getBukkitYmlFile() (string, error) {
	bukkitYml := bukkitYml{
		Settings: bukkitYmlSettings{
			AllowEnd: !b.Instance.Spec.World.DisableEnd,
		},
	}

	out, err := yaml.Marshal(&bukkitYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
