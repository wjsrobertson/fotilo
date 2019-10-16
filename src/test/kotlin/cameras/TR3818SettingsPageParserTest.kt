package net.ylophones.fotilo.cameras

import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat

class TR3818SettingsPageParserTest {

    @Test
    internal fun checkActualFileIsParsed() {
        // given
        val resourceAsStream = this::class.java.getResourceAsStream("/tr3818_get_camera_params.cgi.html")

        // when
        val cameraSettings = TR3818SettingsParser.parse(resourceAsStream)

        // then
        with(cameraSettings) {
            assertThat(panTiltSpeed).isEqualTo(10)
            assertThat(brightness).isEqualTo(123)
            assertThat(frameRate).isEqualTo(15)
            assertThat(resolution).isEqualTo("640x480")
            assertThat(contrast).isEqualTo(128)
            assertThat(infrRedCutEnabled).isEqualTo(false)
        }
    }
}