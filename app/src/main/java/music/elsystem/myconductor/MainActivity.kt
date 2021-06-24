package music.elsystem.myconductor

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import music.elsystem.myconductor.databinding.ActivityMainBinding

//入力項目
val rhythm = 2
val tempo = 30


//グローバル変数
var surfaceWidth = 0
var surfaceHeight = 0

class MainActivity : Activity() {
    private val bd by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bd.root)
        //サーフェスビューの親ビューであるLinearLayoutを生成する。
        val observer: ViewTreeObserver = bd.layoutGlSurfaceView.viewTreeObserver
        observer.addOnGlobalLayoutListener {
            Log.i(tagMsg, "---ViewTreeObserver---!")
            Log.i(tagMsg, "Surfaceview Width: ${bd.layoutParent.width}")
            Log.i(tagMsg, "Surfaceview Height: ${bd.layoutParent.height}")
            Log.i(tagMsg, "Surfaceview Width: ${bd.layoutGlSurfaceView.width}")
            Log.i(tagMsg, "Surfaceview Height: ${bd.layoutGlSurfaceView.height}")
            surfaceWidth = bd.layoutGlSurfaceView.width
            surfaceHeight = bd.layoutGlSurfaceView.height
        }

        bd.btnView.setOnClickListener {
//            val glSurfaceview = GlSurfaceView(this)
//            bd.layoutGlSurfaceView.addView(glSurfaceview)
        }
        bd.btnTrajectory.setOnClickListener {
            val gltrajectoryView = GlTrajectoryView(this)
            bd.layoutGlSurfaceView.addView(gltrajectoryView)
        }
        bd.btnStop.setOnClickListener {
            bd.layoutGlSurfaceView.isEnabled = false
        }
    }

    companion object {
        private const val tagMsg = "My_MainActivity : "
    }
}