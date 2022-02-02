package frontend

import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import frontend.commands.*


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
        val startCommand = StartCommand.registerAsCommand()
        val helpCommand = HelpCommand.registerAsCommand()
        val commandsList = listOf(OnGoingComicsCommand,
            CompletedComicsCommand, SearchCommand,PopularComicsCommand,LatestComicsCommand)
       commandsList.forEach { stateMachine.onEvent(it) }
        with(bot){
            // handle each command and log the state
            initializeSpecialCommands(commandsList)
            val botCommands = commandsList.map { BotCommand(command = "/${it.commandName}",description = it.commandDescription) }.toMutableList()
            botCommands+=startCommand
            botCommands += helpCommand
            setMyCommands(botCommands)
        }
    }
    // help and start command
    private fun Bot.initializeSpecialCommands(commands:List<Command>){
        val commandsMap= commands.associate { it.commandName to it.commandHelpMessage }.toMutableMap()
        commandsMap[StartCommand.commandName] = StartCommand.commandHelpMessage
        commandsMap[HelpCommand.commandName] = HelpCommand.commandHelpMessage
        onCommand("/${StartCommand.commandName}"){
            message, _ ->
            StartCommand.onMessageDoAction(this,message)
        }
        onCommand("/${HelpCommand.commandName}"){
            message, s ->
            HelpCommand.onMessageDoAction(this,message,s,
            commandsMap)
        }
    }
    fun onCreate(){
       bot.start()
    }

}