package resourceutils

import (
	"context"
	"errors"
	"fmt"
	"net/url"
	"strings"

	"github.com/iamblueslime/shulker/packages/shulker-crds/v1alpha1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type ResourceRefResolver struct {
	client.Client
	Ctx       context.Context
	Namespace string
}

func (r *ResourceRefResolver) ResolveUrl(resourceRef *v1alpha1.ResourceRef) (string, error) {
	if resourceRef == nil {
		return "", errors.New("resourceRef is nil")
	}

	if resourceRef.Url != "" {
		return resourceRef.Url, nil
	}

	if resourceRef.UrlFrom != nil {
		if resourceRef.UrlFrom.MavenRef != nil {
			return r.resolveMavenRefUrl(resourceRef.UrlFrom.MavenRef)
		}
	}

	return "", errors.New("no resourceRef combination")
}

func (r *ResourceRefResolver) resolveMavenRefUrl(mavenSelector *v1alpha1.ResourceRefMavenSelector) (string, error) {
	classifierSuffix := ""
	if mavenSelector.Classifier != "" {
		classifierSuffix = fmt.Sprintf("-%s", mavenSelector.Classifier)
	}

	mavenUrl, err := url.Parse(fmt.Sprintf(
		"%[1]s/%[2]s/%[3]s/%[4]s/%[3]s-%[4]s%[5]s.jar",
		mavenSelector.Repository,
		strings.ReplaceAll(mavenSelector.GroupId, ".", "/"),
		mavenSelector.ArtifactId,
		mavenSelector.Version,
		classifierSuffix,
	))
	if err != nil {
		return "", err
	}

	if mavenSelector.CredentialsSecretName != "" {
		secret := &corev1.Secret{}
		err := r.Get(r.Ctx, types.NamespacedName{
			Namespace: r.Namespace,
			Name:      mavenSelector.CredentialsSecretName,
		}, secret)
		if err != nil {
			return "", err
		}

		username, ok := secret.Data["username"]
		if !ok {
			return "", errors.New("missing username in credential secret")
		}

		password, ok := secret.Data["password"]
		if !ok {
			return "", errors.New("missing password in credential secret")
		}

		mavenUrl.User = url.UserPassword(string(username), string(password))
	}

	return mavenUrl.String(), nil
}
