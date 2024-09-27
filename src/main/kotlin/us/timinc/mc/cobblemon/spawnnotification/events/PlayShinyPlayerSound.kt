package us.timinc.mc.cobblemon.spawnnotification.events

import com.cobblemon.mod.common.api.events.pokemon.PokemonSentPostEvent
import us.timinc.mc.cobblemon.spawnnotification.SpawnNotification.SHINY_SOUND_EVENT
import us.timinc.mc.cobblemon.spawnnotification.SpawnNotification.config
import us.timinc.mc.cobblemon.spawnnotification.broadcasters.SoundBroadcaster

object PlayShinyPlayerSound {
    fun handle(evt: PokemonSentPostEvent) {
        if (!(config.playShinySoundPlayer && evt.pokemon.shiny)) return
        SoundBroadcaster(
            evt.pokemonEntity.level(),
            evt.pokemonEntity.blockPosition(),
            SHINY_SOUND_EVENT
        ).playShinySound()
    }
}