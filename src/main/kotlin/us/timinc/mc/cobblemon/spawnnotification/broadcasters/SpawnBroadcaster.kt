package us.timinc.mc.cobblemon.spawnnotification.broadcasters

import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import us.timinc.mc.cobblemon.spawnnotification.SpawnNotification.config

class SpawnBroadcaster(
    val pokemon: Pokemon,
    val spawnPool: SpawnPool,
    val coords: BlockPos,
    val biome: ResourceLocation,
    val dimension: ResourceLocation
) {
    private val shiny
        get() = pokemon.shiny
    private val label
        get() = pokemon.form.labels.firstOrNull { it in config.labelsForBroadcast }
    private val buckets
        get() = spawnPool
            .mapNotNull { if (it is PokemonSpawnDetail) it else null }
            .filter { it.pokemon.matches(pokemon) }
            .map { it.bucket.name }
    private val bucket
        get() = config.bucketsForBroadcast.firstOrNull { it in buckets }
    private val shouldBroadcast
        get() = (shiny && config.broadcastShiny) || label != null || bucket != null

    fun getBroadcast(): Component? {
        if (!shouldBroadcast) return null

        return config.getComponent(
            "notification.spawn",
            if (shiny && config.broadcastShiny) config.getComponent(
                "notification.shiny",
                config.getComponent("shiny")
            ) else "",
            if (label != null) config.getComponent(
                "notification.label",
                config.getComponent("label.$label")
            ) else "",
            if (bucket != null) config.getComponent(
                "notification.bucket",
                config.getComponent("bucket.$bucket")
            ) else "",
            if (config.broadcastSpeciesName) pokemon.species.translatedName else Component.translatable("cobblemon.entity.pokemon"),
            if (config.broadcastBiome) config.getComponent(
                "notification.biome",
                config.getRawComponent("biome.${biome.toLanguageKey()}")
            ) else "",
            if (config.broadcastCoords) config.getComponent(
                "notification.coords",
                coords.x,
                coords.y,
                coords.z
            ) else "",
            if (config.announceCrossDimensions) config.getComponent(
                "notification.dimension",
                config.getRawComponent("dimension.${dimension.toLanguageKey()}")
            ) else ""
        )
    }
}