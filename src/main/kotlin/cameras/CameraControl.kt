package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.*
import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Path
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class ContentStream(val mimeType: String, val stream: InputStream)

interface SettingsParser {
    fun parse(page: InputStream): CameraSettings
}

interface CameraUrls {
    fun videoStreamUrl(): String

    fun settingsPageUrl(): String

    fun snapshotUrl(): String

    fun moveUrl(direction: Direction): String

    fun orientationUrl(orientation: Orientation): String = throw UnsupportedOperationException()

    fun flipUrl(rotation: Rotation): String = throw UnsupportedOperationException()

    fun stopUrl(lastDirection: Direction): String

    fun panTiltSpeedUrl(speed: Int): String

    fun brightnessUrl(brightness: Int): String

    fun contrastUrl(contrast: Int): String

    fun resolutionUrl(resolution: String): String

    fun storeLocationUrl(location: Int): String

    fun gotoLocationUrl(location: Int): String

    fun frameRateUrl(fps: Int): String

    fun infraRedLightUrl(on: Boolean): String
}

interface CameraControl {

    fun getVideoStream(): ContentStream

    fun getCameraSettings(): CameraSettings

    fun getCameraDefinition(): CameraDefinition

    fun getCameraOverview(): CameraOverview = CameraOverview(getCameraSettings(), getCameraDefinition())

    fun move(direction: Direction)

    fun move(direction: Direction, duration: Int)

    fun stopMovement()

    fun saveSnapshot(path: Path)

    fun setPanTiltSpeed(speed: Int)

    fun setBrightness(brightness: Int)

    fun setContrast(contrast: Int)

    fun setResolution(resolution: String)

    fun flip(rotation: Rotation)

    fun storeLocation(location: Int)

    fun gotoLocation(location: Int)

    fun setFrameRate(fps: Int)

    fun setInfraRedLightOn(on: Boolean)

    fun orientation(orientation: Orientation)
}

/**
 * CameraControl implementation which sends / retrieves HTTP requests to camera to control it
 */
class HttpGetBasedCameraControl(private val cameraInfo: CameraInfo,
                                private val cameraDefinition: CameraDefinition,
                                private val urls: CameraUrls,
                                private val settingsPageParser: SettingsParser,
                                private val httpclient: CloseableHttpClient = HttpClientFactory.create(cameraInfo),
                                private val stopper: ScheduledCameraMovementStopper = ScheduledCameraMovementStopper()) : CameraControl, AutoCloseable {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val movementLock = ReentrantLock()

    private val lastMovementDirection = AtomicReference(Direction.LEFT)

    override fun getCameraDefinition(): CameraDefinition = cameraDefinition

    override fun getVideoStream(): ContentStream =
            if (cameraDefinition.supportsVideoStreaming)
                getNativeVideoStream()
             else
                streamSnapshots()

    private fun streamSnapshots(): ContentStream {
        val streamer = SnapshotMJpegStreamer(httpclient, urls.snapshotUrl())
        streamer.start()
        Thread.sleep(1000)

        return ContentStream("multipart/x-mixed-replace;boundary=fotilo", streamer.inputStream)
    }

    private fun getNativeVideoStream(): ContentStream {
        val response = httpclient.execute(HttpGet(urls.videoStreamUrl()))

        check(response?.statusLine?.statusCode == HttpStatus.SC_OK) { "invalid response from camera" }

        return ContentStream(response.getFirstHeader("Content-Type").value, response.entity.content)
    }

    override fun getCameraSettings(): CameraSettings {
        val response = httpclient.execute(HttpGet(urls.settingsPageUrl()))
        try {
            val responseStream = response.entity.content
            return settingsPageParser.parse(responseStream)
        } finally {
            IOUtils.closeQuietly(response)
        }
    }

    override fun move(direction: Direction) {
        movementLock.withLock {
            sendGetRequest(urls.moveUrl(direction))
            lastMovementDirection.updateAndGet { direction }
        }
    }

    override fun move(direction: Direction, duration: Int) {
        move(direction)
        stopper.stopMovementAfterTime(this, duration)
    }

    override fun stopMovement() {
        movementLock.withLock {
            sendGetRequest(urls.stopUrl(lastMovementDirection.get()))
        }
    }

    override fun setPanTiltSpeed(speed: Int) {
        sendGetRequest(urls.panTiltSpeedUrl(speed))
    }

    override fun setBrightness(brightness: Int) {
        sendGetRequest(urls.brightnessUrl(brightness))
    }

    override fun setContrast(contrast: Int) {
        sendGetRequest(urls.contrastUrl(contrast))
    }

    override fun setResolution(resolution: String) {
        sendGetRequest(urls.resolutionUrl(resolution))
    }

    override fun flip(rotation: Rotation) {
        sendGetRequest(urls.flipUrl(rotation))
    }

    override fun storeLocation(location: Int) {
        sendGetRequest(urls.storeLocationUrl(location))
    }

    override fun gotoLocation(location: Int) {
        sendGetRequest(urls.gotoLocationUrl(location))
    }

    override fun setFrameRate(fps: Int) {
        sendGetRequest(urls.frameRateUrl(fps))
    }

    override fun setInfraRedLightOn(on: Boolean) {
        sendGetRequest(urls.infraRedLightUrl(on))
    }

    override fun orientation(orientation: Orientation) {
        sendGetRequest(urls.orientationUrl(orientation))
    }

    override fun saveSnapshot(path: Path): Unit = with(cameraInfo) {
        val response = httpclient.execute(HttpGet(urls.snapshotUrl()))
        try {
            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                val fos = FileOutputStream(path.toFile())
                IOUtils.copy(response.entity.content, fos)
            } else {
                // throw exception with error code
            }
        } finally {
            IOUtils.closeQuietly(response)
        }
    }

    private fun sendGetRequest(url: String): Boolean {
        logger.info("request to ${cameraDefinition.cameraType}: $url")
        val response = httpclient.execute(HttpGet(url))
        IOUtils.closeQuietly(response)
        val statusCode = response?.statusLine?.statusCode
        logger.info("camera response: $statusCode")

        return statusCode == HttpStatus.SC_OK
    }

    override fun close() {
        IOUtils.closeQuietly(httpclient)
    }
}
