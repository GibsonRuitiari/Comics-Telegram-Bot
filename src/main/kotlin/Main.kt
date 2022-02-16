import frontend.BotController

fun main()  {

     val botController = BotController()
     botController.onCreate()
     botController.initializeCommands()

//    comicPages("https://viewcomics.me/maniac-of-new-york-the-bronx-is-burning/issue-3/full")
//        .onCompletion { cause->
//            cause?.let {
//                if (it is SocketException || it is HttpTimeoutException || it is UnknownHostException){
//                    println("check your internet connection")
//                }else{
//                    println(it.message)
//                }
//            }
//        }.collect {
//            val tmpBaseDir = Path(System.getProperty("user.dir"),"comics_tmp")
//
//        val zip = net.lingala.zip4j.ZipFile(tmpBaseDir.resolve("maniac-of-newyork-the-bronx-is-burning-issue3.cbz").toFile())
//        val logger = KotlinLogging.logger { }
//        if (tmpBaseDir.notExists()){
//            tmpBaseDir.createDirectory()
//        }
//        val saveDir = createTempDirectory(tmpBaseDir,"maniac-new-york-the-bronx-is-burning")
//        withContext(Dispatchers.IO){
//            it.pages.forEach {
//                val path=URL(it.pageThumbnail).openStream().use { is_->
//                    val pic=saveDir.resolve("${it.pageDetail}.png")
//                    pic.outputStream().use { os-> is_.copyTo(os) }
//                    return@use pic
//                }
//                zip.addFile(path.toFile())
//            }
//        }
//            // wrap in a catch
//            val url=uploadComicToRemoteServer(zip.file.toPath(),logger){
//                println("progress: $it% \r")
//            }
//            println("url:  $url")
//            zip.file.delete()
//            if (saveDir.exists()){
//                try {
//                    FileUtils.forceDelete(saveDir.toFile())
//                }catch (ex:IOException){
//                    // swallow the error
//                }
//
//            }
//
//    }
    }


