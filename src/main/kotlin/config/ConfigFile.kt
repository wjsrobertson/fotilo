package net.ylophones.fotilo.config

import com.google.gson.Gson
import java.io.FileReader
import java.lang.RuntimeException
import java.nio.file.Path
import java.nio.file.Paths

data class ConfigFile(val cameras: List<CameraConfig>)

data class CameraConfig(val id: String,
                        val name: String,
                        val host: String,
                        val port: Int,
                        val username: String,
                        val password: String,
                        val type: String)

object ConfigFileParser {

    private const val CONFIG_FILE_PROPERTY = "fotilo.config.file"

    val readConfigFile: ConfigFile
        get() {
            val filePath = System.getProperty(CONFIG_FILE_PROPERTY)
                    ?: throw RuntimeException("Config file system property not found - $CONFIG_FILE_PROPERTY")

            return parseConfigFile(Paths.get(filePath))
        }

    private fun parseConfigFile(path: Path): ConfigFile {
        val fileReader = FileReader(path.toFile())

        return Gson().fromJson<ConfigFile>(fileReader, ConfigFile::class.java)
    }
}