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

type MinecraftServerDeploymentConfigMapBuilder struct {
	*MinecraftServerDeploymentResourceBuilder
}

func (b *MinecraftServerDeploymentResourceBuilder) MinecraftServerDeploymentConfigMap() *MinecraftServerDeploymentConfigMapBuilder {
	return &MinecraftServerDeploymentConfigMapBuilder{b}
}

func (b *MinecraftServerDeploymentConfigMapBuilder) Build() (client.Object, error) {
	return &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getConfigMapName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerDeploymentConfigMapBuilder) Update(object client.Object) error {
	configMap := object.(*corev1.ConfigMap)
	configMapData := make(map[string]string)

	configMapData["init-fs.sh"] = strings.Trim(`
set -x
cp -H -r $SHULKER_CONFIG_DIR/* $SHULKER_DATA_DIR/
if [ -e "$SHULKER_CONFIG_DIR/server-icon.png" ]; then cat $SHULKER_CONFIG_DIR/server-icon.png | base64 -d > $SHULKER_DATA_DIR/server-icon.png; fi
if [ ! -z "$SHULKER_LIMBO_SCHEMATIC_URL" ]; then wget -O $SHULKER_DATA_DIR/limbo.schematic $SHULKER_LIMBO_SCHEMATIC_URL; fi
files_with_folder=$(cd $SHULKER_CONFIG_DIR && find -L . -type f -name '*___*' | tr '\n' ' ')
for file in $files_with_folder; do
	escaped=$(echo $file | sed 's/___/\//g')
	mkdir -p "$SHULKER_DATA_DIR/$(dirname $escaped)"
	cp $SHULKER_CONFIG_DIR/$file $SHULKER_DATA_DIR/$escaped
done
	`, "\n ")

	configMapData["init-plugins.sh"] = strings.Trim(`
set -x
mkdir -p $SHULKER_DATA_DIR/plugins
plugins=$(echo "$SHULKER_PLUGINS_URL" | tr ';' ' ')
for plugin in $plugins; do cd $SHULKER_DATA_DIR/plugins && curl -L -O $plugin; done
		`, "\n ")

	if b.Instance.Spec.World != nil && b.Instance.Spec.World.SchematicUrl != "" {
		configMapData["init-limbo-schematic.sh"] = strings.Trim(`
set -x
wget -O $SHULKER_DATA_DIR/limbo.schematic $SHULKER_LIMBO_SCHEMATIC_URL
echo "bungeecord=true" >> $SHULKER_DATA_DIR/server.properties
echo "default-gamemode=adventure" >> $SHULKER_DATA_DIR/server.properties
echo "world-spawn=world;${SHULKER_LIMBO_WORLD_SPAWN}" >> $SHULKER_DATA_DIR/server.properties
echo "level-dimension=minecraft:the_end" >> $SHULKER_DATA_DIR/server.properties
echo "allow-chat=false" >> $SHULKER_DATA_DIR/server.properties
echo "handshake-verbose=false" >> $SHULKER_DATA_DIR/server.properties
	`, "\n ")
	}

	if b.Instance.Spec.ServerIcon != "" {
		configMapData["server-icon.png"] = b.Instance.Spec.ServerIcon
	} else {
		configMapData["server-icon.png"] = defaultServerIcon
	}

	if b.Instance.Spec.ExtraFiles != nil {
		for path, value := range *b.Instance.Spec.ExtraFiles {
			configMapData[strings.ReplaceAll(path, "/", "___")] = value
		}
	}

	bukkitYml, err := b.getBukkitYmlFile()
	if err != nil {
		return err
	}
	configMapData["bukkit.yml"] = bukkitYml

	spigotYml, err := b.getSpigotYmlFile()
	if err != nil {
		return err
	}
	configMapData["spigot.yml"] = spigotYml

	configMap.Data = configMapData

	if err := controllerutil.SetControllerReference(b.Instance, configMap, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for ConfigMap: %v", err)
	}

	return nil
}

func (b *MinecraftServerDeploymentConfigMapBuilder) CanBeUpdated() bool {
	return true
}

type bukkitYml struct {
	Settings bukkitYmlSettings `yaml:"settings"`
}

type bukkitYmlSettings struct {
	AllowEnd bool `yaml:"allow-end"`
}

func (b *MinecraftServerDeploymentConfigMapBuilder) getBukkitYmlFile() (string, error) {
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

type spigotYml struct {
	Settings     spigotSettingsYml `yaml:"settings"`
	Advancements spigotSaveableYml `yaml:"advancements"`
	Players      spigotSaveableYml `yaml:"players"`
	Stats        spigotSaveableYml `yaml:"stats"`
}

type spigotSettingsYml struct {
	BungeeCord     bool `yaml:"bungeecord"`
	RestartOnCrash bool `yaml:"restart-on-crash"`
}

type spigotSaveableYml struct {
	DisableSaving bool `yaml:"disable-saving"`
}

func (b *MinecraftServerDeploymentConfigMapBuilder) getSpigotYmlFile() (string, error) {
	spigotYml := spigotYml{
		Settings: spigotSettingsYml{
			BungeeCord:     true,
			RestartOnCrash: false,
		},
		Advancements: spigotSaveableYml{
			DisableSaving: true,
		},
		Players: spigotSaveableYml{
			DisableSaving: true,
		},
		Stats: spigotSaveableYml{
			DisableSaving: true,
		},
	}

	out, err := yaml.Marshal(&spigotYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
