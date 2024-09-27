package us.timinc.mc.cobblemon.spawnnotification.events

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonFaintedEvent
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.biomeRegistry
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraftforge.event.entity.EntityLeaveLevelEvent
import us.timinc.mc.cobblemon.spawnnotification.SpawnNotification.config
import us.timinc.mc.cobblemon.spawnnotification.broadcasters.DespawnBroadcaster
import us.timinc.mc.cobblemon.spawnnotification.util.Broadcast
import us.timinc.mc.cobblemon.spawnnotification.util.DespawnReason
import us.timinc.mc.cobblemon.spawnnotification.util.PlayerUtil.getValidPlayers

object BroadcastDespawn {
    fun handle(evt: PokemonCapturedEvent) {
        if (!config.broadcastDespawns) return

        val entity = evt.pokeBallEntity
        val coords = entity.blockPosition()
        val level = entity.level()

        broadcast(
            evt.pokemon,
            coords,
            level.biomeRegistry.getKey(level.getBiome(coords).get())!!,
            level.dimension().location(),
            level,
            DespawnReason.CAPTURED
        )
    }

    fun handle(evt: PokemonFaintedEvent) {
        if (!config.broadcastDespawns) return
        if (!evt.pokemon.isWild()) return

        val entity = evt.pokemon.entity ?: return
        val coords = entity.blockPosition()
        val level = entity.level()

        broadcast(
            evt.pokemon,
            coords,
            level.biomeRegistry.getKey(level.getBiome(coords).get())!!,
            level.dimension().location(),
            level,
            DespawnReason.FAINTED
        )
    }

    fun handle(evt: EntityLeaveLevelEvent) {
        if (!config.broadcastVolatileDespawns) return
        val entity = evt.entity
        if (entity !is PokemonEntity) return

        val coords = entity.blockPosition()
        val level = evt.level

        broadcast(
            entity.pokemon,
            coords,
            level.biomeRegistry.getKey(level.getBiome(coords).get())!!,
            level.dimension().location(),
            level,
            DespawnReason.DESPAWNED
        )
    }

    private fun broadcast(
        pokemon: Pokemon,
        coords: BlockPos,
        biome: ResourceLocation,
        dimension: ResourceLocation,
        level: Level,
        reason: DespawnReason
    ) {
        DespawnBroadcaster(
            pokemon,
            CobblemonSpawnPools.WORLD_SPAWN_POOL,
            coords,
            biome,
            dimension,
            reason
        ).getBroadcast()?.let { message ->
            if (config.announceCrossDimensions) {
                Broadcast.broadcastMessage(message)
            } else if (config.broadcastRangeEnabled) {
                Broadcast.broadcastMessage(getValidPlayers(level, coords), message)
            } else {
                Broadcast.broadcastMessage(level, message)
            }
        }
    }
}