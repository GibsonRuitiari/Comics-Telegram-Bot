package frontend

import backend.comicDetails
import backend.comic_models.SManga
import backend.comic_models.SMangaInfo
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import kotlinx.coroutines.flow.onCompletion
import mu.KLogger
import java.net.UnknownHostException
import java.net.http.HttpTimeoutException
import java.time.LocalDateTime
import kotlin.math.roundToInt


// our inputs or events
interface Command{
    val commandName:String
    val commandDescription:String
    val commandHelpMessage:String
   suspend fun onMessageDoAction(bot: Bot, message: Message, options:String?):Result
   // default helper methods

    /**
     * Unless needed, kindly refrain from not override this method
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
    fun SMangaInfo.constructComicDetailsMessageContentFromMangaInfo(): Pair<String, List<Pair<String, String>>> {
        val genres = genres.joinToString(separator = ",")
        val comicName = "\uD83D\uDCDA *${comicAlternateName}*"
        val comicAuthor = "_by: ${comicAuthor}_"
        val comicGenre = "*Genre:* $genres"
        val comicRatings = "*Views:* \uD83C\uDF1F ${comicViews.roundToInt()} \uD83C\uDF1F"
        val comicYearReleased = "*Year:* ${yearOfRelease}"
        val comicStatus = "*Status:* \uD83D\uDCCC ${comicStatus} \uD83D\uDCCC"
        val comicDescription = comicDescription.cleanAndEscapeReservedCharacters()
        val issues_=issues.joinToString(separator = "\n"){
            val issueCommand = "[/vd_${it.issueLink.returnIssueLink().second}](http://details_url)"
            "\uD83D\uDD17 ${it.issueName}  |  ${it.issueReleaseDate}\n*view details:*$issueCommand\n\n"
        }
        val issueLinks = issues.map { it.issueLink.returnIssueLink() }
        val comicDetailsMessage = "$comicName \n$comicAuthor\n\n$comicGenre\n\n$comicRatings\n\n$comicStatus\n\n$comicYearReleased\n\n$comicDescription\n\n$issues_"
       return comicDetailsMessage to issueLinks
    }

    /**
     * Given a list containing a commands eg /v_th (the-hotell)
     * we need to observe this list such that when the user clicks we react on the command
     */
    fun onComicDetailsClicked(commandsList:List<Pair<String,String>>,bot: Bot,
    logger: KLogger){
        println("commands list: ${commandsList.joinToString(",")}")
        commandsList.forEach {
           bot.handleClickedComics(it,logger)
        }
    }
    private fun Bot.handleClickedIssues(list: List<Pair<String,String>>){
        println("handle clicked issues:: ${list.joinToString(",")}")
        list.forEach {
            onCommand("/vd_${it.second}"){
                message, _ ->
                val fullUrl ="https://viewcomics.me/${it.first}"
                sendMessage(message.chat.id,fullUrl)
            }
        }
    }

    private fun Bot.handleClickedComics(pair:Pair<String,String>,logger:KLogger){
         onCommand("/v_${pair.second}"){message, _ ->
             val fullUrl ="https://viewcomics.me/comic/${pair.first}"
             logger.info { "${LocalDateTime.now()} $fullUrl" }
             comicDetails(fullUrl)
                 .onCompletion { cause ->
                     cause?.let {
                         logger.error { "${LocalDateTime.now()} an error occurred while fetching comic details: ${cause.message}" }
                         val errorMessage = parseThrowableAndGiveErrorMessage(cause)
                         sendMessage(message.chat.id,errorMessage)
                     }
                 }.collect{
                     val (details,issueCommandsList)=it.constructComicDetailsMessageContentFromMangaInfo()
                      handleClickedIssues(issueCommandsList)
                     val caption= "$details \n [photo](${it.comicImagePosterLink})"
                     logger.info { "${LocalDateTime.now()}: issue commands list ${issueCommandsList.joinToString(",")}" }
                     sendMessage(message.chat.id,caption, parseMode = "Markdown")
                 }
         }
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

    /**
     * The comic name is supposed to be stripped from the original comic url eg
     * :https://viewcomics.me/comic/the-hotell the name here is the-hotell
     * so given the comic name stripped from url come up with a string that can be used as a command
     */
    private fun String.parseComicNameAndReturnComicLink():String{
       return replace("-","_").split("_").joinToString(separator = "") { it.take(1) }
    }
     fun parseThrowableAndGiveErrorMessage(cause:Throwable):String = when(cause){
        is HttpTimeoutException, is UnknownHostException->"\uD83E\uDD39 our elves have notified us that there is a problem with your internet connection.\nPlease check your internet connection and try again :XD."
        else->{
            "\uD83D\uDC40 shucks an error occurred on our side,please sit tight and wait for the problem to be rectified by our elves.\nPlease try again later :XD"
        }
    }

}