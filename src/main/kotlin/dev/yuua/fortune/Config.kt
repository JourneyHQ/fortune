package dev.yuua.fortune

data class Config(
    val discord: String,
    val env: String? = "dev",
    val devGuild: String? = null,
)
