package us.timinc.mc.cobblemon.spawnnotification

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.playSoundServer
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraftforge.event.entity.EntityLeaveLevelEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import us.timinc.mc.cobblemon.spawnnotification.config.ConfigBuilder
import us.timinc.mc.cobblemon.spawnnotification.config.SpawnNotificationConfig
import us.timinc.mc.cobblemon.spawnnotification.util.Broadcast
import us.timinc.mc.cobblemon.spawnnotification.util.DespawnReason
import us.timinc.mc.cobblemon.spawnnotification.util.PlayerUtil

@Mod(SpawnNotification.MOD_ID)
object SpawnNotification {
    const val MOD_ID = "spawn_notification"
    private var config: SpawnNotificationConfig = ConfigBuilder.load(SpawnNotificationConfig::class.java, MOD_ID)

    @JvmStatic
    var SHINY_SOUND_ID: ResourceLocation = ResourceLocation("$MOD_ID:pla_shiny")

    @JvmStatic
    var SHINY_SOUND_EVENT: SoundEvent = SoundEvent.createVariableRangeEvent(SHINY_SOUND_ID)

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    object Registration {
        @SubscribeEvent
        fun onInit(e: ServerStartedEvent) {
            CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { evt ->
                val world = evt.ctx.world
                if (world.isClientSide) return@subscribe

                val pokemon = evt.entity.pokemon
                if (pokemon.isPlayerOwned()) return@subscribe

                val pos = evt.ctx.position

                broadcastSpawn(evt)
                if (config.playShinySound && pokemon.shiny) {
                    if (config.broadcastRangeEnabled) {
                        getValidPlayers(world, pos).forEach { playShinySoundClient(it) }
                    } else {
                        playShinySound(world, pos)
                    }
                }
            }
            CobblemonEvents.POKEMON_SENT_POST.subscribe { evt ->
                if (config.playShinySoundPlayer && evt.pokemon.shiny) {
                    playShinySound(evt.pokemonEntity.level(), evt.pokemonEntity.blockPosition())
                }
            }
            CobblemonEvents.POKEMON_CAPTURED.subscribe { evt ->
                broadcastDespawn(evt.pokemon, DespawnReason.CAPTURED)
            }
            CobblemonEvents.POKEMON_FAINTED.subscribe { evt ->
                broadcastDespawn(evt.pokemon, DespawnReason.FAINTED)
            }
        }

        @SubscribeEvent
        fun onEntityUnload(e: EntityLeaveLevelEvent) {
            if (e.level.isClientSide) return
            val target = e.entity
            if (target !is PokemonEntity) return
            if (!target.pokemon.isWild()) return

            broadcastDespawn(target.pokemon, DespawnReason.DESPAWNED)
        }
    }

    private fun getValidPlayers(level: Level, pos: BlockPos): List<Player> {
        return if (config.playerLimitEnabled) PlayerUtil.getValidPlayers(
            pos, config.broadcastRange, level.dimension(), config.playerLimit
        ) else PlayerUtil.getValidPlayers(pos, config.broadcastRange, level.dimension())
    }

    private fun broadcastDespawn(
        pokemon: Pokemon,
        reason: DespawnReason
    ) {
        if (config.broadcastDespawns && (pokemon.shiny || pokemon.isLegendary())) {
            Broadcast.broadcastMessage(
                Component.translatable(
                    "$MOD_ID.notification.${reason.translationKey}",
                    pokemon.getDisplayName()
                )
            )
        }
    }

    private fun broadcastSpawn(
        evt: SpawnEvent<PokemonEntity>
    ) {
        val pokemon = evt.entity.pokemon
        val pokemonName = pokemon.getDisplayName()

        val matchedLabel = pokemon.form.labels.firstOrNull { config.labelsForBroadcast.contains(it) }

        val message = when {
            matchedLabel != null && config.broadcastShiny && pokemon.shiny -> "$MOD_ID.notification.$matchedLabel.shiny"
            matchedLabel != null -> "$MOD_ID.notification.$matchedLabel"
            config.broadcastShiny && pokemon.shiny -> "$MOD_ID.notification.shiny"
            else -> return
        }

        var color = config.formatting["$matchedLabel${if (pokemon.shiny) ".shiny" else ""}"]
        if (color == null && pokemon.shiny) {
            color = config.formatting[matchedLabel]
        }
        if (color == null && pokemon.shiny) {
            color = config.formatting["shiny"]
        }

        val possibleFormatting = ChatFormatting.getByName(color)
        val formattedPokemonName = possibleFormatting?.let { pokemonName.withStyle(it) } ?: pokemonName

        var messageComponent = Component.translatable(message, formattedPokemonName)
        val pos = evt.ctx.position
        if (config.broadcastCoords) {
            messageComponent = messageComponent.append(
                Component.translatable(
                    "$MOD_ID.notification.coords",
                    pos.x,
                    pos.y,
                    pos.z
                )
            )
        }
        val level = evt.ctx.world
        if (config.broadcastBiome) {
            messageComponent = messageComponent.append(
                Component.translatable(
                    "$MOD_ID.notification.biome",
                    Component.translatable("biome.${evt.ctx.biomeName.toLanguageKey()}")
                )
            )
        }

        if (config.announceCrossDimensions) {
            messageComponent = messageComponent.append(
                Component.translatable(
                    "$MOD_ID.notification.dimension",
                    Component.translatable("dimension.${level.dimension().location().toLanguageKey()}")
                )
            )

            Broadcast.broadcastMessage(messageComponent)
        } else if (config.broadcastRangeEnabled) {
            Broadcast.broadcastMessage(getValidPlayers(level, pos), messageComponent)
        } else {
            Broadcast.broadcastMessage(level, messageComponent)
        }
    }

    private fun playShinySound(
        level: Level,
        pos: BlockPos
    ) {
        level.playSoundServer(pos.center, SHINY_SOUND_EVENT, SoundSource.NEUTRAL, 10f, 1f)
    }

    private fun playShinySoundClient(
        player: Player
    ) {
        player.playSound(SHINY_SOUND_EVENT, 10f, 1f)
    }
}