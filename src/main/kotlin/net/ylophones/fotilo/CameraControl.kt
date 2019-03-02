package net.ylophones.fotilo

import net.ylophones.fotilo.cameras.TR3818CameraControl
import org.apache.http.client.methods.CloseableHttpResponse
import java.io.IOException
import java.lang.IllegalStateException
import java.nio.file.Path

interface CameraControl {

    fun getVideoStream(): CloseableHttpResponse

    fun getCameraSettings(): CameraSettings

    fun getCameraDefinition(): CameraDefinition

    @Throws(IOException::class)
    fun getCameraOverview(): CameraOverview = CameraOverview(getCameraSettings(), getCameraDefinition())

    @Throws(IOException::class)
    fun move(direction: Direction)

    @Throws(IOException::class)
    fun move(direction: Direction, duration: Int)

    @Throws(IOException::class)
    fun stopMovement()

    @Throws(IOException::class)
    fun saveSnapshot(path: Path)

    @Throws(IOException::class)
    fun setPanTiltSpeed(speed: Int)

    @Throws(IOException::class)
    fun setBrightness(brightness: Int)

    @Throws(IOException::class)
    fun setContrast(contrast: Int)

    @Throws(IOException::class)
    fun setResolution(resolution: String)

    @Throws(IOException::class)
    fun flip(rotation: Rotation)

    @Throws(IOException::class)
    fun storeLocation(location: Int)

    @Throws(IOException::class)
    fun gotoLocation(location: Int)

    @Throws(IOException::class)
    fun setFrameRate(fps: Int)

    @Throws(IOException::class)
    fun setInfraRedLightOn(on: Boolean)

    @Throws(IOException::class)
    fun oritentation(orientation: Orientation)

}
