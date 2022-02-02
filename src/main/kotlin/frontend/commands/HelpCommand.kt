package frontend.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import com.elbekD.bot.types.Message

object HelpCommand {
    val commandName: String
        get() = "help"
    val commandDescription: String
        get() = "shows the help message"
    val commandHelpMessage: String
        get() = "type /help to show help message"


    fun onMessageDoAction(bot: Bot, message: Message,opts:String?,commandMap:Map<String,String>) {
        if (opts.isNullOrEmpty()){
            bot.sendMessage(message.chat.id, HELP_MSG, parseMode = "Markdown")
            return
        }else{
            val args=opts.split(' ').map { it.trim().lowercase() }.filter { it.isNotBlank() }
            if (args.size>1){
                bot.sendMessage(message.chat.id, ON_HELP_ERROR,parseMode = "Markdown")
                return
            }
            bot.sendMessage(
                message.chat.id,
                commandMap[args[0].removePrefix("/")] ?: ON_HELP_ERROR,
                "Markdown"
            )
        }

    }
    fun registerAsCommand(): BotCommand {
        return BotCommand(commandName, commandDescription)
    }
    // help message template
    private val ON_HELP_ERROR =
        """
    The Lord helps those who help themselves 👼

    Expected no arguments or command to explain
    Follow /help with the unclear command or leave empty for general guide
    """.trimIndent()
    private val HELP_MSG =
        """
         Available commands:
        /start - summons the bot
        /help  - show this usage guide
        /${LatestComicsCommand.commandName}  - show the latest comics
        /${PopularComicsCommand.commandName}  - get all the popular comics of our time
        /${CompletedComicsCommand.commandName} - get all the completed comics
        /${OnGoingComicsCommand.commandName} - get all the ongoing comics
        /${SearchCommand.commandName}  <comic name> - search for a given comic eg /search hotell
        /${GenresCommand.commandName} - lists all the available genres for use
        /${GenreCommand.commandName} - gets all the comics based on a genre eg /${GenreCommand.commandName} horror
    """.trimIndent()
}