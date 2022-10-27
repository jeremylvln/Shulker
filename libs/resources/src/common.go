/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"math/rand"

	"sigs.k8s.io/controller-runtime/pkg/client"
)

type ResourceBuilder interface {
	Build() (client.Object, error)
	Update(client.Object) error
	CanBeUpdated() bool
}

var randomCharacters = []rune("abcdefghijklmnopqrstuvwxyz0123456789")

func RandomResourceId(length int) string {
	id := make([]rune, length)
	for i := range id {
		id[i] = randomCharacters[rand.Intn(len(randomCharacters))]
	}
	return string(id)
}
