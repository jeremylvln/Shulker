package io.shulkermc.cluster.api.data

import java.time.Instant

data class RegisteredProxy(val proxyName: String, val proxyCapacity: Int, val lastSeenAt: Instant)
