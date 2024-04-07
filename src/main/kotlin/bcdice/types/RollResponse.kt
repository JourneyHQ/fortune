package dev.yuua.bcdice.types

import kotlinx.serialization.Serializable

@Serializable
data class RollResponse(
    val ok: Boolean,
    val text: String,
    val secret: Boolean,
    val success: Boolean,
    val failure: Boolean,
    val critical: Boolean,
    val fumble: Boolean,
    val rands: List<Rand>
) {
    private val emoji = when {
        success -> if (critical) ":star:" else ":white_check_mark:"
        failure -> if (fumble) ":boom:" else ":no_entry_sign:"
        else -> ":game_die:"
    }

    val message = "$emoji $text"

    fun messageWithSkill(skill: String) = "$emoji **$skill** $text"
}

@Serializable
data class Rand(
    val kind: String,
    val sides: Int,
    val value: Int
)