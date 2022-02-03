package frontend.commands

import backend.search
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import frontend.Command
import frontend.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import mu.KotlinLogging
import java.time.LocalDateTime
import kotlin.properties.Delegates

object SearchCommand:Command {
    private val logger = KotlinLogging.logger {  }
    override val commandName: String
        get() = "search_comics"
    override val commandDescription: String
        get() = "Enables you to search for a given comic using its name eg /$commandName hotell"
    override val commandHelpMessage: String
        get() = "type /$commandName comic name to search for a comic eg /$commandName hotell"
    private var botInstance:Bot?=null
    private var clickedComicsObservable:List<Pair<String,String>> by Delegates.observable(emptyList()){
        _, _, newValue ->
        logger.info { "${LocalDateTime.now()} new value: $newValue " }
        if (botInstance!=null){
            onComicDetailsClicked(newValue, botInstance!!, logger)
        }
    }
    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
        botInstance = bot
        return if (options.isNullOrEmpty()){
            bot.sendMessage(message.chat.id,"\uD83D\uDE35 Search parameter cannot be blank!Please try again")
            Result(true,null)
        }else{
            logger.info { "search params: $options" }
            search(options)
                .onCompletion {
                    cause->
                    cause?.let {
                        logger.error { "${LocalDateTime.now()} the following error occurred while searching for comics: $cause" }
                     bot.sendMessage(message.chat.id,parseThrowableAndGiveErrorMessage(cause))
                    }
                }.collect{
                    if (it.isNotEmpty()){
                        logger.info { "these are the results: ${it.joinToString { it.comicName }}" }
                        val msg=parseMangasAndReturnTheContentMessage(it)
                        val commandsPairList = it.map { it.comicLink.constructComicCommandsFromComicLinks()}
                        clickedComicsObservable = commandsPairList
                        bot.sendMessage(message.chat.id,msg,parseMode = "Markdown")
                    }else bot.sendMessage(message.chat.id,"shucks our elves did not find any comic with such a name. please counter check the name and try again")

            }
            Result(true,null)
        }
    }
    override val index: MutableStateFlow<Int> = MutableStateFlow(0)

}