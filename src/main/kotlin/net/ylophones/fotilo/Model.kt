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
        val orientationControlType: OrientationControlType
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
    RIGHT
}

enum class Rotation {
    VERTICAL,
    HORIZONTAL;
}

enum class Orientation {
    NORMAL,
    FLIP,
    MIRROR,
    FLIP_AND_MIRROR
}

data class CameraInfo(val host: String, val port: Int, val user: String, val pass: String)