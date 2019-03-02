package net.ylophones.fotilo.web.controllers

import net.ylophones.fotilo.*
import net.ylophones.fotilo.cameras.CameraConnectionFactory
import org.apache.commons.io.IOUtils.closeQuietly
import org.apache.http.entity.InputStreamEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.io.OutputStream
import javax.servlet.http.HttpServletResponse

private const val CONTENT_TYPE = "Content-Type"
private const val SUCCESS = """{success: true}"""

@RestController
@RequestMapping(value = ["/api/camera"], produces = ["application/json"])
class CameraController(private val cameraConnectionFactory: CameraConnectionFactory) {

    @RequestMapping(value = ["/{cameraId}"], method = [RequestMethod.GET])
    @Throws(IOException::class)
    fun getSettings(@PathVariable("cameraId") cameraId: String): CameraOverview {
        return getConnection(cameraId).getCameraOverview()
    }

    @RequestMapping(value = ["/{cameraId}/settings/frame-rate/{fps}"], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun setFrameRate(@PathVariable("cameraId") cameraId: String, @PathVariable("fps") fps: Int): String {
        getConnection(cameraId).setFrameRate(fps)
        return SUCCESS
    }

    @RequestMapping(value = ["/{cameraId}/settings/brightness/{brightness}"], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun setBrightness(@PathVariable("cameraId") cameraId: String, @PathVariable("brightness") brightness: Int): String {
        getConnection(cameraId).setBrightness(brightness)
        return SUCCESS
    }

    @RequestMapping(value = ["/{cameraId}/settings/contrast/{contrast}"], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun setContrast(@PathVariable("cameraId") cameraId: String, @PathVariable("contrast") contrast: Int): String {
        getConnection(cameraId).setContrast(contrast)
        return SUCCESS
    }

    @RequestMapping(value = ["/{cameraId}/settings/resolution/{resolution}"], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun setResolution(@PathVariable("cameraId") cameraId: String, @PathVariable("resolution") resolution: String): String {
        getConnection(cameraId).setResolution(resolution)
        return SUCCESS
    }

    @RequestMapping(value = ["/{cameraId}/settings/pan-tilt-speed/{panTiltSpeed}"], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun setPanTiltSpeed(@PathVariable("cameraId") cameraId: String, @PathVariable("panTiltSpeed") panTiltSpeed: Int): String {
        getConnection(cameraId).setPanTiltSpeed(panTiltSpeed)
        return SUCCESS
    }

    @RequestMapping("/{cameraId}/control/move/{direction}")
    @Throws(IOException::class)
    fun move(@PathVariable("cameraId") cameraId: String,
             @PathVariable("direction") direction: String,
             @RequestParam(value = "duration", required = false) duration: Int?): String {

        val moveInDirection = Direction.valueOf(direction)

        if (duration != null) {
            getConnection(cameraId).move(moveInDirection, duration)
        } else {
            getConnection(cameraId).move(moveInDirection)
        }

        return SUCCESS
    }

    @RequestMapping("/{cameraId}/control/stop")
    @Throws(IOException::class)
    fun stop(@PathVariable("cameraId") cameraId: String): String {
        getConnection(cameraId).stopMovement()
        return SUCCESS
    }

    @RequestMapping(value = ["/{cameraId}/settings/flip/{rotation}"], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun flip(@PathVariable("cameraId") cameraId: String, @PathVariable("rotation") rotation: String): String {
        getConnection(cameraId).flip(Rotation.valueOf(rotation))
        return SUCCESS
    }

    @RequestMapping(value = ["/{cameraId}/settings/orientation/{orientation}"], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun setOrientation(@PathVariable("cameraId") cameraId: String, @PathVariable("orientation") orientation: String): String {
        getConnection(cameraId).oritentation(Orientation.valueOf(orientation))
        return SUCCESS
    }

    @RequestMapping(value = ["/{cameraId}/settings/infra-red-light-on/{onOrOff}"], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun setInfraRedLightOnOrOff(@PathVariable("cameraId") cameraId: String, @PathVariable("onOrOff") onOrOff: String): String {
        getConnection(cameraId).setInfraRedLightOn(onOrOff.toBoolean())
        return SUCCESS
    }

    @RequestMapping("/{cameraId}/stream")
    @Throws(IOException::class)
    fun streamVideo(outputStream: OutputStream, response: HttpServletResponse,
                    @PathVariable("cameraId") cameraId: String) {

        val cameraControl = getConnection(cameraId)
        val cameraResponse = cameraControl.getVideoStream()

        try {
            val contentTypeValue = cameraResponse.getFirstHeader(CONTENT_TYPE).value
            response.setHeader(CONTENT_TYPE, contentTypeValue)

            val responseEntity = InputStreamEntity(cameraResponse.entity.content)
            responseEntity.writeTo(outputStream)
        } finally {
            closeQuietly(cameraResponse)
        }
    }

    private fun getConnection(cameraId: String): CameraControl = cameraConnectionFactory.getConnection(cameraId)

}