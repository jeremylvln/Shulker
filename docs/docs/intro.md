---
slug: /
sidebar_position: 0
---

# Introduction

Shulker aims to be your default choice to managing Minecraft infrastructures
in the cloud with Kubernetes.

> Everybody with some knowledge about Kubernetes
> should be able to connect to a Minecraft Network within 10 minutes. **That's
> the goal.**

## The Idea

Hosting a Minecraft server to play with some friends is fairly easy. And if you
do not want to do it yourself, they are plenty of hosting providers that will
fee you a couple of money to handle this for you.

**However**, once you'll need some custom architecture, like having multiple Minecraft
servers a player can connect to, or if you have the ambition of handling a bigger
number of players, **things gets complicated at best**.

Managing proxies like Velocity or BungeeCord is still manageable by hand, while
their number is still reasonnable. The same apply for servers that are persistent
as they should only be started once and then kept alive. This will only **cost you
money and time**, things that you'll not be able to invest on something else.

Things gets really difficult when your Minecraft servers are ephemeral, as they
should be destroyed and cleaned up on each reboot. Doing this by hand is impossible,
and scripts would only cover the basic cases while struggling on any error.

Containers is part of the solution here, they provide a way of running
isolated programs in a descriptive way, without any human management (apart from
launching the containers, of course). However you'll still have to automate the
creation and destruction of containers.

Let's consider that you have a perfect solution to run your containers automatically
on your server fleet. The last, and maybe the most important, point you'll have
to figure out is the **cost**: having dedicated servers running servers is fine,
however this is profitable only if they are running at 100% usage every hour, every
day. That's where cloud computing shows some benefits: having a dynamic infrastructure
is not only starting Minecraft servers when needed, but also redeeming only the
compute power you need, when you need it.

**This is where Kubernetes and Shulker comes in.**

## Boundaries

To be everybody _go-to_, we should pay close attention to what arbitrary choices
we make in order to simplify your life while not constraining you from custom
development. To achieve that we had to define strong principles that define the
limits of what Shulker should do:

- **Schedule automatically Minecraft proxies and servers from a specification
  you describe**: to allow you configure your softwares without having to
  manage multiple separate files by hand.
- **Keep up-to-date the server registry of your proxies**: as Shulker already has
  the list and availability of all the servers in the Cluster, it should also
  keep the proxies up-to-date about the creation and deletion of servers.
- **Try to save the players as most as possible**: while errors can occur, Shulker
  should try as most as possible to avoid the disconnection of the player.

Everything beyond this should be opt-in at the discretion of the user.
