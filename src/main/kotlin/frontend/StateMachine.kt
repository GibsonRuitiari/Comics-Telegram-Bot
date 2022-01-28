package frontend

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import mu.KotlinLogging
import java.time.LocalDateTime


sealed class CommandState
object NotIssued:CommandState()
data class Issued(val commandName:String):CommandState()
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
// output
data class ActionResult(val succeeded:Boolean, val message:String?) // error message which is optional
private infix fun CommandState.log(result: ActionResult){
    val logger = KotlinLogging.logger {  }
    val state=if (this is Issued) this else null
    val (succeeded,error)= result
    if (!succeeded) logger.error { "[*] ${LocalDateTime.now()}: Command ${state?.commandName} failed.${System.lineSeparator()} Reason: $error" }
    else logger.info { "[*] ${LocalDateTime.now()}: Command ${state?.commandName} succeeded."}

}

class StateMachine(private val bot: Bot){
     private val initialState:CommandState = NotIssued // used for testing purposes
    private var _currentState:CommandState = initialState
    private val logger by lazy { KotlinLogging.logger{} }
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
        logger.info { "[*] ${LocalDateTime.now()} bot started" }
    }
    private inline fun deltaFunction(input: Event,crossinline action:Bot.(Message,String?)->ActionResult){
        when(input){
            is OnStartCommand ->{
                _currentState = Issued(input.commandName)
                onCreate()
                // since the bot is running we perform an action that is, the bot needs to send a message (greet the user :XD)
                botInstance!!.onCommand("/${input.commandName}"){ msg,opts->
                    val result= botInstance!!.action(msg,opts)
                    _currentState  log result
                }
                // transition to processed state which is the final state
              _currentState= Processed
            }
            is OnStopCommand->{
                _currentState= Issued(input.commandName)
                // if there was no bot instance maybe client issued this command by mistake so just return
                // Albeit, this would be a very weird situation nonetheless cater for it
                if (botInstance==null){
                   _currentState= Processed
                    isRunning = false
                    return
                }
                botInstance!!.onCommand("/${input.commandName}"){msg,opts->
                    // send a bye bye message :XD
                    val result =botInstance!!.action(msg,opts)
                    _currentState   log result
                }
                // clean up everything
                onDestroy()
                _currentState=Processed
            }
        }
    }
   private fun onDestroy(){
        if (botInstance!=null) {
            botInstance?.stop()
            botInstance=null
        }
        isRunning=false
        logger.info { "[*] ${LocalDateTime.now()} bot stopped and has stopped running" }
    }
    val onEvent:(input:Event,action:(Bot,Message,String?)->ActionResult)->Unit = {input, action -> deltaFunction(input, action) }

}





