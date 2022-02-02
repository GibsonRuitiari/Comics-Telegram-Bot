package frontend.commands

sealed class CommandType
object Ongoing:CommandType()
object Completed:CommandType()
object Popular:CommandType()
object Latest:CommandType()