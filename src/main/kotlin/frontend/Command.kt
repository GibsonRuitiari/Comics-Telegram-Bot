package frontend

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message

// our inputs or events
interface Command{
    val commandName:String
    val commandDescription:String
    val commandHelpMessage:String
    fun onMessageDoAction(bot:Bot,message: Message,options:String?):Result
}