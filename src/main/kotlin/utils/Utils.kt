package utils

import backend.comic_models.SMangaPage
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogger
import net.lingala.zip4j.ZipFile
import java.net.URL
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.*
import kotlin.io.path.*

private const val serverUrl = "https://transfer.sh/"
private const val contentType="application/cbz"

suspend  fun uploadComicToRemoteServer(currentPath: Path, logger:KLogger, progressBlock:(Int)->Unit):String? = withContext(Dispatchers.IO){
    val file = FileDataPart.from(currentPath.absolutePathString(), filename = "test_comic",
        contentType = contentType)
    var progressToReport = 0
    val (_,_, result) = Fuel.upload(serverUrl)
        .add { file}
        .progress { readBytes, totalBytes ->
            val percentageDownloaded = readBytes *100.0 / totalBytes
            if (percentageDownloaded -  progressToReport >=5){
                progressToReport = percentageDownloaded.toInt()
                logger.info { "progress: $progressToReport" }
                progressBlock.invoke(progressToReport)
            }
        }.responseString()

    val downloadUrl:String?=when(result){
        is com.github.kittinunf.result.Result.Failure->{
            logger.error { "${LocalDateTime.now()}: the following error occurred while uploading file: ${result.getException()}" }
            null
        }
        is com.github.kittinunf.result.Result.Success->{
            val anteriorUrl=result.value.removePrefix(serverUrl)
            val downloadUrl="https://transfer.sh/get/$anteriorUrl"
            logger.info { "${LocalDateTime.now()} downloaded successfully ${System.lineSeparator()} This is the url: $downloadUrl" }
            println(downloadUrl)
            downloadUrl
        }
    }
    return@withContext downloadUrl
}

 fun createBaseTmpDir():Path{
    val tmpBaseDir = Path(System.getProperty("user.dir"),"comics_tmp")
    if (tmpBaseDir.notExists()) tmpBaseDir.createDirectory()
    return tmpBaseDir
}
 fun createTempZipFile(baseDir: Path, zipFileName: String): ZipFile {
    return ZipFile(baseDir.resolve(zipFileName).toFile())
}
 fun createTempImgSaveDir(baseDir: Path,dirName:String):Path{
    return createTempDirectory(baseDir,dirName)
}
 suspend fun saveComicImagesToZip(pages:List<SMangaPage>,
                                         zipFile: ZipFile, saveDir:Path){
    withContext(Dispatchers.IO){
        pages.forEach {
            URL(it.pageThumbnail).openStream().use {
                    is_->
                val picture = saveDir.resolve("${it.pageDetail}.png")
                picture.outputStream().use { os->
                    is_.copyTo(os)
                }
                zipFile.addFile(picture.toFile())
            }
        }
    }
}
suspend fun <T> CompletableFuture<T>.await(): T = suspendCoroutine {
    whenComplete { res, ex -> if (ex == null) it.resume(res) else it.resumeWithException(ex) }
}


fun <T> future(context: CoroutineContext = Dispatchers.IO, block: suspend () -> T): CompletableFuture<T> =
    CompletableFutureCoroutine<T>(context).also { block.startCoroutine(completion = it) }

class CompletableFutureCoroutine<T>(override val context: CoroutineContext) : CompletableFuture<T>(),
    Continuation<T> {
    override fun resumeWith(result: Result<T>) {
        if (result.isSuccess) {
            complete(result.getOrThrow())
        } else {
            completeExceptionally(result.exceptionOrNull())
        }
    }
}