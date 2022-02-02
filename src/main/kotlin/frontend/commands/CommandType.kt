package frontend.commands

sealed class CommandType
object Ongoing:CommandType()
object Completed:CommandType()
