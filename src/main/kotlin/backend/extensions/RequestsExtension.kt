package backend.extensions

import okhttp3.*
import java.util.concurrent.TimeUnit


private val DEFAULT_CACHE_CONTROL = CacheControl.Builder().maxAge(10, TimeUnit.MINUTES).build()
private val DEFAULT_HEADERS = Headers.Builder().build()
private val DEFAULT_BODY: RequestBody = FormBody.Builder().build()
internal fun GET(url:String, headers:Headers= DEFAULT_HEADERS,
        cacheControl: CacheControl= DEFAULT_CACHE_CONTROL
): Request {
    return Request.Builder()
        .url(url)
        .headers(headers)
        .cacheControl(cacheControl)
        .build()
}

internal fun POST(url:String, headers: Headers= DEFAULT_HEADERS, body:RequestBody= DEFAULT_BODY,
         cacheControl: CacheControl= DEFAULT_CACHE_CONTROL
):Request{
    return Request.Builder()
        .url(url)
        .post(body)
        .headers(headers)
        .cacheControl(cacheControl)
        .build()
}