package backend.comic_models

interface SMangaChapter {
    var pages:List<SMangaPage> // list of the images
    val totalPages:Int
    get() {
       return pages.size
    }
    companion object{
        fun create():SMangaChapter{
            return SMangaChapterImpl()
        }

    }

}
