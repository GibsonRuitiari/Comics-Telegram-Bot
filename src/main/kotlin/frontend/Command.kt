package frontend

import backend.comicDetails
import backend.comicPages
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
import org.apache.commons.io.FileUtils
import utils.*
import java.net.SocketException
import java.net.UnknownHostException
import java.net.http.HttpTimeoutException
import java.time.LocalDateTime
import kotlin.io.path.exists
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
    get() = listOf(InlineKeyboardButton("⇠ Prev",
    callback_data = "prev_cb"),
    InlineKeyboardButton("Next ⇢", callback_data = "next_cb"))

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
       // it is SocketException || it is HttpTimeoutException || it is UnknownHostException
        is HttpTimeoutException, is UnknownHostException, is SocketException->"\uD83E\uDD39 our elves have notified us that there is a problem with your internet connection.\nPlease check your internet connection and try again :XD."
        else->{
            "\uD83D\uDC40 shucks an error occurred on our side,please sit tight and wait for the problem to be rectified by our elves.\nPlease try again later :XD"
        }
    }
    fun String.formatComicPageLinkToName():String{
        val u = replace("https://viewcomics.me/","")
        val firstSegment=u.split("/").first().replace("-","")
        val secondSegment=u.split("/")[1].replace("issue-","")
        return "${firstSegment}_${secondSegment}"
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
                       val editedIssuesText = chunkedIssues[index.value].formatToString()
                       bot.editMessageText(chatId,messageId = cb.message?.message_id,
                           inlineMessageId = cb.inline_message_id, text = "*Issues*\n\n$editedIssuesText", parseMode = "Markdown",
                           markup = issuesButtonMarkup)
                   }

               // handle call backs [END]

               }else{
                   val text="*Comic Issues*\n\n${chunkedIssues[0].formatToString()}"
                   bot.sendMessage(message.chat.id,
                   text,
                   parseMode = "Markdown",
                   replyTo = message.message_id)
               }
           }
        }
    }

    private fun Bot.handleClickedIssues(list: List<Pair<String,String>>,logger: KLogger){
        list.forEach {
            onCommand("/vd_${it.second}"){
                message, _ ->
                val url = "https://viewcomics.me/${it.first}"
                val comicName =url.formatComicPageLinkToName()
                val fullUrl ="$url/full"
                try {
                    val sentMessage=sendMessage(message.chat.id,"working please wait a moment :XD").await()
                    comicPages(fullUrl)
                        .onCompletion { cause->
                            cause?.let {
                                val errorMsg=parseThrowableAndGiveErrorMessage(it)
                                logger.error { "${LocalDateTime.now()}: errorMessage--> $errorMsg" }
                                sendMessage(sentMessage.chat.id,errorMsg, parseMode = "Markdown").await()
                            }
                        }
                        .collect{pages->
                            val baseDir = createBaseTmpDir()
                            val comicZip=createTempZipFile(baseDir,"${comicName}.cbz")
                            val saveDir = createTempImgSaveDir(baseDir,comicName)
                            try {
                                saveComicImagesToZip(pages.pages, zipFile = comicZip,saveDir)
                                sendMessage(message.chat.id,"uploading file to server :XD")
                                val downloadUrl=uploadComicToRemoteServer(comicZip.file.toPath(),logger){
                                    future {
                                       loopy@ while (true){
                                            if (it ==100) {
                                                editMessageText(message.chat.id, message.message_id,null,"done :XD").await()
                                                break@loopy
                                            }
                                        }
                                    }
                                }
                                sendMessage(message.chat.id,"Here is the download link ${System.lineSeparator()}$downloadUrl ${System.lineSeparator()}It expires in 3 days from now. :XD").await()
                                comicZip.file.delete()
                                // delete the dir
                                // note using Files.deleteDir or similar method will throw directory not empty exception
                                if (saveDir.exists()) FileUtils.forceDelete(saveDir.toFile())
                            }catch (ex:Exception){
                                val errMsg = parseThrowableAndGiveErrorMessage(ex)
                                logger.error { "${LocalDateTime.now()}: errorMessage--> $errMsg" }
                                sendMessage(message.chat.id,errMsg)
                            }

                        }
                }catch (ex:Exception){
                   val errorMessage= parseThrowableAndGiveErrorMessage(ex)
                    logger.error { "${LocalDateTime.now()}: errorMessage--> $errorMessage" }
                    sendMessage(message.chat.id,errorMessage)
                }

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
                     val caption= "$details \n [photo](${it.comicImagePosterLink})"
                     val issues= it.issues.reversed()
                     val issueCommandsList = issues.map {issue-> issue.issueLink.returnIssueLink() }

                     handleClickedIssues(issueCommandsList,logger)
                     logger.info { "${LocalDateTime.now()}: issue commands list ${issueCommandsList.joinToString(",")}" }

                     try {
                      val result = sendMessage(message.chat.id,caption, parseMode = "Markdown").await()
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