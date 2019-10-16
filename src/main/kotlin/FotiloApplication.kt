package net.ylophones.fotilo

import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class FotiloApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<FotiloApplication>(*args)
        }
    }

}