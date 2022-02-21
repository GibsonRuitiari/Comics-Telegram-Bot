package frontend

import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import frontend.commands.*


class BotController {
    private val botToken = "5118772624:AAGzGzVfcMp_YSEGwlyO97K7UC7z6AkjO2U" // todo: save this somewhere else; also replace this with your bot token 
    private val username = "@Xcd87_bot"
    // open for testing but read only
     val bot: Bot by lazy {
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
        val genresCommand = GenresCommand.registerAsCommand()
        val commandsList = listOf(OnGoingComicsCommand,
            CompletedComicsCommand, SearchCommand,PopularComicsCommand,LatestComicsCommand,
        GenreCommand)
       commandsList.forEach { stateMachine.onEvent(it) }
        with(bot){
            // handle each command and log the state
            initializeSpecialCommands(commandsList)
            val botCommands = commandsList.map { BotCommand(command = "/${it.commandName}",description = it.commandDescription) }.toMutableList()
            botCommands+=startCommand
            botCommands += helpCommand
            botCommands += genresCommand

            setMyCommands(botCommands)
        }
    }
    // help and start command
    private fun Bot.initializeSpecialCommands(commands:List<Command>){
        val commandsMap= commands.associate { it.commandName to it.commandHelpMessage }.toMutableMap()
        commandsMap[StartCommand.commandName] = StartCommand.commandHelpMessage
        commandsMap[HelpCommand.commandName] = HelpCommand.commandHelpMessage
        commandsMap[GenresCommand.commandName] = GenresCommand.commandHelpMessage
        onCommand("/${StartCommand.commandName}"){
            message, _ ->
            StartCommand.onMessageDoAction(this,message)
        }
        onCommand("/${HelpCommand.commandName}"){
            message, s ->
            HelpCommand.onMessageDoAction(this,message,s,
            commandsMap)
        }
        onCommand("/${GenresCommand.commandName}"){
            message, _ ->
            GenresCommand.onMessageDoAction(this,message)
        }
    }
    fun onCreate(){
       bot.start()
    }

}
