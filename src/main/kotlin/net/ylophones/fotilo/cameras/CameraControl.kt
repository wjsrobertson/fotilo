package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.*
import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path

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

    fun stopUrl(): String

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

    fun getVideoStream(): CloseableHttpResponse

    fun getCameraSettings(): CameraSettings

    fun getCameraDefinition(): CameraDefinition

    @Throws(IOException::class)
    fun getCameraOverview(): CameraOverview = CameraOverview(getCameraSettings(), getCameraDefinition())

    @Throws(IOException::class)
    fun move(direction: Direction)

    @Throws(IOException::class)
    fun move(direction: Direction, duration: Int)

    @Throws(IOException::class)
    fun stopMovement()

    @Throws(IOException::class)
    fun saveSnapshot(path: Path)

    @Throws(IOException::class)
    fun setPanTiltSpeed(speed: Int)

    @Throws(IOException::class)
    fun setBrightness(brightness: Int)

    @Throws(IOException::class)
    fun setContrast(contrast: Int)

    @Throws(IOException::class)
    fun setResolution(resolution: String)

    @Throws(IOException::class)
    fun flip(rotation: Rotation)

    @Throws(IOException::class)
    fun storeLocation(location: Int)

    @Throws(IOException::class)
    fun gotoLocation(location: Int)

    @Throws(IOException::class)
    fun setFrameRate(fps: Int)

    @Throws(IOException::class)
    fun setInfraRedLightOn(on: Boolean)

    @Throws(IOException::class)
    fun oritentation(orientation: Orientation)
}

/**
 * CameraControl implementation which sends / retrieves HTTP requests to camera to control it
 */
class HttpGetBasedCameraControl(private val cameraInfo: CameraInfo,
                                private val cameraDefinition: CameraDefinition,
                                private val urls: CameraUrls = TR3818Urls(cameraInfo),
                                private val settingsPageParser: SettingsParser = TR3818SettingsParser,
                                private val httpclient: CloseableHttpClient = HttpClientFactory.create(cameraInfo),
                                private val stopper: ScheduledCameraMovementStopper = ScheduledCameraMovementStopper()) : CameraControl, AutoCloseable {

    override fun getCameraDefinition(): CameraDefinition = cameraDefinition

    @Throws(IOException::class)
    override fun getVideoStream(): CloseableHttpResponse = httpclient.execute(HttpGet(urls.videoStreamUrl()))

    @Throws(IOException::class)
    override fun getCameraSettings(): CameraSettings {
        val response = httpclient.execute(HttpGet(urls.settingsPageUrl()))
        try {
            val responseStream = response.entity.content
            return settingsPageParser.parse(responseStream)
        } finally {
            IOUtils.closeQuietly(response)
        }
    }

    @Throws(IOException::class)
    override fun move(direction: Direction) {
        sendGetRequest(urls.moveUrl(direction))
    }

    @Throws(IOException::class)
    override fun move(direction: Direction, duration: Int) {
        move(direction)
        stopper.stopMovementAfterTime(this, duration)
    }

    @Throws(IOException::class)
    override fun stopMovement() {
        sendGetRequest(urls.stopUrl())
    }

    @Throws(IOException::class)
    override fun setPanTiltSpeed(speed: Int) {
        sendGetRequest(urls.panTiltSpeedUrl(speed))
    }

    @Throws(IOException::class)
    override fun setBrightness(brightness: Int) {
        sendGetRequest(urls.brightnessUrl(brightness))
    }

    @Throws(IOException::class)
    override fun setContrast(contrast: Int) {
        sendGetRequest(urls.contrastUrl(contrast))
    }

    @Throws(IOException::class)
    override fun setResolution(resolution: String) {
        sendGetRequest(urls.resolutionUrl(resolution))
    }

    @Throws(IOException::class)
    override fun flip(rotation: Rotation) {
        sendGetRequest(urls.flipUrl(rotation))
    }

    @Throws(IOException::class)
    override fun storeLocation(location: Int) {
        sendGetRequest(urls.storeLocationUrl(location))
    }

    @Throws(IOException::class)
    override fun gotoLocation(location: Int) {
        sendGetRequest(urls.gotoLocationUrl(location))
    }

    @Throws(IOException::class)
    override fun setFrameRate(fps: Int) {
        sendGetRequest(urls.frameRateUrl(fps))
    }

    @Throws(IOException::class)
    override fun setInfraRedLightOn(on: Boolean) {
        sendGetRequest(urls.infraRedLightUrl(on))
    }

    @Throws(IOException::class)
    override fun oritentation(orientation: Orientation) {
        sendGetRequest(urls.orientationUrl(orientation))
    }

    @Throws(IOException::class)
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

    @Throws(IOException::class)
    private fun sendGetRequest(url: String): Boolean {
        val response = httpclient.execute(HttpGet(url))
        IOUtils.closeQuietly(response)
        return response.statusLine.statusCode == HttpStatus.SC_OK
    }

    override fun close() {
        IOUtils.closeQuietly(httpclient)
    }
}
