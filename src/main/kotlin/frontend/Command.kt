package frontend

import com.elbekD.bot.Bot

// our inputs or events
interface Command{
    val commandName:String
    val commandDescription:String
    val commandHelpMessage:String
    fun onMessageDoAction(bot: Bot)
}