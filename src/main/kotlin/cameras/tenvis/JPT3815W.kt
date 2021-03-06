package net.ylophones.fotilo.cameras.tenvis

import net.ylophones.fotilo.*
import net.ylophones.fotilo.cameras.CameraUrls
import net.ylophones.fotilo.cameras.SettingsParser
import net.ylophones.fotilo.io.parseLines
import java.io.*
import java.lang.IllegalArgumentException

val jpt3815wDefinition = CameraDefinition(
        cameraManufacturer = "Tenvis",
        cameraType = "JPT3815W",
        brightnessRange = SettingsRange(0, 255),
        contrastRange = SettingsRange(0, 5),
        panTiltSpeedRange = SettingsRange(1, 10),
        frameRateRange = SettingsRange(1, 30),
        locationRange = SettingsRange(1, 10),
        supportedResolutions = arrayListOf("160x120", "320x240", "640x480"),
        supportsInfraRedCut = false,
        orientationControlType = OrientationControlType.VERTICAL_AND_HORIZONTAL_FLIP
)

class Jpt3815w2014Urls(private val cameraInfo: CameraInfo) : Jpt3815wUrls(cameraInfo) {
    override fun videoStreamUrl() = with(cameraInfo) {
        "http://$host:$port/vjpeg.v"
    }

    override fun controlParamUrl(command: Int, value: Int) = with(cameraInfo) {
        "http://$host:$port/cgi-bin/control.cgi?action=cmd&code=$command&value=$value"
    }

    override fun snapshotUrl(): String = with(cameraInfo) {
        "http://$host:$port/snapshot.cgi"
    }
}

class Jpt3815w2013Urls(private val cameraInfo: CameraInfo) : Jpt3815wUrls(cameraInfo) {
    override fun videoStreamUrl() = with(cameraInfo) {
        "http://$host:$port/media/?action=stream"
    }

    override fun controlParamUrl(command: Int, value: Int) = with(cameraInfo) {
        "http://$host:$port/media/?action=cmd&code=$command&value=$value"
    }

    override fun snapshotUrl() = with(cameraInfo) {
        "http://$host:$port/media/?action=snapshot"
    }
}

/**
 * Provides URLs for operations and data retrieval from the Jpt3815wUrls web interface
 */
abstract class Jpt3815wUrls(private val cameraInfo: CameraInfo) : CameraUrls {

    private enum class Command(val id: Int) {
        SET_SPEED(1),
        START(2),
        STOP(3),
        SET_CONTRAST(5),
        SET_BRIGHTNESS(6),
        SET_RESOLUTION(7),
        SET_FRAME_RATE(8),
        SET_ROTATION(9),
        STORE_LOCATION(11),
        GOTO_LOCATION(13)
    }

    private enum class RotationValue(val value: Int) {
        VERTICAL(23),
        HORIZONTAL(13)
    }

    abstract fun controlParamUrl(command: Int, value: Int): String

    override fun settingsPageUrl() = with(cameraInfo) {
        "http://$host:$port/video/livesp.asp"
    }

    override fun moveUrl(direction: Direction): String {
        val directionValue = getDirectionCommandValue(direction)

        return controlParamUrl(Command.START.id, directionValue)
    }

    private fun getDirectionCommandValue(direction: Direction): Int {
        return when (direction) {
            Direction.UP -> 1
            Direction.UP_LEFT -> 6
            Direction.UP_RIGHT -> 5
            Direction.LEFT -> 4
            Direction.DOWN -> 2
            Direction.DOWN_RIGHT -> 7
            Direction.DOWN_LEFT -> 8
            Direction.RIGHT -> 3
        }
    }

    override fun stopUrl(lastDirection: Direction): String {
        val directionValue = getDirectionCommandValue(lastDirection)
        return controlParamUrl(Command.STOP.id, directionValue)
    }

    override fun panTiltSpeedUrl(speed: Int): String {
        checkWithinRange(speed, jpt3815wDefinition.panTiltSpeedRange, "speed")
        return controlParamUrl(Command.SET_SPEED.id, speed)
    }

    override fun brightnessUrl(brightness: Int): String {
        checkWithinRange(brightness, jpt3815wDefinition.brightnessRange, "brightness")
        return controlParamUrl(Command.SET_BRIGHTNESS.id, brightness)
    }

    override fun contrastUrl(contrast: Int): String {
        checkWithinRange(contrast, jpt3815wDefinition.contrastRange, "contrast")
        return controlParamUrl(Command.SET_CONTRAST.id, contrast)
    }

    override fun resolutionUrl(resolution: String): String {
        val resolutionValue: Int = when (resolution) {
            "160x120" -> 10485880
            "320x240" -> 20971760
            else -> 41943520 // "640x480"
        }

        return controlParamUrl(Command.SET_RESOLUTION.id, resolutionValue)
    }

    override fun flipUrl(rotation: Rotation): String {
        return when (rotation) {
            Rotation.VERTICAL -> controlParamUrl(Command.SET_ROTATION.id, RotationValue.VERTICAL.value)
            Rotation.HORIZONTAL -> controlParamUrl(Command.SET_ROTATION.id, RotationValue.HORIZONTAL.value)
        }
    }

    override fun storeLocationUrl(location: Int): String {
        checkWithinRange(location, jpt3815wDefinition.locationRange, "location")
        return controlParamUrl(Command.STORE_LOCATION.id, location)
    }

    override fun gotoLocationUrl(location: Int): String {
        checkWithinRange(location, jpt3815wDefinition.locationRange, "location")
        return controlParamUrl(Command.GOTO_LOCATION.id, location)
    }

    override fun frameRateUrl(fps: Int): String {
        checkWithinRange(fps, jpt3815wDefinition.frameRateRange, "frame rate")
        return controlParamUrl(Command.SET_FRAME_RATE.id, fps)
    }

    override fun infraRedLightUrl(on: Boolean): String = throw UnsupportedOperationException()

    private fun checkWithinRange(value: Int, range: SettingsRange, fieldName: String = "") =
            checkWithinRange("$fieldName not within range", value, range.min, range.max)
}

object Jpt3815wSettingsParser : SettingsParser {

    private enum class Jpt3815wSetting(val regex: Regex, val default: Int, val transform: (Int) -> Int = { it }) {
        FrameRate(".*document\\.getElementById\\(\"camFPS\"\\)\\.value = (\\d+).*".toRegex(), 5),
        Brightness(".*sbar_pos\\('pos_brig', (\\d+).*".toRegex(), 5, { (255 * (it.toFloat() / 10)).toInt() }),
        Contrast("^sbar_pos\\('pos_cntr', (\\d+).*".toRegex(), 3),
        PanTiltSpeed("var ptz_speed=(\\d+).*".toRegex(), 1),
        Resolution(".*switch\\((\\d+).*".toRegex(), 32),
    }

    override fun parse(page: InputStream): CameraSettings {
        val lines = parseLines(page)

        return CameraSettings(
                asInt(lines, Jpt3815wSetting.FrameRate),
                asInt(lines, Jpt3815wSetting.Brightness),
                asInt(lines, Jpt3815wSetting.Contrast),
                asInt(lines, Jpt3815wSetting.PanTiltSpeed),
                false,
                formatResolution(asInt(lines, Jpt3815wSetting.Resolution))
        )
    }

    private fun asInt(lines: List<String>, setting: Jpt3815wSetting): Int {
        val value = lines
                .mapNotNull { setting.regex.find(it) }
                .filter { it.groupValues.size == 2 }
                .map { it.groupValues[1].toInt() }
                .firstOrNull() ?: setting.default

        return setting.transform(value)
    }

    private fun formatResolution(resolutionId: Int): String = when (resolutionId) {
        32 -> "640x480"
        8 -> "320x240"
        2 -> "160x120"
        else -> throw IllegalArgumentException("unrecognised resolution")
    }
}
