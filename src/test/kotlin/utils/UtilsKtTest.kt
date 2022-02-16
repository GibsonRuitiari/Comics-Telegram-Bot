package utils

import kotlinx.coroutines.runBlocking
import mu.KLogger
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.*

internal class UtilsKtTest {
    private var logger:KLogger?=null
    private var testResourcesPath:Path? = null
    private var zipFile:File?=null
    private var tmpBaseDir:Path?=null
    private val expectedZipFileName ="test.zip"
    @BeforeEach
    fun setUp(){
        logger = KotlinLogging.logger {  }
        testResourcesPath= Path(System.getProperty("user.dir"),"test","resources")
        tmpBaseDir= createTempDirectory("tmpDir")
        zipFile = createTempZipFile(tmpBaseDir!!,expectedZipFileName).file
    }
    @AfterEach
    fun tearDown(){
        logger = null
        testResourcesPath = null
        try {
            tmpBaseDir?.deleteIfExists()
            zipFile?.delete()
        }catch (ex:Exception){
            // ignore the error since it is related with deletion
            logger?.error { ex }
        }



    }
    @Test
     fun uploadComicToRemoteServer():Unit = runBlocking {
        testResourcesPath?.let {
           val testFile=it.resolve("test.txt")
            assertTrue { testFile.exists()}
            try {
                val url= uploadComicToRemoteServer(testFile,logger!!){
                    logger!!.info { "progress: $it" }
                }
                if (url!=null){
                  assertTrue { url.contains("test.txt") }
                    val sanitizedUrl =url.removePrefix("https://transfer.sh/")
                    val expectedSanitizedUrlEndsInFileName ="test.txt"
                    assertTrue {
                        sanitizedUrl.contains(expectedSanitizedUrlEndsInFileName)
                    }
                  val gottenContentLength=URL("https://transfer.sh/get/$sanitizedUrl").openConnection().contentLength
                    assertTrue { gottenContentLength>0 }
                }

            }catch (ex:IOException){
                // some io/http error encountered while uploading
                logger!!.error { ex }
            }


        }

    }

    @Test
    fun createBaseTmpDir() {
       val expectedDirName ="testTmpDir"
        val createdPath= createBaseTmpDir(expectedDirName)
        kotlin.test.assertTrue {
            createdPath.name.contains(expectedDirName)
        }
    }

    @Test
    fun createTempZipFile() {
      zipFile?.let {
          val actualZipFileName = it.name
          assertEquals(expectedZipFileName,actualZipFileName)
      }
    }


}