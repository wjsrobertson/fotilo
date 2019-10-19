package net.ylophones.fotilo

data class CameraSettings(
        val frameRate: Int,
        val brightness: Int,
        val contrast: Int,
        val panTiltSpeed: Int,
        val infrRedCutEnabled: Boolean,
        val resolution: String
)

data class SettingsRange(val min: Int, val max: Int)

enum class OrientationControlType {
    VERTICAL_AND_HORIZONTAL_FLIP,
    EXPLICIT
}

class CameraDefinition(
        val cameraManufacturer: String,
        val cameraType: String,
        val brightnessRange: SettingsRange,
        val contrastRange: SettingsRange,
        val panTiltSpeedRange: SettingsRange,
        val frameRateRange: SettingsRange,
        val locationRange: SettingsRange,
        val supportedResolutions: List<String>,
        val supportsInfraRedCut: Boolean,
        val orientationControlType: OrientationControlType,
        val supportsVideoStreaming: Boolean = true
)

data class CameraOverview(
        val settings: CameraSettings,
        val definition: CameraDefinition
)

enum class Direction {
    UP,
    UP_LEFT,
    UP_RIGHT,
    LEFT,
    DOWN,
    DOWN_RIGHT,
    DOWN_LEFT,
    RIGHT;

    companion object {
        fun toValue(str: String) = Direction.values().find { it.name.toLowerCase() == str.toLowerCase() } ?: LEFT
    }
}

enum class Rotation {
    VERTICAL,
    HORIZONTAL;

    companion object {
        fun toValue(str: String) = Rotation.values().find { it.name.toLowerCase() == str.toLowerCase() } ?: VERTICAL
    }
}

enum class Orientation {
    NORMAL,
    FLIP,
    MIRROR,
    FLIP_AND_MIRROR;

    companion object {
        fun toValue(str: String) = Orientation.values().find { it.name.toLowerCase() == str.toLowerCase() } ?: NORMAL
    }
}

data class CameraInfo(val host: String, val port: Int, val user: String, val pass: String)

interface RegexParseableCameraSetting {
    val default: String
    val regex: Regex

    fun extractOrDefault(lines: List<String>): String = lines
            .mapNotNull { regex.find(it) }
            .filter { it.groupValues.size == 2 }
            .map { it.groupValues[1] }
            .firstOrNull() ?: default
}