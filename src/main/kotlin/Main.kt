import com.elbekD.bot.Bot
import com.elbekD.bot.types.ReplyKeyboard
import frontend.BotController
import frontend.OnStartCommand
import frontend.TextParams


fun main() {
    val botToken = "1814039661:AAEX69rqHYpFfkL6ZE7kG7RX49GQr50iTO4"
    val username = "@DummyB_Bot"
    val bot = Bot.createPolling(username, botToken) {
        allowedUpdates = listOf()

    }
    val textParams = object:TextParams{
        override val text: String
            get() = "Hello There"
        override val parseMode: String= "Markdown"
        override val disableNotification: Boolean
            get() = false
        override val markup: ReplyKeyboard? = null
        override val replyTo: Long? =null
    }
    val controller = BotController(bot)
   controller.send(OnStartCommand("start",
   "fires up the bot","type /start to start the bot"),
   textParams =textParams)


}



