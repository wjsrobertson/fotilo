package net.ylophones.fotilo.cameras

import com.nhaarman.mockitokotlin2.*
import net.ylophones.fotilo.CameraInfo
import net.ylophones.fotilo.ScheduledCameraMovementStopper
import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.support.io.TempDirectory
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path

@ExtendWith(TempDirectory::class)
class TR3818CameraControlTest {

    private val cameraInfo: CameraInfo = CameraInfo("example.com", 80, "peter", "parker")
    private val httpclient: CloseableHttpClient = mock()
    private val urls: TR3818Urls = TR3818Urls(cameraInfo)
    private val stopper: ScheduledCameraMovementStopper = ScheduledCameraMovementStopper()
    private val settingsPageParser: TR3818SettingsParser = TR3818SettingsParser

    private val underTest: TR3818CameraControl = TR3818CameraControl(cameraInfo, httpclient, urls, stopper, settingsPageParser)

    @BeforeEach internal fun setUp() {
        reset(httpclient)
    }

    @Test
    internal fun checkSettingsAreRetrieved() {
        setupSettingsHttpRequest()

        val cameraSettings = underTest.getCameraSettings()

        assertThat(cameraSettings.resolution).isEqualTo("640x480")
    }

    private fun setupSettingsHttpRequest() {
        val settingsFileStream = this::class.java.getResourceAsStream("/tr3818_get_camera_params.cgi.html")

        val response: CloseableHttpResponse = mock()
        whenever(httpclient.execute(isA())).thenReturn(response)

        val entity: HttpEntity = mock()
        whenever(entity.content).thenReturn(settingsFileStream)
        whenever(response.entity).thenReturn(entity)
    }

    @Test
    internal fun checkSnapshotWritten(@TempDirectory.TempDir tempDirectory: Path) {
        val file = tempDirectory.resolve("test")
        snapshotHttpRequest()

        underTest.saveSnapshot(file)

        val content = Files.readAllLines(file)[0]
        assertThat(content).isEqualTo("OK")
    }

    private fun snapshotHttpRequest() {
        val okStream = ByteArrayInputStream(byteArrayOf(79, 75)) // 79=o 75=k

        val response: CloseableHttpResponse = mock()
        whenever(httpclient.execute(isA())).thenReturn(response)

        val status: StatusLine = mock()
        whenever(status.statusCode).thenReturn(200)
        whenever(response.statusLine).thenReturn(status)

        val entity: HttpEntity = mock()
        whenever(entity.content).thenReturn(okStream)
        whenever(response.entity).thenReturn(entity)
    }
}