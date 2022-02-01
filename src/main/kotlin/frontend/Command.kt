package frontend

import backend.comic_models.SManga
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message


// our inputs or events
interface Command{
    val commandName:String
    val commandDescription:String
    val commandHelpMessage:String
   suspend fun onMessageDoAction(bot: Bot, message: Message, options:String?):Result
   fun String.cleanAndEscapeReservedCharacters():String{
     return  replace("-","\\-")
           .replace(".","\\.")
           .replace(".","\\.")
           .replace("[","\\[")
           .replace("]","\\]")
           .replace("(","\\(")
           .replace(")","\\)")
           .replace("\\"," ")
   }
    fun parseMangasAndReturnTheContentMessage(mangas: List<SManga>): String {
        val modifiedList = mangas.map {
            val sanitizedComicName = it.comicName.cleanAndEscapeReservedCharacters()
            val comicCommandFromComicLink = it.comicLink.constructComicCommandsFromComicLinks().second
            sanitizedComicName to comicCommandFromComicLink
        }
        return modifiedList.joinToString(separator = "\n") { "⚝*${it.first}* \n view details: [/v_${it.second}](http://start_url)\n" }
    }

    /**
     * Given a string that represents a link return a pair containing the command link and the original name
     * for example:https://viewcomics.me/comic/the-hotell
     * the original name would be --> the-hotel
     * the command link would be th
     */
   fun String.constructComicCommandsFromComicLinks():Pair<String,String>{
       // the original name which we are going to embed in the url
      val originalName= replace("https://viewcomics.me/comic/","")
       // the command link
     return originalName to originalName.parseComicNameAndReturnComicLink()
   }

    /**
     * The comic name is supposed to be stripped from the original comic url eg
     * :https://viewcomics.me/comic/the-hotell the name here is the-hotell
     * so given the comic name stripped from url come up with a string that can be used as a command
     * so th
     */
    private fun String.parseComicNameAndReturnComicLink():String{
       return replace("-","_").split("_").joinToString(separator = "") { it.take(1) }
    }
}