package backend

import backend.comic_models.Genres
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FunctionalTest {

    @Test fun testLatestComicsFunction(): Unit = runBlocking {
            val latestComics = latestComics(1)
                .onCompletion { cause->
                    cause?.let {
                        if (it is NoSuchElementException){
                            assertTrue {
                                it.message!!.contains("no such element")
                            }
                        }else throw it
                    }
                }
                .first().mangas
            assertThat(latestComics.isNotEmpty())

    }
    @Test fun testComicsWithGenreFunction() = runBlocking {
         comicsByGenre(1,Genres.MARVEL)
            .onCompletion { cause->
                cause?.let {
                    throw it
                }
            }
            .collect{
                assertEquals(40, it.mangas.count())
            }
    }

    @Test fun searchFunctionTest() = runBlocking {
        search("hotell")
            .onCompletion { cause->
                cause?.let {
                    throw it
                }
            }
            .collect{
                assertEquals(2,it.count())
            }
    }


    @Test fun testComicPagesFunction() = runBlocking {
        comicPages("https://viewcomics.me/maniac-of-new-york-the-bronx-is-burning/issue-3/full")
            .onCompletion { cause->
                cause?.let {
                    throw it
                }
            }
            .collect{
                // todo: check the real number of pages
                assertEquals(32,it.pages.size)
            }
    }

    @Test fun testGenreEmoji(){
        val actualMarvelEmoji=Genres.valueOf("marvel").emoji
        val expectedMarvelEmoji ="\uD83E\uDD2F"
       assertEquals(actualMarvelEmoji,expectedMarvelEmoji)
    }



}