import com.elbekD.bot.Bot
import frontend.BotController


fun main() {
     val x=BotController()
    x.initializeCommands()
    x.onCreate()






//    val streamArray = arrayOf(URL(imgAdress).file,URL(imgAdress).file,URL(imgAdress).file)
//    var out=ZipOutputStream(BufferedOutputStream(FileOutputStream("second.zip")))
//    var data = ByteArray(1024)
//    for (file in streamArray){
//
//        val fi = FileInputStream(file)
//        var origin = BufferedInputStream(fi)
//        var entry = ZipEntry(file.substring(file.lastIndexOf("/")))
//        out.putNextEntry(entry)
//        origin.copyTo(out,1024)
//        origin.close()
//    }
//    out.close()

}


