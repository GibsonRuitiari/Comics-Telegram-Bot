package frontend

import frontend.commands.GenresCommand
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import utils.await

internal class BotControllerTest {

    @Test
    fun initializeCommands() = runBlocking{
        val controller = BotController()
        controller.initializeCommands()
        val commands=controller.bot.getMyCommands().await()
        assertTrue {
            commands.isNotEmpty()
        }
        assertTrue {
            commands.contains(GenresCommand.registerAsCommand())
        }

    }


}