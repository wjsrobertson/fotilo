package net.ylophones.fotilo.cameras

import org.junit.Rule
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat

class Tr3818v31114SettingsParserTest {

    @Test
    internal fun checkActualFileIsParsed() {
        // given
        val resourceAsStream = this::class.java.getResourceAsStream("/tr3818-3.1.1.1.4.test.cgi.xml")

        // when
        val cameraSettings = Tr3818v31114SettingsParser.parse(resourceAsStream)

        // then
        with(cameraSettings) {
            assertThat(panTiltSpeed).isEqualTo(4)
            assertThat(brightness).isEqualTo(5)
            assertThat(frameRate).isEqualTo(10)
            assertThat(resolution).isEqualTo("640x480")
            assertThat(contrast).isEqualTo(70)
        }
    }

}