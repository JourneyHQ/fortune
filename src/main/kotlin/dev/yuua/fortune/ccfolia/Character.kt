package dev.yuua.fortune.ccfolia

import dev.kordex.core.pagination.builders.PaginatorBuilder
import dev.kord.common.Color
import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val kind: String,
    val data: CharacterData
)

@Serializable
data class CharacterData(
    val name: String,
    val initiative: Int,
    val externalUrl: String,
    val iconUrl: String,
    val commands: String,
    val status: List<Status>,
    val params: List<Param>
) {
    companion object {
        fun getSkillName(skill: String) = skill.split(" ")
            .find { it.startsWith("【") && it.endsWith("】") }
    }

    fun getSkills() = commands.split("\n").map {
        val statusReplaced = status.fold(it) { replaced, status ->
            replaced.replace("{${status.label}}", status.value.toString())
        }

        params.fold(statusReplaced) { replaced, param ->
            replaced.replace("{${param.label}}", param.value.toString())
        }.replace("　", " ")
    }

    fun getSkillNames() = getSkills().map(::getSkillName)

    val nameOnly = name.replace(Regex(" \\(.+\\)"), "")

    val readOnly = name.replace(nameOnly, "").removeSurrounding(prefix = " (", suffix = ")")

    fun getEmbed(): suspend PaginatorBuilder.() -> Unit = {
        for (skillPage in getSkills().chunked(10)) {
            page {
                title = nameOnly
                description = readOnly

                thumbnail {
                    url = iconUrl
                }

                field(":chart_with_upwards_trend: Status") {
                    status.joinToString("・") { status ->
                        val value = status.value.toString().padStart(2, ' ')
                        val max = status.max.toString().padStart(2, ' ')
                        "**${status.label}** $value/$max"
                    }
                }

                field(":bar_chart: Parameters") {
                    params.chunked(4).joinToString("\n") { chunkedParams ->
                        chunkedParams.joinToString("・") { param ->
                            "**${param.label}** ${param.value}"
                        }
                    }
                }

                field(":game_die: Commands") {
                    skillPage.joinToString("\n")
                }
                color = Color(0x5dffa6)
            }
        }
    }
}

@Serializable
data class Status(
    val label: String,
    var value: Int,
    val max: Int
)

@Serializable
data class Param(
    val label: String,
    val value: Int
)