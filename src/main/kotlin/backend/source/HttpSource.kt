package backend.source


import backend.comic_models.Genres
import backend.comic_models.MangaPage
import backend.comic_models.SMangaChapter
import backend.comic_models.SMangaInfo
import backend.constants.baseLink
import backend.constants.genreBaseLink
import backend.extensions.GET
import backend.extensions.awaitBody
import backend.network.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

internal abstract class HttpSource: Source {
    open val client:OkHttpClient // client can provide their own implementation of okhttp client if need be
    get() = NetworkHelper.client
    private val headers:Headers by lazy { headersBuilder().build()}
    protected open fun headersBuilder()=Headers.Builder()
        .add("User-Agent", DEFAULT_USER_AGENT)
    companion object {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63"
    }
    // modify it if necessary
    open val baseUrl:String by lazy { baseLink }

    /**
     * Returns manga details given the manga url
     * Use this when for instance the user has selected a certain manga from the list
     * @param absoluteUrl url of the selected  manga
     * @return SMangaInfo flow
     */
    override fun fetchMangaDetails(absoluteUrl: String): Flow<SMangaInfo> = flow{
        val response = client.newCall(mangaDetailsRequest(absoluteUrl)).awaitBody()
       emit(mangaDetailsParse(response))
    }.flowOn(Dispatchers.IO)

    /**
     * Returns the request for the details of a manga. Override only if it's needed to change the
     * url, send different headers or request method like POST.
     *
     * @param absoluteUrl url of the selected manga.
     */
    open fun mangaDetailsRequest(absoluteUrl: String): Request {
        return GET(absoluteUrl, headers)
    }
    /**
     * Parses the response from the site and returns the details of a manga.
     * @param response the response from the site.
     */
    protected abstract fun mangaDetailsParse(response: Response): SMangaInfo




    /** Returns a flow of manga page given the category and the page
    * Use this if you want to fetch mangas based on different categories eg marvel,dc etc
    * @param page the page number
    * @param category the category to be retrieved
    */
    override fun fetchComicsByGenre(page: Int, category: Genres): Flow<MangaPage> = flow {
        val response= client.newCall(mangaComicsByCategoryRequest(page, category)).awaitBody()
        emit(parseOnGoingComicsAndBasedGenreParse(response))
    }.flowOn(Dispatchers.IO)

    override fun fetchCompletedComics(page: Int): Flow<MangaPage> = flow {
        val response = client.newCall(mangaCompletedComicsRequest(page)).awaitBody()
        emit(parseCompletedComicsParse(response))
    }.flowOn(Dispatchers.IO)

    override fun fetchLatestComics(page: Int): Flow<MangaPage> = flow{
        val response = client.newCall(mangaLatestComicsRequest(page)).awaitBody()
        emit(parsePopularAndNewComicsParse(response))
    }.flowOn(Dispatchers.IO)

    override fun fetchPopularComics(page: Int): Flow<MangaPage> = flow{
        val response = client.newCall(mangaPopularComicsRequest(page)).awaitBody()
        emit(parsePopularAndNewComicsParse(response))
    }.flowOn(Dispatchers.IO)

    override fun fetchOnGoingComics(page: Int): Flow<MangaPage> = flow{
        val response = client.newCall(mangaOnGoingComicsRequest(page)).awaitBody()
        emit(parseOnGoingComicsAndBasedGenreParse(response))
    }.flowOn(Dispatchers.IO)

    override fun fetchComicPages(absoluteIssueLink: String): Flow<SMangaChapter> = flow{
        val response= client.newCall(mangaIssuesComicsRequest(absoluteIssueLink)).awaitBody()
        emit(parseMangaIssue(response))
    }.flowOn(Dispatchers.IO)


    /**
     * Returns the request for the manga comics encapsulated in a manga page
     * Override this method is required only for instance setting different headers or if there is a category
     * not included in the default ones
     */
    open  fun mangaComicsByCategoryRequest(page: Int, genre: Genres):Request{
        val url="${genreBaseLink}${genre.genreName}?page=$page"
        return GET(url, headers)
    }
    open fun mangaLatestComicsRequest(page: Int):Request{
        return GET("$baseUrl/new-comics?page=$page", headers)
    }
    open fun mangaPopularComicsRequest(page:Int):Request{
        return GET("$baseUrl/popular-comics?page=$page",headers)
    }
    open fun mangaOnGoingComicsRequest(page:Int):Request{
        return GET("$baseUrl/ongoing-comics?page=$page", headers)
    }
    open fun mangaCompletedComicsRequest(page: Int):Request{
        return GET("$baseUrl/advanced-search?status=CMP&page=$page", headers)
    }
    open fun mangaIssuesComicsRequest(issueLink:String):Request{
        return GET(issueLink,headers)
    }

    protected abstract fun parsePopularAndNewComicsParse(response: Response):MangaPage
    protected abstract fun parseCompletedComicsParse(response: Response):MangaPage
    protected abstract fun parseOnGoingComicsAndBasedGenreParse(response: Response):MangaPage
    protected abstract fun parseMangaIssue(response: Response):SMangaChapter
}