package frontend.commands

import backend.comic_models.Genres
import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import com.elbekD.bot.types.Message

// special command that showcases all the genres available
object GenresCommand {
    val commandName: String
        get() = "genres"
    val commandDescription: String
        get() = "shows all the available genres"
    val commandHelpMessage: String
        get() = "type /genres to show all the available genres"

    fun onMessageDoAction(bot: Bot, message: Message) {
        val genresString=Genres.values().joinToString(System.lineSeparator()){
            "☘*${it.genreName}* ${it.emoji} \n"
        }
        val contentMsg="*Available Genres*\n\n$genresString"
        bot.sendMessage(message.chat.id,contentMsg, parseMode = "Markdown")
    }
    fun registerAsCommand(): BotCommand {
        return BotCommand(commandName, commandDescription)
    }

}