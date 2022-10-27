package resources

import (
	"gopkg.in/yaml.v2"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
)

type bungeeCordServerYml struct {
	Motd       string `yaml:"motd"`
	Address    string `yaml:"address"`
	Restricted bool   `yaml:"restricted"`
}

type bungeeCordListenerYml struct {
	Host               string   `yaml:"host"`
	QueryPort          int16    `yaml:"query_port"`
	Motd               string   `yaml:"motd"`
	MaxPlayers         int32    `yaml:"max_players"`
	Priorities         []string `yaml:"priorities"`
	PingPassthrough    bool     `yaml:"ping_passthrough"`
	ForceDefaultServer bool     `yaml:"force_default_server"`
	ProxyProtocol      bool     `yaml:"proxy_protocol"`
}

type bungeeCordYml struct {
	Servers                 map[string]bungeeCordServerYml `yaml:"servers"`
	Listeners               []bungeeCordListenerYml        `yaml:"listeners"`
	Groups                  map[string]interface{}         `yaml:"groups"`
	OnlineMode              bool                           `yaml:"online_mode"`
	IpForward               bool                           `yaml:"ip_forward"`
	PreventProxyConnections bool                           `yaml:"prevent_proxy_connections"`
	EnforceSecureProfile    bool                           `yaml:"enforce_secure_profile"`
	LogPings                bool                           `yaml:"log_pings"`
}

func GetBungeeCordYml(spec *shulkermciov1alpha1.ProxyConfigurationSpec) (string, error) {
	bungeeCordYml := bungeeCordYml{
		Servers: map[string]bungeeCordServerYml{
			"limbo": {
				Motd:       spec.Motd,
				Address:    "localhost:25565",
				Restricted: false,
			},
		},
		Listeners: []bungeeCordListenerYml{{
			Host:               "0.0.0.0:25577",
			QueryPort:          int16(25577),
			Motd:               spec.Motd,
			MaxPlayers:         spec.MaxPlayers,
			Priorities:         []string{"limbo"},
			PingPassthrough:    false,
			ForceDefaultServer: true,
			ProxyProtocol:      spec.ProxyProtocol,
		}},
		Groups:                  map[string]interface{}{},
		OnlineMode:              true,
		IpForward:               true,
		PreventProxyConnections: true,
		EnforceSecureProfile:    true,
		LogPings:                false,
	}

	out, err := yaml.Marshal(&bungeeCordYml)
	if err != nil {
		return "", err
	}

	return string(out), nil
}
