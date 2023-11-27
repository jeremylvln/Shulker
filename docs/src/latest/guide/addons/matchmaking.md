# Matchmaking <Badge type="danger" text="alpha" />

Shulker Matchmaking is an addon for Shulker providing queuing
capabilities to your `MinecraftServerFleets`. Players will be able
to join **matchmaking queues**.

This addon makes use of [Open Match](https://open-match.dev/site/), a match
making system created by Google meant to be scalable and deeply configurable.

It will allow you to define `MatchmakingQueues` that associate a `MinecraftServerFleet`
with a **matchmaking function** (an algorithm that groups queued players
to form matches). While you'll be able to create your own match making
algorithms to fit your needs, the addon provides basic algorithm sufficient
for most use-cases:

- A **Batch** algorithm, to group players _as they go_, without distinction.
  This makes easy for you to create a queue and provision servers when they
  are needed
- A **Elo** algorithm, which will group players depending on a _global score_
  that you'll have to define and implement

:::warning

The Elo algorithm is planned but is not available for now.

:::

While manipulating Open Match directly to handle the queues is possible,
Shulker Matchmaking makes it really easy by providing an SDK to be used in
your plugins. After installing and scaling Open Match depending on your needs,
you should forget about it.

:::tip

This documentation will make use of the terms used by Open Match, it is advised
for you to read the Open Match's documentation prior to the use of this addon:

https://open-match.dev/site/docs/guides/matchmaker/

:::

## Installation

Shulker Matchmaking requires [Open Match](https://open-match.dev/site/) to be
installed on your Kubernetes cluster. You may need to tune its configuration
to fit your needs, especially if you require high-availabiilty of your matchmaking
system.

The addon can be enabled using the Helm chart, by toggling the `enabled` flag
in the `values.yaml` file:

```yaml
shulker-addon-matchmaking:
  enabled: true
```

This will create two deployments:

- One named `director` that will read your `MatchmakingQueues`, poll Open Match
  for queued players at a fixed interval and finally will create and assign
  the servers
- One named `mmf` _(for Match Making Function)_ that exposes the built-in
  algorithms for you to use out-of-the-box

:::warning

While the first one cannot be scaled for now (this is a limitation that could
be removed later on after a stabilization period), the `mmf` deployment **can**
and **should** as it will be highly sollicitated if you have a large number
of `MatchmakingQueues`. This is done by manipulating the Helm values.

:::

## Usage

### Creating a `MatchmakingQueue`

A `MatchmakingQueue` is a Kubernetes resource describing the shape of the
matches to create. It associates a `MinecraftServerFleet` and a matchmaking
function. It also specifies the size boundaries of the matches to create:

```yaml
apiVersion: matchmaking.shulkermc.io/v1alpha1
kind: MatchmakingQueue
metadata:
  name: free-for-all
spec:
  targetFleetRef:
    name: free-for-all
  mmf:
    builtIn:
      type: Batch
  minPlayers: 8
  maxPlayers: 12
```

The previous manifests describes a `MatchmakingQueue` that:

- targets a `MinecraftServerFleet` named `free-for-all`
- groups the players using the built-in `Batch` algorithm
- creates matches of 12 players at most
- allows creation of partial matches for at least 8 players

:::tip

The `minPlayers` field is optional and can be ommited. When defined,
the built-in matchmaking functions will allow the creation of _partial matches_,
using the **backfills** machanism of Open Match (see Open Match's documentation for
more information).

Otherwise, a _full match_ will only be created when there are at lease `maxPlayers`
players in the queue.

:::

### Creating in-game tickets

TODO.

## Recipes

### Available built-in matchmaking functions

| Name      | Description                                                                          |
| --------- | ------------------------------------------------------------------------------------ |
| **Batch** | Groups the players as they go, without any discriminator                             |
| **Elo**   | Groups the players depending on an integer score and their waiting time in the queue |

### Using a custom matchmaking function

You can create your own matchmaking function by exposing a gRPC server compliant with
Open Match's specification. After deploying it in your Kubernetes cluster, you can refer
to it in your `MatchmakingQueue`:

```yaml
apiVersion: matchmaking.shulkermc.io/v1alpha1
kind: MatchmakingQueue
metadata:
  name: free-for-all
spec:
  targetFleetRef:
    name: free-for-all
  mmf: // [!code focus]
    provided: // [!code focus]
      host: my-mmf // [!code focus]
      port: 9876 // [!code focus]
```
