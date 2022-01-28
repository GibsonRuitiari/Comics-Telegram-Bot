package frontend

import com.elbekD.bot.Bot

class StateMachine constructor(private val bot:Bot){
    private var initialState:State = State.BlankState // initial state
    private var _currentState = initialState

    // delta function
   fun handleEvent(command: Command){
       _currentState = State.BlankState
       bot.onCommand("/${command.commandName}"){
           msg,opts->
           _currentState= State.CommandProcessing
           val (succeeded,failureMsg)= command.onMessageDoAction(bot,msg,opts)
           _currentState=if (!succeeded){
               println("command ${command.commandName} processing failed: ${System.lineSeparator()} Reason: $failureMsg")
               State.CommandProcessed
           } else{
               println("command  ${command.commandName} processing succeeded")
               State.CommandProcessed
           }
       }
   }

}