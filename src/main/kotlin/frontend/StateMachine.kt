package frontend

import com.elbekD.bot.Bot
import kotlinx.coroutines.flow.MutableStateFlow
import mu.KotlinLogging
import java.time.LocalDateTime

class StateMachine constructor(private val bot: Bot){
    private var initialState = MutableStateFlow<State>(State.BlankState) // initial state
    private var _currentState = initialState
    private val logger = KotlinLogging.logger {  }
    // delta function
    fun onEvent(command: Command){
        _currentState.value = State.CommandIssued
        bot.onCommand("/${command.commandName}"){msg,opts->
            _currentState.value= State.CommandProcessing
            val (succeeded,failureMsg)=command.onMessageDoAction(bot,msg,opts)
            _currentState.value = State.CommandProcessed
            // log the result
            if (succeeded){
                logger.info { "${LocalDateTime.now()}: command ${command.commandName} executed successfully" }
            }else logger.error { "${LocalDateTime.now()}:  command ${command.commandName} failed to execute due to $failureMsg" }
        }
    }


}