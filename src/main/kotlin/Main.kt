import frontend.BotController

fun main() {
    val botController = BotController()
    botController.onCreate()
    botController.initializeCommands()
}