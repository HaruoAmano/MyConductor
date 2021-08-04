package music.elsystem.myconductor.gldraw.lineDraw


import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView


//GLSurfaceViewを継承
class LineSurfaceView(context: Context?,rhythm:Int,bmpBeat:Bitmap?)
    : GLSurfaceView(context) {
    companion object {
        private const val OPENGL_ES_VERSION = 2
    }

    init {
        setEGLContextClientVersion(OPENGL_ES_VERSION)
        setRenderer(LineRenderer(rhythm,bmpBeat))
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}