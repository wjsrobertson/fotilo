package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.*
import org.apache.commons.io.IOUtils
import java.io.*
import java.nio.charset.StandardCharsets

val jptw3815WHDDefinition = CameraDefinition(
        cameraManufacturer = "Tenvis",
        cameraType = "JPW3815W-HD",
        brightnessRange = SettingsRange(0, 100),
        contrastRange = SettingsRange(0, 100),
        panTiltSpeedRange = SettingsRange(0, 100),
        frameRateRange = SettingsRange(1, 8),
        locationRange = SettingsRange(0, 7),
        supportedResolutions = arrayListOf("640x480", "1280x720"),
        supportsInfraRedCut = true,
        orientationControlType = OrientationControlType.EXPLICIT,
        supportsVideoStreaming = false
)

class JPW3815WHDUrls(private val cameraInfo: CameraInfo) : CameraUrls {

    override fun videoStreamUrl() = throw UnsupportedOperationException()

    private fun controlParamUrl(command: String, attribute: String, value: String, att2: String? = null, val2: String? = null) = with(cameraInfo) {
        val secondAttr = if (att2 == null || val2 == null) "" else "&-$att2=$val2"
        "http://$host:$port/cgi-bin/hi3510/param.cgi?cmd=$command&-$attribute=$value$secondAttr"
    }

    override fun snapshotUrl(): String = with(cameraInfo) {
        "http://$host:$port/tmpfs/snap.jpg"
    }

    override fun settingsPageUrl() = with(cameraInfo) {
        "http://$host:$port/cgi-bin/hi3510/param.cgi?cmd=getlightattr&cmd=getircutattr&cmd=getmotorattr&cmdgetircutattr&cmdgetinfrared&cmd=getimageattr"
    }

    private fun ptzUrl(direction: String) = with(cameraInfo) {
        "http://$host:$port/cgi-bin/hi3510/ptzctrl.cgi?-step=0&-act=$direction&-speed=45"
    }

    override fun moveUrl(direction: Direction): String = ptzUrl(getDirectionCommandValue(direction))

    private fun getDirectionCommandValue(direction: Direction): String {
        return when (direction) {
            Direction.UP -> "up"
            Direction.UP_LEFT -> "upleft"
            Direction.UP_RIGHT -> "upright"
            Direction.LEFT -> "left"
            Direction.DOWN -> "down"
            Direction.DOWN_RIGHT -> "downright"
            Direction.DOWN_LEFT -> "downleft"
            Direction.RIGHT -> "right"
        }
    }

    override fun stopUrl(lastDirection: Direction): String = ptzUrl("stop")

    override fun panTiltSpeedUrl(speed: Int): String = throw UnsupportedOperationException()

    override fun brightnessUrl(brightness: Int): String = controlParamUrl("setimageattr", "brightness", brightness.toString())

    override fun contrastUrl(contrast: Int): String = controlParamUrl("setimageattr", "contrast", contrast.toString())

    override fun resolutionUrl(resolution: String): String = throw UnsupportedOperationException()

    override fun orientationUrl(orientation: Orientation): String = when (orientation) {
        Orientation.FLIP -> controlParamUrl("setimageattr", "flip", "on", "mirror", "off")
        Orientation.FLIP_AND_MIRROR -> controlParamUrl("setimageattr", "flip", "on", "mirror", "on")
        Orientation.MIRROR -> controlParamUrl("setimageattr", "flip", "off", "mirror", "on")
        Orientation.NORMAL -> controlParamUrl("setimageattr", "flip", "off", "mirror", "off")
    }

    override fun storeLocationUrl(location: Int): String = with(cameraInfo) {
        checkWithinRange(location, jpt3815wDefinition.locationRange, "location")
        return "http://$host:$port/cgi-bin/hi3510/preset.cgi?-act=set&-status=1&-number=$location"
    }

    override fun gotoLocationUrl(location: Int): String = with(cameraInfo) {
        checkWithinRange(location, jpt3815wDefinition.locationRange, "location")
        return "http://$host:$port/cgi-bin/hi3510/param.cgi?cmd=preset&-act=goto&-number=$location"
    }

    override fun frameRateUrl(fps: Int): String = throw UnsupportedOperationException()

    override fun infraRedLightUrl(on: Boolean): String = with(cameraInfo) {
        val onOrOff = if (on) "open" else "closed"
        return "http://$host:$port/cgi-bin/hi3510/setinfrared.cgi?-infraredstat=$onOrOff"
    }

    private fun checkWithinRange(value: Int, range: SettingsRange, fieldName: String = "") =
            net.ylophones.fotilo.checkWithinRange("$fieldName not within range", value, range.min, range.max)
}

object JPW3815WHDSettingsParser : SettingsParser {

    private enum class Jpt3815wSetting(val regex: Regex, val default: String) {
        Brightness(".*var brightness=\"(\\d+)\".*".toRegex(), "50"),
        Contrast(".*var contrast=\"(\\d+)\".*".toRegex(), "50"),
        InfraRedCut("var display_mode=\"(\\d+)\".*".toRegex(), "0") // off = 1, on = 0
    }

    override fun parse(page: InputStream): CameraSettings {
        val lines = parseLines(page)

        return CameraSettings(
                0,
                extract(lines, Jpt3815wSetting.Brightness).toInt(),
                extract(lines, Jpt3815wSetting.Contrast).toInt(),
                0,
                extract(lines, Jpt3815wSetting.Contrast) == "0",
                "1280x720")
    }

    private fun parseLines(page: InputStream): List<String> {
        val reader = BufferedReader(InputStreamReader(page, StandardCharsets.UTF_8))
        return IOUtils.readLines(reader)
    }

    private fun extract(lines: List<String>, setting: Jpt3815wSetting): String {
        val parsedValue = lines
                .map { setting.regex.find(it) }
                .filter { it?.groupValues?.size == 2 }
                .map { it!!.groupValues[1] }
                .firstOrNull()

        return parsedValue ?: setting.default
    }
}
