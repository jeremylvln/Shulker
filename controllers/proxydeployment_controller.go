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

	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"

	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	resource "shulkermc.io/m/v2/internal/resource/proxy"
)

// ProxyDeploymentReconciler reconciles a ProxyDeployment object
type ProxyDeploymentReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups="",resources=services,verbs=get;list;watch;create;update
//+kubebuilder:rbac:groups="apps",resources=deployments,verbs=get;list;watch;create;update;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=proxydeployments,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=proxydeployments/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=shulkermc.io,resources=proxydeployments/finalizers,verbs=update

func (r *ProxyDeploymentReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	logger := log.FromContext(ctx)

	proxyDeployment, err := r.getProxyDeployment(ctx, req.NamespacedName)

	if client.IgnoreNotFound(err) != nil {
		return ctrl.Result{}, err
	} else if k8serrors.IsNotFound(err) {
		// No need to requeue if the resource no longer exists
		return ctrl.Result{}, nil
	}

	// Check if the resource has been marked for deletion
	if !proxyDeployment.ObjectMeta.DeletionTimestamp.IsZero() {
		logger.Info("Deleting")
		return ctrl.Result{}, r.prepareForDeletion(ctx, proxyDeployment)
	}

	if err := r.addFinalizerIfNeeded(ctx, proxyDeployment); err != nil {
		return ctrl.Result{}, err
	}

	minecraftCluster := &shulkermciov1alpha1.MinecraftCluster{}
	err = r.Get(ctx, types.NamespacedName{
		Namespace: proxyDeployment.Namespace,
		Name:      proxyDeployment.Spec.ClusterRef.Name,
	}, minecraftCluster)
	if err != nil {
		return ctrl.Result{}, err
	}

	resourceBuilder := resource.ProxyDeploymentResourceBuilder{
		Instance: proxyDeployment,
		Cluster:  minecraftCluster,
		Scheme:   r.Scheme,
	}
	builders, dirtyBuilders := resourceBuilder.ResourceBuilders()

	err = ReconcileWithResourceBuilders(r.Client, ctx, builders, dirtyBuilders)
	if err != nil {
		return ctrl.Result{}, err
	}

	deployment := appsv1.Deployment{}
	err = r.Get(ctx, client.ObjectKey{
		Namespace: proxyDeployment.Namespace,
		Name:      resourceBuilder.GetDeploymentName(),
	}, &deployment)
	if err != nil && !k8serrors.IsNotFound(err) {
		return ctrl.Result{}, err
	}

	if err == nil {
		selector, err := metav1.LabelSelectorAsSelector(resourceBuilder.GetPodSelector())
		if err != nil {
			return ctrl.Result{}, err
		}

		proxyDeployment.Status.Replicas = int32(deployment.Status.Replicas)
		proxyDeployment.Status.AvailableReplicas = int32(deployment.Status.AvailableReplicas)
		proxyDeployment.Status.UnavailableReplicas = int32(deployment.Status.UnavailableReplicas)
		proxyDeployment.Status.Selector = selector.String()

		for _, condition := range deployment.Status.Conditions {
			if condition.Type == appsv1.DeploymentAvailable {
				proxyDeployment.Status.SetCondition(shulkermciov1alpha1.ProxyDeploymentAvailableCondition, metav1.ConditionStatus(condition.Status), condition.Reason, condition.Message)
			} else if condition.Type == appsv1.DeploymentProgressing {
				proxyDeployment.Status.SetCondition(shulkermciov1alpha1.ProxyDeploymentProgressingCondition, metav1.ConditionStatus(condition.Status), condition.Reason, condition.Message)
			}
		}

		if proxyDeployment.Status.AvailableReplicas > 0 {
			proxyDeployment.Status.SetCondition(shulkermciov1alpha1.ProxyDeploymentReadyCondition, metav1.ConditionTrue, "Ready", "Proxy is ready")
		} else {
			proxyDeployment.Status.SetCondition(shulkermciov1alpha1.ProxyDeploymentReadyCondition, metav1.ConditionFalse, "NotReady", "Proxy is not ready")
		}
	} else {
		proxyDeployment.Status.SetCondition(shulkermciov1alpha1.ProxyDeploymentReadyCondition, metav1.ConditionFalse, "NotReady", "Proxy is not ready")
	}

	return ctrl.Result{}, r.Status().Update(ctx, proxyDeployment)
}

func (r *ProxyDeploymentReconciler) getProxyDeployment(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.ProxyDeployment, error) {
	proxyDeploymentInstance := &shulkermciov1alpha1.ProxyDeployment{}
	err := r.Get(ctx, namespacedName, proxyDeploymentInstance)
	return proxyDeploymentInstance, err
}

func (r *ProxyDeploymentReconciler) SetupWithManager(mgr ctrl.Manager) error {
	err := mgr.GetFieldIndexer().IndexField(context.Background(), &shulkermciov1alpha1.ProxyDeployment{}, ".spec.minecraftClusterRef.name", func(object client.Object) []string {
		proxyDeployment := object.(*shulkermciov1alpha1.ProxyDeployment)
		return []string{proxyDeployment.Spec.ClusterRef.Name}
	})

	if err != nil {
		return err
	}

	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.ProxyDeployment{}).
		Owns(&appsv1.Deployment{}).
		Owns(&corev1.ConfigMap{}).
		Owns(&corev1.Service{}).
		Owns(&corev1.ServiceAccount{}).
		Owns(&rbacv1.RoleBinding{}).
		Complete(r)
}
