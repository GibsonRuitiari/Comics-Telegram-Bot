package frontend.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.http.TelegramApiError
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import frontend.Command
import frontend.Result

object MainMenuCommand: Command {
    override val commandName: String
        get() = "main_menu"
    override val commandDescription: String
        get() = "This command displays the main menu"
    override val commandHelpMessage: String
        get() = "type /main_menu to display main menu"

    override suspend fun onMessageDoAction(bot: Bot, message: Message, options: String?): Result {
       return with(bot){
            // latest completed on going by genres  popular
            // 2 columns 3 rows
            val firstRow = listOf(InlineKeyboardButton("latest comics", callback_data = latestCallbackData),
            InlineKeyboardButton("ongoing comics", callback_data = onGoingCallbackData))
            val secondRow = listOf(InlineKeyboardButton("completed comics", callback_data = completedCallbackData),
            InlineKeyboardButton("popular comics", callback_data = popularCallbackData))
            val thirdRow = listOf(InlineKeyboardButton("genres", callback_data = genresCallbackData))
            val mainMenuReplyMarkup = InlineKeyboardMarkup(listOf(firstRow,secondRow,thirdRow))
           return@with try {
                sendMessage(message.chat.id, text = "Please pick an option from the choices below",
                    markup = mainMenuReplyMarkup, disableNotification = true, parseMode = "Markdown")
                Result(true,null)
            }catch (ex:TelegramApiError){
                Result(false, ex.message)
            }
        }
    }
    private const val latestCallbackData ="latest_cb"
    private const val onGoingCallbackData ="ongoing_cb"
    private const val genresCallbackData = "genre_cb"
    private const val popularCallbackData = "popular_cb"
    private const val completedCallbackData="completed_cb"


}