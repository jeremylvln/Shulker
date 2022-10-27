package resources

import (
	toml "github.com/pelletier/go-toml/v2"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
)

type velocityPlayerInfoForwardingMode string

const (
	velocityPlayerInfoForwardingModeNone        velocityPlayerInfoForwardingMode = "none"
	velocityPlayerInfoForwardingModeLegacy      velocityPlayerInfoForwardingMode = "legacy"
	velocityPlayerInfoForwardingModeBungeeguard velocityPlayerInfoForwardingMode = "bungeeguard"
	velocityPlayerInfoForwardingModeModern      velocityPlayerInfoForwardingMode = "modern"
)

type velocityAdvancedToml struct {
	HAProxyProtocol bool `toml:"haproxy-protocol"`
	TCPFastOpen     bool `toml:"tcp-fast-open"`
}

type velocityForcedHostsToml struct{}

type velocityServersToml struct {
	Limbo string   `toml:"limbo"`
	Try   []string `toml:"try"`
}

type velocityToml struct {
	ConfigVersion                 string                           `toml:"config-version"`
	Bind                          string                           `toml:"bind"`
	Motd                          string                           `toml:"motd"`
	ShowMaxPlayers                int32                            `toml:"show-max-players"`
	OnlineMode                    bool                             `toml:"online-mode"`
	ForceKeyAuthentication        bool                             `toml:"force-key-authentication"`
	PreventClientProxyConnections bool                             `toml:"prevent-client-proxy-connections"`
	ForwardingSecretFile          string                           `toml:"forwarding-secret-file"`
	PlayerInfoForwardingMode      velocityPlayerInfoForwardingMode `toml:"player-info-forwarding-mode"`
	Servers                       velocityServersToml              `toml:"servers"`
	ForcedHosts                   velocityForcedHostsToml          `toml:"forced-hosts"`
	Advanced                      velocityAdvancedToml             `toml:"advanced"`
}

func GetVelocityToml(spec *shulkermciov1alpha1.ProxyConfigurationSpec) (string, error) {
	velocityToml := velocityToml{
		ConfigVersion:                 "2.5",
		Bind:                          "0.0.0.0:25577",
		Motd:                          spec.Motd,
		ShowMaxPlayers:                spec.MaxPlayers,
		OnlineMode:                    true,
		ForceKeyAuthentication:        true,
		PreventClientProxyConnections: true,
		PlayerInfoForwardingMode:      velocityPlayerInfoForwardingModeModern,
		ForwardingSecretFile:          "/mnt/shulker/forwarding-secret/key",
		Servers: velocityServersToml{
			Limbo: "localhost:25565",
			Try:   []string{"limbo"},
		},
		ForcedHosts: velocityForcedHostsToml{},
		Advanced: velocityAdvancedToml{
			HAProxyProtocol: spec.ProxyProtocol,
			TCPFastOpen:     true,
		},
	}

	out, err := toml.Marshal(&velocityToml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
