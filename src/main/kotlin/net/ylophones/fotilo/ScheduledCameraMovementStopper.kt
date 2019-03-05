package net.ylophones.fotilo

import net.ylophones.fotilo.cameras.CameraControl
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ScheduledCameraMovementStopper(private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)) {
    fun stopMovementAfterTime(cameraControl: CameraControl, seconds: Int) {
        scheduler.schedule({ cameraControl.stopMovement() }, seconds.toLong(), TimeUnit.SECONDS)
    }

    fun shutdown() {
        scheduler.shutdownNow()
    }
}