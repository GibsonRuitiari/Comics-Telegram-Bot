package frontend

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message


sealed class CommandState
object NotIssued:CommandState()
object Issued:CommandState()
object Processed:CommandState()

sealed class Event:Command
data class OnStartCommand(override val commandName: String,
                          override val commandDescription: String,
                          override val commandHelpMessage: String):Event()
data class OnStopCommand(
    override val commandName: String,
    override val commandDescription: String,
    override val commandHelpMessage: String
):Event()

class StateMachine(private val bot: Bot){
     val initialState:CommandState = NotIssued // used for testing purposes
    private var _currentState = initialState
    val currentState = _currentState
    private var botInstance:Bot? = null
    @Volatile
    private var isRunning:Boolean=false
  //  val isFinalState:(CommandState)->Boolean={it ==initialState} // final state must be the command is processed
    // initializes the bot and changes the command state
   private fun onCreate(){
        if (botInstance==null) botInstance = bot
        botInstance!!.start()
        isRunning=true
    }
    private inline fun deltaFunction(input: Event,crossinline action:Bot.(Message,String?)->Unit){
        when(input){
            is OnStartCommand ->{
                _currentState = Issued
                onCreate()
                // since the bot is running we perform an action that is, the bot needs to send a message (greet the user :XD)
                botInstance!!.onCommand("/${input.commandName}"){ msg,opts-> action(botInstance!!,msg,opts)}
              _currentState= Processed
            }
            is OnStopCommand->{
                _currentState= Issued
                // issue a bye bye message :XD
                if (botInstance==null){
                   _currentState= Processed
                    return
                }
                botInstance!!.onCommand("/${input.commandName}"){msg,opts-> botInstance!!.action(msg,opts)}
                onDestroy()
                Processed
            }
        }
    }
   private fun onDestroy(){
        if (botInstance!=null) {
            botInstance?.stop()
            botInstance=null
        }
        isRunning=false
    }
    val onCommand:(input:Event,action:(Bot,Message,String?)->Unit)->Unit = {input, action -> deltaFunction(input, action) }



}





