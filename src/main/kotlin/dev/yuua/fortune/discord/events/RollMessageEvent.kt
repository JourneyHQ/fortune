package dev.yuua.fortune.discord.events

import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.getJumpUrl
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.yuua.fortune.Fortune
import dev.yuua.fortune.bcdice.BCDiceAPI
import dev.yuua.fortune.ccfolia.CharacterData

class RollMessageEvent : Extension() {
    override val name = this::class.simpleName!!

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check {
                isNotBot()

                val message = event.message

                val info = BCDiceAPI.getGameSystemInfo(
                    Fortune.channelGameSystems[message.channelId] ?: "Cthulhu7th"
                )

                if (info == null) {
                    fail()
                    return@check
                }

                val content = message.content
                val dice = content.split(" ", limit = 2).first()

                val shouldRoll = info.getRegex().containsMatchIn(dice) ||
                        Regex("reroll", RegexOption.IGNORE_CASE).matches(dice) ||
                        (Fortune.characters[message.author?.id]
                            ?.getSkillNames()
                            ?.any { it?.contains(content) ?: false }
                            ?: false)

                failIfNot(shouldRoll)
            }

            action {
                val message = event.message
                val author = message.author ?: return@action
                val command = message.content

                val shouldReroll = Regex("reroll", RegexOption.IGNORE_CASE).matches(command)

                val dice = if (shouldReroll) {
                    Fortune.lastRolls[message.author?.id] ?: kotlin.run {
                        message.reply {
                            content = "**:interrobang: No Recent Command:** Let's roll some dice! Try: `1d100`"
                        }
                        return@action
                    }
                } else {
                    val finalCommand = Fortune.characters[author.id]?.getSkills()
                        ?.find { CharacterData.getSkillName(it)?.contains(command) ?: false } ?: command
                    Fortune.lastRolls[author.id] = finalCommand
                    finalCommand
                }

                val roll = BCDiceAPI.getRoll(
                    Fortune.channelGameSystems[message.channelId] ?: "Cthulhu7th",
                    dice
                )

                if (roll == null) return@action

                val skill = CharacterData.getSkillName(dice)

                val text = if (skill != null) roll.messageWithSkill(skill) else roll.message

                if (roll.secret) {
                    val dm = author.dm(text)

                    if (dm == null) {
                        message.reply { content = "**:interrobang: Roll Failed:** Couldn't send a DM." }
                        return@action
                    }

                    message.reply {
                        content = "||**:shushing_face: Secret dice:** Check out [DM](<${dm.getJumpUrl()}>)||"
                    }
                } else {
                    message.reply { content = text }
                }
            }
        }
    }
}