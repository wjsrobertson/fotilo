package net.ylophones.fotilo.cameras

import net.ylophones.fotilo.cameras.tenvis.Jpt3815wSettingsParser
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat

class Jpt3815wSettingsParserTest {

    @Test
    internal fun checkActualFileIsParsed() {
        // given
        val resourceAsStream = this::class.java.getResourceAsStream("/jpt3815w_livesp.asp.html")

        // when
        val cameraSettings = Jpt3815wSettingsParser.parse(resourceAsStream)

        // then
        with(cameraSettings) {
            assertThat(panTiltSpeed).isEqualTo(0)
            assertThat(brightness).isEqualTo(102)
            assertThat(frameRate).isEqualTo(30)
            assertThat(resolution).isEqualTo("640x480")
            assertThat(contrast).isEqualTo(2)
            assertThat(infrRedCutEnabled).isEqualTo(false)
        }
    }

}