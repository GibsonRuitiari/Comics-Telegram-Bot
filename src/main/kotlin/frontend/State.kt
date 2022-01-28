package frontend

// our outputs
sealed class State{
    object BlankState:State() // no command issued
    object CommandIssued:State()
    object CommandProcessing:State()
    object CommandProcessed:State() // final state
}
