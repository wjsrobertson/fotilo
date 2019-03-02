package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.CameraControl
import org.springframework.stereotype.Component
import java.lang.UnsupportedOperationException

@Component
class CameraConnectionFactory {

    fun getCameraConnection(cameraId: String): CameraControl {
        throw UnsupportedOperationException()
        //return null
    }

}