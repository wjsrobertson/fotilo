package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.CameraSettings
import java.io.InputStream
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

private enum class Setting(val key: String, val default: String) {
    FrameRate("enc_framerate", "15"),
    Brightness("vbright", "128"),
    Contrast("vcontrast", "128"),
    PanTiltSpeed("speed", "10"),
    Resolution("resolution", "640x480"),
    InfraRedCut("ircut", "0")
}

object TR3818SettingsParser {

    fun parse(page: InputStream): CameraSettings {
        val settings = parseLines(page)
                .map { it.split("=") }
                .filter { it.size == 2 }
                .associateBy({ stripPrefix(it[0]) }, { stripSuffix(it[1]) })

        return CameraSettings(
                asInt(settings, Setting.FrameRate),
                asInt(settings, Setting.Brightness),
                asInt(settings, Setting.Contrast),
                asInt(settings, Setting.PanTiltSpeed),
                asBoolean(settings, Setting.InfraRedCut),
                formatResolution(settings[Setting.Resolution.key])
        )
    }

    private fun parseLines(page: InputStream): List<String> {
        val reader = BufferedReader(InputStreamReader(page, StandardCharsets.UTF_8))
        return IOUtils.readLines(reader)
    }

    private fun stripSuffix(part: String) = part.substring(0, part.length - 1)

    private fun stripPrefix(part: String) = part.substring(4)

    private fun asInt(settings: Map<String, String>, setting: Setting) = settings.getOrDefault(setting.key, setting.default).toInt()

    private fun asBoolean(settings: Map<String, String>, setting: Setting) = settings.getOrDefault(setting.key, setting.default) == "1"

    private fun formatResolution(resolutionId: String?) =
            if (resolutionId == "0") {
                "640x480"
            } else {
                "320x240"
            }

}

