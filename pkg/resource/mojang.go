package resource

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

type mojangVersion struct {
	Downloads struct {
		Server struct {
			Sha1 string `json:"sha1"`
			Size uint64 `json:"size"`
			Url  string `json:"url"`
		} `json:"server"`
	} `json:"downloads"`
}

type mojangVersionManifest struct {
	Latest struct {
		Release  string `json:"release"`
		Snapshot string `json:"snapshot"`
	} `json:"latest"`
	Versions []struct {
		Id          string    `json:"id"`
		Type        string    `json:"type"`
		Url         string    `json:"url"`
		Time        time.Time `json:"time"`
		ReleaseTime time.Time `json:"releaseTime"`
	} `json:"versions"`
}

func (rs *ResourceStore) FetchVanillaServer(version_name string) (io.ReadCloser, error) {
	manifest := mojangVersionManifest{}
	err := getVersionManifest(&manifest)
	if err != nil {
		return nil, err
	}

	version := mojangVersion{}
	err = getVersion(&manifest, version_name, &version)
	if err != nil {
		return nil, err
	}

	res, err := http.Get(version.Downloads.Server.Url)
	if err != nil {
		return nil, err
	}

	return res.Body, nil
}

func (rs *ResourceStore) FetchVanillaServerHash(version_name string) (string, error) {
	manifest := mojangVersionManifest{}
	err := getVersionManifest(&manifest)
	if err != nil {
		return "", err
	}

	version := mojangVersion{}
	err = getVersion(&manifest, version_name, &version)
	if err != nil {
		return "", err
	}

	return version.Downloads.Server.Sha1, nil
}

func getVersionManifest(manifest *mojangVersionManifest) error {
	res, err := http.Get("https://launchermeta.mojang.com/mc/game/version_manifest.json")
	if err != nil {
		return err
	}

	decoder := json.NewDecoder(res.Body)
	decoder.DisallowUnknownFields()
	err = decoder.Decode(manifest)
	return err
}

func getVersion(manifest *mojangVersionManifest, version_name string, version *mojangVersion) error {
	for _, manifest_version := range manifest.Versions {
		if manifest_version.Id == version_name {
			res, err := http.Get(manifest_version.Url)
			if err != nil {
				return err
			}

			decoder := json.NewDecoder(res.Body)
			err = decoder.Decode(version)
			return err
		}
	}

	return fmt.Errorf("version %s not found", version_name)
}
