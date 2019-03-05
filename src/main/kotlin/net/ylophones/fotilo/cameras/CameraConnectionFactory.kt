package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.CameraInfo
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
        val cameraConfig = configFile.cameras.find { it.id == cameraId }
                ?: throw IllegalArgumentException("Invalid camera ID $cameraId")

        val cameraInfo = with(cameraConfig) {
            CameraInfo(host, port, username, password)
        }

        return when (cameraConfig.type) {
            "TR3818" -> HttpGetBasedCameraControl(cameraInfo, tr3818Definition)
            "JPT3815W"-> HttpGetBasedCameraControl(cameraInfo, jpt3815wDefinition)
            else -> throw IllegalStateException("Camera type is invalid: ${cameraConfig.type}")
        }
    }
}