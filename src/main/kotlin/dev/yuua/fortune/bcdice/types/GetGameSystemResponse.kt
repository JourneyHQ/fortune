package dev.yuua.fortune.bcdice.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetGameSystemResponse(
    @SerialName("game_system") val gameSystem: List<GameSystemData>
)

@Serializable
data class GameSystemData(
    val id: String,
    val name: String,
    @SerialName("sort_key") val sortKey: String
)