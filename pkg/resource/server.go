package resource

import (
	"crypto/sha1"
	"encoding/hex"
	"io"
	"os"
	"path"
	"path/filepath"
	"strings"

	shulkermciov1alpha1 "github.com/IamBlueSlime/Shulker/apis/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

func (rs *ResourceStore) fetchServer(channel shulkermciov1alpha1.TemplateSpecVersionChannel, name string) (io.ReadCloser, error) {
	dirty, err := rs.isServerDirty(channel, name)
	if err != nil {
		return nil, err
	}

	filename := rs.getServerPath(channel, name)

	if dirty {
		log.Log.Info("Fetching server from channel", "channel", channel, "version", name)

		err = os.MkdirAll(path.Dir(filename), 0755)
		if err != nil {
			return nil, err
		}

		file, err := os.Create(filename)
		if err != nil {
			return nil, err
		}
		defer file.Close()

		var stream io.ReadCloser
		if channel == shulkermciov1alpha1.Vanilla {
			stream, err = rs.FetchVanillaServer(name)
		}

		if err != nil {
			return nil, err
		}
		defer stream.Close()

		_, err = io.Copy(file, stream)
		if err != nil {
			return nil, err
		}
	} else {
		log.Log.Info("Server in cache is up to date", "channel", channel, "version", name)
	}

	return os.Open(filename)
}

func (rs *ResourceStore) isServerDirty(channel shulkermciov1alpha1.TemplateSpecVersionChannel, name string) (bool, error) {
	filename := rs.getServerPath(channel, name)
	file, err := os.Open(filename)
	if err != nil {
		if os.IsNotExist(err) {
			return true, nil
		}

		return false, err
	}
	defer file.Close()

	local_hash_sha := sha1.New()
	if _, err := io.Copy(local_hash_sha, file); err != nil {
		return false, err
	}

	local_hash := hex.EncodeToString(local_hash_sha.Sum(nil)[:20])

	var remote_hash string
	if channel == shulkermciov1alpha1.Vanilla {
		remote_hash, err = rs.FetchVanillaServerHash(name)
	}

	if err != nil {
		return false, err
	}

	return local_hash != remote_hash, nil
}

func (rs *ResourceStore) getServerPath(channel shulkermciov1alpha1.TemplateSpecVersionChannel, name string) string {
	return filepath.Join(rs.RootPath, "servers", strings.ToLower(string(channel)), strings.ReplaceAll(name, ".", "_"), "server.jar")
}
