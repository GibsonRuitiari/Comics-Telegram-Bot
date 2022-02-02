package frontend.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import frontend.Result
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import kotlin.properties.Delegates

object LatestComicsCommand:PaginatedCommands {
    private val pageNumber = MutableStateFlow(1)
    private var botInstance:Bot?=null
    override val nextCallbackQueryData: String
        get() = "latest_comics_next_cb"
    override val prevCallbackQueryData: String
        get() = "latest_comics_prev_cb"
    override val commandType: CommandType
        get() = Latest
    override var clickedComicsObservable: List<Pair<String, String>> by Delegates.observable(emptyList<Pair<String,String>>()){
            _, _, newValue ->
        logger.info { "${LocalDateTime.now()} new value: $newValue " }
        if (botInstance !=null){
            onComicDetailsClicked(newValue,
                botInstance!!, logger
            )
        }
    }
    override val commandName: String
        get() = "latest_comics"
    override val commandDescription: String
        get() = "show cases the latest comics"
    override val commandHelpMessage: String
        get() = "type /$commandName to get a list of the latest comics"

    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
        botInstance = bot
        with(bot){
            showComicsList(pageNumber.value,message.chat.id)
            onPaginationButtonsClicked(pageNumber)
        }
       return Result(true,null)
    }
}