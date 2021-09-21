package resource

import (
	"archive/tar"
	"compress/gzip"
	"io"
	"os"
	"path"
	"path/filepath"

	"k8s.io/apimachinery/pkg/api/meta"

	shulkermciov1alpha1 "github.com/IamBlueSlime/Shulker/apis/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

func (rs *ResourceStore) PackTemplate(template *shulkermciov1alpha1.Template, resources []*shulkermciov1alpha1.TemplateResource) error {
	log.Log.Info("Packing Template", "namespace", template.Namespace, "name", template.Name)

	work_dir, err := os.MkdirTemp("", "shulker-archive-*")
	if err != nil {
		return err
	}
	defer os.RemoveAll(work_dir)

	err = rs.extractTemplateResources(work_dir, resources)
	if err != nil {
		return err
	}

	server_filename := filepath.Join(work_dir, "server.jar")
	server_stream, err := rs.fetchServer(template.Spec.Version.Channel, template.Spec.Version.Name)
	if err != nil {
		return err
	}
	defer server_stream.Close()
	server_file, err := os.Create(server_filename)
	if err != nil {
		return err
	}
	defer server_file.Close()
	_, err = io.Copy(server_file, server_stream)
	if err != nil {
		return err
	}

	filename := rs.getTemplatePath(template)
	err = rs.compressTemplateWorkDir(work_dir, filename)
	if err != nil {
		return err
	}

	log.Log.Info("Packed Template successfuly", "namespace", template.Namespace, "name", template.Name)

	return nil
}

func (rs *ResourceStore) DeleteTemplate(template *shulkermciov1alpha1.Template) error {
	filename := rs.getTemplatePath(template)
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

func (rs *ResourceStore) IsTemplateDirty(template *shulkermciov1alpha1.Template, resources []*shulkermciov1alpha1.TemplateResource) (bool, error) {
	if template.Status.LastPackDate.IsZero() {
		return true, nil
	}

	_, err := os.Stat(rs.getTemplatePath(template))
	if err != nil {
		if os.IsNotExist(err) {
			return true, nil
		}

		return false, err
	}

	for _, resource := range resources {
		condition := meta.FindStatusCondition(resource.Status.Conditions, "Ready")
		if condition == nil {
			continue
		}

		if condition.LastTransitionTime.After(template.Status.LastPackDate.Time) {
			return true, nil
		}
	}

	return false, nil
}

func (rs *ResourceStore) extractTemplateResources(work_dir string, resources []*shulkermciov1alpha1.TemplateResource) error {
	for _, resource := range resources {
		if !resource.Spec.Destination.Extract {
			in, err := os.Open(rs.getResourcePath(resource))
			if err != nil {
				return err
			}
			defer in.Close()

			path := filepath.Join(work_dir, resource.Spec.Destination.Path, resource.Spec.Destination.FileName)
			err = os.MkdirAll(filepath.Dir(path), 0755)
			if err != nil {
				return err
			}

			out, err := os.Create(filepath.Join(work_dir, resource.Spec.Destination.Path, resource.Spec.Destination.FileName))
			if err != nil {
				return err
			}
			defer out.Close()

			_, err = io.Copy(out, in)
			if err != nil {
				return err
			}

			continue
		}

		stream, err := rs.openResource(resource)
		if err != nil {
			return err
		}
		stream, err = gzip.NewReader(stream)
		if err != nil {
			return err
		}

		tar_reader := tar.NewReader(stream)

		for {
			header, err := tar_reader.Next()
			if err == io.EOF {
				break
			} else if err != nil {
				return err
			}

			switch header.Typeflag {
			case tar.TypeDir:
				path := filepath.Join(work_dir, header.Name)
				err := os.Mkdir(path, 0755)
				if err != nil {
					return err
				}

			case tar.TypeReg:
				path := filepath.Join(work_dir, header.Name)
				out, err := os.Create(path)
				if err != nil {
					return err
				}
				defer out.Close()

				_, err = io.Copy(out, tar_reader)
				if err != nil {
					return err
				}
			}
		}
	}

	return nil
}

func (rs *ResourceStore) compressTemplateWorkDir(work_dir string, dest string) error {
	err := os.MkdirAll(filepath.Dir(dest), 0755)
	if err != nil {
		return err
	}

	out, err := os.Create(dest)
	if err != nil {
		return err
	}
	defer out.Close()

	stream := gzip.NewWriter(out)
	defer stream.Close()
	tar_writer := tar.NewWriter(stream)
	defer tar_writer.Close()

	err = filepath.Walk(work_dir, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if info.IsDir() {
			return nil
		}

		tar_path := path[len(work_dir)+1:]

		file, err := os.Open(path)
		if err != nil {
			return err
		}
		defer file.Close()

		info, err = file.Stat()
		if err != nil {
			return err
		}

		header, err := tar.FileInfoHeader(info, info.Name())
		if err != nil {
			return err
		}

		header.Name = tar_path

		err = tar_writer.WriteHeader(header)
		if err != nil {
			return err
		}

		_, err = io.Copy(tar_writer, file)
		if err != nil {
			return err
		}
		return nil
	})

	return err
}

func (rs *ResourceStore) OpenTemplate(template *shulkermciov1alpha1.Template) (io.Reader, error) {
	filename := rs.getTemplatePath(template)
	file, err := os.Open(filename)
	if err != nil {
		return nil, err
	}
	return file, nil
}

func (rs *ResourceStore) getTemplatePath(template *shulkermciov1alpha1.Template) string {
	return filepath.Join(rs.RootPath, "templates", template.Namespace, template.Name, "template.tar.gz")
}
