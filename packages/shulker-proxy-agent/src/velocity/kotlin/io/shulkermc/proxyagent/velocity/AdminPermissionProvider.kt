package io.shulkermc.proxyagent.velocity

import com.velocitypowered.api.permission.PermissionFunction
import com.velocitypowered.api.permission.PermissionProvider
import com.velocitypowered.api.permission.PermissionSubject
import com.velocitypowered.api.permission.Tristate

class AdminPermissionProvider : PermissionProvider, PermissionFunction {
    companion object {
        val INSTANCE = AdminPermissionProvider()
    }

    override fun createFunction(subject: PermissionSubject?): PermissionFunction = this

    override fun getPermissionValue(permission: String?): Tristate = Tristate.TRUE
}
