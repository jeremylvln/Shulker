# Installation

Shulker is composed of multiple components, some of them being
optional. By design, only the **Shulker Operator** is required
to be installed as it contains the core logic.

## Using Helm

If you need to fine-tune Shulker and its different components,
an exhaustive Helm chart is provided. The default configuration
is enough to get started.

The Helm Chart is available in the [`kube/helm`](https://github.com/jeremylvln/Shulker/tree/main/kube/helm)
folder of the repository.

To install Shulker using Helm:

```bash
$ git clone https://github.com/jeremylvln/Shulker
$ cd Shulker/kube/helm
$ helm install -n shulker-system .
```

## Using pre-rendered manifests

Pre-rendered manifests for common uses are provided and are
generated for the Helm charts. It allows you to test Shulker
in your cluster without hassle.

The manifests are available in the [`kube/manifests`](https://github.com/jeremylvln/Shulker/tree/main/kube/manifests)
folder of the repository.

There are 4 pre-rendered variants available:

- `stable.yaml`: a default configuration as you would render
  the Helm chart without modifying the values
- `stable-with-prometheus.yaml`: the same default configuration
  with Prometheus support, including `ServiceMonitor` for the
  different components
- `next.yaml`: the same configuration as for `stable.yaml`, with
  the images tagged to `next` to quickly test the future release
- `next-with-prometheus.yaml`: the combination of `next.yaml` with
  Prometheus support

You can apply them directly with `kubectl`:

```bash
$ kubectl apply -f stable.yaml -n shulker-system
```
