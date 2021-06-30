package music.elsystem.myconductor


import android.content.Context
import android.opengl.GLSurfaceView


//GLSurfaceViewを継承
class LineSurfaceView(context: Context?) : GLSurfaceView(context) {
    companion object {
        private const val OPENGL_ES_VERSION = 2
    }

    init {
        setEGLContextClientVersion(OPENGL_ES_VERSION)
        setRenderer(LineRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}