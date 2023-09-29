package main

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/julienschmidt/httprouter"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

type FleetAutoscalingHttpController struct {
	Reconciler *MinecraftServerFleetReconciler
}

func (c *FleetAutoscalingHttpController) Register(router *httprouter.Router) {
	router.POST("/minecraftserverfleets/:namespace/:name/summon", c.handleMinecraftServerFleetsSummon)
}

func (c *FleetAutoscalingHttpController) handleMinecraftServerFleetsSummon(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	logger := log.FromContext(r.Context())

	gameServer, err := c.Reconciler.Summon(r.Context(), types.NamespacedName{
		Namespace: ps.ByName("namespace"),
		Name:      ps.ByName("name"),
	})

	if err != nil {
		if k8serrors.IsNotFound(err) {
			logger.Error(err, "Referenced MinecraftServerFleet not found")
			w.WriteHeader(http.StatusNotFound)
			w.Header().Set("Content-Type", "application/json")
		} else {
			logger.Error(err, "Failed to summon game server from MinecraftServerFleet")
			w.WriteHeader(http.StatusInternalServerError)
			w.Header().Set("Content-Type", "application/json")
			json.NewEncoder(w).Encode(err)
		}
	} else {
		logger.Info("Summoned game server from MinecraftServerFleet", "gameServer", fmt.Sprintf("%s/%s", gameServer.ObjectMeta.Namespace, gameServer.ObjectMeta.Name), "minecraftServerFleet", fmt.Sprintf("%s/%s", ps.ByName("namespace"), ps.ByName("name")))
		w.WriteHeader(http.StatusCreated)
		w.Header().Set("Content-Type", "application/json")

		res := make(map[string]string)
		res["namespace"] = gameServer.ObjectMeta.Namespace
		res["name"] = gameServer.ObjectMeta.Name

		jsonRes, err := json.Marshal(res)
		if err != nil {
			logger.Error(err, "ailed to marshal game server")
		}

		w.Write(jsonRes)
	}
}
