package net.ylophones.fotilo.web.controllers

import net.ylophones.fotilo.cameras.CameraConnectionFactory
import org.apache.commons.io.IOUtils.closeQuietly
import org.apache.http.entity.InputStreamEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.IOException
import java.io.OutputStream
import javax.servlet.http.HttpServletResponse

@Controller
class VideoStreamController(private val cameraConnectionFactory: CameraConnectionFactory) {

    private val CONTENT_TYPE = "Content-Type"

    @RequestMapping("/stream")
    @Throws(IOException::class)
    fun streamVideo(@RequestParam("camera") cameraId: String, outputStream: OutputStream, response: HttpServletResponse) {
        val cameraControl = cameraConnectionFactory.getCameraConnection(cameraId)
        val cameraResponse = cameraControl.getVideoStream()

        try {
            val contentTypeValue = cameraResponse.getFirstHeader(CONTENT_TYPE).getValue()
            response.setHeader(CONTENT_TYPE, contentTypeValue)

            val responseEntity = InputStreamEntity(cameraResponse.getEntity().getContent())
            responseEntity.writeTo(outputStream)
        } finally {
            closeQuietly(cameraResponse)
        }
    }
}
