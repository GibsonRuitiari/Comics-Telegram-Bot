package frontend.commands

import backend.comicDetails
import backend.comic_models.SManga
import backend.ongoingComics
import com.elbekD.bot.Bot
import com.elbekD.bot.feature.chain.chain
import com.elbekD.bot.http.TelegramApiError
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.elbekD.bot.util.Action
import frontend.Command
import frontend.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.io.IOException
import java.time.LocalDateTime

object OnGoingComicsCommand:Command {
    private  val logger = KotlinLogging.logger {  }
    private const val prefix="https://viewcomics.me/comic/"
    private const val dummyViewUri="http://view_url"
    override val commandName: String
        get() = "on_going_comics"
    override val commandDescription: String
        get() = "Delivers/Shows the current on going comics"
    override val commandHelpMessage: String
        get() = "type /on_going_comics to get a list of the current on going comics"
    private var page=1

    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result = coroutineScope {
       // fetch data asynchronously
        val comics=async(Dispatchers.IO) {
            var pair=Pair<List<SManga>,String?>(emptyList(),null);ongoingComics(page).catch {
            logger.error { "${LocalDateTime.now()} error fetching manga ${it.message}" }

            val errMsg =if (it is IOException) networkErrorMsg else unknownErrorMsg
            pair= Pair(emptyList(),errMsg)}
            .collect{ pair= Pair(it.mangas,null)}
            return@async pair}
         val (mangas, errorMsg)=comics.await()
            if (errorMsg!=null){
                // failed so return here
                return@coroutineScope Result(false, errorMsg)
            }
        logger.info { "[*] ${LocalDateTime.now()}: Successfully fetched comics ${System.lineSeparator()} comic size: ${mangas.size}" }
        // we did not fail
       return@coroutineScope try {
            with(bot){
                val comicListString=mangas.joinToString(separator = "\n"){
                    val modifiedUrl = it.comicLink.returnModifiedUrl()// returns a name that can be embedded back to the url to form a complete url
                    "★ *${it.comicName}* ${System.lineSeparator()} view: [/vw$modifiedUrl](http://view_url) ${System.lineSeparator()}"
                }


                // 1 row 3 columns
                // prev-back-next
                val firstRow = listOf(InlineKeyboardButton("<< prev", callback_data = previousOngoingComicsCb),
                    InlineKeyboardButton("next >>", callback_data = nextOngoingComicsCb))
                val onGoingComicsMarkup = InlineKeyboardMarkup(listOf(firstRow))
                sendMessage(message.chat.id,
                    markup =onGoingComicsMarkup,
                    text = comicListString.replace("]","\\]").replace(")","\\)").replace("-","\\-").replace("(","\\(").replace(".","\\.").replace("_","\\_")+
                            System.lineSeparator()
                ).get()

            //   this  onComicClicked mangas.map { it.comicLink.returnModifiedUrl() }
            }
            Result(true,null)
        }catch (ex:TelegramApiError){
            logger.error { "${LocalDateTime.now()} error:${ex.message}" }
            Result(false,ex.message)
        }

    }
    private fun Bot.handleCallbacks(callbackData:List<String>){
        callbackData.forEach {
            cb->
            onCallbackQuery(cb){
                query->
                answerCallbackQuery(query.id,"cb answered")
                val chatId= query.message?.chat?.id ?: return@onCallbackQuery
                // send a
                when {
                    cb.contentEquals(nextOngoingComicsCb) -> {
                    }
                    cb.contentEquals(previousOngoingComicsCb) -> {
                    }

                }

            }
        }

    }
    private infix fun Bot.onComicClicked(commands: List<String>){
        commands.forEach {
            // each command ==  /vw_$modifiedUrl
            onCommand(it){msg,_->
                sendChatAction(msg.chat.id,Action.Typing)
                val comicName = it.removePrefix("/vw_") // can be embedded back to the original url
                val fullUrl =  comicName embedNameToUrl prefix // embed the name back to the url

                comicDetails(fullUrl).catch { cause ->
                    logger.error { "${LocalDateTime.now()}: ${cause.message} error while fetching comic details for $comicName" }
                    if (cause is IOException) {
                        // a http exception so tell the user to retry
                        sendMessage(
                            msg.chat.id, networkErrorMsg)
                    }
                }.collect { info ->
                    logger.info { "${LocalDateTime.now()} succeeded fetching comic details for $comicName" }
                    val genres = info.genres.joinToString(separator = " ") { genre -> "#$genre" }
                    val comicInfoMsg = """
                        Showing comic details for ${info.comicAlternateName} ${System.lineSeparator()}
                        Description: ${info.comicDescription} ${System.lineSeparator()}
                        Author: ${info.comicAuthor}${System.lineSeparator()}
                        $genres ${System.lineSeparator()}
                        Status: ${info.comicStatus} ${System.lineSeparator()}
                        Views:  ${info.comicViews} ${System.lineSeparator()}
                        Released: ${info.yearOfRelease} ${System.lineSeparator()}
                        *Issues* ${System.lineSeparator()}
                        ${info.issues.joinToString { issue -> "⦁ $issue \n"}
                    }
                    """.trimIndent()
                    logger.info { "${LocalDateTime.now()} comic details \n $comicInfoMsg" }
                    sendMessage(msg.chat.id, comicInfoMsg, parseMode = "MarkdownV2")
                }
            }
        }
    }
    // returns a name that can be embedded back to the url to form a complete url also it escapes some characters
    private fun String.returnModifiedUrl():String{
      return  this.removePrefix(prefix)
         }
    private fun String.escapeReservedCharacters():String{
        return replace("(", "\\(")
            .replace(")","\\)")
            .replace("-", "\\-")
            .replace("[","\\[")
            .replace("]","\\]")
            .replace("`","\\`")
            .replace("\\","")
    }
    private infix fun String.embedNameToUrl(prefix:String):String{
        return "${prefix}${this}"
    }

    private const val previousOngoingComicsCb ="previous_cb"
    private const val nextOngoingComicsCb ="next_cb"
    private const val unknownErrorMsg ="Unknown error occurred please try again later"
    private val networkErrorMsg ="Yikes!\uD83E\uDD39 Our elves have notified us that your network is unstable/bad. ${System.lineSeparator()}Please try the request again"

}