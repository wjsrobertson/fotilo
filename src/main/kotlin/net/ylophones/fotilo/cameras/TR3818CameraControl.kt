package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.*
import org.apache.commons.io.IOUtils
import org.apache.commons.io.IOUtils.closeQuietly
import org.apache.http.HttpStatus
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Path

private val tr3818Definition = CameraDefinition(
        cameraManufacturer = "Tenvis",
        cameraType = "TR3818",
        brightnessRange = SettingsRange(-128, 128),
        contrastRange = SettingsRange(1, 254),
        panTiltSpeedRange = SettingsRange(1, 10),
        frameRateRange = SettingsRange(1, 30),
        locationRange = SettingsRange(1, 8),
        supportedResolutions = arrayListOf("320x240", "640x480"),
        supportsInfraRedCut = true,
        orientationControlType = OrientationControlType.EXPLICIT
)

private enum class Tr3818Orientation(val code: Int) {
    ORIENTATION_NORMAL(0),
    ORIENTATION_FLIP(1),
    ORIENTATION_MIRROR(2),
    ORIENTATION_FLIP_AND_MIRROR(3)
}

private enum class Tr3818Param(val id: Int) {
    PARAM_RESOLUTION(0),
    PARAM_BRIGHTNESS(1),
    PARAM_SET_CONTRAST(2),
    PARAM_ORIENTATION(5),
    PARAM_FRAME_RATE(6),
    PARAM_INFRA_RED(14)
}

private enum class Tr3818Command(val id: Int) {
    COMMAND_STOP(1),
    COMMAND_SET_LOCATION_OFFSET(29),
    COMMAND_GOTO_LOCATION_OFFSET(30)
}

/**
 * Provides URLs for operations and data retrieval from the TR3818 web interface
 */
class TR3818Urls(private val cameraInfo: CameraInfo) {

    fun videoStreamUrl() = with(cameraInfo) {
        "http://$host:$port/videostream.cgi?loginuse=$user&loginpas=$pass"
    }

    fun settingsPageUrl() = with(cameraInfo) {
        "http://$host:$port/get_camera_params.cgi?loginuse=$user&loginpas=$pass"
    }

    fun snapshotUrl(): String = with(cameraInfo) {
        "http://$host:$port/snapshot.cgi?user=$user&pwd=$pass"
    }

    fun moveUrl(direction: Direction): String {
        val command = when (direction) {
            Direction.UP -> 0
            Direction.UP_LEFT -> 92
            Direction.UP_RIGHT -> 91
            Direction.LEFT -> 4
            Direction.DOWN -> 2
            Direction.DOWN_RIGHT -> 93
            Direction.DOWN_LEFT -> 92
            Direction.RIGHT -> 6
        }

        return createDecoderControlCommandUrl(command)
    }

    fun oritentationUrl(orientation: Orientation): String {
        val code = when (orientation) {
            Orientation.FLIP -> Tr3818Orientation.ORIENTATION_FLIP.code
            Orientation.FLIP_AND_MIRROR -> Tr3818Orientation.ORIENTATION_FLIP_AND_MIRROR.code
            Orientation.MIRROR -> Tr3818Orientation.ORIENTATION_MIRROR.code
            Orientation.NORMAL -> Tr3818Orientation.ORIENTATION_NORMAL.code
        }

        return cameraControlParamUrl(Tr3818Param.PARAM_ORIENTATION.id, code)
    }

    fun stopUrl() = createDecoderControlCommandUrl(Tr3818Command.COMMAND_STOP.id)

    fun panTiltSpeedUrl(speed: Int): String {
        checkWithinRange(speed, tr3818Definition.panTiltSpeedRange, "speed")
        return miscSettingUrl("ptz_patrol_rate", speed)
    }

    fun brightnessUrl(brightness: Int): String {
        checkWithinRange(brightness, tr3818Definition.brightnessRange, "brightness")
        return cameraControlParamUrl(Tr3818Param.PARAM_BRIGHTNESS.id, brightness)
    }

    fun contrastUrl(contrast: Int): String {
        checkWithinRange(contrast, tr3818Definition.contrastRange, "contrast")
        return cameraControlParamUrl(Tr3818Param.PARAM_SET_CONTRAST.id, contrast)
    }

    fun resolutionUrl(resolution: String): String {
        val resolutionValue: Int = if ("640x480" == resolution) 0 else 1
        return cameraControlParamUrl(Tr3818Param.PARAM_RESOLUTION.id, resolutionValue)
    }

    fun storeLocationUrl(location: Int): String {
        checkWithinRange(location, tr3818Definition.locationRange, "location")
        val locationCommand = 2 * location - 1 + Tr3818Command.COMMAND_SET_LOCATION_OFFSET.id
        return createDecoderControlCommandUrl(locationCommand)
    }

    fun gotoLocationUrl(location: Int): String {
        checkWithinRange(location, tr3818Definition.locationRange, "location")
        val locationCommand = 2 * location - 1 + Tr3818Command.COMMAND_GOTO_LOCATION_OFFSET.id
        return createDecoderControlCommandUrl(locationCommand)
    }

    fun frameRateUrl(fps: Int): String {
        checkWithinRange(fps, tr3818Definition.frameRateRange, "frame rate")
        return cameraControlParamUrl(Tr3818Param.PARAM_FRAME_RATE.id, fps)
    }

    fun infraRedLightUrl(on: Boolean): String {
        val irValue = if (on) 1 else 0
        return cameraControlParamUrl(Tr3818Param.PARAM_INFRA_RED.id, irValue)
    }

    private fun createDecoderControlCommandUrl(command: Int) = with(cameraInfo) {
        "http://$host:$port/decoder_control.cgi?loginuse=$user&loginpas=$pass&command=$command&onestep=0"
    }

    private fun cameraControlParamUrl(param: Int, value: Int) = with(cameraInfo) {
        "http://$host:$port/camera_control.cgi?loginuse=$user&loginpas=$pass&param=$param&value=$value"
    }

    private fun miscSettingUrl(setting: String, value: Int) = with(cameraInfo) {
        "http://$host:$port/set_misc.cgi?loginuse=$user&loginpas=$pass&$setting=$value"
    }

    private fun checkWithinRange(value: Int, range: SettingsRange, fieldName: String = "") =
            net.ylophones.fotilo.checkWithinRange("$fieldName not within range", value, range.min, range.max)
}

/**
 * Sends / retrieves HTTP requests to TR3818 web interface
 */
class TR3818CameraControl(private val cameraInfo: CameraInfo,
                          private val httpclient: CloseableHttpClient,
                          private val urls: TR3818Urls,
                          private val stopper: ScheduledCameraMovementStopper = ScheduledCameraMovementStopper(),
                          private val settingsPageParser: TR3818SettingsParser = TR3818SettingsParser) : CameraControl, AutoCloseable {

    override fun getCameraDefinition(): CameraDefinition = tr3818Definition

    @Throws(IOException::class)
    override fun getVideoStream(): CloseableHttpResponse = httpclient.execute(HttpGet(urls.videoStreamUrl()))

    @Throws(IOException::class)
    override fun getCameraSettings(): CameraSettings {
        val response = httpclient.execute(HttpGet(urls.settingsPageUrl()))
        try {
            val responseStream = response.entity.content
            return settingsPageParser.parse(responseStream)
        } finally {
            closeQuietly(response)
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
    override fun flip(rotation: Rotation) = throw UnsupportedOperationException()

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
        sendGetRequest(urls.oritentationUrl(orientation))
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
            closeQuietly(response)
        }
    }

    @Throws(IOException::class)
    private fun sendGetRequest(url: String): Boolean {
        val response = httpclient.execute(HttpGet(url))
        closeQuietly(response)
        return response.statusLine.statusCode == HttpStatus.SC_OK
    }

    override fun close() {
        closeQuietly(httpclient)
    }
}
