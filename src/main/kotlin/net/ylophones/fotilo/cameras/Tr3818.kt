package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.*
import org.apache.commons.io.IOUtils
import java.io.*
import java.nio.charset.StandardCharsets

val tr3818Definition = CameraDefinition(
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

/**
 * Provides URLs for operations and data retrieval from the TR3818 web interface
 */
class TR3818Urls(private val cameraInfo: CameraInfo): CameraUrls {

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

    override fun videoStreamUrl() = with(cameraInfo) {
        "http://$host:$port/videostream.cgi?loginuse=$user&loginpas=$pass"
    }

    override fun settingsPageUrl() = with(cameraInfo) {
        "http://$host:$port/get_camera_params.cgi?loginuse=$user&loginpas=$pass"
    }

    override fun snapshotUrl(): String = with(cameraInfo) {
        "http://$host:$port/snapshot.cgi?user=$user&pwd=$pass"
    }

    override fun moveUrl(direction: Direction): String {
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

    override fun orientationUrl(orientation: Orientation): String {
        val code = when (orientation) {
            Orientation.FLIP -> Tr3818Orientation.ORIENTATION_FLIP.code
            Orientation.FLIP_AND_MIRROR -> Tr3818Orientation.ORIENTATION_FLIP_AND_MIRROR.code
            Orientation.MIRROR -> Tr3818Orientation.ORIENTATION_MIRROR.code
            Orientation.NORMAL -> Tr3818Orientation.ORIENTATION_NORMAL.code
        }

        return cameraControlParamUrl(Tr3818Param.PARAM_ORIENTATION.id, code)
    }

    override fun stopUrl() = createDecoderControlCommandUrl(Tr3818Command.COMMAND_STOP.id)

    override fun panTiltSpeedUrl(speed: Int): String {
        checkWithinRange(speed, tr3818Definition.panTiltSpeedRange, "speed")
        return miscSettingUrl("ptz_patrol_rate", speed)
    }

    override fun brightnessUrl(brightness: Int): String {
        checkWithinRange(brightness, tr3818Definition.brightnessRange, "brightness")
        return cameraControlParamUrl(Tr3818Param.PARAM_BRIGHTNESS.id, brightness)
    }

    override fun contrastUrl(contrast: Int): String {
        checkWithinRange(contrast, tr3818Definition.contrastRange, "contrast")
        return cameraControlParamUrl(Tr3818Param.PARAM_SET_CONTRAST.id, contrast)
    }

    override fun resolutionUrl(resolution: String): String {
        val resolutionValue: Int = if ("640x480" == resolution) 0 else 1
        return cameraControlParamUrl(Tr3818Param.PARAM_RESOLUTION.id, resolutionValue)
    }

    override fun storeLocationUrl(location: Int): String {
        checkWithinRange(location, tr3818Definition.locationRange, "location")
        val locationCommand = 2 * location - 1 + Tr3818Command.COMMAND_SET_LOCATION_OFFSET.id
        return createDecoderControlCommandUrl(locationCommand)
    }

    override fun gotoLocationUrl(location: Int): String {
        checkWithinRange(location, tr3818Definition.locationRange, "location")
        val locationCommand = 2 * location - 1 + Tr3818Command.COMMAND_GOTO_LOCATION_OFFSET.id
        return createDecoderControlCommandUrl(locationCommand)
    }

    override fun frameRateUrl(fps: Int): String {
        checkWithinRange(fps, tr3818Definition.frameRateRange, "frame rate")
        return cameraControlParamUrl(Tr3818Param.PARAM_FRAME_RATE.id, fps)
    }

    override fun infraRedLightUrl(on: Boolean): String {
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


object TR3818SettingsParser : SettingsParser {

    private enum class Tr3818Setting(val key: String, val default: String) {
        FrameRate("enc_framerate", "15"),
        Brightness("vbright", "128"),
        Contrast("vcontrast", "128"),
        PanTiltSpeed("speed", "10"),
        Resolution("resolution", "640x480"),
        InfraRedCut("ircut", "0")
    }

    override fun parse(page: InputStream): CameraSettings {
        val settings = parseLines(page)
                .map { it.split("=") }
                .filter { it.size == 2 }
                .associateBy({ stripPrefix(it[0]) }, { stripSuffix(it[1]) })

        return CameraSettings(
                asInt(settings, Tr3818Setting.FrameRate),
                asInt(settings, Tr3818Setting.Brightness),
                asInt(settings, Tr3818Setting.Contrast),
                asInt(settings, Tr3818Setting.PanTiltSpeed),
                asBoolean(settings, Tr3818Setting.InfraRedCut),
                formatResolution(settings[Tr3818Setting.Resolution.key])
        )
    }

    private fun parseLines(page: InputStream): List<String> {
        val reader = BufferedReader(InputStreamReader(page, StandardCharsets.UTF_8))
        return IOUtils.readLines(reader)
    }

    private fun stripSuffix(part: String) = part.substring(0, part.length - 1)

    private fun stripPrefix(part: String) = part.substring(4)

    private fun asInt(settings: Map<String, String>, setting: Tr3818Setting) = settings.getOrDefault(setting.key, setting.default).toInt()

    private fun asBoolean(settings: Map<String, String>, setting: Tr3818Setting) = settings.getOrDefault(setting.key, setting.default) == "1"

    private fun formatResolution(resolutionId: String?) = if (resolutionId == "0") "640x480" else "320x240"


}

