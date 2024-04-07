package dev.yuua.fortune.bcdice.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameSystemInfoResponse(
    val ok: Boolean,
    val id: String,
    val name: String,
    @SerialName("sort_key") val sortKey: String,
    @SerialName("command_pattern") val commandPattern: String,
    @SerialName("help_message") val helpMessage: String
) {
    fun getRegex() = Regex(commandPattern, RegexOption.IGNORE_CASE)
}