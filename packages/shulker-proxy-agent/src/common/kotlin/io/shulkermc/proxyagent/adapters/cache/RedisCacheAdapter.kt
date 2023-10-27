package io.shulkermc.proxyagent.adapters.cache

import io.shulkermc.proxyagent.api.ShulkerProxyAPI.PlayerPosition
import redis.clients.jedis.JedisPool
import redis.clients.jedis.params.SetParams
import java.util.Optional
import java.util.UUID

class RedisCacheAdapter(private val jedisPool: JedisPool) : CacheAdapter {
    override fun registerProxy(name: String) {
        this.jedisPool.resource.use { jedis ->
            val pipeline = jedis.pipelined()
            pipeline.sadd("shulker:proxies", name)
            pipeline.hset("shulker:proxies:last-seen", name, System.currentTimeMillis().toString())
            pipeline.sync()
        }
    }

    override fun unregisterProxy(name: String) {
        this.jedisPool.resource.use { jedis ->
            val pipeline = jedis.pipelined()
            pipeline.srem("shulker:proxies", name)
            pipeline.hdel("shulker:proxies:last-seen", name)
            val playerIdsResponse = pipeline.smembers("shulker:proxies:$name:players")
            pipeline.del("shulker:proxies:$name:players")
            pipeline.sync()

            val playerIds = playerIdsResponse.get()
            val playerPipeline = jedis.pipelined()

            playerIds.forEach { playerId ->
                playerPipeline.srem("shulker:players:online", playerId)
                playerPipeline.hdel("shulker:players:current-proxy", playerId)
                playerPipeline.hdel("shulker:players:current-server", playerId)
            }

            playerPipeline.sync()
        }
    }

    override fun updateProxyLastSeen(name: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.hset("shulker:proxies:last-seen", name, System.currentTimeMillis().toString())
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

    override fun setPlayerPosition(playerId: UUID, proxyName: String, serverName: String) {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()
            val currentProxy = jedis.hget("shulker:players:current-proxy", playerIdString)

            val pipeline = jedis.pipelined()
            pipeline.sadd("shulker:players:online", playerIdString)
            pipeline.hset("shulker:players:current-proxy", playerIdString, proxyName)
            pipeline.hset("shulker:players:current-server", playerIdString, serverName)

            if (currentProxy != null)
                pipeline.srem("shulker:proxies:$currentProxy:players", playerIdString)
            pipeline.sadd("shulker:proxies:$proxyName:players", playerIdString)

            pipeline.sync()
        }
    }

    override fun unsetPlayerPosition(playerId: UUID) {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()
            val currentProxy = jedis.hget("shulker:players:current-proxy", playerIdString)

            val pipeline = jedis.pipelined()
            pipeline.srem("shulker:players:online", playerIdString)
            pipeline.hdel("shulker:players:current-proxy", playerIdString)
            pipeline.hdel("shulker:players:current-server", playerIdString)
            pipeline.srem("shulker:proxies:$currentProxy:players", playerIdString)

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

    override fun tryLockLostProxiesPurgeTask(ownerProxyName: String, ttlSeconds: Long): Optional<CacheAdapter.Lock> =
        this.tryLock(ownerProxyName, "shulker:lock:lost-proxies-purge", ttlSeconds)

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
