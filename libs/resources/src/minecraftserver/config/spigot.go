package resources

import (
	"gopkg.in/yaml.v2"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
)

type spigotSettingsYml struct {
	BungeeCord     bool `yaml:"bungeecord"`
	RestartOnCrash bool `yaml:"restart-on-crash"`
}

type spigotSaveableYml struct {
	DisableSaving bool `yaml:"disable-saving"`
}

type spigotYml struct {
	Settings                spigotSettingsYml `yaml:"settings"`
	Advancements            spigotSaveableYml `yaml:"advancements"`
	Players                 spigotSaveableYml `yaml:"players"`
	Stats                   spigotSaveableYml `yaml:"stats"`
	SaveUserCacheOnStopOnly bool              `yaml:"save-user-cache-on-stop-only"`
}

func GetSpigotYml(spec *shulkermciov1alpha1.MinecraftServerConfigurationSpec) (string, error) {
	spigotYml := spigotYml{
		Settings: spigotSettingsYml{
			BungeeCord:     spec.ProxyForwardingMode == shulkermciov1alpha1.MincraftServerConfigurationProxyForwardingModeBungeeCord,
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
		SaveUserCacheOnStopOnly: true,
	}

	out, err := yaml.Marshal(&spigotYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
