package resource

import (
	"crypto/sha1"
	"encoding/hex"
	"io"
	"net/http"
	"os"
	"path"
	"path/filepath"
	"strings"

	shulkermciov1alpha1 "github.com/IamBlueSlime/Shulker/apis/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

type ResourceStore struct {
	RootPath string
}

func NewResourceStore(root_path string) (ResourceStore, error) {
	rs := ResourceStore{
		RootPath: root_path,
	}

	err := os.MkdirAll(rs.RootPath, 0755)
	if err != nil {
		return rs, err
	}

	return rs, nil
}

func (rs *ResourceStore) FetchResource(resource *shulkermciov1alpha1.TemplateResource) (string, error) {
	log.Log.Info("Fetching TemplateResource", "namespace", resource.Namespace, "name", resource.Name)

	filename := rs.getResourcePath(resource)
	err := os.MkdirAll(path.Dir(filename), 0755)
	if err != nil {
		return "", err
	}

	file, err := os.Create(filename)
	if err != nil {
		return "", err
	}
	defer file.Close()

	log.Log.Info("Fetching TemplateResource from URL", "namespace", resource.Namespace, "name", resource.Name, "url", resource.Spec.Source.Url)
	res, err := http.Get(resource.Spec.Source.Url)
	if err != nil {
		return "", err
	}
	defer res.Body.Close()

	_, err = io.Copy(file, res.Body)
	if err != nil {
		return "", err
	}

	local_hash, err := rs.getResourceLocalHash(resource)
	if err != nil {
		return "", err
	}
	log.Log.Info("Computed hash of TemplateResource", "namespace", resource.Namespace, "name", resource.Name, "hash", local_hash)

	return local_hash, nil
}

func (rs *ResourceStore) DeleteResource(resource *shulkermciov1alpha1.TemplateResource) error {
	filename := rs.getResourcePath(resource)
	err := os.RemoveAll(path.Dir(filename))
	if err != nil {
		return err
	}

	parent_path := path.Dir(path.Dir(filename))
	parent_entries, err := os.ReadDir(parent_path)
	if err != nil {
		return err
	} else if len(parent_entries) == 0 {
		err = os.Remove(parent_path)
		if err != nil {
			return err
		}
	}

	return nil
}

func (rs *ResourceStore) IsResourceDirty(resource *shulkermciov1alpha1.TemplateResource) (bool, error) {
	if resource.Status.Hash == "" {
		return true, nil
	}

	local_hash, err := rs.getResourceLocalHash(resource)
	if err != nil {
		if os.IsNotExist(err) {
			return true, nil
		}

		return false, err
	}

	remote_hash, err := rs.getResourceRemoteHash(resource)
	if err != nil {
		return false, err
	}

	return local_hash != remote_hash, nil
}

func (rs *ResourceStore) getResourceLocalHash(resource *shulkermciov1alpha1.TemplateResource) (string, error) {
	file, err := rs.openResource(resource)
	if err != nil {
		return "", err
	}

	hasher := sha1.New()
	if _, err := io.Copy(hasher, file); err != nil {
		return "", err
	}

	hash := hasher.Sum(nil)[:20]
	return hex.EncodeToString(hash), nil
}

func (rs *ResourceStore) getResourceRemoteHash(resource *shulkermciov1alpha1.TemplateResource) (string, error) {
	res, err := http.Get(resource.Spec.Source.Url + ".sha1")
	if err != nil {
		return "", err
	}
	defer res.Body.Close()

	hash, err := io.ReadAll(res.Body)
	if err != nil {
		return "", err
	}

	return strings.TrimSpace(string(hash)), nil
}

func (rs *ResourceStore) openResource(resource *shulkermciov1alpha1.TemplateResource) (io.Reader, error) {
	filename := rs.getResourcePath(resource)
	file, err := os.Open(filename)
	if err != nil {
		return nil, err
	}
	return file, nil
}

func (rs *ResourceStore) getResourcePath(resource *shulkermciov1alpha1.TemplateResource) string {
	return filepath.Join(rs.RootPath, "resources", resource.Namespace, resource.Name, resource.Spec.Destination.FileName)
}
