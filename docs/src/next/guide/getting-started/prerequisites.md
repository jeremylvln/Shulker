# Prerequisites

## Kubernetes Cluster

Shulker should be able to be installed on any Kubernetes cluster meeting
the following criterias:

- The minimum known working Kubernetes version is **1.27**, but it may
  work on older versions as well
- At least one **Linux node** is needed for the Shulker operators to work
  (amd64 or arm64 architectures are supported)

:::info

By default, any `ProxyFleet` will create automatically a Kubernetes Service
of `LoadBalancer` kind. For this behavior to work properly, your cloud provider
should support load balancer provisioning. While this is a non-issue for
almost all cloud providers, it may be one if you are self-provisioning your
own Kubernetes Cluster.

:::

:::info

The node requirements are those for the Shulker operators to work. It
may not reflect those of containers created by Shulker. While there is
no such restrictions by default, a custom configuration from you may
prevent some pods to schedule properly.

:::

All Shulker components should be installed in the same namespace,
`shulker-system` by default.

## Mandatory softwares

### Agones

Shulker delegates the management of game servers (proxies and servers) to
Agones.

- Website: https://agones.dev/site/
- Installation guide: https://agones.dev/site/docs/installation/

Shulker requires that you to configure Agones to work properly:

- Add your Shulker deployment's namespace (`shulker-system` by default) to
  Agones's list of `GameServer` namespaces. This will make Agones create
  the secret containing the gRPC credentials Shulker will use to interact
  with its API. Add the namespace to the `gameservers.namespaces` Helm value
- Enable Agones Allocator component. It is used to summon manually a new
  `GameServer` when needed (mostly used in Shulker addons). Set the
  `agones.allocator.install=true` Helm value. Optionally make its `Service`
  be of type `ClusterIP` so it will be only used internally by setting the
  `agones.allocator.service.serviceType=ClusterIP` value

:::warning

Watch out that while Shulker does not need heavy scaling to handle
production workload, Agones sure does. Please consider your needs when
installing and configuring Agones.

:::

## Optional softwares

**[Prometheus](https://github.com/prometheus-operator/prometheus-operator)**
metrics are also exposed by some components. Monitor manifests can be
optionally installed.
