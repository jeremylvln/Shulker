package io.shulkermc.proxyagent.adapters.cache

import io.shulkermc.proxyagent.api.ShulkerProxyAPI.PlayerPosition
import redis.clients.jedis.JedisPool
import redis.clients.jedis.params.SetParams
import java.util.Optional
import java.util.UUID

class RedisCacheAdapter(private val jedisPool: JedisPool) : CacheAdapter {
    companion object {
        private const val PROXY_LOST_PURGE_LOCK_SECONDS = 15L
        private const val PLAYER_ID_CACHE_TTL_SECONDS = 60L * 60 * 24 * 14

        private const val KEY_PREFIX = "shulker"

        // Proxies keys
        private const val PROXIES_KEY_PREFIX = "$KEY_PREFIX:proxies"
        private const val PROXIES_SET_KEY = PROXIES_KEY_PREFIX
        private const val PROXIES_CAPACITY_HASH_KEY = "$PROXIES_KEY_PREFIX:capacity"
        private const val PROXIES_LAST_SEEN_HASH_KEY = "$PROXIES_KEY_PREFIX:last-seen"
        private val PROXIES_PLAYERS_SET_KEY = { proxyName: String -> "$PROXIES_KEY_PREFIX:$proxyName:players" }

        // Servers keys
        private const val SERVERS_KEY_PREFIX = "$KEY_PREFIX:servers"
        private val SERVERS_PLAYERS_SET_KEY = { serverName: String -> "$SERVERS_KEY_PREFIX:$serverName:players" }

        // Players keys
        private const val PLAYERS_KEY_PREFIX = "$KEY_PREFIX:players"
        private const val PLAYERS_ONLINE_SET_KEY = "$PLAYERS_KEY_PREFIX:online"
        private const val PLAYERS_CURRENT_PROXY_HASH_KEY = "$PLAYERS_KEY_PREFIX:current-proxy"
        private const val PLAYERS_CURRENT_SERVER_HASH_KEY = "$PLAYERS_KEY_PREFIX:current-server"

        // UUID cache keys
        private const val UUID_CACHE_KEY_PREFIX = "$KEY_PREFIX:uuid-cache"
        private val UUID_CACHE_NAME_TO_ID_KEY = { name: String -> "$UUID_CACHE_KEY_PREFIX:name-to-id:$name" }
        private val UUID_CACHE_ID_TO_NAME_KEY = { id: String -> "$UUID_CACHE_KEY_PREFIX:id-to-name:$id" }

        // Locks keys
        private const val LOCKS_KEY_PREFIX = "$KEY_PREFIX:locks"
        private const val LOCKS_LOST_PROXIES_PURGE_KEY = "$LOCKS_KEY_PREFIX:lost-proxies-purge"
    }

    override fun registerProxy(proxyName: String, proxyCapacity: Int) {
        this.jedisPool.resource.use { jedis ->
            val pipeline = jedis.pipelined()
            pipeline.sadd(PROXIES_SET_KEY, proxyName)
            pipeline.hset(PROXIES_CAPACITY_HASH_KEY, proxyName, proxyCapacity.toString())
            pipeline.hset(PROXIES_LAST_SEEN_HASH_KEY, proxyName, System.currentTimeMillis().toString())
            pipeline.sync()
        }
    }

    override fun unregisterProxy(proxyName: String) {
        this.jedisPool.resource.use { jedis ->
            val playersRedisKey = PROXIES_PLAYERS_SET_KEY(proxyName)
            val playerIds = jedis.smembers(playersRedisKey)

            val pipeline = jedis.pipelined()
            pipeline.srem(PROXIES_SET_KEY, proxyName)
            pipeline.hdel(PROXIES_CAPACITY_HASH_KEY, proxyName)
            pipeline.hdel(PROXIES_LAST_SEEN_HASH_KEY, proxyName)
            pipeline.del(playersRedisKey)
            pipeline.sync()

            val playerPipeline = jedis.pipelined()
            playerIds.forEach { playerId ->
                playerPipeline.srem(PLAYERS_ONLINE_SET_KEY, playerId)
                playerPipeline.hdel(PLAYERS_CURRENT_PROXY_HASH_KEY, playerId)
                playerPipeline.hdel(PLAYERS_CURRENT_SERVER_HASH_KEY, playerId)
            }
            playerPipeline.sync()
        }
    }

    override fun updateProxyLastSeen(proxyName: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.hset(PROXIES_LAST_SEEN_HASH_KEY, proxyName, System.currentTimeMillis().toString())
        }
    }

    override fun listRegisteredProxies(): List<CacheAdapter.RegisteredProxy> {
        this.jedisPool.resource.use { jedis ->
            val registeredProxies = jedis.smembers(PROXIES_SET_KEY)
            val capacities = jedis.hgetAll(PROXIES_CAPACITY_HASH_KEY)
            val lastSeenMillis = jedis.hgetAll(PROXIES_LAST_SEEN_HASH_KEY)

            return registeredProxies.map { proxyName ->
                val capacity = capacities[proxyName]?.toInt() ?: 0
                val lastSeen = lastSeenMillis[proxyName]?.toLong() ?: 0L
                CacheAdapter.RegisteredProxy(proxyName, capacity, lastSeen)
            }
        }
    }

    override fun tryLockLostProxiesPurgeTask(ownerProxyName: String): Optional<CacheAdapter.Lock> =
        this.tryLock(ownerProxyName, LOCKS_LOST_PROXIES_PURGE_KEY, PROXY_LOST_PURGE_LOCK_SECONDS)

    override fun unregisterServer(serverName: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.del(SERVERS_PLAYERS_SET_KEY(serverName))
        }
    }

    override fun listPlayersInServer(serverName: String): List<UUID> {
        this.jedisPool.resource.use { jedis ->
            val playerIds = jedis.smembers(SERVERS_PLAYERS_SET_KEY(serverName))
            return playerIds.map(UUID::fromString)
        }
    }

    override fun setPlayerPosition(playerId: UUID, proxyName: String, serverName: String) {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()
            val oldProxyName = jedis.hget(PLAYERS_CURRENT_PROXY_HASH_KEY, playerIdString)
            val oldServerName = jedis.hget(PLAYERS_CURRENT_SERVER_HASH_KEY, playerIdString)

            val pipeline = jedis.pipelined()
            pipeline.sadd(PLAYERS_ONLINE_SET_KEY, playerIdString)
            pipeline.hset(PLAYERS_CURRENT_PROXY_HASH_KEY, playerIdString, proxyName)
            pipeline.hset(PLAYERS_CURRENT_SERVER_HASH_KEY, playerIdString, serverName)

            if (oldProxyName != null) {
                pipeline.srem(PROXIES_PLAYERS_SET_KEY(oldProxyName), playerIdString)
            }
            pipeline.sadd(PROXIES_PLAYERS_SET_KEY(proxyName), playerIdString)

            if (oldServerName != null) {
                pipeline.srem(SERVERS_PLAYERS_SET_KEY(oldServerName), playerIdString)
            }
            pipeline.sadd(SERVERS_PLAYERS_SET_KEY(serverName), playerIdString)

            pipeline.sync()
        }
    }

    override fun unsetPlayerPosition(playerId: UUID) {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()
            val currentProxyName = jedis.hget(PLAYERS_CURRENT_PROXY_HASH_KEY, playerIdString)
            val currentServerName = jedis.hget(PLAYERS_CURRENT_SERVER_HASH_KEY, playerIdString)

            val pipeline = jedis.pipelined()
            pipeline.srem(PLAYERS_ONLINE_SET_KEY, playerIdString)
            pipeline.hdel(PLAYERS_CURRENT_PROXY_HASH_KEY, playerIdString)
            pipeline.hdel(PLAYERS_CURRENT_SERVER_HASH_KEY, playerIdString)
            pipeline.srem(PROXIES_PLAYERS_SET_KEY(currentProxyName), playerIdString)
            pipeline.srem(SERVERS_PLAYERS_SET_KEY(currentServerName), playerIdString)
            pipeline.sync()
        }
    }

    override fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition> {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()

            val pipeline = jedis.pipelined()
            val proxyNameResponse = pipeline.hget(PLAYERS_CURRENT_PROXY_HASH_KEY, playerIdString)
            val serverNameResponse = pipeline.hget(PLAYERS_CURRENT_SERVER_HASH_KEY, playerIdString)
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
            return jedis.sismember(PLAYERS_ONLINE_SET_KEY, playerId.toString())
        }
    }

    override fun updateCachedPlayerName(playerId: UUID, playerName: String) {
        this.jedisPool.resource.use { jedis ->
            val playerIdString = playerId.toString()
            val params = SetParams().ex(PLAYER_ID_CACHE_TTL_SECONDS)

            val pipeline = jedis.pipelined()
            pipeline.set(UUID_CACHE_ID_TO_NAME_KEY(playerIdString), playerName, params)
            pipeline.set(UUID_CACHE_NAME_TO_ID_KEY(playerName), playerIdString, params)
            pipeline.sync()
        }
    }

    override fun getPlayerNameFromId(playerId: UUID): Optional<String> {
        this.jedisPool.resource.use { jedis ->
            return Optional.ofNullable(jedis.get(UUID_CACHE_ID_TO_NAME_KEY(playerId.toString())))
        }
    }

    override fun getPlayerIdFromName(playerName: String): Optional<UUID> {
        this.jedisPool.resource.use { jedis ->
            return Optional.ofNullable(jedis.get(UUID_CACHE_NAME_TO_ID_KEY(playerName))).map(UUID::fromString)
        }
    }

    override fun getPlayerNamesFromIds(playerIds: List<UUID>): Map<UUID, String> {
        this.jedisPool.resource.use { jedis ->
            val pipeline = jedis.pipelined()
            val responses = playerIds.associateWith { uuid -> pipeline.get(UUID_CACHE_ID_TO_NAME_KEY(uuid.toString())) }
            pipeline.sync()

            return responses.mapValues { (_, response) -> response.get() }
        }
    }

    override fun countOnlinePlayers(): Int {
        this.jedisPool.resource.use { jedis ->
            return jedis.scard(PLAYERS_ONLINE_SET_KEY).toInt()
        }
    }

    override fun countPlayerCapacity(): Int {
        this.jedisPool.resource.use { jedis ->
            return jedis.hgetAll(PROXIES_CAPACITY_HASH_KEY).values.sumOf { it.toInt() }
        }
    }

    private fun tryLock(ownerProxyName: String, key: String, ttlSeconds: Long): Optional<CacheAdapter.Lock> {
        this.jedisPool.resource.use { jedis ->
            val success = jedis.set(key, ownerProxyName, SetParams().nx().ex(ttlSeconds)) != null

            if (success) {
                return Optional.of(object : CacheAdapter.Lock {
                    override fun close() {
                        jedisPool.resource.use { jedis -> jedis.del(key) }
                    }
                })
            }

            return Optional.empty()
        }
    }
}
