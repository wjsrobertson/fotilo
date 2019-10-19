package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.cameras.tenvis.JPW3815WHDSettingsParser
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat

class JPW3815WHDSettingsParserTest {

    @Test
    internal fun checkActualFileIsParsed() {
        // given
        val resourceAsStream = this::class.java.getResourceAsStream("/JPT3815w-hd-param.html")

        // when
        val cameraSettings = JPW3815WHDSettingsParser.parse(resourceAsStream)

        // then
        with(cameraSettings) {
            assertThat(panTiltSpeed).isEqualTo(0)
            assertThat(brightness).isEqualTo(50)
            assertThat(frameRate).isEqualTo(0)
            assertThat(resolution).isEqualTo("1280x720")
            assertThat(contrast).isEqualTo(55)
            assertThat(infrRedCutEnabled).isEqualTo(false)
        }
    }
}