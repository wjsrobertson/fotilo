package net.ylophones.fotilo

import org.apache.commons.io.IOUtils
import org.apache.http.ConnectionClosedException
import org.apache.http.HttpStatus
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create an mjpeg stream from JPEG source
 *
 * Repeatedly retrieves a JPEG from `snapshotUrl` using `httpClient` and streams it through `inputStream`
 */
class SnapshotMJpegStreamer(private val httpClient: CloseableHttpClient, val snapshotUrl: String) {

    val inputStream: PipedInputStream = PipedInputStream(1024 * 1024)

    private val running = AtomicBoolean(false)

    private val thread = Thread {
        val outputStream = PipedOutputStream(inputStream)

        while (running.get()) {
            val response = httpClient.execute(HttpGet(snapshotUrl))

            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                try {
                    outputImage(response, outputStream)
                } catch (e: IOException) {
                    logger.info("image processing thread write failed ${e.message}")
                    if (!isPrematureEndOfContent(e)) {
                        break
                    }
                }
            } else {
                logger.info("received ${response.statusLine.statusCode} when retrieving ${snapshotUrl}")
                break
            }

            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    init {
        thread.name = "mjpeg snapshot retriever ${SnapshotMJpegStreamer.id.incrementAndGet()}"
    }

    private fun outputImage(response: CloseableHttpResponse, outputStream: OutputStream) {
        val contentLength = response.getFirstHeader("Content-Length").elements[0].name

        outputStream.write("--fotilo\n".toByteArray())
        outputStream.write("Content-Type: image/jpeg\n".toByteArray())
        outputStream.write("Content-Length: $contentLength\n".toByteArray())
        outputStream.write("\n".toByteArray())
        IOUtils.copy(response.entity.content, outputStream)
        outputStream.write("\n".toByteArray())

        logger.info("Copied across an image")
    }

    private fun isPrematureEndOfContent(exception: IOException) = when (exception) {
        is ConnectionClosedException -> exception.message?.contains("Premature end of Content-Length") ?: false
        else -> false
    }

    fun start() {
        running.set(true)
        thread.start()
    }

    fun stop() {
        running.set(false)
        thread.interrupt()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SnapshotMJpegStreamer::class.java)

        private val id = AtomicInteger(0)
    }
}