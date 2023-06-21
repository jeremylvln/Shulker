package resourceutils

import (
	"context"
	"fmt"

	agonesv1 "agones.dev/agones/pkg/apis/agones/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

func SummonFromFleet(client client.Client, ctx context.Context, fleet *agonesv1.Fleet) (agonesv1.GameServer, error) {
	labels := make(map[string]string, len(fleet.Spec.Template.ObjectMeta.Labels))
	for k, v := range fleet.Spec.Template.ObjectMeta.Labels {
		labels[k] = v
	}

	labels["agones.dev/fleet"] = fleet.Name
	labels["shulkermc.io/summoned"] = "true"

	gameServer := agonesv1.GameServer{
		ObjectMeta: metav1.ObjectMeta{
			GenerateName: fmt.Sprintf("%s-", fleet.Name),
			Name:         "",
			Namespace:    fleet.Namespace,
			Labels:       labels,
			Annotations:  fleet.Spec.Template.ObjectMeta.Annotations,
		},
		Spec: fleet.Spec.Template.Spec,
	}

	if err := controllerutil.SetControllerReference(fleet, &gameServer, client.Scheme()); err != nil {
		return gameServer, fmt.Errorf("failed setting controller reference for GameServer: %v", err)
	}

	if err := client.Create(ctx, &gameServer); err != nil {
		return gameServer, err
	}

	return gameServer, nil
}
