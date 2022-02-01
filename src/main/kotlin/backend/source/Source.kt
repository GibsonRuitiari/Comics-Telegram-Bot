package backend.source

import backend.comic_models.*
import kotlinx.coroutines.flow.Flow

internal interface Source {
    /**
     * Returns a flow of the latest/new comics encapsulated in a MangaPage
     */
    fun fetchLatestComics(page: Int): Flow<MangaPage>
    /**
     * Returns a flow of the popular comics encapsulated in a MangaPage
     */
    fun fetchPopularComics(page: Int):Flow<MangaPage>
    /**
     * Return a flow of comics based on categories encapsulated in a MangaPage
     * @param category the manga category to retrieve
     * @param page the page number to retrieve
     */
    fun fetchComicsByGenre(page: Int,category: Genres):Flow<MangaPage>

    /**
     * Returns a flow of the on going comics encapsulated in a MangaPage
     */
    fun fetchOnGoingComics(page: Int):Flow<MangaPage>
    /**
     * Returns a flow of the completed comics encapsulated in a MangaPage
     */
    fun fetchCompletedComics(page:Int):Flow<MangaPage>

    fun searchForComic(term:String):Flow<List<SManga>>
    /**
     * Returns a flow of the manga chapter encapsulated in a MangaPage
     * a manga chapter consists of list of pages and the total number of
     * pages present in this chapter
     * @param absoluteIssueLink:String -> the link of the chosen issue
     */
    fun fetchComicPages(absoluteIssueLink:String):Flow<SMangaChapter>

    /**
     * Return a flow of [SMangaInfo]
     * @param absoluteUrl the manga clicked/chosen
     */
    fun fetchMangaDetails(absoluteUrl: String):Flow<SMangaInfo>




}

