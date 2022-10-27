package resources

import (
	"gopkg.in/yaml.v2"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
)

type bukkitYmlSettings struct {
	AllowEnd bool `yaml:"allow-end"`
}

type bukkitYmlAutoUpdater struct {
	Enabled bool `yaml:"enabled"`
}

type bukkitYml struct {
	Settings    bukkitYmlSettings    `yaml:"settings"`
	AutoUpdater bukkitYmlAutoUpdater `yaml:"auto-updater"`
}

func GetBukkitYml(spec *shulkermciov1alpha1.MinecraftServerConfigurationSpec) (string, error) {
	bukkitYml := bukkitYml{
		Settings: bukkitYmlSettings{
			AllowEnd: !spec.DisableEnd,
		},
		AutoUpdater: bukkitYmlAutoUpdater{
			Enabled: false,
		},
	}

	out, err := yaml.Marshal(&bukkitYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
