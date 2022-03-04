package resource

import (
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type ResourceBuilder interface {
	Build() (client.Object, error)
	Update(client.Object) error
	CanBeUpdated() bool
}
