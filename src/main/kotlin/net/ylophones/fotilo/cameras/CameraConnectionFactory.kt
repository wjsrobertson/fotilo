package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.CameraControl
import net.ylophones.fotilo.CameraInfo
import net.ylophones.fotilo.config.ConfigFile
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException
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
            CameraInfo(host, port, user, pass)
        }

        return when (cameraConfig.type) {
            "TR3818" -> TR3818CameraControl(cameraInfo)
            else -> throw IllegalStateException("Camera type is invalid: ${cameraConfig.type}")
        }
    }
}