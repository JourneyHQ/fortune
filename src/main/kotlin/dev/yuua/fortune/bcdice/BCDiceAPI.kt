package dev.yuua.fortune.bcdice

import dev.yuua.fortune.bcdice.types.GameSystemData
import dev.yuua.fortune.bcdice.types.GameSystemInfoResponse
import dev.yuua.fortune.bcdice.types.GetGameSystemResponse
import dev.yuua.fortune.bcdice.types.RollResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object BCDiceAPI {
    private const val BASE_URL = "https://bcdice.onlinesession.app/v2/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val gameSystems = runBlocking { getGameSystems() }
    private var gameSystemLastFetch = System.currentTimeMillis()

    suspend fun getGameSystems(): List<GameSystemData> {
        // Cache for 1 hour
        if (System.currentTimeMillis() - gameSystemLastFetch < 1000 * 60 * 60)
            return gameSystems

        val response = client.get(BASE_URL) {
            url { appendPathSegments("game_system") }
        }

        return when (response.status) {
            HttpStatusCode.OK -> response.body<GetGameSystemResponse>().gameSystem

            else -> emptyList()
        }
    }

    private val gameSystemInfos = mutableMapOf<GameSystemInfoResponse, Long>()

    suspend fun getGameSystemInfo(id: String): GameSystemInfoResponse? {
        val cached = gameSystemInfos.keys.find { it.id == id }

        if (cached != null && System.currentTimeMillis() - gameSystemInfos[cached]!! < 1000 * 60 * 60)
            return cached

        val response = client.get(BASE_URL) {
            url { appendPathSegments("game_system", id) }
        }

        return when (response.status) {
            HttpStatusCode.OK -> {
                val info = response.body<GameSystemInfoResponse>()
                gameSystemInfos[info] = System.currentTimeMillis()
                info
            }

            else -> null
        }
    }

    suspend fun getRoll(id: String, command: String): RollResponse? {
        val response = client.get(BASE_URL) {
            url { appendPathSegments("game_system", id, "roll") }
            parameter("command", command)
        }

        return when (response.status) {
            HttpStatusCode.OK -> response.body<RollResponse>()

            else -> null
        }
    }
}