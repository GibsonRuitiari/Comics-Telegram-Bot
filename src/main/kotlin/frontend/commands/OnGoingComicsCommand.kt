package frontend.commands

import backend.comic_models.SManga
import backend.ongoingComics
import com.elbekD.bot.Bot
import com.elbekD.bot.http.TelegramApiError
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import frontend.Command
import frontend.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch

object OnGoingComicsCommand:Command {
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
            var pair=Pair<List<SManga>,String?>(emptyList(),null);ongoingComics(page).catch {pair= Pair(emptyList(),it.message) }.collect{ pair= Pair(it.mangas,null)}
            return@async pair}
         val (mangas, errorMsg)=comics.await()
            if (errorMsg!=null){
                // failed so return here
                return@coroutineScope Result(false, errorMsg)
            }
        println("mangas: ${mangas.size}")
        // we did not fail
       return@coroutineScope try {
            with(bot){
                // 1 row 3 columns
                // prev-back-next
                val firstRow = listOf(InlineKeyboardButton("previous", callback_data = previousOngoingComicsCb),InlineKeyboardButton("back", callback_data = backCb), InlineKeyboardButton("next", callback_data = nextOngoingComicsCb))
                val onGoingComicsMarkup = InlineKeyboardMarkup(listOf(firstRow))
                sendMessage(message.chat.id,
                    markup =onGoingComicsMarkup,
                    text = "Click next to view the next page ${System.lineSeparator()} previous to view the previous page ${System.lineSeparator()} back to return to the main menu")
            }
            Result(true,null)
        }catch (ex:TelegramApiError){
            Result(false,ex.message)
        }
    }
    private const val previousOngoingComicsCb ="previous_cb"
    private const val nextOngoingComicsCb ="next_cb"
    private const val backCb="back_cb"


}