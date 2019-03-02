package net.ylophones.fotilo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FotiloConfiguration {

    @Bean
    open fun createConfigFile(): ConfigFile = ConfigFileParser.readConfigFile

}
