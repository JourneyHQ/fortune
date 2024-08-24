package dev.yuua.fortune

import dev.kordex.core.ExtensibleBot
import dev.kordex.core.utils.FilterStrategy
import dev.kordex.core.utils.suggestStringMap
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.yuua.fortune.bcdice.BCDiceAPI
import dev.yuua.fortune.ccfolia.CharacterData
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.io.path.Path

object Fortune {
    lateinit var config: Config
    lateinit var bot: ExtensibleBot

    fun isDev() = config.env == "dev"

    val gameSystemAutoComplete: suspend AutoCompleteInteraction.(AutoCompleteInteractionCreateEvent) -> Unit = {
        suggestStringMap(
            BCDiceAPI.getGameSystems().associate { (id, name) -> "$name (ID: $id)" to id },
            FilterStrategy.Contains
        )
    }

    val json = Json {
        ignoreUnknownKeys = true
    }

    val dataFolder = Path("./data").apply { toFile().mkdir() }

    private fun File.readOrCreate() = if (exists()) readText() else {
        createNewFile()
        writeText("{}")
        "{}"
    }

    val channelGameSystemsFile: File = dataFolder.resolve(Path("channel-game-systems.json")).toFile()

    val channelGameSystems = json.decodeFromString(
        MapSerializer(
            Snowflake.serializer(),
            String.serializer()
        ),
        channelGameSystemsFile.readOrCreate()
    ).toMutableMap()

    val lastRollsFile: File = dataFolder.resolve(Path("last-rolls.json")).toFile()

    val lastRolls = json.decodeFromString(
        MapSerializer(
            Snowflake.serializer(),
            String.serializer()
        ),
        lastRollsFile.readOrCreate()
    ).toMutableMap()

    val charactersFile: File = dataFolder.resolve(Path("characters.json")).toFile()

    val characters = json.decodeFromString(
        MapSerializer(
            Snowflake.serializer(),
            CharacterData.serializer()
        ),
        charactersFile.readOrCreate()
    ).toMutableMap()
}