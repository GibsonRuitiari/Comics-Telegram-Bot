import backend.latestComics
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FunctionalTest {

    @Test fun testLatestComicsFunction() = runBlocking {
        val latestComics = latestComics(1).first().mangas
        val comicName=latestComics.first().comicName
        assertThat(comicName).isNotEmpty()
    }


}