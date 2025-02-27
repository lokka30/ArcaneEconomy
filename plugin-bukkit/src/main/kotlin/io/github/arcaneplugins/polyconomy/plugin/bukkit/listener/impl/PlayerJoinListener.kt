package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.PolyListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(
    plugin: Polyconomy,
) : PolyListener(
    plugin,
) {

    @EventHandler(priority = EventPriority.LOWEST)
    fun handle(event: PlayerJoinEvent) {
        createPlayerAccount(event.player)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createPlayerAccount(player: Player) {
        GlobalScope.launch {
            with(plugin.storageManager.handler) {
                playerCacheSetName(player.uniqueId, player.name)
                getOrCreatePlayerAccount(player.uniqueId, player.name)
                    .setName(player.name)
            }
        }
    }

}