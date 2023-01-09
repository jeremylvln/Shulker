# Prerequisites

## Kubernetes Cluster

Shulker should be able to be installed on any Kubernetes cluster meeting
the following criterias:

- The minimum Kubernetes version supported is **TODO**
- At least one Linux node is needed for the Shulker operators to work
  (no special architecture is required)

:::note

By default, any **ProxyDeployment** created will create automatically
a Kubernetes Service with the `LoadBalancer` kind. For this behavior
to work properly, your cloud provider should support load balancer
provisionning. While this is a non-issue for almost all cloud providers,
it may be one if you are self-provisionning your own Kubernetes Cluster.

:::

:::note

The node requirements are those for the Shulker operators to work. It
may not reflect those of containers created by Shulker. While there is
no such restrictions by default, a custom configuration from you may
prevent some pods to schedule properly.

:::

All Shulker components should be installed in the same namespace,
`shulker-system` by default.

## Third-party softwares

In addition to a working Kubernetes Cluster, **[cert-manager](https://cert-manager.io/)**
is also required to be installed. It will be used to generate self-signed
certificates for internal use (mostly to secure internal communication
with Kubernetes's controle plane).

**[Prometheus](https://github.com/prometheus-operator/prometheus-operator)**
metrics are also exposed by some components. Monitor manifests can be
optionally installed.
