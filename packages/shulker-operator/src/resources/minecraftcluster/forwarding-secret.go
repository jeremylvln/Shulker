/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"
	"math/rand"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftClusterForwardingSecretBuilder struct {
	*MinecraftClusterResourceBuilder
}

func (b *MinecraftClusterResourceBuilder) MinecraftClusterForwardingSecret() *MinecraftClusterForwardingSecretBuilder {
	return &MinecraftClusterForwardingSecretBuilder{b}
}

func (b *MinecraftClusterForwardingSecretBuilder) Build() (client.Object, error) {
	return &corev1.Secret{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getForwardingSecretName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
		Type: corev1.SecretTypeOpaque,
		StringData: map[string]string{
			"key": getProxyGuardSecret(),
		},
	}, nil
}

func (b *MinecraftClusterForwardingSecretBuilder) Update(object client.Object) error {
	secret := object.(*corev1.Secret)

	if err := controllerutil.SetControllerReference(b.Instance, secret, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Secret: %v", err)
	}

	return nil
}

func (b *MinecraftClusterForwardingSecretBuilder) CanBeUpdated() bool {
	return true
}

const guardSecretChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

func getProxyGuardSecret() string {
	secret := make([]byte, 64)

	for i := range secret {
		secret[i] = guardSecretChars[rand.Intn(len(guardSecretChars))]
	}
	return string(secret)
}
