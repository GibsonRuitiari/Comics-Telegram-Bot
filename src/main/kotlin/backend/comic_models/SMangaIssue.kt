package backend.comic_models

interface SMangaIssue {
    var issueName:String
    var issueLink:String
    var issueReleaseDate:String // in form of string
    companion object{
        fun create():SMangaIssue{
            return SMangaIssueImpl()
        }
    }

}