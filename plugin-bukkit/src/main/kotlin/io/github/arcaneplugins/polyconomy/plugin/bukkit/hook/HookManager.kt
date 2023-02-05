package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook

import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.HOOK_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryHook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.VaultHook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.TerminateLoadException
import org.bukkit.Bukkit

object HookManager {

    val hooks: Set<Hook> = mutableSetOf(
        TreasuryHook,
        VaultHook,
    )

    fun registerAll() {
        Log.d(HOOK_MANAGER) { "Registering hooks." }

        hooks
            .filter(Hook::canRegister)
            .forEach { hook ->
                Log.d(HOOK_MANAGER) { "Registering hook '${hook.id}'." }
                hook.register()
                Log.d(HOOK_MANAGER) { "Registered hook '${hook.id}'." }
            }

        Log.d(HOOK_MANAGER) {
            "Registered ${hooks.count { it.registered }} of ${hooks.size} hooks."
        }
    }

    fun unregisterAll() {
        Log.d(HOOK_MANAGER) {
            "Unregistering ${hooks.count { it.registered }} hooks."
        }

        hooks
            .filter(Hook::registered)
            .forEach { hook ->
                Log.d(HOOK_MANAGER) { "Unregistering hook '${hook.id}'." }
                hook.unregister()
                Log.d(HOOK_MANAGER) { "Unregistered hook '${hook.id}'." }
            }

        Log.d(HOOK_MANAGER) { "Unregistered hooks." }
    }

    fun ensureHardDependencies() {
        Log.d(HOOK_MANAGER) { "Enforcing hard dependencies..." }

        val missing = listOf("Treasury")
            .filter { Bukkit.getPluginManager().getPlugin(it) == null }

        if(missing.isEmpty()) {
            Log.d(HOOK_MANAGER) { "All hard dependencies are present." }
            throw TerminateLoadException()
        }

        Log.s(
            """
            You have not read Polyconomy's documentation and installed the following plugin dependencies:
            ${missing.joinToString(separator = "\n", prefix = " ✘ ")}
            """.trimIndent()
        )
    }

}