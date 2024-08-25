package dev.yuua.fortune

import com.github.ajalt.clikt.core.CliktCommand
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.commands.application.ApplicationCommandRegistry
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.yaml.YamlPropertySource
import dev.yuua.fortune.discord.commands.CharacterCommand
import dev.yuua.fortune.discord.commands.GameSystemCommand
import dev.yuua.fortune.discord.commands.RollCommand
import dev.yuua.fortune.discord.events.RollMessageEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import java.io.File

class Main : CliktCommand() {
    private val logger = KotlinLogging.logger {}

    override fun run() {
        logger.info { "Starting Fortune..." }

        val config = ConfigLoaderBuilder.default()
            .addSource(YamlPropertySource(File("./config.yml").readText()))
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

                    val commands = bot.extensions.flatMap { (_, extension) ->
                        extension.slashCommands.map {
                            registry.unregisterGeneric(it)
                            it.name
                        }
                    }

                    println("Unregistered ${commands.size} commands:")
                    println(commands.joinToString())

                    try {
                        bot.stop()
                    } catch (_: IllegalStateException) {
                        // Ignore
                    }

                    println("Clean Discord bot complete.")
                }

                println("Bye!")
            })

            bot.start()
        }
    }
}

fun main(args: Array<String>) = Main().main(args)