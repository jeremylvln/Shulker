# Enabling Proxy Protocol

Most of the time, the IP address received by your proxy will be the
one of the cloud provider's load balancer, thus not the one of the
connecting player.

This is a problem because Shulker configures the proxies to expect
that the connecting IP is the same than the one the player used to
authenticate agaisn't Mojang's servers. This is one way to prevent
spoofing attacks.

The Proxy Protocol is a protocol that allows the load balancer to
send the real IP address of the client to the underlying service, here
the proxy. It is supported by most cloud providers.

To enable it, you need to set the `proxyProtocol` option to `true` in
the configuration of your proxy:

```yaml title="proxy.yaml" showLineNumbers
apiVersion: shulkermc.io/v1alpha1
kind: ProxyFleet
metadata:
  name: proxy
spec:
  clusterRef:
    name: my-cluster
  replicas: 1
  template:
    spec:
      config:
        proxyProtocol: true
```

:::note

You'll may need to set the `externalTrafficPolicy` of the Kubernetes
Service created for the `ProxyFleet` to `Local`, to avoid having one of
your node forwarding traffic to another.

:::

:::note

Your cloud provider *may* also expect you of adding some annotations to
the Service as well for them to configure properly your load balancer.
Check out your cloud provider documentation to learn more.

:::

## Real-world example

You can find a real-world example of a `ProxyFleet` with proxy protocol
enabled, for Scaleway Cloud Provider, by looking at the `with-proxy-protocol`
example **[from the repository](https://github.com/jeremylvln/Shulker/tree/main/examples/with-proxy-protocol)**.
