package us.timinc.mc.cobblemon.spawnnotification.util

import com.cobblemon.mod.common.util.server
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

object Broadcast {
    fun broadcastMessage(message: Component) {
        val serverInstance = server() ?: return
        serverInstance.sendSystemMessage(message)
        serverInstance.playerList.players.forEach { it.sendSystemMessage(message) }
    }

    fun broadcastMessage(level: Level, message: Component) {
        level.players().forEach { it.sendSystemMessage(message) }
    }

    fun broadcastMessage(players: List<Player>, message: Component) {
        players.forEach { broadcastMessage(it, message) }
    }

    private fun broadcastMessage(player: Player, message: Component) {
        player.sendSystemMessage(message)
    }
}