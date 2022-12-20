package resources

import (
	"fmt"
	"strconv"
	"strings"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
)

func GetServerProperties(spec *shulkermciov1alpha1.MinecraftServerConfigurationSpec) string {
	properties := make(map[string]string)

	for k, v := range spec.ServerProperties {
		properties[k] = v
	}

	properties["online-mode"] = "false"
	properties["prevent-proxy-connections"] = "false"
	properties["enforce-secure-profiles"] = "true"
	properties["max-players"] = strconv.Itoa(*spec.MaxPlayers)
	properties["allow-nether"] = strconv.FormatBool(!spec.DisableNether)

	lines := []string{}
	for k, v := range properties {
		lines = append(lines, fmt.Sprintf("%s=%s", k, v))
	}

	return strings.Join(lines, "\n")
}
