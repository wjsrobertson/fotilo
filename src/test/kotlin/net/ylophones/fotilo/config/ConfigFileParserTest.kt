package net.ylophones.fotilo.config

import org.junit.Rule
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith()
class ConfigFileParserTest {

    @Test
    internal fun getReadConfigFile() {
        val config = this::class.java.getResource("/ConfigFileParserTest_validConfigFile.json").readText()


    }
}