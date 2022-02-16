package frontend.commands

import backend.*
import backend.comic_models.Genres
import backend.comic_models.MangaPage
import com.elbekD.bot.Bot
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import frontend.Command
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import mu.KLogger
import mu.KotlinLogging
import java.time.LocalDateTime

interface PaginatedCommands:Command {
    val nextCallbackQueryData:String
    val prevCallbackQueryData:String
    val commandType:CommandType
    var clickedComicsObservable:List<Pair<String,String>>
    // to be implemented by genre command
    val genres:Genres?
    val logger:KLogger
    get() = KotlinLogging.logger {  }

    /**
     First row, so it will be something like this
     <button> <button>
     */
    val inlineKeyboardRows: List<InlineKeyboardButton>
        get() = listOf(InlineKeyboardButton("<< prev", callback_data = prevCallbackQueryData),
            InlineKeyboardButton("next >>", callback_data = nextCallbackQueryData))
    val inlineKeyboardMarkup:InlineKeyboardMarkup
    get() = InlineKeyboardMarkup(listOf(inlineKeyboardRows))
    /*
    * End of mark up buttons
    */

    /***
     * Start of Handle call back queries
     */
    fun Bot.onPaginationButtonsClicked(pageNumber:MutableStateFlow<Int>){
        // next button
        onCallbackQuery(nextCallbackQueryData){
            answerCallbackQuery(it.id)
            val chatId = it.message?.chat?.id ?: return@onCallbackQuery
            pageNumber.value +=1
            updateComicsListOnPaginationButtonClicked(chatId,
                it.message?.message_id,it.inline_message_id,pageNumber.value)
        }
        //prev button
        onCallbackQuery(prevCallbackQueryData){
            answerCallbackQuery(it.id)
            val chatId = it.message?.chat?.id ?: return@onCallbackQuery
            pageNumber.value = if (pageNumber.value>1) pageNumber.value-1 else 1
            updateComicsListOnPaginationButtonClicked(chatId,
                it.message?.message_id,it.inline_message_id,
            pageNumber.value)
        }

    }
    // shows the initial list i.e. page =1
    suspend fun Bot.showComicsList(pageNumber: Int,chatId:Long){
        val contentMessage = fetchAndParseComicsBasedOnType(pageNumber){
            clickedComicsObservable = it
        }
        if (contentMessage.second.isNotEmpty() or contentMessage.second.isNotBlank()){
            sendMessage(chatId,contentMessage.second)
        }else {
            val header = returnComicsListHeaderBasedOnCommandType()
            val message="$header \n\n${contentMessage.first}"
            sendMessage(chatId,message, parseMode = "Markdown", markup = inlineKeyboardMarkup)
        }
    }

    // updates the list shown to the user i.e. when page > 1
    // this method will be called only when the user clicks the paginated buttons
    private suspend fun Bot.updateComicsListOnPaginationButtonClicked(chatId:Long,
    messageId:Int?,inlineMessageId:String?,pageNumber:Int){
           val contentMessage = fetchAndParseComicsBasedOnType(pageNumber = pageNumber){
               clickedComicsObservable = it
           }
        if (contentMessage.second.isNotEmpty() or contentMessage.second.isNotBlank()){
            sendMessage(chatId,contentMessage.second)
        }else {
            val header = returnComicsListHeaderBasedOnCommandType()
            val message="$header \n\n${contentMessage.first}"
            editMessageText(chatId, messageId = messageId, inlineMessageId =inlineMessageId,
                message, parseMode = "Markdown", markup = inlineKeyboardMarkup
            )
        }
    }


    private fun returnComicsListHeaderBasedOnCommandType():String{
        return when(commandType){
            is CommandType.Completed->"*Showing Completed Comics*"
            is CommandType.Ongoing ->"*Showing On going Comics*"
            is CommandType.Popular->"*Showing Popular Comics*"
            is CommandType.Latest->"*Showing Latest Comics*"
            is CommandType.ByGenre->"*Showing Comics By Genre*"
        }
    }

    private suspend inline fun fetchAndParseComicsBasedOnType(pageNumber: Int,crossinline action:(List<Pair<String,String>>)->Unit):Pair<String,String>{
        var contentMessage = ""
        var errorMessage=""
        when(commandType){
            is CommandType.Completed->{
                val(msgToBeSent,errMsg)=parseFlow(pageNumber, block1 = completedComics, block2 = action)
                contentMessage = msgToBeSent
                errorMessage=errMsg
            }
            is CommandType.Ongoing->{
                val(msgToBeSent,errMsg)=parseFlow(pageNumber, block1 = ongoingComics, block2 = action)
                contentMessage = msgToBeSent
                errorMessage=errMsg

            }
            is CommandType.Popular->{
                val(msgToBeSent,errMsg)=parseFlow(pageNumber, block1 = popularComics, block2 = action)
                contentMessage = msgToBeSent
                errorMessage=errMsg
            }
            is CommandType.Latest->{
                val(msgToBeSent,errMsg)=parseFlow(pageNumber, block1 = latestComics, block2 = action)
                contentMessage = msgToBeSent
                errorMessage=errMsg
            }
            is CommandType.ByGenre->{
               require(genres!=null){
                    "Genre cannot be null" }
               val genre= genres!!
                val(msgToBeSent,errMsg)=parseComicByGenreFlow(pageNumber,genre,action)
                contentMessage = msgToBeSent
                errorMessage=errMsg
            }
        }
      return  contentMessage to errorMessage
    }
    private suspend inline fun parseComicByGenreFlow(pageNumber: Int,genres: Genres,crossinline block1:(List<Pair<String,String>>)->Unit):Pair<String,String>{
        var contentMessage = ""
        var errorMessage=""
        comicsByGenre(pageNumber,genres).onCompletion {
                cause->
            cause?.let {
                logger.error { "${LocalDateTime.now()}: The following error occurred while fetching paginated comics: ${cause.message}" }
                errorMessage= parseThrowableAndGiveErrorMessage(cause)
            }

        }.collect { mangaPage ->
            val mangas = mangaPage.mangas
            contentMessage = parseMangasAndReturnTheContentMessage(mangas)
            val commandsPairList=mangas.map { it.comicLink.constructComicCommandsFromComicLinks() }
            block1(commandsPairList)
            logger.info {  "${LocalDateTime.now()}: content message is $contentMessage"  }
        }

        return contentMessage to errorMessage

    }
    private suspend inline fun parseFlow(pageNumber: Int, crossinline block1:(number:Int)->Flow<MangaPage>,
                                         crossinline block2:(List<Pair<String,String>>)->Unit):Pair<String,String>{
        var contentMessage = ""
        var errorMessage=""
        block1(pageNumber).onCompletion {
            cause->
            cause?.let {
                logger.error { "${LocalDateTime.now()}: The following error occurred while fetching paginated comics: ${cause.message}" }
                 errorMessage= parseThrowableAndGiveErrorMessage(cause)
            }

        }.collect { mangaPage ->
            val mangas = mangaPage.mangas
            contentMessage = parseMangasAndReturnTheContentMessage(mangas)
            val commandsPairList=mangas.map { it.comicLink.constructComicCommandsFromComicLinks() }
            block2(commandsPairList)
            logger.info {  "${LocalDateTime.now()}: content message is $contentMessage"  }
        }
       return contentMessage to errorMessage
    }


}