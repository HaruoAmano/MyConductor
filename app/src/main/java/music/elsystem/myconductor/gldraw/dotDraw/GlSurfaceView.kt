package music.elsystem.myconductor.gldraw.dotDraw


import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView


//GLSurfaceViewを継承
class GlSurfaceView(context: Context,rhythm:Int,tempo:Int,bmpBeat: Bitmap?, voice: String,motionYMultiplier:Double) : GLSurfaceView(context) {
    companion object {
        private const val OPENGL_ES_VERSION = 2
    }
    init {
        setEGLContextClientVersion(OPENGL_ES_VERSION)
        setRenderer(GlRenderer(context, rhythm,tempo,bmpBeat,voice,motionYMultiplier))
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}