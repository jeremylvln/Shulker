package resources

import (
	"fmt"
	"strings"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
)

func GetServerProperties(spec *shulkermciov1alpha1.MinecraftServerConfigurationSpec) string {
	properties := []string{
		"online-mode=false",
		"prevent-proxy-connections=false",
		"enforce-secure-profiles=true",
		"previews-chat=true",
		fmt.Sprintf("max-players=%d", *spec.MaxPlayers),
		fmt.Sprintf("allow-nether=%t", !spec.DisableNether),
	}

	return strings.Join(properties, "\n")
}
