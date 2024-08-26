package classes

import org.openrndr.animatable.Animatable

enum class Stage {
    IDLE,
    TRACKING,
    PLOTTING
}

class Orchestrator: Animatable() {

    val timeBeforeCalling = 5 * 60 * 1000L
    val timeBeforeTracking = 5 * 1000L
    val timeBeforePlotting = 10 * 1000L
    val timeBeforeIdle = 60 * 1000L

    var presenceTimeStamp = System.currentTimeMillis()
    var plottingTimeStamp = System.currentTimeMillis()
    var callingTimeStamp = System.currentTimeMillis()

    var timeSinceTracked = System.currentTimeMillis()
    var timeSinceCalling = System.currentTimeMillis()
    var timeSincePlotting = System.currentTimeMillis()

    var currentStage = Stage.IDLE
    var presence = false
        set(value) {
            if (!field && value) {
                presenceTimeStamp = System.currentTimeMillis()
            }
            field = value
        }


    fun updateKinects(k1: Boolean, k2: Boolean) {
        if (k1 || k2) {
            presence = true
        } else {
            presence = false
        }

        timeSinceTracked = System.currentTimeMillis() - presenceTimeStamp

    }

    fun update(k1: Boolean, k2: Boolean) {
        updateKinects(k1, k2)

        when(currentStage) {
            Stage.IDLE -> {
                // plot idlestate
                if (presence && timeSinceTracked > timeBeforeTracking) {
                    currentStage = Stage.TRACKING
                }
            }
            Stage.TRACKING -> {
                if (timeSinceTracked > timeBeforeCalling) {
                    currentStage = Stage.PLOTTING
                    callingTimeStamp = System.currentTimeMillis()
                }
            }
            Stage.PLOTTING -> {
                presence = false
                timeSinceCalling = System.currentTimeMillis() - callingTimeStamp
                if (timeSinceCalling > timeBeforePlotting) {
                    // plot
                    timeSincePlotting = System.currentTimeMillis() - plottingTimeStamp
                    if (timeSincePlotting > timeBeforeIdle) {
                        currentStage = Stage.IDLE
                    }

                } else {
                    plottingTimeStamp = System.currentTimeMillis()
                }
            }
        }


    }
}
