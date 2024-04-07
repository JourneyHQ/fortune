package dev.yuua

import com.github.ajalt.clikt.core.CliktCommand
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandRegistry
import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.yaml.YamlPropertySource
import dev.kord.common.entity.Snowflake
import dev.yuua.ccfolia.CharacterData
import dev.yuua.discord.commands.CharacterCommand
import dev.yuua.discord.commands.GameSystemCommand
import dev.yuua.discord.commands.RollCommand
import dev.yuua.discord.events.RollMessageEvent
import dev.yuua.mongodb.LastRollItem
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.bson.BsonInt64
import org.bson.Document
import org.bson.types.ObjectId
import java.io.File

class Main : CliktCommand() {
    private val logger = KotlinLogging.logger {}

    override fun run() {
        logger.info { "Starting Fortune..." }

        val config = ConfigLoaderBuilder.default()
            .addSource(YamlPropertySource(File("./config/config.yml").readText()))
            .build()
            .loadConfigOrThrow<Config>()

        Fortune.config = config

        runBlocking {
            val bot = ExtensibleBot(config.discord) {
                applicationCommands {}
                extensions {
                    add(::RollCommand)
                    add(::GameSystemCommand)
                    add(::CharacterCommand)
                    add(::RollMessageEvent)
                }
            }

            Fortune.bot = bot

            Runtime.getRuntime().addShutdownHook(Thread {
                logger.info { "Shutting down Fortune..." }

                Fortune.run {
                    channelGameSystemsFile.writeText(json.encodeToString(channelGameSystems))
                    lastRollsFile.writeText(json.encodeToString(lastRolls))
                    charactersFile.writeText(json.encodeToString(characters))

                    println("Save internal data complete.")
                }

                runBlocking {
                    val registry = bot.getKoin().get<ApplicationCommandRegistry>()

                    bot.extensions.forEach { (_, extension) ->
                        extension.slashCommands.forEach { registry.unregisterGeneric(it) }
                    }

                    bot.close()

                    println("Clean Discord bot complete.")
                }

                println("Bye!")
            })

            bot.start()
        }


    }
}

fun main(args: Array<String>) = Main().main(args)