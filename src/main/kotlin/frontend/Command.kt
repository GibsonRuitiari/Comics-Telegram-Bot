package frontend

import backend.comicDetails
import backend.comic_models.SManga
import backend.comic_models.SMangaInfo
import backend.comic_models.SMangaIssue
import com.elbekD.bot.Bot
import com.elbekD.bot.http.TelegramApiError
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import mu.KLogger
import java.net.URL
import java.net.UnknownHostException
import java.net.http.HttpTimeoutException
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.math.roundToInt


// our inputs or events
interface Command{
    val commandName:String
    val commandDescription:String
    val commandHelpMessage:String
    suspend fun onMessageDoAction(bot: Bot, message: Message, options:String?):Result

    /******************************
     * Default Variables Start
     *****************************/
    // keeps track of the index of a chunked list
     val index:MutableStateFlow<Int>



    // issues button row
    val buttonRow:List<InlineKeyboardButton>
    get() = listOf(InlineKeyboardButton("<<prev",
    callback_data = "prev_cb"),
    InlineKeyboardButton("next>>", callback_data = "next_cb"))

    //markup
    val issuesButtonMarkup:InlineKeyboardMarkup
    get() = InlineKeyboardMarkup(listOf(buttonRow))
    /******************************
     * Default Variables End
     *****************************/


    /*
     * Unless needed, kindly refrain from overriding these default methods
     */
    /******************************
     * Sanitizer methods Start
     *****************************/
    /*
     * These methods clean out unnecessary characters in the names and descriptions
     * and give out a "near-perfect" strings as per Telegram Api demands
     */
   private fun String.cleanAndEscapeReservedCharacters():String{
     return  replace("-","\\-")
           .replace(".","\\.")
           .replace(".","\\.")
           .replace("[","\\[")
           .replace("]","\\]")
           .replace("(","\\(")
           .replace(")","\\)")
           .replace("\\"," ")
   }
    fun parseMangasAndReturnTheContentMessage(mangas: List<SManga>): String {
        val modifiedList = mangas.map {
            val sanitizedComicName = it.comicName.cleanAndEscapeReservedCharacters()
            val comicCommandFromComicLink = it.comicLink.constructComicCommandsFromComicLinks().second
            sanitizedComicName to comicCommandFromComicLink
        }
        return modifiedList.joinToString(separator = "\n") { "⚝*${it.first}* \n view details: [/v_${it.second}](http://start_url)\n" }
    }
    /*
      Like its counterparts this method formats the issue lists and gives out the string/message to be
      sent to the user
     */
    private fun List<SMangaIssue>.formatToString():String{
        return joinToString(separator = "\n"){
            val issueCommand = "[/vd_${it.issueLink.returnIssueLink().second}](http://details_url)"
            "\uD83D\uDD17 ${it.issueName}  |  ${it.issueReleaseDate}\n*view details:*$issueCommand\n\n"}
    }
    /******************************
     * Sanitizer methods END
     *****************************/


    /******************************
     * Helper methods END
     *****************************/
    /**
     * The comic name is supposed to be stripped from the original comic url eg
     * :https://viewcomics.me/comic/the-hotell the name here is the-hotell
     * so given the comic name stripped from url come up with a string that can be used as a command
     */
    private fun String.parseComicNameAndReturnComicLink():String{
        return replace("-","_").split("_").joinToString(separator = "") { it.take(1) }
    }
    /*
     Parses an error & gives out the appropriate error message to be shown to the user
     */
    fun parseThrowableAndGiveErrorMessage(cause:Throwable):String = when(cause){
        is HttpTimeoutException, is UnknownHostException->"\uD83E\uDD39 our elves have notified us that there is a problem with your internet connection.\nPlease check your internet connection and try again :XD."
        else->{
            "\uD83D\uDC40 shucks an error occurred on our side,please sit tight and wait for the problem to be rectified by our elves.\nPlease try again later :XD"
        }
    }

    /**
     an example of an issue link is "https://viewcomics.me/x-deaths-of-wolverine/issue-1"
     so it returns xdowi1 and x-deaths-of-wolverine/issue-1
     the xdowi1 can be combined with /vd_ to make /vd_xdowi1 (hence a whole issue command)
     so when the user clicks on this command, we can be able to fetch the issue since we have
     the original name which is x-deaths-of-wolverine/issue-1
     */
    private fun String.returnIssueLink():Pair<String,String>{
        val originalName = replace("https://viewcomics.me/","")
        val link = originalName.replace("/","-").split("-").joinToString(""){it.take(1)}
        return originalName to link
    }

    /**
     * This function writes a stream to a file
     * given an url
     */
    private fun writeStreamToFile(url:String){
        URL(url).openStream().use { is_->
            // behind the scenes this uses a buffer
            Path(System.getProperty("user.home"),"test.png").toFile()
                .outputStream().use { os->
                    is_.copyTo(os)
                }

        }
    }
    /******************************
     * Helper methods END
     *****************************/


    /******************************
     * Constructor methods Start
     *****************************/
    fun SMangaInfo.constructComicDetails(): String {
        val genres = genres.joinToString(separator = ",")
        val comicName = "\uD83D\uDCDA *${comicAlternateName}*"
        val comicAuthor = "_by: ${comicAuthor}_"
        val comicGenre = "*Genre:* $genres"
        val comicRatings = "*Views:* \uD83C\uDF1F ${comicViews.roundToInt()} \uD83C\uDF1F"
        val comicYearReleased = "*Year:* $yearOfRelease"
        val comicStatus = "*Status:* \uD83D\uDCCC $comicStatus \uD83D\uDCCC"
        val comicDescription = comicDescription.cleanAndEscapeReservedCharacters()
        return "$comicName \n$comicAuthor\n\n$comicGenre\n\n$comicRatings\n\n$comicStatus\n\n$comicYearReleased\n\n$comicDescription\n\n"

    }
    /**
     * Given a string that represents a link return a pair containing the command link and the original name
     * for example:https://viewcomics.me/comic/the-hotell
     * the original name would be --> the-hotel
     * the command link would be th
     */
    fun String.constructComicCommandsFromComicLinks():Pair<String,String>{
        // the original name which we are going to embed in the url
        val originalName= replace("https://viewcomics.me/comic/","")
        // the command link
        return originalName to originalName.parseComicNameAndReturnComicLink()
    }
    /******************************
     * Constructor methods END
     *****************************/



    /******************************
     * Main methods Start
     *****************************/

    /**
     * Given a list containing a commands eg /v_th (the-hotell)
     * we need to observe this list such that when the user clicks we react on the command
     */
    fun onComicDetailsClicked(commandsList:List<Pair<String,String>>,bot: Bot,
    logger: KLogger){
        commandsList.forEach {
           bot.handleClickedComics(it,logger){
               message, sMangaIssues ->
               val chunkedIssues= sMangaIssues.chunked(60)
               val indices = chunkedIssues.indices
               if (indices.count()>1){
                  logger.info { "big list chunked! crunk crunk" }
                   logger.info { "indices ${indices.joinToString(",")}" }
                   // initial message
                   bot.sendMessage(message.chat.id,"*Comic Issues*\n\n${chunkedIssues[index.value].formatToString()}",
                   parseMode = "Markdown",
                   replyTo = message.message_id,
                   markup = issuesButtonMarkup)


               // handle call backs [START]
                   // next button callback
                   bot.onCallbackQuery("next_cb"){cb->
                       bot.answerCallbackQuery(cb.id)
                       val chatId = cb.message?.chat?.id ?: return@onCallbackQuery
                       index.value = if (index.value<indices.last) index.value+1 else indices.last
                       logger.info {  "index value current [NEXT]: ${index.value}"}
                       val editedIssuesText = chunkedIssues[index.value].formatToString()
                       bot.editMessageText(chatId,messageId = cb.message?.message_id,
                           inlineMessageId = cb.inline_message_id, text = "*Issues*\n\n$editedIssuesText", parseMode = "Markdown",
                           markup = issuesButtonMarkup)
                   }
                   // previous button callback
                   bot.onCallbackQuery("prev_cb"){cb->
                       bot.answerCallbackQuery(cb.id)
                       val chatId = cb.message?.chat?.id ?: return@onCallbackQuery
                       index.value = if (index.value>indices.first) index.value-1 else indices.first
                       logger.info {  "index value current [PREV]: ${index.value}"}
                       val editedIssuesText = chunkedIssues[index.value].formatToString()
                       bot.editMessageText(chatId,messageId = cb.message?.message_id,
                           inlineMessageId = cb.inline_message_id, text = "*Issues*\n\n$editedIssuesText", parseMode = "Markdown",
                           markup = issuesButtonMarkup)
                   }

               // handle call backs [END]

               }else{
                   logger.info { "issues list is not chunked bleh" }
                   val text="*Comic Issues*\n\n${chunkedIssues[0].formatToString()}"
                   bot.sendMessage(message.chat.id,
                   text,
                   parseMode = "Markdown",
                   replyTo = message.message_id)
               }
           }
        }
    }
    private fun Bot.handleClickedIssues(list: List<Pair<String,String>>){
        list.forEach {
            onCommand("/vd_${it.second}"){
                message, _ ->
                val fullUrl ="https://viewcomics.me/${it.first}"
                sendMessage(message.chat.id,fullUrl)
            }
        }
    }

    private inline fun Bot.handleClickedComics(pair:Pair<String,String>,logger:KLogger,crossinline block:(message:Message,List<SMangaIssue>)->Unit){
         onCommand("/v_${pair.second}"){message, _ ->
             val fullUrl ="https://viewcomics.me/comic/${pair.first}"
             comicDetails(fullUrl)
                 .onCompletion { cause ->
                     cause?.let {
                         logger.error { "${LocalDateTime.now()} an error occurred while fetching comic details: ${cause.message}" }
                         val errorMessage = parseThrowableAndGiveErrorMessage(cause)
                         sendMessage(message.chat.id,errorMessage)
                     }
                 }.collect{
                     val details=it.constructComicDetails()
                  //    handleClickedIssues(issueCommandsList)
                     val caption= "$details \n [photo](${it.comicImagePosterLink})"
                     val issues= it.issues.reversed()
                     val issueCommandsList = issues.map {issue-> issue.issueLink.returnIssueLink() }

                     handleClickedIssues(issueCommandsList)
                     logger.info { "${LocalDateTime.now()}: issue commands list ${issueCommandsList.joinToString(",")}" }

                     try {
                      val result = sendMessage(message.chat.id,caption, parseMode = "Markdown").get()
                         block.invoke(result,issues)
                     }catch (ex:TelegramApiError){
                         logger.error { "error while handling clicked comics ${ex.cause}" }
                     }
                 }
         }
     }
    /******************************
     * Main methods END
     *****************************/


}