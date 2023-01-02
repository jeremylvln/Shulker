package io.shulkermc.proxyagent.domain

import net.kyori.adventure.text.Component

data class PlayerPreLoginHookResult(val allowed: Boolean, val rejectComponent: Component?) {
    companion object {
        fun allow() = PlayerPreLoginHookResult(true, null)
        fun disallow(rejectComponent: Component) = PlayerPreLoginHookResult(false, rejectComponent)
    }
}

typealias PlayerPreLoginHook = () -> PlayerPreLoginHookResult
