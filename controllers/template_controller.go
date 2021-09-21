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
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	ctrllog "sigs.k8s.io/controller-runtime/pkg/log"

	shulkermciov1alpha1 "github.com/IamBlueSlime/Shulker/apis/v1alpha1"
	"github.com/IamBlueSlime/Shulker/pkg/resource"
	"github.com/go-logr/logr"
)

const templateFinalizer = "template.shulkermc.com/finalizer"

// TemplateReconciler reconciles a Template object
type TemplateReconciler struct {
	client.Client
	Scheme        *runtime.Scheme
	ResourceStore *resource.ResourceStore
}

//+kubebuilder:rbac:groups=shulkermc.io,resources=templates,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=templates/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=shulkermc.io,resources=templates/finalizers,verbs=update

func (r *TemplateReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	log := ctrllog.FromContext(ctx)

	template := &shulkermciov1alpha1.Template{}
	err := r.Get(ctx, req.NamespacedName, template)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Info("Template resource not found. Ignoring since object must be deleted")
			return ctrl.Result{}, nil
		}

		log.Error(err, "Failed to get Template")
		return ctrl.Result{}, err
	}

	isMarkedToBeDeleted := template.GetDeletionTimestamp() != nil
	if isMarkedToBeDeleted {
		if controllerutil.ContainsFinalizer(template, templateFinalizer) {
			if err := r.finalize(log, template); err != nil {
				return ctrl.Result{}, err
			}

			controllerutil.RemoveFinalizer(template, templateFinalizer)
			err := r.Update(ctx, template)
			if err != nil {
				return ctrl.Result{}, err
			}
		}

		return ctrl.Result{}, nil
	}

	resources := make([]*shulkermciov1alpha1.TemplateResource, 0)
	for _, resource_name := range template.Spec.Resources {
		resource := &shulkermciov1alpha1.TemplateResource{}
		err := r.Get(ctx, types.NamespacedName{
			Namespace: req.Namespace,
			Name:      resource_name,
		}, resource)
		if err != nil {
			if errors.IsNotFound(err) {
				log.Info("TemplateResource resource not found. Ignoring since object must be deleted")
				return ctrl.Result{}, nil
			}

			log.Error(err, "Failed to get TemplateResource")
			return ctrl.Result{}, err
		}

		if meta.IsStatusConditionFalse(resource.Status.Conditions, "Ready") {
			log.Info("One of the Template resources is not yet ready")
			return ctrl.Result{}, nil
		}

		resources = append(resources, resource)
	}

	dirty, err := r.ResourceStore.IsTemplateDirty(template, resources)
	if err != nil {
		log.Error(err, "Failed to test if Template is dirty")
		return ctrl.Result{}, err
	}

	if dirty {
		meta.SetStatusCondition(&template.Status.Conditions, metav1.Condition{
			Type:    "Ready",
			Status:  metav1.ConditionFalse,
			Reason:  "PackingTemplate",
			Message: "The template is being packed",
		})
		err = r.Status().Update(ctx, template)
		if err != nil {
			log.Error(err, "Failed to update Template status")
			return ctrl.Result{}, err
		}

		err = r.ResourceStore.PackTemplate(template, resources)
		if err != nil {
			log.Error(err, "Failed to pack Template")
			return ctrl.Result{}, err
		}

		template.Status.LastPackDate = metav1.Time{
			Time: time.Now(),
		}
		meta.SetStatusCondition(&template.Status.Conditions, metav1.Condition{
			Type:    "Ready",
			Status:  metav1.ConditionTrue,
			Reason:  "Ready",
			Message: "The template is ready to be used",
		})
		err = r.Status().Update(ctx, template)
		if err != nil {
			log.Error(err, "Failed to update Template status")
			return ctrl.Result{}, err
		}
	}

	if !controllerutil.ContainsFinalizer(template, templateFinalizer) {
		controllerutil.AddFinalizer(template, templateFinalizer)
		err = r.Update(ctx, template)
		if err != nil {
			return ctrl.Result{}, err
		}
	}

	return ctrl.Result{
		RequeueAfter: time.Duration(30) * time.Second,
	}, nil
}

func (r *TemplateReconciler) finalize(reqLogger logr.Logger, template *shulkermciov1alpha1.Template) error {
	return r.ResourceStore.DeleteTemplate(template)
}

// SetupWithManager sets up the controller with the Manager.
func (r *TemplateReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.Template{}).
		Complete(r)
}
