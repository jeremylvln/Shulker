/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package controllers

import (
	"context"

	resources "github.com/iamblueslime/shulker/libs/resources/src"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/types"
	clientretry "k8s.io/client-go/util/retry"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

func ReconcileWithResourceBuilders(client client.Client, ctx context.Context, builders []resources.ResourceBuilder, dirtyBuilders []resources.ResourceBuilder) error {
	for _, builder := range builders {
		resource, err := builder.Build()
		if err != nil {
			return err
		}

		err = clientretry.RetryOnConflict(clientretry.DefaultRetry, func() error {
			var apiError error

			if builder.CanBeUpdated() {
				_, apiError = controllerutil.CreateOrUpdate(ctx, client, resource, func() error {
					return builder.Update(resource)
				})
			} else {
				existingResource := resource
				apiError = client.Get(ctx, types.NamespacedName{
					Namespace: resource.GetNamespace(),
					Name:      resource.GetName(),
				}, existingResource)

				if k8serrors.IsNotFound(apiError) {
					apiError = builder.Update(resource)
					if apiError != nil {
						return apiError
					}

					return client.Create(ctx, resource)
				}
			}

			return apiError
		})

		if err != nil {
			return err
		}
	}

	for _, dirtyBuilder := range dirtyBuilders {
		resource, err := dirtyBuilder.Build()
		if err != nil {
			return err
		}

		existingResource := resource
		apiError := client.Get(ctx, types.NamespacedName{
			Namespace: resource.GetNamespace(),
			Name:      resource.GetName(),
		}, existingResource)

		if apiError == nil {
			apiError = client.Delete(ctx, existingResource)
			return apiError
		} else if !k8serrors.IsNotFound(apiError) {
			return apiError
		}
	}

	return nil
}
