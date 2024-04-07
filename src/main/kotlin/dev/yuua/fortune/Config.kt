package dev.yuua.fortune

data class Config(
    val discord: String,
    val env: String? = "dev",
    val devGuild: String? = null,
    val mongodb: String = "mongodb://fortune:fortune-mongodb@localhost:27017/",
)
