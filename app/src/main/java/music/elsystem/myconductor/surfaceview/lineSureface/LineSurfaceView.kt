package music.elsystem.myconductor.surfaceview.lineSureface


import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView


//GLSurfaceViewを継承
class LineSurfaceView(context: Context?)
    : GLSurfaceView(context) {
    companion object {
        private const val OPENGL_ES_VERSION = 2
    }

    init {
        setEGLContextClientVersion(OPENGL_ES_VERSION)
        setRenderer(LineRenderer())
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}