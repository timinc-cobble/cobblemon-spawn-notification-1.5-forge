package us.timinc.mc.cobblemon.spawnnotification.config

class SpawnNotificationConfig {
    val broadcastShiny = true
    val broadcastCoords = true
    val broadcastBiome = false
    val playShinySound = true
    val playShinySoundPlayer = false
    val announceCrossDimensions = false
    val broadcastDespawns = false

    val broadcastRange: Int = -1
    val playerLimit: Int = -1

    val labelsForBroadcast: MutableSet<String> = mutableSetOf("legendary");

    @Suppress("KotlinConstantConditions")
    val broadcastRangeEnabled: Boolean
        get() = broadcastRange > 0

    @Suppress("KotlinConstantConditions")
    val playerLimitEnabled: Boolean
        get() = playerLimit > 0

    val formatting = mutableMapOf<String, String>(
    )
}