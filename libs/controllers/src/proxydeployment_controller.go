/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package controllers

import (
	"context"
	"fmt"
	"hash/fnv"

	corev1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/rand"
	hashutil "k8s.io/kubernetes/pkg/util/hash"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/log"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
	common "github.com/iamblueslime/shulker/libs/resources/src"
	resources "github.com/iamblueslime/shulker/libs/resources/src/proxydeployment"
)

// ProxyDeploymentReconciler reconciles a ProxyDeployment object
type ProxyDeploymentReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=shulkermc.io,resources=proxies,verbs=get;list;watch;create;update
//+kubebuilder:rbac:groups=shulkermc.io,resources=proxydeployments,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=proxydeployments/status,verbs=get;update;patch

func (r *ProxyDeploymentReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	logger := log.FromContext(ctx)

	logger.Info("Reconciling ProxyDeployment")
	proxyDeployment, err := r.getProxyDeployment(ctx, req.NamespacedName)

	if client.IgnoreNotFound(err) != nil {
		return ctrl.Result{}, err
	} else if k8serrors.IsNotFound(err) {
		// No need to requeue if the resource no longer exists
		return ctrl.Result{}, nil
	}

	cluster := &shulkermciov1alpha1.MinecraftCluster{}
	err = r.Get(ctx, types.NamespacedName{
		Namespace: proxyDeployment.Namespace,
		Name:      proxyDeployment.Spec.ClusterRef.Name,
	}, cluster)
	if err != nil {
		logger.Error(err, "Referenced MinecraftCluster does not exists")
		return ctrl.Result{}, err
	}

	resourceBuilder := resources.ProxyDeploymentResourceBuilder{
		Instance: proxyDeployment,
		Scheme:   r.Scheme,
	}
	builders, dirtyBuilders := resourceBuilder.ResourceBuilders()

	err = ReconcileWithResourceBuilders(r.Client, ctx, builders, dirtyBuilders)
	if err != nil {
		return ctrl.Result{}, err
	}

	allProxies, err := r.getAllProxies(ctx, proxyDeployment)
	if err != nil {
		return ctrl.Result{}, err
	}

	templateHash := getProxyTemplateHash(&proxyDeployment.Spec.Template)
	var oldProxies, matchingProxies []*shulkermciov1alpha1.Proxy
	var availableReplicas, unavailableReplicas uint

	for _, proxy := range allProxies.Items {
		if proxy.Labels[shulkermciov1alpha1.ProxyDeploymentTemplateHashLabelName] == templateHash {
			matchingProxies = append(matchingProxies, &proxy)
		} else {
			oldProxies = append(oldProxies, &proxy)
		}

		for _, condition := range proxy.Status.Conditions {
			if shulkermciov1alpha1.ProxyStatusCondition(condition.Type) == shulkermciov1alpha1.ProxyReadyCondition {
				if condition.Status == metav1.ConditionTrue {
					availableReplicas += 1
				} else {
					unavailableReplicas += 1
				}
			}
		}
	}

	if len(matchingProxies) < int(proxyDeployment.Spec.Replicas) {
		proxiesToCreate := int(proxyDeployment.Spec.Replicas) - len(matchingProxies)

		for i := 0; i < proxiesToCreate; i += 1 {
			proxyId := common.RandomResourceId(6)
			proxy := shulkermciov1alpha1.Proxy{}

			labels := r.getProxyLabels(proxyDeployment)
			for k, v := range proxyDeployment.Spec.Template.Labels {
				labels[k] = v
			}
			labels[shulkermciov1alpha1.ProxyDeploymentTemplateHashLabelName] = templateHash

			proxy.Namespace = proxyDeployment.Namespace
			proxy.Name = fmt.Sprintf("%s-%s-%s", proxyDeployment.Name, templateHash, proxyId)
			proxy.Labels = labels
			proxy.Spec = proxyDeployment.Spec.Template.Spec
			proxy.Spec.ClusterRef = proxyDeployment.Spec.ClusterRef
			proxy.Spec.Configuration = shulkermciov1alpha1.ProxyConfigurationSpec{
				ExistingConfigMapName: resourceBuilder.GetConfigMapName(),
			}

			if err := controllerutil.SetControllerReference(proxyDeployment, &proxy, r.Scheme); err != nil {
				err = fmt.Errorf("failed setting controller reference for Proxy: %v", err)
				return ctrl.Result{}, err
			}

			err = r.Create(ctx, &proxy)
			if err != nil {
				return ctrl.Result{}, err
			}
		}
	}

	if len(oldProxies) > 0 {
		for _, proxy := range oldProxies {
			if proxy.Annotations == nil {
				proxy.Annotations = make(map[string]string)
			}

			if proxy.Annotations[shulkermciov1alpha1.ProxyDrainAnnotationName] != "true" {
				proxy.Annotations[shulkermciov1alpha1.ProxyDrainAnnotationName] = "true"
				err = r.Update(ctx, proxy)
				if err != nil {
					return ctrl.Result{}, err
				}
			}
		}
	}

	selector, err := metav1.LabelSelectorAsSelector(resourceBuilder.GetPodSelector())
	if err != nil {
		return ctrl.Result{}, err
	}

	proxyDeployment.Status.Replicas = proxyDeployment.Spec.Replicas
	proxyDeployment.Status.AvailableReplicas = int32(availableReplicas)
	proxyDeployment.Status.UnavailableReplicas = int32(unavailableReplicas)
	proxyDeployment.Status.Selector = selector.String()

	if availableReplicas > 0 {
		proxyDeployment.Status.SetCondition(shulkermciov1alpha1.ProxyDeploymentAvailableCondition, metav1.ConditionTrue, "AtLeastOneReady", "One or more proxies are ready")
	} else {
		proxyDeployment.Status.SetCondition(shulkermciov1alpha1.ProxyDeploymentAvailableCondition, metav1.ConditionFalse, "NotReady", "No proxy is ready")
	}

	return ctrl.Result{}, r.Status().Update(ctx, proxyDeployment)
}

func (r *ProxyDeploymentReconciler) getProxyDeployment(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.ProxyDeployment, error) {
	proxyDeployment := &shulkermciov1alpha1.ProxyDeployment{}
	err := r.Get(ctx, namespacedName, proxyDeployment)
	return proxyDeployment, err
}

func (r *ProxyDeploymentReconciler) getProxyLabels(deployment *shulkermciov1alpha1.ProxyDeployment) map[string]string {
	labels := map[string]string{
		"minecraftcluster.shulkermc.io/name": deployment.Spec.ClusterRef.Name,
		"proxydeployment.shulkermc.io/name":  deployment.Name,
	}
	return labels
}

func (r *ProxyDeploymentReconciler) getAllProxies(ctx context.Context, deployment *shulkermciov1alpha1.ProxyDeployment) (*shulkermciov1alpha1.ProxyList, error) {
	list := shulkermciov1alpha1.ProxyList{}
	err := r.List(ctx, &list, client.InNamespace(deployment.Namespace), client.MatchingLabels(r.getProxyLabels(deployment)))

	return &list, err
}

// SetupWithManager sets up the controller with the Manager.
func (r *ProxyDeploymentReconciler) SetupWithManager(mgr ctrl.Manager) error {
	err := mgr.GetFieldIndexer().IndexField(context.Background(), &shulkermciov1alpha1.ProxyDeployment{}, ".spec.clusterRef.name", func(object client.Object) []string {
		proxyDeployment := object.(*shulkermciov1alpha1.ProxyDeployment)
		return []string{proxyDeployment.Spec.ClusterRef.Name}
	})

	if err != nil {
		return err
	}

	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.ProxyDeployment{}).
		Owns(&corev1.Service{}).
		Owns(&shulkermciov1alpha1.Proxy{}).
		Complete(r)
}

func getProxyTemplateHash(template *shulkermciov1alpha1.ProxyTemplate) string {
	hasher := fnv.New32a()
	hashutil.DeepHashObject(hasher, *template)

	return rand.SafeEncodeString(fmt.Sprint(hasher.Sum32()))
}
