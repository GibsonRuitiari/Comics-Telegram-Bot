package frontend.commands

import backend.comic_models.Genres
import com.elbekD.bot.Bot
import com.elbekD.bot.http.TelegramApiError
import com.elbekD.bot.types.Message
import frontend.Result
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import kotlin.properties.Delegates


object OnGoingComicsCommand: PaginatedCommands {
    override val genres: Genres?
        get() = null
    override val index: MutableStateFlow<Int> = MutableStateFlow(0)
    override val nextCallbackQueryData: String
        get() ="ongoing_comics_next_cb"
    override val prevCallbackQueryData: String
        get() = "ongoing_comics_prev_cb"
    override val commandType: CommandType
        get() = CommandType.Ongoing
    override var clickedComicsObservable: List<Pair<String, String>> by Delegates.observable(emptyList<Pair<String,String>>()){
            _, _, newValue ->
        logger.info { "${LocalDateTime.now()} new value: $newValue " }
        if (botInstance!=null){
            onComicDetailsClicked(newValue,
                botInstance!!, logger)
        }
    }
    override val commandName: String
        get() = "on_going_comics"
    override val commandDescription: String
        get() = "Shows the current on going comics"
    override val commandHelpMessage: String
        get() = "type /$commandName to get a list of the current on going comics"
    private val pageNumber = MutableStateFlow(1)
    private var botInstance:Bot?=null
    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
        botInstance = bot
        try {
           with(bot){
               showComicsList(pageNumber.value,message.chat.id)
               onPaginationButtonsClicked(pageNumber)
           }
       }catch (ex:TelegramApiError){
           logger.error { "${LocalDateTime.now()} error occurred while sending the message ${ex.cause}" }
       }
       return Result(true,null)
    }

  }