import backend.comicDetails
import backend.ongoingComics
import com.elbekD.bot.Bot
import com.elbekD.bot.feature.chain.chain
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.MessageEntity
import com.elbekD.bot.util.AllowedUpdate
import frontend.BotController
import frontend.commands.OnGoingComicsCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


fun main() {

val name="x-deaths-of-wolverine"
    name.replace("-"," ")
     val botToken = "1814039661:AAEX69rqHYpFfkL6ZE7kG7RX49GQr50iTO4"
     val username = "@DummyB_Bot"
     val bot:Bot by lazy {
        Bot.createPolling(username,botToken)
    }
    bot.onCommand("/l"){
        msg,_->
       try {
           val mangas=ongoingComics(1).first().mangas

           val modified=mangas.map {
               val name_=it.comicName
                   .replace("-","\\-")
                   .replace(".","\\.")
                   .replace(".","\\.")
                   .replace("[","\\[")
                   .replace("]","\\]")
                   .replace("(","\\(")
                   .replace(")","\\)")
                   .replace("\\","")
               val replacedLink = it.comicLink.replace("https://viewcomics.me/comic/","")

               val ln = replacedLink.replace("-","_")
               M(name_,ln)
           }
           val s=modified.joinToString("\n"){ "★ *${it.name}* \n _view_: [/v_${it.link}](http://start_url)\n"}

           bot.sendMessage(msg.chat.id, s,parseMode = "Markdown").get()
       }catch (ex:Exception){
           println(ex.localizedMessage)
           println(ex.message)
       }
    }
    // vxlow
val v= "v_x_lives_of_wolverine"
    for (x in v){
        if (x=='_'){

        }
    }
 // bot.start()
//    val comics=(0..20).map {
//        Comic(name="comic $it", description = "description $it")
//    }
//    comics.joinToString {
//        "*bold \\*[*] ${it.name}"
//    }
//    bot.onCommand("/latest"){
//        msg,opts->
//        try {
//            val comics_= listOf("Invicible","Going2","Come1")
//            val comicString=comics_.joinToString(separator = "\n") {
//                "★ *$it* ${System.lineSeparator()} view: [/vw\\_$it](http://start_url) ${System.lineSeparator()}"
//            }
//            bot.sendMessage(msg.chat.id,"*Showing Latest Comics* ${System.lineSeparator()}$comicString",
//                parseMode = "MarkdownV2").get()
//           comics_.map {
//                "/vw_$it"
//            }.forEach {
//                bot.onCommand(it){
//                    msg,_->
//                    val comicName = it.removePrefix("/vw_")
//                    bot.sendMessage(msg.chat.id,"comic *${comicName}*", parseMode = "MarkdownV2").get()
//                }
//            }
//        }catch (ex:Exception){
//            println(ex.message)
//        }
//    }
    //${System.lineSeparator()} _download_  [/vw\\_invincible](http://start_url)"



    // https://viewcomics.me/comic/clive-barker-s-the-great-and-secret-show
    // https://viewcomics.me/comic/invincible
//    bot.onCommand("/vw_invincible"){
//        msg,opts->
//        println(msg.text)
//        bot.sendMessage(msg.chat.id,"hello there")
//    }

//    bot.onCallbackQuery("test"){
//        bot.answerCallbackQuery(it.id)
//        val chatId=it.message?.chat?.id ?: return@onCallbackQuery
//        bot.editMessageText(chatId, inlineMessageId = it.inline_message_id, messageId = it.message?.message_id, text = "Hello there")
//    }

}


data class M(val name:String,val link:String)

