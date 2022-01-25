package backend.comic_models

enum class Genres (val genreName:String) {
   MARVEL("marvel"),
    DC_COMICS("dc-comics"),
    ACTION("action"),
    ADVENTURE("adventure"),
    ANTHOLOGY("anthology"),
    ANTHROPOMORPHIC("anthropomorphic"),
    BIOGRAPHY("biography"),
    CHILDREN("children"),
    COMEDY("comedy"),
    CYBORGS("cyborgs"),
    DARK_HORSE("dark-hose"),
    DEMONS("demons"),
    DRAMA("drama"),
    FANTASY("fantasy"),
    FAMILY("family"),
    FIGHTING("fighting"),
    GORE("gore"),
    GRAPHIC_NOVELS("graphic-novels"),
    HISTORICAL("historical"),
    LEADING_LADIES("leading-ladies"),
    LITERATURE("literature"),
    MAGIC("magic"),
    MANGA("manga"),
    MARTIAL_ARTS("martial-arts"),
    MECHA("mecha"),
    MATURE("mature"),
    MILITARY("military"),
    MOVIE_CINEMATIC("movie-cinematic"),
    MYSTERY("mystery"),
    MYTHOLOGY("mythology"),
    PERSONAL("personal"),
    POLITICAL("political"),
    PSYCHOLOGICAL("psychological"),
    POST_APOCALYPTIC("post-apocalyptic"),
    PULP("pulp"),
    SCI_FI("sci-fi"),
    ROMANCE("romance"),
    ROBOTS("robots"),
    SPY("spy"),
    SPORTS("sports"),
    SUPERHERO("superhero"),
    SUPERNATURAL("supernatural"),
    SUSPENSE("suspense"),
    SCIENCE_FICTION("science-fiction"),
    SLICE_OF_LIFE("slice-of-life"),
    THRILLER("thriller"),
    TRAGEDY("tragedy"),
    VAMPIRES("vampires"),
    VERTIGO("vertigo"),
    VIDEO_GAMES("video-games"),
    WAR("war"),
    WESTERN("western"),
    ZOMBIES("zombies");
 companion object{
      private val values by lazy{ values() }
     fun fromGenreValue(value:String)= values.firstOrNull { it.genreName==value }
 }


}