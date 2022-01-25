package backend.comic_models

interface SMangaPage{
    var pageDetail:String // page number for instance eg 2 of 49
    var pageThumbnail:String
    companion object{
        fun create():SMangaPage{
            return SMangaPageImpl()
        }
    }
}