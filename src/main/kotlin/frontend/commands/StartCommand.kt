package frontend.commands


/*object StartCommand: Command {
    override val commandName: String
        get() = "start"
    override val commandDescription: String
        get() = "fires up the bot"
    override val commandHelpMessage: String
        get() = "type /start to start the bot"

    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
       return try {
           bot.sendMessage(message.chat.id,"hello there!")
           Result(true,null)
       }catch (ex:TelegramApiError){
           Result(false,ex.message)
       }
    }

}*/