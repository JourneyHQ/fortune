package dev.yuua.discord

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.commands.UnsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSlashCommand
import dev.yuua.Fortune
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

typealias Options = Arguments

object KordExExtensions {
    private suspend fun SlashCommand<*, *, *>.setGuilds() {
        val devGuildId = Fortune.config.devGuild?.toLong()
        if (devGuildId != null) guild(devGuildId)
        else Fortune.bot.kordRef.guilds.collect {
            guild(it.id)
        }
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
        builder: suspend UnsafeSlashCommand<O, ModalForm>.() -> Unit
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
        builder: suspend UnsafeSlashCommand<Options, ModalForm>.() -> Unit
    ) {
        unsafeSlashCommand {
            this.name = if (Fortune.isDev()) "dev-$name" else name
            this.description = description

            setGuilds()

            apply { builder() }
        }
    }
}