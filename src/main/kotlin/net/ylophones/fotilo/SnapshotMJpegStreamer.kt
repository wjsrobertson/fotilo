package net.ylophones.fotilo

import org.apache.commons.io.IOUtils
import org.apache.commons.io.IOUtils.closeQuietly
import org.apache.http.HeaderElement
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Create an mjpeg stream from JPEG source
 *
 * Repeatedly retrieves a JPEG from `snapshotUrl` using `httpClient` and streams it to `out`
 */
class SnapshotMJpegStreamer(private val httpClient: CloseableHttpClient, val snapshotUrl: String, val out: OutputStream) {

    private val closed = AtomicBoolean(false)

    fun stop() {
        closed.set(true)
    }

    fun stream() {
        while (!closed.get()) {
            val response = httpClient.execute(HttpGet(snapshotUrl))
            if (response.statusLine.statusCode == HttpStatus.SC_OK) {

                val contentLength = response.getFirstHeader("Content-Length").elements[0].name

                out.write("--fotilo\n".toByteArray())
                out.write("Content-Type: image/jpeg\n".toByteArray())
                out.write("Content-Length: $contentLength\n".toByteArray())
                out.write("\n".toByteArray())
                IOUtils.copy(response.entity.content, out)
                out.write("\n".toByteArray())

                logger.info("Copied across an image")
            } else {
                logger.info("received ${response.statusLine.statusCode} when retrieving ${snapshotUrl}")
                break
            }

            Thread.sleep(100)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SnapshotMJpegStreamer::class.java)
    }
}