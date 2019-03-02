package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.CameraControl
import net.ylophones.fotilo.config.ConfigFile
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class CameraConnectionFactory(private val configFile: ConfigFile) {

    private val connections: ConcurrentMap<String, CameraControl> = ConcurrentHashMap()

    fun getConnection(cameraId: String): CameraControl {
        connections.computeIfAbsent(cameraId) { id -> }


    }

    private fun createCameraConnection(cameraId: String): CameraControl {
        val cameraConfig = configFile.cameras.find { it.id == cameraId } ?: throw IllegalArgumentException("Invalid camera ID $cameraId")

        when (cameraConfig.type) {
            // TODO - replace with factory
            "TR3818" -> TR3818CameraControl(cameraConfig)
            else -> IllegalStateException("Camera type is invalid: ${cameraConfig.type}")
        }

    }

}