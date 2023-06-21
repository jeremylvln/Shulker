package resourceutils

import (
	agonesv1 "agones.dev/agones/pkg/apis/agones/v1"
	apiequality "k8s.io/apimachinery/pkg/api/equality"
)

func GetActiveGameServerSet(fleet *agonesv1.Fleet, list []agonesv1.GameServerSet) *agonesv1.GameServerSet {
	for _, gsSet := range list {
		if apiequality.Semantic.DeepEqual(gsSet.Spec.Template, fleet.Spec.Template) {
			return &gsSet
		}
	}

	return nil
}
