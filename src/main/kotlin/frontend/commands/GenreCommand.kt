package frontend.commands

import backend.comic_models.Genres
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import frontend.Result
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import kotlin.properties.Delegates

object GenreCommand:PaginatedCommands {
    override val nextCallbackQueryData: String
        get() = "by_genre_next_cb"
    override val prevCallbackQueryData: String
        get() = "by_genre_prev_cb"
    override val commandType: CommandType
        get() = CommandType.ByGenre
    private val pageNumber = MutableStateFlow(1)
    private val currentGenre= MutableStateFlow(Genres.ACTION)
    private var botInstance:Bot?=null
    override var clickedComicsObservable: List<Pair<String, String>> by Delegates.observable(emptyList()) { _, _, newValue ->
        logger.info { "${LocalDateTime.now()} new value: $newValue " }
        if (botInstance !=null){
            onComicDetailsClicked(newValue, botInstance!!, logger)
        }
    }

    override val genres: Genres
        get() = currentGenre.value
    override val commandName: String
        get() = "by_genre"
    override val commandDescription: String
        get() = "shows a list of comics belonging to a certain genre"
    override val commandHelpMessage: String
        get() ="type /$commandName <genre> to get a list of comics based on a certain genre eg /$commandName horror or /$commandName sci-fi"

    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
         botInstance = bot
         if (!options.isNullOrEmpty()){
             val args=options.split(' ').map { it.trim().lowercase() }.filter { it.isNotBlank() }
             if (args.size>1){
                 bot.sendMessage(message.chat.id,"You cannot provide two genres at once. Please only input one genre eg Horror")

             }else{
                 val genre = Genres.values().first { it.genreName.contentEquals(args[0],ignoreCase = true) }
                 currentGenre.value=genre
                 logger.info { "chosen genre ${currentGenre.value.name}  ${genre.name}" }
                 bot.showComicsList(pageNumber.value,message.chat.id)
                 bot.onPaginationButtonsClicked(pageNumber)
             }

         }else bot.sendMessage(message.chat.id,"Kindly follow the command with a genre")
        return Result(true,null)
    }

    override val index: MutableStateFlow<Int> = MutableStateFlow(0)

}