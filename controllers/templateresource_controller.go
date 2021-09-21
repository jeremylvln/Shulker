/*
Copyright 2021.

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
	"time"

	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	ctrllog "sigs.k8s.io/controller-runtime/pkg/log"

	shulkermciov1alpha1 "github.com/IamBlueSlime/Shulker/apis/v1alpha1"
	"github.com/IamBlueSlime/Shulker/pkg/resource"
	"github.com/go-logr/logr"
)

const templateResourceFinalizer = "templateresource.shulkermc.com/finalizer"

// TemplateResourceReconciler reconciles a TemplateResource object
type TemplateResourceReconciler struct {
	client.Client
	Scheme        *runtime.Scheme
	ResourceStore *resource.ResourceStore
}

//+kubebuilder:rbac:groups=shulkermc.io,resources=templateresources,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=templateresources/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=shulkermc.io,resources=templateresources/finalizers,verbs=update

// Reconcile is part of the main kubernetes reconciliation loop which aims to
// move the current state of the cluster closer to the desired state.
// TODO(user): Modify the Reconcile function to compare the state specified by
// the TemplateResource object against the actual cluster state, and then
// perform operations to make the cluster state reflect the state specified by
// the user.
//
// For more details, check Reconcile and its Result here:
// - https://pkg.go.dev/sigs.k8s.io/controller-runtime@v0.9.2/pkg/reconcile
func (r *TemplateResourceReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	log := ctrllog.FromContext(ctx)

	template_resource := &shulkermciov1alpha1.TemplateResource{}
	err := r.Get(ctx, req.NamespacedName, template_resource)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Info("TemplateResource resource not found. Ignoring since object must be deleted")
			return ctrl.Result{}, nil
		}

		log.Error(err, "Failed to get TemplateResource")
		return ctrl.Result{}, err
	}

	isMarkedToBeDeleted := template_resource.GetDeletionTimestamp() != nil
	if isMarkedToBeDeleted {
		if controllerutil.ContainsFinalizer(template_resource, templateResourceFinalizer) {
			if err := r.finalize(log, template_resource); err != nil {
				return ctrl.Result{}, err
			}

			controllerutil.RemoveFinalizer(template_resource, templateResourceFinalizer)
			err := r.Update(ctx, template_resource)
			if err != nil {
				return ctrl.Result{}, err
			}
		}

		return ctrl.Result{}, nil
	}

	dirty, err := r.ResourceStore.IsResourceDirty(template_resource)
	if err != nil {
		log.Error(err, "Failed to test if TemplateResource is dirty")
		return ctrl.Result{}, err
	}

	if dirty {
		meta.SetStatusCondition(&template_resource.Status.Conditions, metav1.Condition{
			Type:    "Ready",
			Status:  metav1.ConditionFalse,
			Reason:  "FetchingResource",
			Message: "The resource is being fetched",
		})
		err = r.Status().Update(ctx, template_resource)
		if err != nil {
			log.Error(err, "Failed to update TemplateResource status")
			return ctrl.Result{}, err
		}

		hash, err := r.ResourceStore.FetchResource(template_resource)
		if err != nil {
			log.Error(err, "Failed to fetch TemplateResource")
			return ctrl.Result{}, err
		}

		template_resource.Status.Hash = hash
		meta.SetStatusCondition(&template_resource.Status.Conditions, metav1.Condition{
			Type:    "Ready",
			Status:  metav1.ConditionTrue,
			Reason:  "Ready",
			Message: "The resource is ready to be used",
		})
		err = r.Status().Update(ctx, template_resource)
		if err != nil {
			log.Error(err, "Failed to update TemplateResource status")
			return ctrl.Result{}, err
		}
	}

	if !controllerutil.ContainsFinalizer(template_resource, templateResourceFinalizer) {
		controllerutil.AddFinalizer(template_resource, templateResourceFinalizer)
		err = r.Update(ctx, template_resource)
		if err != nil {
			return ctrl.Result{}, err
		}
	}

	return ctrl.Result{
		RequeueAfter: time.Duration(60) * time.Second,
	}, nil
}

func (r *TemplateResourceReconciler) finalize(reqLogger logr.Logger, template_resource *shulkermciov1alpha1.TemplateResource) error {
	return r.ResourceStore.DeleteResource(template_resource)
}

// SetupWithManager sets up the controller with the Manager.
func (r *TemplateResourceReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.TemplateResource{}).
		Complete(r)
}
