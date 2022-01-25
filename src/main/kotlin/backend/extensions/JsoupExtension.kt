package backend.extensions

import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

internal fun Response.asJsoup():Document{
    return Jsoup.parse(body!!.string(), request.url.toString())
}