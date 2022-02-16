package frontend

import com.elbekD.bot.Bot
import frontend.commands.OnGoingComicsCommand
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StateMachineTest {
    private var testBot:Bot?=null
    private var stateMachine:StateMachine?=null
    @BeforeEach
    fun setUp() {
         val botToken = "1814039661:AAEX69rqHYpFfkL6ZE7kG7RX49GQr50iTO4"
         val username = "@DummyB_Bot"
        testBot= Bot.createPolling(username,botToken){
                allowedUpdates= listOf() }
        stateMachine = StateMachine(bot = testBot!!)
    }

    @AfterEach
    fun tearDown() {
        if (testBot!=null) testBot=null
    }

    @Test
    fun onEvent() {
        val expectedState = MutableStateFlow(State.BlankState)
        assertEquals(expectedState,stateMachine?.currentState) // initial state
        stateMachine?.onEvent(OnGoingComicsCommand)
        val expectedStateAfterProcessing = MutableStateFlow(State.CommandProcessed)
        assertEquals(expectedStateAfterProcessing.value, stateMachine?.currentState?.value)
    }
}