# Defining Network administrators

You can define a list of player UUIDs to consider _Network
Administrators_. These players will be granted all permissions
on the proxies and servers. This is mostly to ease maintainance
on the entire cluster.

:::danger

These players will be granted the most rights possible, please
act wisely when adding players to this list!

:::

To set the list of network administrators, set the `networkAdmins`
option in your cluster:

```yaml
apiVersion: shulkermc.io/v1alpha1
kind: MinecraftCluster
metadata:
  name: cluster
spec:
  networkAdmins: // [!code focus]
    - 29b2b527-1b59-45df-b7b0-d5ab20d8731a // [!code focus]
```
