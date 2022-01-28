package frontend
//
//// desired states
//sealed class State
//object S0:State()
//object S1:State()
//
//// inputs
//sealed class Input
//object A:Input()
//
//
//// out Deterministic finite automaton
//data class DFA(val states:Set<State>,
//val inputs:Set<Input>,
//val delta:(State,Input)->State,
//val initialState: State,
//val isFinalState:(State)->Boolean)
//
//
//
//
//// our transformation function / delta-hat function
//fun deltaHatFunctionIterative(dfa:DFA, input:List<Input>):State{
//    var state = dfa.initialState
//    for (character in input){
//        state = dfa.delta(state, character)
//    }
//    return state
//}
//// functional style
//fun deltaHat(dfa:DFA, input: List<Input>):State = input.fold(dfa.initialState,dfa.delta)