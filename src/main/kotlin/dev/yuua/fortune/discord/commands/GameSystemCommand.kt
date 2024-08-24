package dev.yuua.fortune.discord.commands

import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.yuua.fortune.Fortune
import dev.yuua.fortune.bcdice.BCDiceAPI
import dev.yuua.fortune.discord.KordExExtensions.publicSlashCommand
import dev.yuua.fortune.discord.KordExExtensions.publicSubCommand
import dev.yuua.fortune.discord.Options

class GameSystemCommand : Extension() {
    override val name = this::class.simpleName!!

    inner class SetGameSystemOptions : Options() {
        val gameSystem by string {
            name = "game-system"
            description = "The game system to use on this channel."

            autoComplete(Fortune.gameSystemAutoComplete)
        }
    }

    inner class HelpGameSystemOptions : Options() {
        val gameSystem by optionalString {
            name = "game-system"
            description = "Select game system or default to the channel's game system."

            autoComplete(Fortune.gameSystemAutoComplete)
        }
    }

    override suspend fun setup() {
        publicSlashCommand("game-system", "Sets the game system for this channel.") {
            publicSubCommand("set", "Sets the game system for this channel.", ::SetGameSystemOptions) {
                action {
                    val gameSystemData = BCDiceAPI.getGameSystems().find { it.id == arguments.gameSystem }

                    if (gameSystemData == null) {
                        respond { content = "**:interrobang: Game system not found!**" }
                        return@action
                    }

                    Fortune.channelGameSystems[channel.id] = gameSystemData.id

                    respond {
                        content = "**:white_check_mark: Game system set to ${gameSystemData.name} for this channel!**"
                    }
                }
            }

            publicSubCommand("help", "View help for the game system command.", ::HelpGameSystemOptions) {
                action {
                    val info = BCDiceAPI.getGameSystemInfo(
                        arguments.gameSystem ?: Fortune.channelGameSystems[channel.id] ?: "Cthulhu7th"
                    )

                    if (info == null) {
                        respond { content = "**:interrobang: Game system not found!**" }
                        return@action
                    }

                    respond {
                        content = "## ${info.name} / ID: `${info.id}` \n ${info.helpMessage}"
                    }
                }
            }
        }
    }
}