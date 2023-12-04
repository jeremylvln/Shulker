# Using custom server JAR

You may want to use a custom server JAR to replace the default
behavior which is to download the matching version from the
channel.

You have the ability to specify a resource reference to a custom
server JAR in the `MinecraftServer` and `MinecraftServerFleet` CRDs:

```yaml
apiVersion: shulkermc.io/v1alpha1
kind: MinecraftServerFleet
metadata:
  name: my-server
spec:
  clusterRef:
    name: my-cluster
  replicas: 1
  template:
    spec: // [!code focus]
      version: // [!code focus]
        channel: Paper // [!code focus]
        name: 1.20.0 // [!code focus]
        customJar: // [!code focus]
          url: https://example.com/custom-jar.jar // [!code focus]
```

:::warning

While this feature allows you to customize the server software to run,
you can't use it to create servers with unsupported platforms. You
still have to specify the version channel so Shulker is aware of which
agent to inject into the server.

You can, however, expect any standard fork of supported platforms to
work as long as they do not introduce breaking changes.

:::
