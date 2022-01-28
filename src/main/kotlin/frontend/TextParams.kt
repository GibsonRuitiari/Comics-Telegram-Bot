package frontend

import com.elbekD.bot.types.ReplyKeyboard

/**
 * Params needed to send a text message
 */
interface TextParams {
    val text:String
    val parseMode:String?
    val disableNotification:Boolean?
    val markup: ReplyKeyboard?
    val replyTo:Long?
}