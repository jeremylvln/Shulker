package io.shulkermc.proxyagent.adapters.cache

import io.shulkermc.proxyagent.api.ShulkerProxyAPI.PlayerPosition
import redis.clients.jedis.JedisPool
import redis.clients.jedis.params.SetParams
import java.util.Optional
import java.util.UUID

class RedisCacheAdapter(private val jedisPool: JedisPool) : CacheAdapter {
    companion object {
        private const val PLAYER_ID_CACHE_TTL_SECONDS = 60L * 60 * 24 * 14
    }

    override fun registerProxy(proxyName: String) {
        this.jedisPool.resource.use { jedis ->
            val pipeline = jedis.pipelined()
            pipeline.sadd("shulker:proxies", proxyName)
            pipeline.hset("shulker:proxies:last-seen", proxyName, System.currentTimeMillis().toString())
            pipeline.sync()
        }
    }

    override fun unregisterProxy(proxyName: String) {
        this.jedisPool.resource.use { jedis ->
            val playerIds = jedis.smembers("shulker:proxies:$proxyName:players")

            val pipeline = jedis.pipelined()
            pipeline.srem("shulker:proxies", proxyName)
            pipeline.hdel("shulker:proxies:last-seen", proxyName)
            pipeline.del("shulker:proxies:$proxyName:players")
            pipeline.sync()

            val playerPipeline = jedis.pipelined()
            playerIds.forEach { playerId ->
                playerPipeline.srem("shulker:players:online", playerId)
                playerPipeline.hdel("shulker:players:current-proxy", playerId)
                playerPipeline.hdel("shulker:players:current-server", playerId)
            }
            playerPipeline.sync()
        }
    }

    override fun updateProxyLastSeen(proxyName: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.hset("shulker:proxies:last-seen", proxyName, System.currentTimeMillis().toString())
        }
    }

    override fun listRegisteredProxies(): List<CacheAdapter.RegisteredProxy> {
        this.jedisPool.resource.use { jedis ->
            val registeredProxies = jedis.smembers("shulker:proxies")
            val lastSeenMillis = jedis.hgetAll("shulker:proxies:last-seen")

            return registeredProxies.map { proxyName ->
                val lastSeen = lastSeenMillis[proxyName]?.toLong() ?: 0L
                CacheAdapter.RegisteredProxy(proxyName, lastSeen)
            }
        }
    }

    override fun tryLockLostProxiesPurgeTask(ownerProxyName: String, ttlSeconds: Long): Optional<CacheAdapter.Lock> =
        this.tryLock(ownerProxyName, "shulker:lock:lost-proxies-purge", ttlSeconds)

    override fun unregisterServer(serverName: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.del("shulker:servers:$serverName:players")
        }
    }

    override fun listPlayersInServer(serverName: String): List<UUID> {
        this.jedisPool.resource.use { jedis ->
            val playerIds = jedis.smembers("shulker:servers:$serverName:players")
            return playerIds.map(UUID::fromString)
        }
    }

    override fun setPlayerPosition(playerId: UUID, proxyName: String, serverName: String) {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()
            val oldProxyName = jedis.hget("shulker:players:current-proxy", playerIdString)
            val oldServerName = jedis.hget("shulker:players:current-server", playerIdString)

            val pipeline = jedis.pipelined()
            pipeline.sadd("shulker:players:online", playerIdString)
            pipeline.hset("shulker:players:current-proxy", playerIdString, proxyName)
            pipeline.hset("shulker:players:current-server", playerIdString, serverName)

            if (oldProxyName != null)
                pipeline.srem("shulker:proxies:$oldProxyName:players", playerIdString)
            pipeline.sadd("shulker:proxies:$proxyName:players", playerIdString)

            if (oldServerName != null)
                pipeline.srem("shulker:servers:$oldServerName:players", playerIdString)
            pipeline.sadd("shulker:servers:$serverName:players", playerIdString)

            pipeline.sync()
        }
    }

    override fun unsetPlayerPosition(playerId: UUID) {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()
            val currentProxyName = jedis.hget("shulker:players:current-proxy", playerIdString)
            val currentServerName = jedis.hget("shulker:players:current-server", playerIdString)

            val pipeline = jedis.pipelined()
            pipeline.srem("shulker:players:online", playerIdString)
            pipeline.hdel("shulker:players:current-proxy", playerIdString)
            pipeline.hdel("shulker:players:current-server", playerIdString)
            pipeline.srem("shulker:proxies:$currentProxyName:players", playerIdString)
            pipeline.srem("shulker:servers:$currentServerName:players", playerIdString)
            pipeline.sync()
        }
    }

    override fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition> {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()

            val pipeline = jedis.pipelined()
            val proxyNameResponse = pipeline.hget("shulker:players:current-proxy", playerIdString)
            val serverNameResponse = pipeline.hget("shulker:players:current-server", playerIdString)
            pipeline.sync()

            if (proxyNameResponse != null && serverNameResponse != null) {
                val proxyName = proxyNameResponse.get()
                val serverName = serverNameResponse.get()
                return Optional.of(PlayerPosition(proxyName, serverName))
            } else {
                this.unsetPlayerPosition(playerId)
            }

            return Optional.empty()
        }
    }

    override fun isPlayerConnected(playerId: UUID): Boolean {
        this.jedisPool.resource.use { jedis ->
            return jedis.sismember("shulker:players:online", playerId.toString())
        }
    }

    override fun updateCachedPlayerName(playerId: UUID, playerName: String) {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()
            val params = SetParams().ex(PLAYER_ID_CACHE_TTL_SECONDS)

            val pipeline = jedis.pipelined()
            pipeline.set("shulker:uuid-cache:id-to-name:$playerIdString", playerName, params)
            pipeline.set("shulker:uuid-cache:name-to-id:$playerName", playerIdString, params)
            pipeline.sync()
        }
    }

    override fun getPlayerNameFromId(playerId: UUID): Optional<String> {
        this.jedisPool.resource.use { jedis ->
            return Optional.ofNullable(jedis.get("shulker:uuid-cache:id-to-name:$playerId"))
        }
    }

    override fun getPlayerIdFromName(playerName: String): Optional<UUID> {
        this.jedisPool.resource.use { jedis ->
            return Optional.ofNullable(jedis.get("shulker:uuid-cache:name-to-id:$playerName")).map(UUID::fromString)
        }
    }

    override fun getPlayerNamesFromIds(playerIds: List<UUID>): Map<UUID, String> {
        this.jedisPool.resource.use { jedis ->
            val pipeline = jedis.pipelined()
            val responses = playerIds.associateWith { uuid -> pipeline.get("shulker:uuid-cache:id-to-name:$uuid") }
            pipeline.sync()

            return responses.mapValues { (_, response) -> response.get() }
        }
    }

    override fun countOnlinePlayers(): Int {
        this.jedisPool.resource.use { jedis ->
            return jedis.scard("shulker:players:online").toInt()
        }
    }

    private fun tryLock(ownerProxyName: String, key: String, ttlSeconds: Long): Optional<CacheAdapter.Lock> {
        this.jedisPool.resource.use { jedis ->
            val success = jedis.set(key, ownerProxyName, SetParams().nx().ex(ttlSeconds)) != null

            if (success) {
                return Optional.of(object : CacheAdapter.Lock {
                    override fun release() {
                        jedisPool.resource.use { jedis -> jedis.del(key) }
                    }
                })
            }

            return Optional.empty()
        }
    }
}
