/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package controllers

import (
	"context"

	corev1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
	resources "github.com/iamblueslime/shulker/libs/resources/src/proxy"
)

// ProxyReconciler reconciles a Proxy object
type ProxyReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups="",resources=pods,verbs=get;list;watch;create;update
//+kubebuilder:rbac:groups=shulkermc.io,resources=proxies,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=proxies/status,verbs=get;update;patch

func (r *ProxyReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	logger := log.FromContext(ctx)

	logger.Info("Reconciling Proxy")
	proxy, err := r.getProxy(ctx, req.NamespacedName)

	if client.IgnoreNotFound(err) != nil {
		return ctrl.Result{}, err
	} else if k8serrors.IsNotFound(err) {
		// No need to requeue if the resource no longer exists
		return ctrl.Result{}, nil
	}

	cluster := &shulkermciov1alpha1.MinecraftCluster{}
	err = r.Get(ctx, types.NamespacedName{
		Namespace: proxy.Namespace,
		Name:      proxy.Spec.ClusterRef.Name,
	}, cluster)
	if err != nil {
		logger.Error(err, "Referenced MinecraftCluster does not exists")
		return ctrl.Result{}, err
	}

	resourceBuilder := resources.ProxyResourceBuilder{
		Instance: proxy,
		Scheme:   r.Scheme,
	}
	builders, dirtyBuilders := resourceBuilder.ResourceBuilders()

	err = ReconcileWithResourceBuilders(r.Client, ctx, builders, dirtyBuilders)
	if err != nil {
		return ctrl.Result{}, err
	}

	pod := corev1.Pod{}
	err = r.Get(ctx, client.ObjectKey{
		Namespace: proxy.Namespace,
		Name:      resourceBuilder.GetPodName(),
	}, &pod)
	if err != nil && !k8serrors.IsNotFound(err) {
		return ctrl.Result{}, err
	}

	if pod.DeletionTimestamp != nil || pod.Status.Phase == corev1.PodSucceeded {
		logger.Info("Pod is terminating, deleting Proxy")
		err = r.Delete(ctx, proxy)
		return ctrl.Result{}, err
	}

	var readyCondition metav1.Condition

	if err == nil {
		readyCondition = proxy.Status.SetCondition(shulkermciov1alpha1.ProxyReadyCondition, metav1.ConditionFalse, "PodNotReady", "Pod is not ready")

		for _, condition := range pod.Status.Conditions {
			if condition.Type == corev1.PodReady && condition.Status == corev1.ConditionTrue {
				readyCondition = proxy.Status.SetCondition(shulkermciov1alpha1.ProxyReadyCondition, metav1.ConditionTrue, "PodReady", "Pod is ready")
			}
		}
	} else {
		readyCondition = proxy.Status.SetCondition(shulkermciov1alpha1.ProxyReadyCondition, metav1.ConditionUnknown, "PodNotExists", "Pod does not exists")
	}

	if readyCondition.Status == metav1.ConditionTrue {
		if proxy.Annotations[shulkermciov1alpha1.ProxyDrainAnnotationName] == "true" {
			proxy.Status.SetCondition(shulkermciov1alpha1.ProxyPhaseCondition, metav1.ConditionUnknown, "Draining", "Proxy is drining and do not accept players")
			proxy.Status.SetCondition(shulkermciov1alpha1.ProxyReadyCondition, metav1.ConditionFalse, "Draining", "Proxy is drining and do not accept players")
		} else {
			proxy.Status.SetCondition(shulkermciov1alpha1.ProxyPhaseCondition, metav1.ConditionUnknown, "Running", "Proxy is running")
		}
	} else {
		proxy.Status.SetCondition(shulkermciov1alpha1.ProxyPhaseCondition, metav1.ConditionUnknown, "Unknown", "Proxy status is unknown")
	}

	return ctrl.Result{}, r.Status().Update(ctx, proxy)
}

func (r *ProxyReconciler) getProxy(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.Proxy, error) {
	proxy := &shulkermciov1alpha1.Proxy{}
	err := r.Get(ctx, namespacedName, proxy)
	return proxy, err
}

// SetupWithManager sets up the controller with the Manager.
func (r *ProxyReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.Proxy{}).
		Owns(&corev1.Pod{}).
		Owns(&corev1.ConfigMap{}).
		Complete(r)
}
