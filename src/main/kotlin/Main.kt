import backend.comicPages
import backend.extensions.GET
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import utils.uploadComicToRemoteServer
import java.io.IOException
import java.net.SocketException
import java.net.URL
import java.net.UnknownHostException
import java.net.http.HttpTimeoutException
import java.util.concurrent.CompletableFuture
import java.util.zip.ZipFile
import kotlin.coroutines.*
import kotlin.io.path.*
import kotlin.system.measureTimeMillis

 fun main() = runBlocking {
     val url="https://viewcomics.me/maniac-of-new-york-the-bronx-is-burning/issue-3" // /full
     val u=url.replace("https://viewcomics.me/","")

     // [maniac-of-new-york-the-bronx-is-burning, issue-3]
     println("${u.split("/").first().replace("-", " ")} ${u.split("/")[1].replace("issue-","")}")


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


