package frontend.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import frontend.Result
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import kotlin.properties.Delegates

object PopularComicsCommand:PaginatedCommands {
    private val pageNumber = MutableStateFlow(1)
    private var botInstance:Bot?=null
    override val nextCallbackQueryData: String
        get() = "popular_comics_next_cb"
    override val prevCallbackQueryData: String
        get() = "popular_comics_prev_cb"
    override val commandType: CommandType
        get() = Popular
    override var clickedComicsObservable: List<Pair<String, String>> by Delegates.observable(emptyList()){
            _, _, newValue ->
        logger.info { "${LocalDateTime.now()} new value: $newValue " }
        if (botInstance !=null){
            onComicDetailsClicked(newValue,
                botInstance!!, logger
            )
        }
    }
    override val commandName: String
        get() = "popular_comics"
    override val commandDescription: String
        get() = "showcases a list of the current popular comics"
    override val commandHelpMessage: String
        get() = "type /$commandName to get a list of current popular comics"

    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
        botInstance = bot
        with(bot){
            showComicsList(pageNumber.value,message.chat.id)
            onPaginationButtonsClicked(pageNumber)
        }
        return Result(true,null)
    }

}