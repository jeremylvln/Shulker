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

	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/api/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/intstr"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	ctrllog "sigs.k8s.io/controller-runtime/pkg/log"

	shulkermciov1alpha1 "github.com/IamBlueSlime/Shulker/apis/v1alpha1"
)

// MinecraftServerReconciler reconciles a MinecraftServer object
type MinecraftServerReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers/finalizers,verbs=update
//+kubebuilder:rbac:groups=apps,resources=deployments,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=core,resources=pods,verbs=get;list;watch
//+kubebuilder:rbac:groups=core,resources=services,verbs=get;list;watch;create;update;patch;delete

func (r *MinecraftServerReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	log := ctrllog.FromContext(ctx)

	mcs := &shulkermciov1alpha1.MinecraftServer{}
	err := r.Get(ctx, req.NamespacedName, mcs)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Info("MinecraftServer resource not found. Ignoring since object must be deleted")
			return ctrl.Result{}, nil
		}

		log.Error(err, "Failed to get MinecraftServer")
		return ctrl.Result{}, err
	}

	// Check if the deployment already exists, if not create a new one
	deployment_found := &appsv1.Deployment{}
	err = r.Get(ctx, types.NamespacedName{Name: mcs.Name, Namespace: mcs.Namespace}, deployment_found)
	if err != nil && errors.IsNotFound(err) {
		dep := r.createDeployment(mcs)
		log.Info("Creating a new Deployment", "Deployment.Namespace", dep.Namespace, "Deployment.Name", dep.Name)
		err = r.Create(ctx, dep)
		if err != nil {
			log.Error(err, "Failed to create new Deployment", "Deployment.Namespace", dep.Namespace, "Deployment.Name", dep.Name)
			return ctrl.Result{}, err
		}
		return ctrl.Result{Requeue: true}, nil
	} else if err != nil {
		log.Error(err, "Failed to get Deployment")
		return ctrl.Result{}, err
	}

	// Check if the Service already exists, if not create a new one
	service_found := &corev1.Service{}
	err = r.Get(ctx, types.NamespacedName{Name: mcs.Name, Namespace: mcs.Namespace}, service_found)
	if err != nil && errors.IsNotFound(err) && mcs.Spec.Service.Enabled {
		svc := r.createService(mcs)
		log.Info("Creating a new Service", "Service.Namespace", svc.Namespace, "Service.Name", svc.Name)
		err = r.Create(ctx, svc)
		if err != nil {
			log.Error(err, "Failed to create new Service", "Service.Namespace", svc.Namespace, "Service.Name", svc.Name)
			return ctrl.Result{}, err
		}
		return ctrl.Result{Requeue: true}, nil
	} else if err != nil {
		log.Error(err, "Failed to get Service")
		return ctrl.Result{}, err
	} else if !mcs.Spec.Service.Enabled {
		log.Info("Deleting existing Service", "Service.Namespace", service_found.Namespace, "Service.Name", service_found.Name)
		err = r.Delete(ctx, service_found)
		if err != nil {
			log.Error(err, "Failed to delete existing Service", "Service.Namespace", service_found.Namespace, "Service.Name", service_found.Name)
			return ctrl.Result{}, err
		}
		return ctrl.Result{Requeue: true}, nil
	}

	return ctrl.Result{}, nil
}

func (r *MinecraftServerReconciler) createDeployment(mcs *shulkermciov1alpha1.MinecraftServer) *appsv1.Deployment {
	ls := labelsForMinecraftServer(mcs)
	replicas := int32(1)

	dep := &appsv1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      mcs.Name,
			Namespace: mcs.Namespace,
		},
		Spec: appsv1.DeploymentSpec{
			Replicas: &replicas,
			Selector: &metav1.LabelSelector{
				MatchLabels: ls,
			},
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: ls,
				},
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{{
						Image: "ghcr.io/iamblueslime/shulker-pod",
						Name:  "minecraft-server",
						Ports: []corev1.ContainerPort{{
							ContainerPort: 25565,
							Name:          "minecraft",
						}},
						Env: []corev1.EnvVar{{
							Name:  "SHULKER_TEMPLATE_URL",
							Value: "https://i.jeremylvln.fr/shulker/template.tar.gz",
						}},
						Resources: corev1.ResourceRequirements{
							Requests: corev1.ResourceList{
								corev1.ResourceCPU:    resource.MustParse("500m"),
								corev1.ResourceMemory: resource.MustParse("256m"),
							},
						},
					}},
				},
			},
		},
	}

	// Set MinecraftServer ownership for Deployment
	ctrl.SetControllerReference(mcs, dep, r.Scheme)
	return dep
}

func (r *MinecraftServerReconciler) createService(mcs *shulkermciov1alpha1.MinecraftServer) *corev1.Service {
	ls := labelsForMinecraftServer(mcs)

	dep := &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:        mcs.Name,
			Namespace:   mcs.Namespace,
			Annotations: mcs.Spec.Service.Annotations,
			Labels:      mcs.Spec.Service.Labels,
		},
		Spec: corev1.ServiceSpec{
			Selector: ls,
			Type:     corev1.ServiceType(mcs.Spec.Service.Type),
			Ports: []corev1.ServicePort{{
				Protocol: corev1.ProtocolTCP,
				Port:     25565,
				TargetPort: intstr.IntOrString{
					Type:   intstr.String,
					StrVal: "minecraft",
				},
			}},
		},
	}

	// Set MinecraftServer ownership for Deployment
	ctrl.SetControllerReference(mcs, dep, r.Scheme)
	return dep
}

func labelsForMinecraftServer(mcs *shulkermciov1alpha1.MinecraftServer) map[string]string {
	return map[string]string{
		"app.kubernetes.io/name":             "shulker-minecraft-server",
		"shulkermc.io/minecraft-server-name": mcs.Name,
		"shulkermc.io/template-name":         mcs.Spec.TemplateRef,
	}
}

// SetupWithManager sets up the controller with the Manager.
func (r *MinecraftServerReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.MinecraftServer{}).
		Owns(&appsv1.Deployment{}).
		Owns(&corev1.Service{}).
		Complete(r)
}
