# Your First Cluster

In this chapter, you'll create your first Minecraft Cluster. It is
based on the "Getting Started" example available on
[GitHub](https://github.com/jeremylvln/Shulker/tree/main/examples/getting-started).

## Creating your tutorial environment

For you to clean the resources created during this tutorial, we
will first create a Kubernetes namespace to hold our resources:

```bash
$ kubectl create ns shulker-tutorial
```

At the end of this tutorial, the cleaning will be as simple as deleting
the namespace.

## A _spoon_ of MinecraftCluster

Everything starts by creating a `MincraftCluster`. As described in
the **[Architecture](/architecture)** chapter, it is the most important
entity because it is referenced in many other resources.

Create a `cluster.yaml` file containing the following:

<<< ../../../../examples/getting-started/cluster.yaml

And then apply this file:

```bash
$ kubectl apply -f cluster.yaml -n shulker-tutorial
```

After this, the Shulker Operator should have started some work but
nothing really visible: no Pods are created yet. However, we are now ready
for the _main course_.

## A _drizzle_ of Proxy

For the players to be able to connect to our Minecraft Cluster, we need
at least one **Proxy**. So let's create a `ProxyFleet`. It will handle
replication among other things, but more importantly, it will create and
bind a **Kubernetes Service** for us, with the `LoadBalancer` type by default.

:::info

Each **ProxyDeployment** has its own, dedicated Service. This allows you
to have a bunch of "public" proxies for your players while having a
separate entrypoint for privileged players (like a staff, etc...).

:::

Create a `proxy.yaml` file containing the following:

<<< ../../../../examples/getting-started/proxy.yaml

And then apply this file:

```bash
$ kubectl apply -f proxy.yaml -n shulker-tutorial
```

You'll soon start to see a `public-xxx-xxx` Pod being scheduled. Wait
a little bit for it to be ready. If you look at the logs of this Pod,
you'll probably see some lines like this:

```
[io.shulkermc.serveragent.BootstrapPlugin]: Identified Shulker proxy: shulker-tutorial/public-xxx-xxx
[io.fabric8.kubernetes.client.dsl.internal.VersionUsageUtils]: The client is using resource type 'proxies' with unstable version 'v1alpha1'
[io.shulkermc.serveragent.BootstrapPlugin]: Proxy will be force stopped in 86400 seconds
[io.fabric8.kubernetes.client.dsl.internal.VersionUsageUtils]: The client is using resource type 'minecraftservers' with unstable version 'v1alpha1'
```

This is due to Shulker injecting automatically an agent plugin to handle
some work that cannot be only done from the infrastructure.

:::info

You probably question yourself about the log line indicating that the
proxy will be force stopped after some time. Due to players being
constantly connected to the proxies, updating them is hard because the
only way of doing it is by rebooting it, causing players to disconnect
without any way to recover them (at least on **Minecraft: Java Edition**).

After a fixed amount of time, a proxy will enter the **Drain** mode,
meaning that Kubernetes will exclude this proxy from the Service. This
will prevent any new player from connecting to this proxy allowing it
to be drained step by step (considering the players will deconnect
after some play time).

The proxy will check that it is empty every 30 seconds, if there is
no player, it will shutdown itself, otherwise it will wait.

It is important to note that a new Proxy is automatically created when
one enters the **Drain** mode, to ensure the availability of the
**ProxyFleet**.

:::

While you read the block above, your Kubernetes Cluster should already
have created a `LoadBalancer` Service. If you add the IP to the game's
server list, you should see an output similar to the following:

<center>
  <img
    alt="Server List Screenshot"
    src="/getting-started/motd.png"
  />
</center>

However, if you try to connect, you'll be kicked out with the following
error:

<center>
  <img
    alt="No Limbo Screenshot"
    src="/getting-started/no-limbo.png"
  />
</center>

By default, the Shulker Agent will try to connect the player to the first
server with the `lobby` tag. If none are found the agent will try to fallback
on a server with the `limbo` tag. Because there is no server currently running
in the cluster, there is no other choice than kicking the player.

## A _pinch_ of MinecraftServer

The concept is more or less the same as for a Proxy, except that no
Kubernetes Service is created because the only entrypoints are the Proxies.

Create a `minecraftserver.yaml` file containing the following:

<<< ../../../../examples/getting-started/minecraftserver.yaml

And then apply this file:

```bash
$ kubectl apply -f minecraftserver.yaml -n shulker-tutorial
```

A new Pod representing the MinecraftServer will be created. After a short
period, the Pod will be marked as healthy. If you look back to the Proxy's
logs, you'll see that the Shulker Agent registered automatically the server
into its server list:

```
[io.shulkermc.serveragent.BootstrapPlugin]: Registering server 'limbo-xxx-xxx' to directory
```

Finally, without any surprise, if you retry to connect to the proxy, you'll
arrive on the server you just created:

<center>
  <img
    alt="Server Screenshot"
    src="/getting-started/server.png"
  />
</center>

:::tip

Pay attention to the `tags` field of the MinecraftServer resource you just
created. The Shulker Agent will connect the player to the first server it
finds with the `lobby` tag. If none, it will try to fallback on one with the
`limbo` tag. It is optional to provide tags to servers. They could, however,
be retrieved using Shulker's Proxy API.

:::

**Congratulations!** You have successfully created your first Minecraft Cluster using Shulker!

## Cleaning everything up

Deleting the three resources you created will destroy all the resources
created by Shulker under the hood:

```bash
$ kubectl delete -f minecraftserver.yaml
$ kubectl delete -f proxy.yaml
$ kubectl delete -f cluster.yaml
```

If you created a namespace dedicated to this tutorial, you can simply
delete it without bothering deleting the resources one-by-one:

```bash
$ kubectl delete ns shulker-tutorial
```

:::tip

The command could block some seconds/minutes during the deletion of all
the resources contained in the namespace.

:::
