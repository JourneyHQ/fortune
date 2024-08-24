package dev.yuua.fortune.discord

import dev.kord.common.entity.Snowflake
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.PublicSlashCommand
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.slash.UnsafeSlashCommand
import dev.kordex.modules.dev.unsafe.extensions.unsafeSlashCommand
import dev.yuua.fortune.Fortune

typealias Options = Arguments

object KordExExtensions {
    private fun String.toSnowflake() = Snowflake(this)

    private fun SlashCommand<*, *, *>.setGuilds() {
        val devGuildId = Fortune.config.devGuild?.toSnowflake()
        if (devGuildId != null) guild(devGuildId)
    }

    // Command Extension Functions
    @JvmName("namedPublicSlashCommandWithOptionModal")
    suspend fun <O : Options, M : ModalForm> Extension.publicSlashCommand(
        name: String,
        description: String,
        options: () -> O,
        modal: () -> M,
        builder: suspend PublicSlashCommand<O, M>.() -> Unit
    ) {
        publicSlashCommand(options, modal) {
            this.name = if (Fortune.isDev()) "dev-$name" else name
            this.description = description

            setGuilds()

            apply { builder() }
        }
    }

    @JvmName("namedPublicSlashCommandWithModal")
    suspend fun <M : ModalForm> Extension.publicSlashCommand(
        name: String,
        description: String,
        modal: () -> M,
        builder: suspend PublicSlashCommand<Options, M>.() -> Unit
    ) {
        publicSlashCommand(modal) {
            this.name = if (Fortune.isDev()) "dev-$name" else name
            this.description = description

            setGuilds()

            apply { builder() }
        }
    }

    @JvmName("namedPublicSlashCommandWithOption")
    suspend fun <O : Options> Extension.publicSlashCommand(
        name: String,
        description: String,
        options: () -> O,
        builder: suspend PublicSlashCommand<O, ModalForm>.() -> Unit
    ) {
        publicSlashCommand(options) {
            this.name = if (Fortune.isDev()) "dev-$name" else name
            this.description = description

            setGuilds()

            apply { builder() }
        }
    }

    @JvmName("namedPublicSlashCommand")
    suspend fun Extension.publicSlashCommand(
        name: String,
        description: String,
        builder: suspend PublicSlashCommand<Options, ModalForm>.() -> Unit
    ) {
        publicSlashCommand {
            this.name = if (Fortune.isDev()) "dev-$name" else name
            this.description = description

            setGuilds()

            apply { builder() }
        }
    }

    // Subcommand Extension Functions
    @JvmName("namedPublicSubCommandWithOptionModal")
    suspend fun <O : Options, M : ModalForm> SlashCommand<*, *, *>.publicSubCommand(
        name: String,
        description: String,
        options: () -> O,
        modal: () -> M,
        builder: suspend PublicSlashCommand<O, M>.() -> Unit
    ) {
        publicSubCommand(options, modal) {
            this.name = name
            this.description = description

            apply { builder() }
        }
    }

    @JvmName("namedPublicSubCommandWithModal")
    suspend fun <M : ModalForm> SlashCommand<*, *, *>.publicSubCommand(
        name: String,
        description: String,
        modal: () -> M,
        builder: suspend PublicSlashCommand<Options, M>.() -> Unit
    ) {
        publicSubCommand(modal) {
            this.name = name
            this.description = description

            apply { builder() }
        }
    }

    @JvmName("namedPublicSubCommandWithOption")
    suspend fun <O : Options> SlashCommand<*, *, *>.publicSubCommand(
        name: String,
        description: String,
        options: () -> O,
        builder: suspend PublicSlashCommand<O, ModalForm>.() -> Unit
    ) {
        publicSubCommand(options) {
            this.name = name
            this.description = description

            apply { builder() }
        }
    }

    @JvmName("namedPublicSubCommand")
    suspend fun SlashCommand<*, *, *>.publicSubCommand(
        name: String,
        description: String,
        builder: suspend PublicSlashCommand<Options, ModalForm>.() -> Unit
    ) {
        publicSubCommand {
            this.name = name
            this.description = description

            apply { builder() }
        }
    }

    // Unsafe Command Extension Functions
    @JvmName("namedUnsafeSlashCommandWithOption")
    @OptIn(UnsafeAPI::class)
    suspend fun <O : Options> Extension.unsafeCommand(
        name: String,
        description: String,
        options: () -> O,
        builder: suspend UnsafeSlashCommand<O, UnsafeModalForm>.() -> Unit
    ) {
        unsafeSlashCommand(options) {
            this.name = if (Fortune.isDev()) "dev-$name" else name
            this.description = description

            setGuilds()

            apply { builder() }
        }
    }

    @OptIn(UnsafeAPI::class)
    suspend fun Extension.unsafeCommand(
        name: String,
        description: String,
        builder: suspend UnsafeSlashCommand<Options, UnsafeModalForm>.() -> Unit
    ) {
        unsafeSlashCommand {
            this.name = if (Fortune.isDev()) "dev-$name" else name
            this.description = description

            setGuilds()

            apply { builder() }
        }
    }
}