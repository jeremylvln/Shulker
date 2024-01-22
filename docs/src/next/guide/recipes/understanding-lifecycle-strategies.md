# Understanding Lifecycle Strategies

Your proxies and servers lifecycles are _described_ by Shulker but
are actually managed by **Agones**.

You may want to customize the lifecycle of your servers so Agones
does not disturb your players at innapropriate times.

:::info EXAMPLE

For instance, you may not want Agones to update your fleet of servers
because you upgraded a plugin while some players are in a mini-game.

:::

While Shulker's server agent marks your server as `Ready` when the
agent is fully loaded, you may want to also mark your server as
`Allocated` when an interrupted game session is needed (a mini-game
for instance).

Shulker allows you to choose a **Lifecycle Strategy** that will
change the automatic behaviors of Agones. It can be changed on
the `MinecraftServer` and `MinecraftServerFleet`:

```yaml
apiVersion: shulkermc.io/v1alpha1
kind: MinecraftServerFleet
metadata:
  name: dropper-game
spec:
  clusterRef:
    name: getting-started
  replicas: 1
  template:
    spec:
      clusterRef:
        name: getting-started
      tags:
        - lobby
      version:
        channel: Paper
        name: '1.18.2'
      config:  // [!code focus]
        lifecycleStrategy: AllocateWhenNotEmpty // [!code focus]
```

## `AllocateWhenNotEmpty` strategy

With this strategy, Shulker's server agent will mark your server as
`Allocated` when at lease one player is connected on the server.

This will disable any automatic reschedule of Agones that may be
due to a plugin upgrade. For your server to be updated, you'll have to
either:

1. Have all the players disconnected for the server is set back to
   `Ready`
2. Shutdown yourself the server (with the `/server` command for instance)

:::info

Shulker will still mark your server as `Ready` once the agent is
fully loaded.

:::

## `Manual` strategy

With this strategy, no extra work is done apart marking the server as
`Ready` after loading. It is up to you and custom implementation to manage
the lifecycle of your server.

:::warning

Using this strategy with no custom implementation will keep the `Ready`
state forever. Thus, Agones will always think that the server is not used
and it may be recreated at any time for any reason.

:::
