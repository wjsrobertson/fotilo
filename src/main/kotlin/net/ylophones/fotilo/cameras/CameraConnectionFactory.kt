package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.CameraControl
import java.lang.UnsupportedOperationException

class CameraConnectionFactory {

    fun getCameraConnection(cameraId: String): CameraControl {
        throw UnsupportedOperationException()
        //return null
    }

}