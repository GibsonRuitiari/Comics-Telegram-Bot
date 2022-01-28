import com.elbekD.bot.Bot



fun main() {
    val botToken = "1814039661:AAEX69rqHYpFfkL6ZE7kG7RX49GQr50iTO4"
    val username = "@DummyB_Bot"
    val bot = Bot.createPolling(username, botToken) {
        allowedUpdates = listOf()

    }


}



