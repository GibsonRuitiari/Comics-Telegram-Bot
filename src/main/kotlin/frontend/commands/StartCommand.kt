package frontend.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import com.elbekD.bot.types.Message


object StartCommand {
     val commandName: String
        get() = "start"
     val commandDescription: String
        get() = "fires up the bot"
     val commandHelpMessage: String
        get() = "type /start to start the bot"

      fun onMessageDoAction(bot: Bot, message: Message) {
          val startMessage ="Hello there. I am *xcd87_01* \uD83E\uDD16 \n I will be serving you by giving all the latest,popular,ongoing + completed comics\nI know right?" +
                  "That's every kids dream :XD \uD83E\uDD24 (better than sex \uD83D\uDE39)" +
                  "\nIn addition, I will also enable you to search for a given comic.\nSit back relax and let's go to comic land \uD83D\uDC63 \uD83D\uDEF8."
          bot.sendMessage(message.chat.id,"*xcd87_01* summoned.. ... .. .. \n", parseMode = "Markdown")
          bot.sendMessage(message.chat.id,startMessage, parseMode = "Markdown")
          bot.sendMessage(message.chat.id,"Type /help or click on the command button right theree \uD83D\uDE01 \uD83D\uDC47 to get started.")
    }
    fun registerAsCommand():BotCommand{
        return BotCommand(commandName, commandDescription)
    }

}