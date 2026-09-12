package com.example.metadata.provider

import java.io.IOException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import kotlin.coroutines.resumeWithException

class MetadataHttpException(val status: Int, val retryAfterSeconds: Long? = null) : IOException(
    if (status == 429 || status == 503) "Provider busy or rate limited (HTTP $status). Try again later." else "Provider returned HTTP $status"
)

suspend fun OkHttpClient.awaitResponse(request: Request): Response = suspendCancellableCoroutine { continuation ->
    val call = newCall(request)
    continuation.invokeOnCancellation { call.cancel() }
    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) { if (!continuation.isCancelled) continuation.resumeWithException(e) }
        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response) { _, resource, _ -> resource.close() }
        }
    })
}

object MusicBrainzRateLimit {
    private val mutex = Mutex()
    private var lastRequest = 0L
    suspend fun awaitTurn() = mutex.withLock {
        val elapsed = (System.nanoTime() - lastRequest) / 1_000_000
        if (elapsed < 1100) delay(1100 - elapsed)
        lastRequest = System.nanoTime()
    }
}

object ItunesRateLimit {
    private val mutex = Mutex()
    private var lastRequest = 0L
    suspend fun awaitTurn() = mutex.withLock {
        val elapsed = (System.nanoTime() - lastRequest) / 1_000_000
        if (elapsed < 3100) delay(3100 - elapsed)
        lastRequest = System.nanoTime()
    }
}
