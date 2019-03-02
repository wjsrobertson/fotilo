package net.ylophones.fotilo.web.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Controller
@RequestMapping("/welcome")
class WelcomeController {

    @Value("\${application.message:Hello World}")
    private val message = "Hello World"

    @RequestMapping(value = ["/", ""])
    fun welcome(model: MutableMap<String, Any>): String {
        model["time"] = Date()
        model["message"] = this.message

        return "welcome"
    }
}
