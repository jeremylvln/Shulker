/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package controllers

import (
	"context"
	"fmt"
	"hash/fnv"

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
	resources "github.com/iamblueslime/shulker/libs/resources/src/minecraftserverdeployment"
)

// MinecraftServerDeploymentReconciler reconciles a MinecraftServerDeployment object
type MinecraftServerDeploymentReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers,verbs=get;list;watch;create;update
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftserverdeployments,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftserverdeployments/status,verbs=get;update;patch

func (r *MinecraftServerDeploymentReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	logger := log.FromContext(ctx)

	logger.Info("Reconciling MinecraftServerDeployment")
	minecraftServerDeployment, err := r.getMinecraftServerDeployment(ctx, req.NamespacedName)

	if client.IgnoreNotFound(err) != nil {
		return ctrl.Result{}, err
	} else if k8serrors.IsNotFound(err) {
		// No need to requeue if the resource no longer exists
		return ctrl.Result{}, nil
	}

	cluster := &shulkermciov1alpha1.MinecraftCluster{}
	err = r.Get(ctx, types.NamespacedName{
		Namespace: minecraftServerDeployment.Namespace,
		Name:      minecraftServerDeployment.Spec.ClusterRef.Name,
	}, cluster)
	if err != nil {
		logger.Error(err, "Referenced MinecraftCluster does not exists")
		return ctrl.Result{}, err
	}

	resourceBuilder := resources.MinecraftServerDeploymentResourceBuilder{
		Instance: minecraftServerDeployment,
		Scheme:   r.Scheme,
	}
	builders, dirtyBuilders := resourceBuilder.ResourceBuilders()

	err = ReconcileWithResourceBuilders(r.Client, ctx, builders, dirtyBuilders)
	if err != nil {
		return ctrl.Result{}, err
	}

	allMinecraftServers, err := r.getAllMinecraftServers(ctx, minecraftServerDeployment)
	if err != nil {
		return ctrl.Result{}, err
	}

	templateHash := getMinecraftServerTemplateHash(&minecraftServerDeployment.Spec.Template)
	var matchingMinecraftServers []*shulkermciov1alpha1.MinecraftServer
	var availableReplicas, unavailableReplicas uint

	for _, minecraftServer := range allMinecraftServers.Items {
		if minecraftServer.Labels[shulkermciov1alpha1.MinecraftServerDeploymentTemplateHashLabelName] == templateHash {
			matchingMinecraftServers = append(matchingMinecraftServers, &minecraftServer)
		}

		for _, condition := range minecraftServer.Status.Conditions {
			if shulkermciov1alpha1.MinecraftServerStatusCondition(condition.Type) == shulkermciov1alpha1.MinecraftServerReadyCondition {
				if condition.Status == metav1.ConditionTrue {
					availableReplicas += 1
				} else {
					unavailableReplicas += 1
				}
			}
		}
	}

	if len(matchingMinecraftServers) < int(minecraftServerDeployment.Spec.Replicas) {
		minecraftServersToCreate := int(minecraftServerDeployment.Spec.Replicas) - len(matchingMinecraftServers)

		for i := 0; i < minecraftServersToCreate; i += 1 {
			minecraftServerId := common.RandomResourceId(6)
			minecraftServer := shulkermciov1alpha1.MinecraftServer{}

			labels := r.getMinecraftServerLabels(minecraftServerDeployment)
			for k, v := range minecraftServerDeployment.Spec.Template.Labels {
				labels[k] = v
			}
			labels[shulkermciov1alpha1.MinecraftServerDeploymentTemplateHashLabelName] = templateHash

			minecraftServer.Namespace = minecraftServerDeployment.Namespace
			minecraftServer.Name = fmt.Sprintf("%s-%s-%s", minecraftServerDeployment.Name, templateHash, minecraftServerId)
			minecraftServer.Labels = labels
			minecraftServer.Spec = minecraftServerDeployment.Spec.Template.Spec
			minecraftServer.Spec.ClusterRef = minecraftServerDeployment.Spec.ClusterRef
			minecraftServer.Spec.Configuration = shulkermciov1alpha1.MinecraftServerConfigurationSpec{
				ExistingConfigMapName: resourceBuilder.GetConfigMapName(),
			}

			if err := controllerutil.SetControllerReference(minecraftServerDeployment, &minecraftServer, r.Scheme); err != nil {
				err = fmt.Errorf("failed setting controller reference for MinecraftServer: %v", err)
				return ctrl.Result{}, err
			}

			err = r.Create(ctx, &minecraftServer)
			if err != nil {
				return ctrl.Result{}, err
			}
		}
	}

	selector, err := metav1.LabelSelectorAsSelector(resourceBuilder.GetPodSelector())
	if err != nil {
		return ctrl.Result{}, err
	}

	minecraftServerDeployment.Status.Replicas = minecraftServerDeployment.Spec.Replicas
	minecraftServerDeployment.Status.AvailableReplicas = int32(availableReplicas)
	minecraftServerDeployment.Status.UnavailableReplicas = int32(unavailableReplicas)
	minecraftServerDeployment.Status.Selector = selector.String()

	if availableReplicas > 0 {
		minecraftServerDeployment.Status.SetCondition(shulkermciov1alpha1.MinecraftServerDeploymentAvailableCondition, metav1.ConditionTrue, "AtLeastOneReady", "One or more servers are ready")
	} else {
		minecraftServerDeployment.Status.SetCondition(shulkermciov1alpha1.MinecraftServerDeploymentAvailableCondition, metav1.ConditionFalse, "NotReady", "No server is ready")
	}

	return ctrl.Result{}, r.Status().Update(ctx, minecraftServerDeployment)
}

func (r *MinecraftServerDeploymentReconciler) getMinecraftServerDeployment(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.MinecraftServerDeployment, error) {
	minecraftServerDeployment := &shulkermciov1alpha1.MinecraftServerDeployment{}
	err := r.Get(ctx, namespacedName, minecraftServerDeployment)
	return minecraftServerDeployment, err
}

func (r *MinecraftServerDeploymentReconciler) getMinecraftServerLabels(deployment *shulkermciov1alpha1.MinecraftServerDeployment) map[string]string {
	labels := map[string]string{
		"minecraftcluster.shulkermc.io/name":          deployment.Spec.ClusterRef.Name,
		"minecraftserverdeployment.shulkermc.io/name": deployment.Name,
	}
	return labels
}

func (r *MinecraftServerDeploymentReconciler) getAllMinecraftServers(ctx context.Context, deployment *shulkermciov1alpha1.MinecraftServerDeployment) (*shulkermciov1alpha1.MinecraftServerList, error) {
	list := shulkermciov1alpha1.MinecraftServerList{}
	err := r.List(ctx, &list, client.InNamespace(deployment.Namespace), client.MatchingLabels(r.getMinecraftServerLabels(deployment)))

	return &list, err
}

// SetupWithManager sets up the controller with the Manager.
func (r *MinecraftServerDeploymentReconciler) SetupWithManager(mgr ctrl.Manager) error {
	err := mgr.GetFieldIndexer().IndexField(context.Background(), &shulkermciov1alpha1.MinecraftServerDeployment{}, ".spec.clusterRef.name", func(object client.Object) []string {
		minecraftServerDeployment := object.(*shulkermciov1alpha1.MinecraftServerDeployment)
		return []string{minecraftServerDeployment.Spec.ClusterRef.Name}
	})

	if err != nil {
		return err
	}

	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.MinecraftServerDeployment{}).
		Owns(&shulkermciov1alpha1.MinecraftServer{}).
		Complete(r)
}

func getMinecraftServerTemplateHash(template *shulkermciov1alpha1.MinecraftServerTemplate) string {
	hasher := fnv.New32a()
	hashutil.DeepHashObject(hasher, *template)

	return rand.SafeEncodeString(fmt.Sprint(hasher.Sum32()))
}
