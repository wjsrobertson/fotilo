package net.ylophones.fotilo.cameras.tenvis

import net.ylophones.fotilo.*
import net.ylophones.fotilo.cameras.CameraUrls
import net.ylophones.fotilo.cameras.SettingsParser
import net.ylophones.fotilo.io.parseLines
import java.io.*
import java.lang.IllegalArgumentException

val tr3818v31114Definition = CameraDefinition(
        cameraManufacturer = "Tenvis",
        cameraType = "TR3818v31114",
        brightnessRange = SettingsRange(0, 255),
        contrastRange = SettingsRange(0, 5),
        panTiltSpeedRange = SettingsRange(1, 12),
        frameRateRange = SettingsRange(1, 30),
        locationRange = SettingsRange(1, 10),
        supportedResolutions = arrayListOf("160x120", "320x240", "640x480"),
        supportsInfraRedCut = true,
        orientationControlType = OrientationControlType.VERTICAL_AND_HORIZONTAL_FLIP
)

class TR3818v31114Urls(private val cameraInfo: CameraInfo) : CameraUrls {

    private enum class Command(val id: Int) {
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

    private enum class Tr3818Orientation(val code: Int) {
        NORMAL(20),
        FLIP(21),
        MIRROR(22),
        FLIP_AND_MIRROR(23)
    }

    override fun videoStreamUrl() = with(cameraInfo) {
        "http://$host:$port/vjpeg.v?user=$user&pwd=$pass"
    }

    private fun controlParamUrl(command: Int, value: Int) = with(cameraInfo) {
        "http://$host:$port/cgi-bin/control.cgi?action=cmd&code=$command&value=$value"
    }

    override fun snapshotUrl(): String = with(cameraInfo) {
        "http://$host:$port/snapshot.cgi"
    }

    override fun settingsPageUrl() = with(cameraInfo) {
        "http://$host:$port/cgi-bin/test.cgi"
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
        return controlParamUrl(Command.STOP.id, 8) // command value is always 8 for stop
    }

    override fun panTiltSpeedUrl(speed: Int): String = throw UnsupportedOperationException()

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

    override fun orientationUrl(orientation: Orientation): String {
        val code = when (orientation) {
            Orientation.FLIP -> Tr3818Orientation.FLIP.code
            Orientation.FLIP_AND_MIRROR -> Tr3818Orientation.FLIP_AND_MIRROR.code
            Orientation.MIRROR -> Tr3818Orientation.MIRROR.code
            Orientation.NORMAL -> Tr3818Orientation.NORMAL.code
        }

        return controlParamUrl(Command.SET_ROTATION.id, code)
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

    override fun infraRedLightUrl(on: Boolean): String {
        throw UnsupportedOperationException()
    }

    private fun checkWithinRange(value: Int, range: SettingsRange, fieldName: String = "") =
            checkWithinRange("$fieldName not within range", value, range.min, range.max)
}

object Tr3818v31114SettingsParser : SettingsParser {

    private enum class Jpt3815wSetting(override val regex: Regex, override val default: String) : RegexParseableCameraSetting {
        FrameRate(".*<fps>(\\d+)</fps>.*".toRegex(), "5"),
        Brightness(".*<brightness>(\\d+)</brightness>.*".toRegex(), "5"),
        Contrast(".*<contrast>(\\d+)</contrast>.*".toRegex(), "3"),
        PanTiltSpeed(".*<speed>(\\d+)</speed>.*".toRegex(), "1"),
        Resolution(".*<resolution>(\\d+)</resolution>.*".toRegex(), "32"),
    }

    override fun parse(page: InputStream): CameraSettings {
        val lines = parseLines(page)

        return CameraSettings(
                Jpt3815wSetting.FrameRate.extractOrDefault(lines).toInt(),
                Jpt3815wSetting.Brightness.extractOrDefault(lines).toInt(),
                Jpt3815wSetting.Contrast.extractOrDefault(lines).toInt(),
                Jpt3815wSetting.PanTiltSpeed.extractOrDefault(lines).toInt(),
                false,
                formatResolution(Jpt3815wSetting.Resolution.extractOrDefault(lines))
        )
    }

    private fun formatResolution(resolutionId: String): String = when (resolutionId) {
        "32" -> "640x480"
        "8" -> "320x240"
        "2" -> "160x120"
        else -> throw IllegalArgumentException("unrecognised resolution")
    }
}
