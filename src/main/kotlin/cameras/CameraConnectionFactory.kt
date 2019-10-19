package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.CameraInfo
import net.ylophones.fotilo.cameras.tenvis.*
import net.ylophones.fotilo.config.ConfigFile
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class CameraConnectionFactory(private val configFile: ConfigFile) {

    private val connections: ConcurrentMap<String, CameraControl> = ConcurrentHashMap()

    fun getConnection(cameraId: String): CameraControl
            = connections.computeIfAbsent(cameraId) { id -> createCameraConnection(id) }

    private fun createCameraConnection(cameraId: String): CameraControl {
        val cameraConfig = configFile.cameras
                .find { it.id == cameraId }
                ?: throw IllegalArgumentException("Invalid camera ID $cameraId")

        val cameraInfo = with(cameraConfig) {
            CameraInfo(host, port, username, password)
        }

        return when (cameraConfig.type) {
            "TR3818" -> HttpGetBasedCameraControl(cameraInfo, tr3818Definition, TR3818Urls(cameraInfo), TR3818SettingsParser)
            "TR3818.3.1.1.1.4" -> HttpGetBasedCameraControl(cameraInfo, tr3818v31114Definition, TR3818v31114Urls(cameraInfo), Tr3818v31114SettingsParser)
            "JPT3815W2014"-> HttpGetBasedCameraControl(cameraInfo, jpt3815wDefinition, Jpt3815w2014Urls(cameraInfo), Jpt3815wSettingsParser)
            "JPT3815W2013"-> HttpGetBasedCameraControl(cameraInfo, jpt3815wDefinition, Jpt3815w2013Urls(cameraInfo), Jpt3815wSettingsParser)
            "JPT3815W-HD"-> HttpGetBasedCameraControl(cameraInfo, jptw3815WHDDefinition, JPW3815WHDUrls(cameraInfo), JPW3815WHDSettingsParser)
            else -> throw IllegalStateException("Camera type is invalid: ${cameraConfig.type}")
        }
    }
}