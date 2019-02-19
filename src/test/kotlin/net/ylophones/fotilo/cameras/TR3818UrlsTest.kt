package net.ylophones.fotilo.cameras

import junit.framework.Assert.fail
import net.ylophones.fotilo.CameraInfo
import net.ylophones.fotilo.Direction
import net.ylophones.fotilo.Orientation
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import java.lang.Exception
import java.util.stream.Stream

class TR3818UrlsTest {

    private val underTest: TR3818Urls = TR3818Urls(CameraInfo("example.com", 80, "peter", "parker"))

    @Test
    internal fun videoStreamUrl() {
        val url = underTest.videoStreamUrl()

        assertThat(url).isEqualTo("http://example.com:80/videostream.cgi?loginuse=peter&loginpas=parker")
    }

    @Test
    internal fun settingsPageUrl() {
        val url = underTest.settingsPageUrl()

        assertThat(url).isEqualTo("http://example.com:80/get_camera_params.cgi?loginuse=peter&loginpas=parker")
    }

    @Test
    internal fun snapshotUrl() {
        val url = underTest.snapshotUrl()

        assertThat(url).isEqualTo("http://example.com:80/snapshot.cgi?user=peter&pwd=parker")
    }

    @ParameterizedTest
    @MethodSource("directionUrls")
    internal fun moveUrl(direction: Direction, expectedUrl: String) {
        val url = underTest.moveUrl(direction)

        assertThat(url).isEqualTo(expectedUrl)
    }

    @ParameterizedTest
    @MethodSource("orientationUrls")
    internal fun oritentationUrl(orientation: Orientation, expectedUrl: String) {
        val url = underTest.oritentationUrl(orientation)

        assertThat(url).isEqualTo(expectedUrl)
    }

    @Test
    internal fun stopUrl() {
        val url = underTest.stopUrl()

        assertThat(url).isEqualTo("http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=1&onestep=0")
    }

    @Test
    internal fun checkValidPanTiltSpeedUrl() {
        val url = underTest.panTiltSpeedUrl(10)

        assertThat(url).isEqualTo("http://example.com:80/set_misc.cgi?loginuse=peter&loginpas=parker&ptz_patrol_rate=10")
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 11])
    internal fun checkInvalidPanTiltSpeeds(speed: Int) {
        checkForIllegalArgumentException {
            underTest.panTiltSpeedUrl(speed)
        }
    }

    @Test
    internal fun checkValidBrightnessUrl() {
        val url = underTest.brightnessUrl(10)

        assertThat(url).isEqualTo("http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=1&value=10")
    }

    @ParameterizedTest
    @ValueSource(ints = [-1000, -129, 129, 1000])
    internal fun checkInvalidBrightness(invalidBrightness: Int) {
        checkForIllegalArgumentException {
            underTest.brightnessUrl(invalidBrightness)
        }
    }

    @Test
    internal fun checkValidContrastUrl() {
        val url = underTest.contrastUrl(10)

        assertThat(url).isEqualTo("http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=2&value=10")
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 255, 256, 1000])
    internal fun checkInvalidContrast(invalidContrast: Int) {
        checkForIllegalArgumentException {
            underTest.contrastUrl(invalidContrast)
        }
    }

    @ParameterizedTest
    @MethodSource("resolutionUrls")
    internal fun checkResolutionUrl(resolution: String, expectedUrl: String) {
        val url = underTest.resolutionUrl(resolution)

        assertThat(url).isEqualTo(expectedUrl)
    }

    @ParameterizedTest
    @MethodSource("validStoreLocationUrls")
    internal fun checkValidStoreLocationUrl(locationId: Int, expectedUrl: String) {
        val url = underTest.storeLocationUrl(locationId)

        assertThat(url).isEqualTo(expectedUrl)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 9, 100])
    internal fun checkInvalidStoreLocationId(locationId: Int) {
        checkForIllegalArgumentException {
            underTest.storeLocationUrl(locationId)
        }
    }

    @ParameterizedTest
    @MethodSource("validGotoLocationUrls")
    internal fun checkValidGotoLocationUrl(locationId: Int, expectedUrl: String) {
        val url = underTest.gotoLocationUrl(locationId)

        assertThat(url).isEqualTo(expectedUrl)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 9, 100])
    internal fun checkInvalidGotoLocationId(locationId: Int) {
        checkForIllegalArgumentException {
            underTest.gotoLocationUrl(locationId)
        }
    }

    @ParameterizedTest
    @MethodSource("frameRateUrls")
    internal fun checkFrameRateUrls(fps: Int, expectedUrl: String) {
        val url = underTest.frameRateUrl(fps)

        assertThat(url).isEqualTo(expectedUrl)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 31, 100])
    internal fun checkInvalidFrameRates(fps: Int) {
        checkForIllegalArgumentException {
            underTest.frameRateUrl(fps)
        }
    }

    @ParameterizedTest
    @MethodSource("irUrls")
    internal fun checkInfraRedUrls(on: Boolean, expectedUrl: String) {
        val url = underTest.infraRedLightUrl(on)

        assertThat(url).isEqualTo(expectedUrl)
    }

    fun checkForIllegalArgumentException(operation: () -> Any) {
        try {
            operation()
            fail("IllegalArgumentException not thrown")
        } catch (e: IllegalArgumentException) {
            // ok
        } catch (e: Exception) {
            fail("wrong exception thrown: ${e.message}")
        }
    }

    companion object {
        @JvmStatic
        private fun resolutionUrls(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of("640x480", "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=0&value=0"),
                    Arguments.of("320x240", "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=0&value=1")
            )
        }

        @JvmStatic
        private fun directionUrls(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(Direction.UP, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=0&onestep=0"),
                    Arguments.of(Direction.UP_LEFT, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=92&onestep=0"),
                    Arguments.of(Direction.UP_RIGHT, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=91&onestep=0"),
                    Arguments.of(Direction.LEFT, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=4&onestep=0"),
                    Arguments.of(Direction.DOWN, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=2&onestep=0"),
                    Arguments.of(Direction.DOWN_RIGHT, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=93&onestep=0"),
                    Arguments.of(Direction.DOWN_LEFT, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=92&onestep=0"),
                    Arguments.of(Direction.RIGHT, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=6&onestep=0")
            )
        }

        @JvmStatic
        private fun orientationUrls(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(Orientation.FLIP, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=5&value=1"),
                    Arguments.of(Orientation.FLIP_AND_MIRROR, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=5&value=3"),
                    Arguments.of(Orientation.MIRROR, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=5&value=2"),
                    Arguments.of(Orientation.NORMAL, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=5&value=0")
            )
        }

        @JvmStatic
        private fun validStoreLocationUrls(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(1, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=30&onestep=0"),
                    Arguments.of(2, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=32&onestep=0"),
                    Arguments.of(3, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=34&onestep=0"),
                    Arguments.of(8, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=44&onestep=0")
            )
        }

        @JvmStatic
        private fun validGotoLocationUrls(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(1, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=31&onestep=0"),
                    Arguments.of(2, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=33&onestep=0"),
                    Arguments.of(3, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=35&onestep=0"),
                    Arguments.of(8, "http://example.com:80/decoder_control.cgi?loginuse=peter&loginpas=parker&command=45&onestep=0")
            )
        }

        @JvmStatic
        private fun frameRateUrls(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(1, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=6&value=1"),
                    Arguments.of(2, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=6&value=2"),
                    Arguments.of(5, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=6&value=5"),
                    Arguments.of(30, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=6&value=30")
            )
        }

        @JvmStatic
        private fun irUrls(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(true, "http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=14&value=1"),
                    Arguments.of(false,"http://example.com:80/camera_control.cgi?loginuse=peter&loginpas=parker&param=14&value=0")
            )
        }
    }
}