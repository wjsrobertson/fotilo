package net.ylophones.fotilo.web.controllers

import net.ylophones.fotilo.config.CameraConfig
import net.ylophones.fotilo.config.ConfigFile
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/cameras"], produces = ["application/json"])
class CamerasController(private val configFile: ConfigFile) {

    @RequestMapping(value = ["", "/"], method = [RequestMethod.GET])
    fun listCameras(): List<CameraConfig> = configFile.cameras

}
