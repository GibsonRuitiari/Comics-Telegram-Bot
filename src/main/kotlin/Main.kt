import com.elbekD.bot.Bot
import frontend.OnStartCommand
import frontend.StateMachine


fun main() {
    val botToken = "1814039661:AAEX69rqHYpFfkL6ZE7kG7RX49GQr50iTO4"
    val username = "@DummyB_Bot"
    val bot = Bot.createPolling(username, botToken) {
        allowedUpdates = listOf()

    }
    val input = OnStartCommand("start","starts the bot",
        "type /start to start the bot")
    val stateMachine  = StateMachine( bot = bot)
    stateMachine.onCommand(input){bot_, message, _ ->
        bot_.sendMessage(message.chat.id,"Hey There I am gibson the bot!").get()
    }
    stateMachine.currentState


}



