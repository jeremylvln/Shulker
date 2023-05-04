# Prerequisites

## Kubernetes Cluster

Shulker should be able to be installed on any Kubernetes cluster meeting
the following criterias:

- The minimum Kubernetes version supported is **TODO**
- At least one Linux node is needed for the Shulker operators to work
  (no special architecture is required)

:::note

By default, any `ProxyFleet` will create automatically a Kubernetes Service
of `LoadBalancer` kind. For this behavior to work properly, your cloud provider
should support load balancer provisioning. While this is a non-issue for
almost all cloud providers,  it may be one if you are self-provisioning your
own Kubernetes Cluster.

:::

:::note

The node requirements are those for the Shulker operators to work. It
may not reflect those of containers created by Shulker. While there is
no such restrictions by default, a custom configuration from you may
prevent some pods to schedule properly.

:::

All Shulker components should be installed in the same namespace,
`shulker-system` by default.

## Mandatory softwares

### Cert-Manager

Cert-Manager will be used to generate self-signed certificates for internal
use (mostly to secure internal communication between Shulker components and
Kubernetes's control plane).

- Website: https://cert-manager.io/
- Installation guide: https://cert-manager.io/docs/installation/

### Agones

Shulker delegates the management of game servers (proxies and servers) to
Agones.

- Website: https://agones.dev/site/
- Installation guide: https://agones.dev/site/docs/installation/

:::warning

Watch out that while Shulker does not need heavy scaling to handle
production workload, Agones sure does. Please consider your needs when
installing and configuring Agones.

:::

## Optional softwares

**[Prometheus](https://github.com/prometheus-operator/prometheus-operator)**
metrics are also exposed by some components. Monitor manifests can be
optionally installed.
