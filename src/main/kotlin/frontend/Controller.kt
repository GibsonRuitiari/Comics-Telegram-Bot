package frontend

import com.elbekD.bot.Bot

/**
 * A proxy that sits between the state machine
 * and the Telegram bot Api
 * It contains needed to carry out the commands issued by the user
 */
interface Controller{
     val bot: Bot
 }