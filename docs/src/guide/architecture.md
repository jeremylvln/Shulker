# Architecture

<center>
   <img
      alt="Architecture Diagram"
      src="/basics/basics.excalidraw.png"
   >
</center>

A **Shulker Cluster** is composed of three main _entities_:

1. The **Cluster**, represented by the `MinecraftCluster` CRD (in purple
   in the diagram above) is the primary entity defining some global
   settings of the cluster. At least one is mandatory as most CRDs
   will requires you to provide the name of the **Cluster** to
   attach to.
2. The **Proxy**, represented by the `ProxyFleet` CRD (in orange in the diagram
   above) is an entrypoint for the players to connect to. A proxy will initiate
   connections to the **Servers**.
3. The **Server**, represented by the `MinecraftServerFleet` CRD (in green in the
   diagram above) is a Minecraft Server by itself. They are ephemeral and
   don't have any persistent storage. They should be destroyed and created
   on demand.

You'll notice the responsability of launching the actual containers is delegated
to **[Agones](https://agones.dev/site/)**. Agones is a Kubernetes Controller
created and maintained by Google whose objective if to _Host, Run and Scale
dedicated game servers on Kubernetes_. Shulker rely on Agones to handle the
scheduling and resiliency part while handling itself the Agones recipes.

## What is exactly a Cluster?

A **Cluster** (meaning the `MinecraftCluster` CRD), is the root entity of a Minecraft
Network: every proxy and server needs one to attach to. It could define global
settings multiple sub-entities may need.

:::warning

From a Kubernetes point-of-view, you can have multiple Clusters in a same
**Kubernetes Namespace**.

While it is possible, it will make harder to distinguish which resource is owned
by which cluster and could go as far as causing issues if some clusters are fighting
for the same resource (if two clusters want to have the same secret name, for instance).

:::

:::info

Currently, the `MinecraftCluster` do not do much things, however it is planned
in the future to let it manage a **Limbo mechanism**. A Limbo is a piece of software
emulating a Minecraft Server without any game logic. Its only purpose is to keep the
player connected to the Network while having an almost-zero performance impact.

:::

## Keeping the proxies up-to-date

### Have an up-to-date list of servers

One of the core requirements of Shulker is to keep the proxies up-to-date about all
the creation and deletion of servers. This is usually done manually using the following
flow:

1. A Minecraft server is started with a custom plugin
2. When the Minecraft server is ready, the custom plugin will contact all the proxies
   for them to **add** _you_ in their server list
3. When a player will want to connect to the new server, the proxy will already know
   it and establish the connection
4. When the Minecraft server is shutting down, the same custom plugin will contact all
   the proxies for them to **remove** _you_ form their server list

While this could _easily_ be done by you with custom development, you'll still have to:
create a custom plugin for this purpose, figure out how to broadcast the information to
all the proxies, have a way of retrieving the IP address the proxy should use to connect
players to the server, have a recovery mechanism to handle crashed servers that may not
have informed proxies to remove them for their list.

Shulker, through Kubernetes, knows every proxy and every server along with their
availability. Supported by a plugin agent to be installed on every proxy, the proxies
will have their server list updated immediately upon every event occurring in the
Kubernetes Cluster.

### Have a shared list of connected players

If you want to work with multiple proxies, mostly to ensure high availability of
the cluster, you'll face the challenges of state sharing. Every proxy is
fundamentally independant, and thus, is not aware that:

1. It way not be the only proxy in the cluster
2. Players may be connected to other proxies

This could create weird situations where a player may want to communicate with
another player in a different proxy, but these two proxies being unable to
proceed because they are not aware that the other player is actually connected
to the cluster.

To solve this problem, Shulker has a built-in proxy synchronization layer based
on Redis that is enabled automatically. This will provide some administrative
commands to the proxy to ease player management (TODO ADD PAGE ABOUT COMMANDS),
but also some useful details like showing the total amount of players in all
proxies when pinging your cluster.

:::tip

While Shulker provides a managed single-node Redis by default when creating a
cluster, it is not advised to use it in production as there will be no high
availability nor any availability guarantee. It is only meant to be used when
testing Shulker or at least on staging infrastructure where availability is
not a critical requirement.

:::

## Proxy and Server extensibility

Shulker is only useful if it allows you to configure your proxies and servers
freely. For the servers, this means that you should have a way of configuring
anything from the game mode to the map to download on startup, like you would do
manually by managing all the servers yourself.

In the background, we use the Docker images of **[itzg](https://github.com/itzg)**,
an open-source developer which has developed Docker images for Minecraft proxies
and servers. These images will handle automatically the software download (like
fetching automatically your favorite Bukkit or BungeeCord version) while the
configuration is solely managed by Shulker.

See the appropriate **Recipes** of this documentation to know what could be
configured.
