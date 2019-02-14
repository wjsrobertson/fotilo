package net.ylophones.fotilo

data class CameraSettings(
        val frameRate: Int,
        val brightness: Int,
        val contrast: Int,
        val panTiltSpeed: Int,
        val infrRedCutEnabled: Boolean,
        val resolution: String
)

