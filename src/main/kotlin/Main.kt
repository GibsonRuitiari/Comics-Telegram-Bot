import backend.comicDetails
import backend.comic_models.SManga
import backend.comic_models.SMangaInfo
import backend.search
import com.elbekD.bot.Bot
import com.elbekD.bot.http.TelegramApiError
import com.elbekD.bot.util.AllowedUpdate

import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import java.io.*
import java.net.URL
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.roundToInt
import kotlin.properties.Delegates


fun main() {
    val botToken = "5202138165:AAGUqIrooKyLF7pwa6VohEl2iM1Ht8ZMy7s"
     val bot: Bot=  Bot.createPolling("", botToken) {
         allowedUpdates = listOf(AllowedUpdate.Message,AllowedUpdate.Message,
             AllowedUpdate.CallbackQuery)
     }
    var issuesListObservable by Delegates.observable(emptyList<Pair<String,String>>()){
        property, oldValue, newValue ->
        println("old value:$oldValue new value$newValue")
        bot.observeIssueLinkList(newValue)
    }
      var observable by Delegates.observable(emptyList<Pair<String,String>>()){ property, oldValue, newValue -> bot.observeList(newValue){
            issuesListObservable = it
      }}
     bot.onCommand("/search"){msg,opts->
         if (opts.isNullOrEmpty()) return@onCommand
         search(opts).collect{
             val (message, copyOfComicCommands)=parseComicListToMessage(it)
             observable=copyOfComicCommands
             bot.sendMessage(msg.chat.id,message,parseMode = "Markdown")
         }

     }
    val imgAdress ="https://blogger.googleusercontent.com/img/a/AVvXsEhUm2Y2Nwo6dKi_8kRuld6NtNGvP9eOBXXaZBin42EvJKq-vklK1DfulEWM28uv4Zgw8gKCmCQescYeTBWqp1nzIvGOa30TlyudPxNavRUkmHQqkLIWKQ7gagFWTkWEjv7EpxFwkIfGxIBpif-mw4cbrCeYSJvl3EbDKkfCaOeESW-9ieER1TRGyLCp"

    bot.onCommand("/photo"){msg,opts ->
       //  val imgAdress ="https://blogger.googleusercontent.com/img/a/AVvXsEhUm2Y2Nwo6dKi_8kRuld6NtNGvP9eOBXXaZBin42EvJKq-vklK1DfulEWM28uv4Zgw8gKCmCQescYeTBWqp1nzIvGOa30TlyudPxNavRUkmHQqkLIWKQ7gagFWTkWEjv7EpxFwkIfGxIBpif-mw4cbrCeYSJvl3EbDKkfCaOeESW-9ieER1TRGyLCp"
          val text="<a href=$imgAdress></a>"
        val textToBeSent ="[photo]($imgAdress)"
        bot.sendMessage(msg.chat.id," $text hello there hehehe", parseMode = "HTML")
        bot.sendMessage(msg.chat.id,"hey hey ${System.lineSeparator()} $textToBeSent", parseMode = "Markdown")
    }
    println(observable.size)
    println(issuesListObservable.size)
  //  bot.start()
//    val a=ClassLoader.getSystemResource("first.png")
//    val b=ClassLoader.getSystemResource("second.png")
//    val c=ClassLoader.getSystemResource("third.png")
//    val streamArray = arrayOf(URL(imgAdress).file,URL(imgAdress).file,URL(imgAdress).file)
//    var out=ZipOutputStream(BufferedOutputStream(FileOutputStream("second.zip")))
//    var data = ByteArray(1024)
//    for (file in streamArray){
//
//        val fi = FileInputStream(file)
//        var origin = BufferedInputStream(fi)
//        var entry = ZipEntry(file.substring(file.lastIndexOf("/")))
//        out.putNextEntry(entry)
//        origin.copyTo(out,1024)
//        origin.close()
//    }
//    out.close()

}

private fun Bot.observeIssueLinkList(list: List<Pair<String, String>>){
    list.forEach {
        onCommand("/vd_${it.second}"){message, _ ->
           val fullUrl ="https://viewcomics.me/${it.first}"
            sendMessage(message.chat.id,fullUrl)
        }
    }
}
private fun Bot.observeList(list:List<Pair<String,String>>,action:(List<Pair<String,String>>)->Unit){
        list.forEach {
            onCommand("/v_${it.first}"){
                msg,_->
                println("https://viewcomics.me/comic/${it.second}")
                comicDetails("https://viewcomics.me/comic/${it.second}")
                    .onCompletion { cause ->
                        cause?.let {
                            println(it.cause)
                        }
                    }.collect{
                    val (details,issueCommandsList)=constructComicDetailsMessage(it)
                    action(issueCommandsList)
                  try {
                      val caption= "$details \n [photo](${it.comicImagePosterLink})"
                      sendMessage(msg.chat.id,caption, parseMode = "Markdown")
                     // sendPhoto(msg.chat.id,it.comicImagePosterLink, caption, parseMode = "Markdown")
                  }catch (ex:TelegramApiError){
                      throw ex
                  }
                }

            }
        }
}

// issue link--> "https://viewcomics.me/x-deaths-of-wolverine/issue-1"
// returns xdowi1 so we just need to add i_ to the returned link to make it a command
private fun String.returnIssueLink():Pair<String,String>{
    val originalName = replace("https://viewcomics.me/","")
    val link = originalName.replace("/","-").split("-").joinToString(""){it.take(1)}
    println(originalName)
    println(link)
    return originalName to link
}
private fun handleErrorMessages(cause:Throwable?):String {
  return when (cause) {
        is java.io.IOException -> "Our elves have notified us that your internet connection is shaky and the request could not be completed.Please attend to it and try again later."
        else->"Our elves have told us an unknown error has occurred . :XD don't fret. Meanwhile check your internet connection.\nMake sure it is not laggy and try again later"
    }
}
private fun parseComicListToMessage(list: List<SManga>): Pair<String, List<Pair<String, String>>> {
     val result=list.map {
        val modifiedName=it.comicName.replace("-","\\-")
            .replace(".","\\.")
            .replace(".","\\.")
            .replace("[","\\[")
            .replace("]","\\]")
            .replace("(","\\(")
            .replace(")","\\)")
            .replace("\\","")
        val originalName= it.comicLink.replace("https://viewcomics.me/comic/","")
        val modifiedLink=originalName .replace("-","_").split("_").joinToString(separator = "") { it.take(1) }
        //action.invoke(originalName, modifiedLink)
         Triple(modifiedName,modifiedLink,originalName)
     }

    val copyOfLinks=result.map { it.second to it.third }

    val returnString=result.joinToString(separator = "\n"){ "⚝*${it.first}* \n view details: [/v_${it.second}](http://start_url)\n"}
   return returnString to copyOfLinks
}


private  fun constructComicDetailsMessage(info: SMangaInfo): Pair<String,List<Pair<String,String>>> {
    val genres = info.genres.joinToString(separator = ",")
    val comicName = "\uD83D\uDCDA *${info.comicAlternateName}*"
    val comicAuthor = "_by: ${info.comicAuthor}_"
    val comicGenre = "*Genre:* $genres"
    val comicRatings = "*Views:* \uD83C\uDF1F ${info.comicViews.roundToInt()} \uD83C\uDF1F"
    val comicYearReleased = "*Year:* ${info.yearOfRelease}"
    val comicStatus = "*Status:* \uD83D\uDCCC ${info.comicStatus} \uD83D\uDCCC"
    val comicDescription = info.comicDescription.replace("-", "\\-")
        .replace(".", "\\.")
        .replace(".", "\\.")
        .replace("[", "\\[")
        .replace("]", "\\]")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("\\", "")
    // send the issues link to the queue
    val issueLinks=info.issues.map {it.issueLink.returnIssueLink()}
    val issues= info.issues.joinToString(separator = "\n"){
        val issueCommand = "[/vd_${it.issueLink.returnIssueLink().second}](http://details_url)"
        "\uD83D\uDD17 ${it.issueName}  |  ${it.issueReleaseDate}\n\n*view details:*$issueCommand\n\n"
    }
    val comicDetails = "$comicName \n$comicAuthor\n\n$comicGenre\n\n$comicRatings\n\n$comicStatus\n\n$comicYearReleased\n\n$comicDescription\n\n$issues"


    return comicDetails to issueLinks
}
/**
val bot: Bot=  Bot.createPolling(username, botToken) {
allowedUpdates = listOf(AllowedUpdate.Message,AllowedUpdate.Message,
AllowedUpdate.CallbackQuery)
}



val number = MutableStateFlow(1)
val channel = Channel<Pair<String, String>>(capacity = 20)
val commandChannel = Channel<Pair<String, String>>(capacity = 2) // rendezvous
bot.drainChannelQueueAndSendIssues(channel)

// on search command
//  it works like a charm :XD
bot.onSearchCommand(commandChannel)

bot.drainChannelQueueAndSendComicDetails(commandChannel, channel)



val firstRow = listOf(
InlineKeyboardButton("<< prev", callback_data = "prev_cb"),
InlineKeyboardButton("next >>", callback_data = "next_cb")
)
val onGoingComicsMarkup = InlineKeyboardMarkup(listOf(firstRow))
bot.onCommand("/hello") { msg, _ ->
try {
//bot.showOnGoingComics(number.value, msg, onGoingComicsMarkup, channel)
bot.sendMessage(msg.chat.id,"Hello there").get()
} catch (ex: Exception) {
println(ex.message)
}
}

bot.start()// end
private fun Bot.handleCallbackQuery(number:MutableStateFlow<Int>,
markup: InlineKeyboardMarkup,channel:Channel<Pair<String,String>>){
    onCallbackQuery("prev_cb") {
            callbackQuery ->
        answerCallbackQuery(callbackQuery.id)
        val chatId = callbackQuery.message?.chat?.id ?: return@onCallbackQuery
        number.value = if (number.value>1) number.value-1 else 1
        updateOnGoingComicsOnPaginationButtonsClick(chatId,
            messageId = callbackQuery.message?.message_id, inlineMessageId = callbackQuery.inline_message_id,
            page = number.value, markup = markup,channel)
    }
    onCallbackQuery("next_cb"){
            callbackQuery ->
        answerCallbackQuery(callbackQuery.id)
        number.value +=1
        val chatId = callbackQuery.message?.chat?.id ?: return@onCallbackQuery
        updateOnGoingComicsOnPaginationButtonsClick(chatId,
            messageId = callbackQuery.message?.message_id, inlineMessageId = callbackQuery.inline_message_id,
            page = number.value, markup = markup,channel)
    }
}
private fun Bot.onSearchCommand(commandChannel: Channel<Pair<String, String>>){
    onCommand("/search"){
        message, s ->
        if (s.isNullOrEmpty()) sendMessage(message.chat.id,"Please provide the comic name that you want to search for \nexample /search the Hotel")
        else{
           search(s).onCompletion {
               cause ->
               cause?.let {
                   println("the following error occurred while searching for comics: ${it.message}")
                   handleErrorMessages(it,message.chat.id)
               }
           }.collect{
               val searchResults=parseComicListToMessage(it,commandChannel)
               try {
                   sendMessage(message.chat.id,searchResults,parseMode = "Markdown").get()
               }catch (ex:TelegramApiError){
                   println("The following exception occurred while sending search results: ${ex.message}")
               }
           }
        }
    }
}
private fun Bot.handleErrorMessages(cause:Throwable?,chatId: Any) {
    val errorMsg=when (cause) {
        is java.io.IOException -> "Our elves have notified us that your internet connection is shaky and the request could not be completed.Please attend to it and try again later."
        else->"Our elves have told us an unknown error has occurred please try again later."
    }
    sendMessage(chatId,errorMsg)
}
private  fun Bot.drainChannelQueueAndSendIssues(channel: ReceiveChannel<Pair<String, String>>){
        // drain the queue
    //expected full url--> https://viewcomics.me/x-deaths-of-wolverine/issue-1
    val result= channel.tryReceive()

    if (result.isSuccess){
       val x= result.getOrThrow().second
        val issueCommand ="/vd_$x"
        onCommand(issueCommand){message, _->
            println("full issue url: https://viewcomics.me/${result.getOrThrow().first}")
            sendMessage(message.chat.id,"received the following command $issueCommand")
        }
    }


    }
private suspend fun Bot.drainChannelQueueAndSendComicDetails(commandChannel: ReceiveChannel<Pair<String, String>>,
issueChannel: Channel<Pair<String, String>>) {
    // drain the queue
    for (pair in commandChannel){
        println(pair.first)
    }


//       for (pair in commandChannel){
//          val command = "/v_${pair.second}"
//           onCommand(command){message, _ ->
//               val fullUrl = "https://viewcomics.me/comic/${pair.first}"
//               comicDetails(fullUrl).onCompletion { cause->
//                   cause?.let {
//                       println("error occurred while fetching the comic details:${it.cause}")
//                   }
//               }.collect{
//                   val caption = constructComicDetailsMessage(it,issueChannel)
//                   try {
//                       sendPhoto(message.chat.id,caption, parseMode = "Markdown").get()
//                   }catch (ex:TelegramApiError){
//                       println("error occurred while sending the photo:${ex.message}")
//                   }
//               }
//           }
//       }


    }
private suspend fun Bot.updateOnGoingComicsOnPaginationButtonsClick(chatId:Long?,messageId:Long?,
                                                                            inlineMessageId:String?,page:Int,markup:InlineKeyboardMarkup,channel: Channel<Pair<String, String>>){
            val message = fetchOngoingComics(page){originalName, modifiedLink ->
                onViewDetailsClicked(originalName, modifiedLink,channel)
            }
            editMessageText(chatId,messageId,inlineMessageId,
                text = "*Showing On Going Comics* \n \n $message",parseMode = "Markdown", markup = markup)
        }
                private suspend fun Bot.showOnGoingComics(page: Int,msg:Message,
            markup:ReplyKeyboard,channel: Channel<Pair<String, String>>){
        val message = fetchOngoingComics(page){originalName, modifiedLink ->
            onViewDetailsClicked(originalName, modifiedLink,channel)
        }
        val chatId = msg.chat.id
        try {
            sendMessage(chatId,"*Showing On Going Comics* \n\n $message",
                parseMode = "Markdown", markup = markup).get()
        }catch (ex:TelegramApiError){
            println("error showing on going comics: ${ex.cause}")
        }
    }

private fun Bot.onViewDetailsClicked(originalName: String,modifiedLink: String,channel: Channel<Pair<String, String>>){
        // example of a modifiedLink-> /v_r

        onCommand("/v_$modifiedLink"){message, _ ->
            // embed the original name in the url to the url
            val fullUrl = "https://viewcomics.me/comic/$originalName"
            comicDetails(fullUrl).onCompletion { cause->
                cause?.let {
                    println("error occurred while fetching details: $it")
                }
            }.collect{info->
                val caption = constructComicDetailsMessage(info,channel)
                try {
                    sendPhoto(message.chat.id, caption = caption, parseMode = "Markdown",
                        photo = info.comicImagePosterLink).get()
                }catch (ex:Exception){
                    println("an error occurred while sending the photo:${ex.cause}")
                }
            }
        }
    }


private suspend inline fun fetchOngoingComics(page:Int, crossinline action:(originalName:String, modifiedLink:String)->Unit):String{
            var returnString=""
            ongoingComics(page).onCompletion { cause->
                cause?.let {
                    println("error occurred while fetching: $it")
                }
            }.cancellable().collect{
                val list= it.mangas.map {
                    val modifiedName=it.comicName.replace("-","\\-")
                        .replace(".","\\.")
                        .replace(".","\\.")
                        .replace("[","\\[")
                        .replace("]","\\]")
                        .replace("(","\\(")
                        .replace(")","\\)")
                        .replace("\\","")
                    val originalName= it.comicLink.replace("https://viewcomics.me/comic/","")
                    val modifiedLink=originalName .replace("-","_").split("_").joinToString(separator = "") { it.take(1) }
                    action.invoke(originalName, modifiedLink)
                    Pair(modifiedName,modifiedLink)}
                returnString=list.joinToString(separator = "\n"){ "⚝*${it.first}* \n view details: [/v_${it.second}](http://start_url)\n"} }
            return returnString
        }






**/
