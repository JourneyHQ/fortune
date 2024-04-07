package dev.yuua.discord.commands

import com.kotlindiscord.kord.extensions.annotations.AlwaysPublicResponse
import com.kotlindiscord.kord.extensions.annotations.DoNotChain
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.FilterStrategy
import com.kotlindiscord.kord.extensions.utils.removeNickname
import com.kotlindiscord.kord.extensions.utils.setNickname
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import dev.yuua.Fortune
import dev.yuua.bcdice.BCDiceAPI
import dev.yuua.ccfolia.Character
import dev.yuua.discord.Options
import dev.yuua.discord.KordExExtensions.publicSlashCommand
import dev.yuua.discord.KordExExtensions.publicSubCommand
import kotlinx.serialization.json.Json

class CharacterCommand : Extension() {
    override val name = this::class.simpleName!!

    inner class ImportModal : ModalForm() {
        override var title = "Import character"

        val data = paragraphText {
            label = "Character Data"
            placeholder = "Paste character data here. Don't think, just paste it."
        }
    }

    inner class StatusOption : Options() {
        val status by string {
            name = "status"
            description = "Status to modify."

            autoComplete {
                suggestStringMap(
                    Fortune.characters[user.id]?.status?.associate { "${it.label} (${it.value}/${it.max})" to it.label }
                        ?: mapOf("Character Not Found" to "no-chara"),
                    FilterStrategy.Contains
                )
            }
        }

        val value by string {
            name = "value"
            description = "Value to set. Start with '+' or '-' to add or subtract. No prefix to set."
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(AlwaysPublicResponse::class, DoNotChain::class)
    override suspend fun setup() {
        publicSlashCommand("chara", "Use TRPG character in Discord!") {
            publicSubCommand("import", "Import character in CCFolia format.", ::ImportModal) {
                action {
                    val character = try {
                        json.decodeFromString(Character.serializer(), it?.data?.value!!)
                    } catch (e: Exception) {
                        respond {
                            content = "**:interrobang: Import Failed:** ${e.message}"
                        }
                        return@action
                    }

                    val data = character.data
                    Fortune.characters[user.id] = data

                    try {
                        member?.asMemberOrNull()?.setNickname(data.nameOnly, "Imported character: ${data.nameOnly}")
                    } catch (_: Exception) {
                    }

                    respondingPaginator(builder = data.getEmbed()).send()
                }
            }

            publicSubCommand("view", "View your current character.") {
                action {
                    val data = Fortune.characters[user.id] ?: run {
                        respond {
                            content = "**:interrobang: Character Not Found:** Please import character first."
                        }
                        return@action
                    }

                    respondingPaginator(builder = data.getEmbed()).send()
                }
            }

            publicSubCommand("remove", "Remove character.") {
                action {
                    val removed = Fortune.characters.remove(user.id)

                    if (removed != null) {
                        val guildMember = member?.asMemberOrNull()
                        if (guildMember?.nickname == removed.nameOnly)
                            try {
                                guildMember.removeNickname("Removed character: ${removed.nameOnly}")
                            } catch (_: Exception) {
                            }

                        respond {
                            content = "**:wave: Character Removed:** ${removed.nameOnly}"
                        }
                    } else {
                        respond {
                            content = "**:interrobang: Character Not Found:** Please import character first."
                        }
                    }
                }
            }

            publicSubCommand("status", "Modify character's status.", ::StatusOption) {
                action { _ ->
                    val data = Fortune.characters[user.id] ?: run {
                        respond {
                            content = "**:interrobang: Character Not Found:** Please import character first."
                        }
                        return@action
                    }

                    val status = data.status.find { it.label == arguments.status } ?: run {
                        respond {
                            content = "**:interrobang: Status Not Found:** Please check your status name."
                        }
                        return@action
                    }

                    val value = arguments.value

                    val prefix = when {
                        value.startsWith('+') -> "+"
                        value.startsWith('-') -> "-"
                        else -> null
                    }

                    val numberOrDice = value.removePrefix("+").removePrefix("-")

                    var rollText: String? = null

                    val number = numberOrDice.toIntOrNull() ?: run {
                        val info = BCDiceAPI.getGameSystemInfo(
                            Fortune.channelGameSystems[channel.id] ?: "Cthulhu7th"
                        )

                        if (info == null) {
                            respond { content = "**:interrobang: Game system not found!**" }
                            return@action
                        }

                        if (info.getRegex().containsMatchIn(numberOrDice)) {
                            val roll = BCDiceAPI.getRoll(info.id, numberOrDice)

                            if (roll == null) {
                                respond { content = "**:interrobang: Roll Failed:** Couldn't roll the dice." }
                                return@action
                            }

                            val text = roll.text
                            val rollNumber = text.split(" ").last().toIntOrNull()

                            if (rollNumber == null) {
                                respond { content = "**:interrobang: Roll Failed:** Couldn't get the result number." }
                                return@action
                            } else {
                                rollText = text
                                rollNumber
                            }
                        } else {
                            respond {
                                content = "**:interrobang: Invalid Number:** Please check your number (or dice)."
                            }
                            return@action
                        }
                    }

                    val originalValue = status.value

                    status.value = when (prefix) {
                        "+" -> status.value + number
                        "-" -> status.value - number
                        else -> number
                    }

                    val newValue = status.value

                    val change = newValue - originalValue

                    val emoji = if (change > 0)
                        ":chart_with_upwards_trend:"
                    else if (change < 0)
                        ":chart_with_downwards_trend:"
                    else
                        ":white_large_square:"

                    respond {
                        content = "${data.nameOnly} **${status.label} : $originalValue → $newValue**".let {
                            if (rollText != null) ":game_die: $rollText\n$emoji $it" else it
                        }
                    }
                }
            }
        }
    }
}