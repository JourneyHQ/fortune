package dev.yuua.fortune.discord.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialSlashCommandResponse
import com.kotlindiscord.kord.extensions.utils.suggestStringCollection
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.yuua.fortune.Fortune
import dev.yuua.fortune.bcdice.BCDiceAPI
import dev.yuua.fortune.ccfolia.CharacterData
import dev.yuua.fortune.discord.KordExExtensions.unsafeCommand
import dev.yuua.fortune.discord.Options

class RollCommand : Extension() {
    override val name = this::class.simpleName!!

    inner class RollOptions : Options() {
        val command by string {
            name = "command"
            description = "The command to roll"

            autoComplete {
                suggestStringCollection(
                    mutableListOf("1d100", "1d20", "2d3", "1d3", "1d10").apply {
                        if (Fortune.lastRolls[user.id] != null) add("reroll")

                        val character = Fortune.characters[user.id]
                        if (character != null) addAll(character.getSkills())
                    }.filter { it.contains(focusedOption.value, ignoreCase = true) }.take(25)
                )
            }
        }

        val gameSystem by optionalString {
            name = "game-system"
            description = "The game system to use"

            autoComplete(Fortune.gameSystemAutoComplete)
        }
    }

    @OptIn(UnsafeAPI::class)
    override suspend fun setup() {
        unsafeCommand("roll", "Rolls a dice.", ::RollOptions) {
            initialResponse = InitialSlashCommandResponse.None

            action {
                val interaction = event.interaction
                val command = arguments.command

                val shouldReroll = Regex("reroll", RegexOption.IGNORE_CASE).matches(command)

                val dice = if (shouldReroll) {
                    Fortune.lastRolls[user.id] ?: kotlin.run {
                        interaction.respondEphemeral {
                            content = "**:interrobang: No Recent Command:** Let's roll some dice! Try: `1d100`"
                        }
                        return@action
                    }
                } else {
                    Fortune.lastRolls[user.id] = command
                    command
                }

                val roll = BCDiceAPI.getRoll(
                    arguments.gameSystem ?: Fortune.channelGameSystems[channel.id] ?: "Cthulhu7th",
                    dice
                )

                if (roll == null) {
                    interaction.respondPublic {
                        content = "**:interrobang: Roll Failed:** Invalid dice or game system."
                    }
                    return@action
                }

                val skill = CharacterData.getSkillName(dice)

                val text = if (skill != null) roll.messageWithSkill(skill) else roll.message

                interaction.run {
                    if (roll.secret) respondEphemeral { content = text }
                    else respondPublic { content = text }
                }
            }
        }

        unsafeCommand("reroll", "Rerolls the last dice.") {
            initialResponse = InitialSlashCommandResponse.None

            action {
                val interaction = event.interaction

                val lastCommand = Fortune.lastRolls[user.id] ?: kotlin.run {
                    interaction.respondEphemeral {
                        content = "**:interrobang: No Recent Command:** Let's roll some dice! Try: `1d100`"
                    }
                    return@action
                }

                val roll = BCDiceAPI.getRoll(
                    Fortune.channelGameSystems[channel.id] ?: "Cthulhu7th",
                    lastCommand
                )

                if (roll == null) {
                    interaction.respondPublic {
                        content = "**:interrobang: Roll Failed:** Invalid dice or game system."
                    }
                    return@action
                }

                val skill = CharacterData.getSkillName(lastCommand)

                val text = if (skill != null) roll.messageWithSkill(skill) else roll.message

                interaction.run {
                    if (roll.secret) respondEphemeral { content = text }
                    else respondPublic { content = text }
                }
            }
        }
    }
}