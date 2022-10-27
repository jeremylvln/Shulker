package resources

import (
	"gopkg.in/yaml.v2"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
)

type paperGlobalProxiesBungeeCordYml struct {
	OnlineMode bool `yaml:"online-mode"`
}

type paperGlobalProxiesVelocityYml struct {
	Enabled    bool   `yaml:"enabled"`
	OnlineMode bool   `yaml:"online-mode"`
	Secret     string `yaml:"secret"`
}

type paperGlobalProxiesYml struct {
	BungeeCord paperGlobalProxiesBungeeCordYml `yaml:"bungee-cord"`
	Velocity   paperGlobalProxiesVelocityYml   `yaml:"velocity"`
}

type paperGlobalYml struct {
	Proxies paperGlobalProxiesYml `yaml:"proxies"`
}

func GetPaperGlobalYml(spec *shulkermciov1alpha1.MinecraftServerConfigurationSpec) (string, error) {
	paperGlobalYml := paperGlobalYml{
		Proxies: paperGlobalProxiesYml{
			BungeeCord: paperGlobalProxiesBungeeCordYml{
				OnlineMode: spec.ProxyForwardingMode == shulkermciov1alpha1.MincraftServerConfigurationProxyForwardingModeBungeeCord,
			},
			Velocity: paperGlobalProxiesVelocityYml{
				Enabled:    spec.ProxyForwardingMode == shulkermciov1alpha1.MincraftServerConfigurationProxyForwardingModeVelocity,
				OnlineMode: true,
				Secret:     "${CFG_VELOCITY_FORWARDING_SECRET}",
			},
		},
	}

	out, err := yaml.Marshal(&paperGlobalYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
