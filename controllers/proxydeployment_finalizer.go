/*
Copyright 2022.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package controllers

import (
	"context"

	clientretry "k8s.io/client-go/util/retry"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
)

const proxyDeploymentDeletionFinalizer = "deletion.finalizers.proxydeployments.shulkermc.io"

func (r *ProxyDeploymentReconciler) addFinalizerIfNeeded(ctx context.Context, proxyDeployment *shulkermciov1alpha1.ProxyDeployment) error {
	if proxyDeployment.ObjectMeta.DeletionTimestamp.IsZero() && !controllerutil.ContainsFinalizer(proxyDeployment, proxyDeploymentDeletionFinalizer) {
		controllerutil.AddFinalizer(proxyDeployment, proxyDeploymentDeletionFinalizer)

		if err := r.Client.Update(ctx, proxyDeployment); err != nil {
			return err
		}
	}

	return nil
}

func (r *ProxyDeploymentReconciler) removeFinalizer(ctx context.Context, proxyDeployment *shulkermciov1alpha1.ProxyDeployment) error {
	controllerutil.RemoveFinalizer(proxyDeployment, proxyDeploymentDeletionFinalizer)

	if err := r.Client.Update(ctx, proxyDeployment); err != nil {
		return err
	}

	return nil
}

func (r *ProxyDeploymentReconciler) prepareForDeletion(ctx context.Context, proxyDeployment *shulkermciov1alpha1.ProxyDeployment) error {
	if controllerutil.ContainsFinalizer(proxyDeployment, proxyDeploymentDeletionFinalizer) {
		if err := clientretry.RetryOnConflict(clientretry.DefaultRetry, func() error {
			// Custom logic here
			return nil
		}); err != nil {
			ctrl.LoggerFrom(ctx).Error(err, "ProxyDeployment deletion")
		}

		if err := r.removeFinalizer(ctx, proxyDeployment); err != nil {
			ctrl.LoggerFrom(ctx).Error(err, "Failed to remove finalizer for deletion")
			return err
		}
	}
	return nil
}
