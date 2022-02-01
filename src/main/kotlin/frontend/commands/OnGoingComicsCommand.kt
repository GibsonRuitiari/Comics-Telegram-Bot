package frontend.commands

import backend.ongoingComics
import com.elbekD.bot.Bot
import com.elbekD.bot.http.TelegramApiError
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import frontend.Command
import frontend.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import mu.KotlinLogging
import java.time.LocalDateTime
import kotlin.properties.Delegates


object OnGoingComicsCommand: Command {
    private  val logger = KotlinLogging.logger {  }
    private const val prefix="https://viewcomics.me/comic/"
    private const val dummyViewUri="http://view_url"
    override val commandName: String
        get() = "on_going_comics"
    override val commandDescription: String
        get() = "Delivers/Shows the current on going comics"
    override val commandHelpMessage: String
        get() = "type /on_going_comics to get a list of the current on going comics"
    private val pageNumber = MutableStateFlow(0)
    // keep track of clicked comics
    private var clickedComics by Delegates.observable(emptyList<Pair<String,String>>()){
            property, oldValue, newValue ->
    }
    /*
     First row, so it will be something like this
     <button> <button>
     */
    private const val prevCallbackQueryData ="prev_cb"
    private const val nextCallbackQueryData ="next_cb"
    private val firstRowOfInlineKeyBoards= listOf(InlineKeyboardButton("<< prev", callback_data = prevCallbackQueryData),
        InlineKeyboardButton("next >>", callback_data = nextCallbackQueryData))
    private val onGoingComicsKeyboardMarkup = InlineKeyboardMarkup(listOf(firstRowOfInlineKeyBoards))

    // start handle call back queries
    private fun Bot.onPaginationButtonsClicked(pageNumber:MutableStateFlow<Int>){
        //  next button
        onCallbackQuery(nextCallbackQueryData){
            answerCallbackQuery(it.id)
            pageNumber.value +=1
            val chatId= it.message?.chat?.id ?: return@onCallbackQuery
            updateOnGoingComicsOnPaginationButtonButtonClicked(chatId,
                it.message?.message_id,it.inline_message_id)
        }
        // previous button
        onCallbackQuery(prevCallbackQueryData) {
            answerCallbackQuery(it.id)
            val chatId = it.message?.chat?.id ?: return@onCallbackQuery
            pageNumber.value = if (pageNumber.value>1) pageNumber.value-1 else 1
           updateOnGoingComicsOnPaginationButtonButtonClicked(chatId,
           it.message?.message_id,it.inline_message_id)
        }
    }
    private suspend fun fetchAndParseOngoingComics(action:(List<Pair<String,String>>)->Unit):String{
        var contentMessage=""
         ongoingComics(pageNumber.value).onCompletion {
             cause ->
             logger.error { "${LocalDateTime.now()}: The following error occurred while fetching on going comics: ${cause?.message}" }
         }.collect{mangaPage->
             val mangas = mangaPage.mangas
              contentMessage=parseMangasAndReturnTheContentMessage(mangas)
              val commandsPairList=mangas.map { it.comicLink.constructComicCommandsFromComicLinks() }
             action(commandsPairList)
             logger.info { "${LocalDateTime.now()} commands pair list ${commandsPairList.joinToString(",")}" }
             logger.info { "${LocalDateTime.now()}: content message is $contentMessage" }

         }
        return contentMessage
    }
    private suspend fun Bot.updateOnGoingComicsOnPaginationButtonButtonClicked(chatId: Long,
                                                                               messageId:Int?,
                                                                               inlineMessageId:String?){
        val contentMessage = fetchAndParseOngoingComics {
            clickedComics=it }
        editMessageText(chatId, messageId = messageId, inlineMessageId =inlineMessageId,
        contentMessage, parseMode = "Markdown", markup = onGoingComicsKeyboardMarkup)
    }
    // send the on going comics
    private suspend fun Bot.showOnGoingComics(chatId: Long){
        val contentMessage= fetchAndParseOngoingComics{
            clickedComics = it
        }
        val message="*Showing On going Comics* \n\n$contentMessage"
        sendMessage(chatId,message, parseMode = "Markdown", markup = onGoingComicsKeyboardMarkup)
    }
    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
       try {
           bot.showOnGoingComics(message.chat.id)
           bot.onPaginationButtonsClicked(pageNumber)
       }catch (ex:TelegramApiError){
           logger.error { "${LocalDateTime.now()} error occurred while sending the message ${ex.cause}" }
       }
       return Result(true,"")
    }

  }