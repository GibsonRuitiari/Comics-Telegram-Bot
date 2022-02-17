/*
 * Copyright (c) 2022. Ruitiari Gibson.
 * All rights reserved.
 */

package backend

import backend.comic_models.*
import backend.source.Comic
import kotlinx.coroutines.flow.Flow


/**
 * Lazily create a comic instance
 */
private object ComicInstance{
    operator fun invoke():Comic{
        val comic by lazy { Comic() }
        return comic
    }
}
/**
 * Provide manga pages according to their page numbers
 */
private object ComicsWithNumber{
    operator fun invoke(page:Int, block: Comic.(page:Int) -> Flow<MangaPage>):Flow<MangaPage>{
        val comic = ComicInstance() // lazy instance of comic
        return comic.block(page)
    }
}
/**
 * Fetch manga pages in accordance with their urls mostly used by comic details and comic pages function
 */
private object ComicWithUrl{
    operator fun<T> invoke(url:String, block:Comic.(String)->Flow<T>):Flow<T>{
        val comic = ComicInstance()
        return comic.block(url)
    }
}

/**
 * Apis to be used by client
 */
val comicsByGenre:(page:Int,category: Genres)->Flow<MangaPage> = { page:Int, category:Genres-> ComicInstance().fetchComicsByGenre(page,category)}
val latestComics:(page:Int)->Flow<MangaPage> = {it->ComicsWithNumber(it){fetchLatestComics(it)} }
val popularComics:(page:Int)->Flow<MangaPage> = {it-> ComicsWithNumber(it){fetchPopularComics(it)} }
val ongoingComics:(page:Int)->Flow<MangaPage> = {it->ComicsWithNumber(it){fetchOnGoingComics(it)} }
val completedComics:(page:Int)->Flow<MangaPage> ={it->ComicsWithNumber(it){fetchCompletedComics(it)} }
val comicDetails:(url:String)->Flow<SMangaInfo> = { it-> ComicWithUrl(it){fetchMangaDetails(it)} }
val comicPages:(url:String) -> Flow<SMangaChapter> = { it-> ComicWithUrl(it){fetchComicPages(it)}}
val search:(term:String)->Flow<List<SManga>> = {it-> ComicWithUrl(it){searchForComic(term = it)} }
