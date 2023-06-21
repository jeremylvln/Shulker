package io.shulkermc.api

import com.google.gson.Gson
import io.shulkermc.api.domain.GameServer
import io.shulkermc.api.domain.NamespaceKey
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class ShulkerAPIHttp(private val host: String, private val port: Int) : ShulkerAPI {
    companion object {
        fun createFromEnvironment(): ShulkerAPI {
            return Builder()
                .withHost(checkNotNull(System.getenv("SHULKER_API_HOST")) { "Missing SHULKER_API_HOST environment variable" })
                .withPort(checkNotNull(System.getenv("SHULKER_API_PORT")) { "Missing SHULKER_API_PORT environment variable" }.toInt())
                .build()
        }
    }

    @Suppress("HttpUrlsUsage")
    private val baseUrl = "http://${this.host}:${this.port}"
    private val client = OkHttpClient()

    private val executor = Executors.newCachedThreadPool()

    override fun summonGameServer(fleetNamespaceKey: NamespaceKey): CompletableFuture<GameServer> {
        val request = Request.Builder()
            .url("${this.baseUrl}/minecraftserverfleets/${fleetNamespaceKey.namespace}/${fleetNamespaceKey.name}/summon")
            .post("text/plain".toRequestBody())
            .build()

        val future = CompletableFuture<GameServer>()

        this.executor.submit {
            this.client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    future.completeExceptionally(RuntimeException("Unexpected HTTP status ${response.code}"))
                } else {
                    val gameServer = Gson().fromJson(response.body!!.charStream(), GameServer::class.java)
                    future.complete(gameServer)
                }
            }
        }

        return future
    }

    class Builder {
        private var host: String? = null
        private var port: Int = 80

        fun build(): ShulkerAPI = ShulkerAPIHttp(
            checkNotNull(this.host),
            this.port
        )

        fun withHost(host: String): Builder {
            this.host = host
            return this
        }

        fun withPort(port: Int): Builder {
            this.port = port
            return this
        }
    }
}
