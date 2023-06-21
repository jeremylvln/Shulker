package main

import (
	"net/http"

	"github.com/julienschmidt/httprouter"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/types"
)

type FleetAutoscalingHttpController struct {
	Reconciler *MinecraftServerFleetReconciler
}

func (c *FleetAutoscalingHttpController) Register(router *httprouter.Router) {
	router.POST("/minecraftserverfleets/:namespace/:name/summon", c.handleMinecraftServerFleetsSummon)
}

func (c *FleetAutoscalingHttpController) handleMinecraftServerFleetsSummon(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	err := c.Reconciler.Summon(r.Context(), types.NamespacedName{
		Namespace: ps.ByName("namespace"),
		Name:      ps.ByName("name"),
	})

	if err != nil {
		if k8serrors.IsNotFound(err) {
			w.WriteHeader(http.StatusNotFound)
		} else {
			w.WriteHeader(http.StatusInternalServerError)
		}
	} else {
		w.WriteHeader(http.StatusAccepted)
	}
}
