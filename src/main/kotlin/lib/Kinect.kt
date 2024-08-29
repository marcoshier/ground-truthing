
package lib

import edu.ufl.digitalworlds.j4k.J4KSDK
import edu.ufl.digitalworlds.j4k.Skeleton
import edu.ufl.digitalworlds.j4k.VideoFrame
import org.openrndr.events.Event
import org.openrndr.math.Vector3

class Kinect(val videoTexture: VideoFrame = VideoFrame()): J4KSDK() {

    val colorFrameEvent = Event<ByteArray?>("color-frame")
    val infraredFrameEvent = Event<ByteArray>("infrared-frame")
    val depthFrameEvent = Event<List<Vector3>>("depth-frame")

    override fun onDepthFrameEvent(
        depth_frame: ShortArray?,
        player_index: ByteArray?,
        XYZ: FloatArray?,
        UV: FloatArray?
    ) {
        XYZ?.let {
            depthFrameEvent.trigger(it.toList().chunked(3).map {l ->
                Vector3(l[0].toDouble(), l[1].toDouble(), l[2].toDouble())
            })
        }
    }


    override fun onSkeletonFrameEvent(
        skeleton_tracked: BooleanArray?,
        joint_position: FloatArray?,
        joint_orientation: FloatArray?,
        joint_status: ByteArray?
    ) {
        val skeletons: Array<Skeleton?> = arrayOfNulls(maxNumberOfSkeletons)
        for (i in 0 until maxNumberOfSkeletons)  {
            skeletons[i] = Skeleton.getSkeleton(i, skeleton_tracked, joint_position, joint_orientation, joint_status, this)
        }
    }


    override fun onColorFrameEvent(data: ByteArray?) {
        videoTexture.update(colorWidth, colorHeight, data)
        colorFrameEvent.trigger(data)
    }


    override fun onInfraredFrameEvent(data: ShortArray) {

        val sz = infraredWidth * infraredHeight
        var bgra = ByteArray(sz * 4)

        var idx = 0
        var iv: Int
        var sv: Short
        var bv: Byte
        for (i in 0 until sz) {
            sv = data[i]
            iv = sv.toInt()
            bv = ((iv) shr 8).toByte()
            bgra[idx] = bv
            idx++
            bgra[idx] = bv
            idx++
            bgra[idx] = bv
            idx++
            bgra[idx] = 0
            idx++
        }

        videoTexture.update(infraredWidth, infraredHeight, bgra)
        infraredFrameEvent.trigger(bgra)
    }


}
