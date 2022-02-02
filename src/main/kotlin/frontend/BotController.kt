package frontend

import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import frontend.commands.OnGoingComicsCommand


class BotController {
    private val botToken = "1814039661:AAEX69rqHYpFfkL6ZE7kG7RX49GQr50iTO4"
    private val username = "@DummyB_Bot"
    private val bot: Bot by lazy {
        Bot.createPolling(username,botToken){
            allowedUpdates= listOf()
        }
    }
    private val stateMachine by lazy{
        StateMachine(bot)
    }
    fun initializeCommands(){
        // a list of inputs
        val commandsList = listOf(OnGoingComicsCommand)
       commandsList.forEach { stateMachine.onEvent(it) }
        with(bot){
            // handle each command and log the state
           setMyCommands(commandsList.map { BotCommand(command = "/${it.commandName}",description = it.commandDescription) })
        }
    }
    fun onCreate(){
       bot.start()
    }

}