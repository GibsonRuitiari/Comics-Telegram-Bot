package frontend

import com.elbekD.bot.Bot
import com.elbekD.bot.http.TelegramApiError


class BotController(override val bot: Bot) :Controller{
    private val stateMachine by lazy { StateMachine(bot) }
    fun send(event: Event,textParams: TextParams){
        stateMachine.onEvent(event){_bot,message,_->
            try {
            _bot.sendMessage(message.chat.id,
                text = textParams.text,
                disableNotification = textParams.disableNotification,
                markup = textParams.markup,
                parseMode = textParams.parseMode,
                replyTo = textParams.replyTo)
            ActionResult(true,null)
        }catch (ex: TelegramApiError){
            ActionResult(false,ex.message)
        }
        }
    }
}
