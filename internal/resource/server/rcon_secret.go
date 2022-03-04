package resource

import (
	"fmt"
	"math/rand"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftServerRconSecretBuilder struct {
	*MinecraftServerResourceBuilder
}

func (b *MinecraftServerResourceBuilder) MinecraftServerRconSecret() *MinecraftServerRconSecretBuilder {
	return &MinecraftServerRconSecretBuilder{b}
}

func (b *MinecraftServerRconSecretBuilder) Build() (client.Object, error) {
	return &corev1.Secret{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getRconSecretName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
		Type: corev1.SecretTypeOpaque,
		StringData: map[string]string{
			"password": getRandomRconPassword(),
		},
	}, nil
}

func (b *MinecraftServerRconSecretBuilder) Update(object client.Object) error {
	secret := object.(*corev1.Secret)

	if err := controllerutil.SetControllerReference(b.Instance, secret, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Secret: %v", err)
	}

	return nil
}

func (b *MinecraftServerRconSecretBuilder) CanBeUpdated() bool {
	return true
}

const passwordChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

func getRandomRconPassword() string {
	password := make([]byte, 16)

	for i := range password {
		password[i] = passwordChars[rand.Intn(len(passwordChars))]
	}
	return string(password)
}
