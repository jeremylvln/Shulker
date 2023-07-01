# Overriding Pod properties

To suit your needs, you may need to override or complete some
properties filled to the underlying Pod. The `podOverrides`
property of both the `ProxyFleet` and `MinecraftServerFleet`
allows you to customize the Pod properties as you were writing
a Pod directly (i.e. the supported sub-properties are identical
to the Pod specification).

You can find all the properties overridable by looking at the
`shulker-crds` package of the repository:

-  [ProxyFleet](https://github.com/jeremylvln/Shulker/blob/main/packages/shulker-crds/v1alpha1/proxyfleet_types.go)
-  [MinecraftServerFleet](https://github.com/jeremylvln/Shulker/blob/main/packages/shulker-crds/v1alpha1/minecraftserverfleet_types.go)

## Add environment variables

Shulker already injects some environment variables that could
be useful. But adding your own is fully supported:

```yaml title="server.yaml" showLineNumbers
apiVersion: shulkermc.io/v1alpha1
kind: MinecraftServerFleet
metadata:
  name: my-server
spec:
  clusterRef:
    name: my-cluster
  replicas: 1
  template:
    spec:
      podOverrides:
        env:
          - name: OPENMATCH_HOST
            value: open-match-frontend.open-match.svc
          - name: OPENMATCH_PORT
            value: '50504'
```

## Setting custom affinities

By default, Agones adds a **preferred* scheduling on nodes
labelled with `agones.dev/role=gameserver`. However you
may want to customize more the scheduling behavior.

For instance, you may want to restrict some servers to some
nodes:

```yaml title="server.yaml" showLineNumbers
apiVersion: shulkermc.io/v1alpha1
kind: MinecraftServerFleet
metadata:
  name: my-server
spec:
  clusterRef:
    name: my-cluster
  replicas: 1
  template:
    spec:
      podOverrides:
        affinity:
          nodeAffinity:
            requiredDuringSchedulingIgnoredDuringExecution:
              nodeSelectorTerms:
                - matchExpressions:
                    - key: devops.example.com/gameserver
                      operator: In
                      values:
                        - my-server
        tolerations:
          - key: "devops.example.com/gameserver"
            operator: "Equal"
            value: "my-server"
            effect: "NoSchedule"
```
