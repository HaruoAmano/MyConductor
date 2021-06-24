package music.elsystem.myconductor


import android.content.Context
import android.opengl.GLSurfaceView
import music.elsystem.myconductor.databinding.ActivityMainBinding


//GLSurfaceViewを継承
class GlTrajectoryView(context: Context?) : GLSurfaceView(context) {
    companion object {
        private const val OPENGL_ES_VERSION = 2
    }

    init {
        setEGLContextClientVersion(OPENGL_ES_VERSION)
        setRenderer(GlTrajectoryRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}