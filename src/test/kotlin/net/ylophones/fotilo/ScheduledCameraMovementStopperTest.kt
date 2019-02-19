package net.ylophones.fotilo

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import java.time.Duration.ofSeconds

import org.junit.jupiter.api.Assertions.assertTimeout

class ScheduledCameraMovementStopperTest {

    @Test
    internal fun checkMovementStopsAfterTime() {
        val underTest = ScheduledCameraMovementStopper()
        val cameraControl: CameraControl = mock()

        assertTimeout(ofSeconds(2)) {
            underTest.stopMovementAfterTime(cameraControl, 1)
            Thread.sleep(1500)
            verify(cameraControl).stopMovement()
        }
    }
}