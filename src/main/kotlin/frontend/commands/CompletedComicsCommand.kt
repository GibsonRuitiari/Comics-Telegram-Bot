package frontend.commands

import backend.comic_models.Genres
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import frontend.Result
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import kotlin.properties.Delegates

object CompletedComicsCommand:PaginatedCommands {
    override val genres: Genres?
        get() = null
    override val index: MutableStateFlow<Int> = MutableStateFlow(0)
    override val commandName: String
        get() = "completed_comics"
    override val commandDescription: String
        get() = "Shows completed comics"
    override val commandHelpMessage: String
        get() = "type /$commandName to get a list of completed comics"
    private val pageNumber = MutableStateFlow(1)
    private var botInstance:Bot?=null

    override val nextCallbackQueryData: String
        get() = "completed_comics_next_cb"
    override val prevCallbackQueryData: String
        get() = "completed_comics_prev_cb"

    override val commandType: CommandType
        get() = CommandType.Completed

    // keep track of clicked comics
    override var clickedComicsObservable: List<Pair<String, String>> by Delegates.observable(emptyList()) { _, _, newValue ->
        logger.info { "${LocalDateTime.now()} new value: $newValue " }
        if (botInstance !=null){
            onComicDetailsClicked(newValue,botInstance!!, logger)
        }
    }

    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
        botInstance = bot
        with(bot){
            showComicsList(pageNumber.value,message.chat.id)
            onPaginationButtonsClicked(pageNumber)
        }
        return Result(true,null)
    }

}