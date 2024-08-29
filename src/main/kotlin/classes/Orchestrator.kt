package classes

import org.openrndr.animatable.Animatable
import org.openrndr.events.Event

enum class Stage {
    IDLE,
    TRACKING,
    PLOTTING
}

class Orchestrator: Animatable() {

    val idleEvent = Event<Unit>()
    val trackEvent = Event<Unit>()
    val plotEvent = Event<Unit>()

    val timeBeforeCalling = 30 * 1000L
    val timeBeforeTracking = 5 * 1000L
    val timeBeforePlotting = 10 * 1000L
    val timeBeforeIdle = 60 * 1000L

    var presenceTimeStamp = 0L
    var plottingTimeStamp = 0L
    var callingTimeStamp = 0L

    var timeSinceTracked = 0L
    var timeSinceCalling = 0L
    var timeSincePlotting = 0L

    var currentStage = Stage.IDLE
        set(value) {
            if (field != value) {
                when(value) {
                    Stage.IDLE -> idleEvent.trigger(Unit)
                    Stage.TRACKING -> trackEvent.trigger(Unit)
                    Stage.PLOTTING -> plotEvent.trigger(Unit)
                }
            }
            field = value
        }
    var presence = false

    fun updateKinects(k1: Boolean, k2: Boolean) {
        presence = k1 || k2

        if (currentStage != Stage.PLOTTING) {
            timeSinceTracked = System.currentTimeMillis() - presenceTimeStamp
        }
    }

    fun update(k1: Boolean, k2: Boolean) {
        updateKinects(k1, k2)

        when(currentStage) {
            Stage.IDLE -> {
                // plot idlestate
                if (presence && timeSinceTracked > timeBeforeTracking) {
                    currentStage = Stage.TRACKING
                    presenceTimeStamp = System.currentTimeMillis()
                }
            }
            Stage.TRACKING -> {
                if (timeSinceTracked > timeBeforeCalling) {
                    currentStage = Stage.PLOTTING
                    callingTimeStamp = System.currentTimeMillis()
                }
            }
            Stage.PLOTTING -> {
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
