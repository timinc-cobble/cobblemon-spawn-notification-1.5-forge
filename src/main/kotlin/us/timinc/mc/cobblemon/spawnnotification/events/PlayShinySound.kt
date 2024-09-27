package us.timinc.mc.cobblemon.spawnnotification.events

import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import us.timinc.mc.cobblemon.spawnnotification.SpawnNotification.SHINY_SOUND_EVENT
import us.timinc.mc.cobblemon.spawnnotification.SpawnNotification.config
import us.timinc.mc.cobblemon.spawnnotification.broadcasters.SoundBroadcaster
import us.timinc.mc.cobblemon.spawnnotification.util.PlayerUtil.getValidPlayers

object PlayShinySound {
    fun handle(evt: SpawnEvent<PokemonEntity>) {
        val world = evt.ctx.world
        val pos = evt.ctx.position

        if (config.playShinySound && evt.entity.pokemon.shiny) {
            val broadcaster = SoundBroadcaster(
                world.level, pos, SHINY_SOUND_EVENT
            )
            if (config.broadcastRangeEnabled) {
                getValidPlayers(world, pos).forEach { broadcaster.playShinySoundClient(it) }
            } else {
                broadcaster.playShinySound()
            }
        }
    }
}