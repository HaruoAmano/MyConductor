package music.elsystem.myconductor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import music.elsystem.myconductor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val bd by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var tempo2 = mutableListOf<Int>()
    var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bd.root)
        //サーフェスビューの親ビューであるLinearLayoutを生成する。
        val observer: ViewTreeObserver = bd.layoutGlSurfaceView.viewTreeObserver
        observer.addOnGlobalLayoutListener {
            Log.i(tagMsg, "---ViewTreeObserver---!")
//            Log.i(tagMsg, "Parent Width: ${bd.layoutParent.width}")
//            Log.i(tagMsg, "Parent Height: ${bd.layoutParent.height}")
            Log.i(tagMsg, "Surfaceview Width: ${bd.layoutGlSurfaceView.width}")
            Log.i(tagMsg, "Surfaceview Height: ${bd.layoutGlSurfaceView.height}")
            surfaceWidth = bd.layoutGlSurfaceView.width
            surfaceHeight = bd.layoutGlSurfaceView.height
        }
        //拍子のデフォルトとして２拍子を選択する。
        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.twobeat)

        /////////////////////////////////////////
        //spnTempo1スピナーの設定
        val tempo1list = listOf(
            "Grave", "Largo", "Adagio", "Andante",
            "Moderate", "Allegretto", "Allegro"
        )
        val adapter1 = ArrayAdapter(applicationContext, R.layout.custom_spinner, tempo1list).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        // spnTempo1 に adapter をセット
        bd.spnTempo1.adapter = adapter1
        bd.spnTempo1.setSelection(3)   //初期値をAndanteとする。
        // リスナーを登録
        bd.spnTempo1.onItemSelectedListener = SpinnerItemSelectedListener1()
        //spnTempo2スピナーの設定
        // リスナーを登録
        bd.spnTempo2.onItemSelectedListener = SpinnerItemSelectedListener2()
        //spnRhythmスピナーの設定
        val rhythmList = listOf(
            "2 beat", "3 beat", "4beat"
        )
        // spinner に adapter をセット
        bd.spnBeat.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, rhythmList).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        // リスナーを登録
        bd.spnBeat.onItemSelectedListener = SpinnerItemSelectedListener3()
        val glSurfaceview = GlSurfaceView(this)
        val lineSurfaceview = LineSurfaceView(this)
        bd.layoutGlSurfaceView.setOnClickListener {
            if (!isStarted) {
                bd.layoutGlSurfaceView.removeView(lineSurfaceview)
                bd.layoutGlSurfaceView.addView(glSurfaceview)
                isStarted = true
            } else {
                //ライン描画のサーフェスビューを表示する。
                bd.layoutGlSurfaceView.removeView(glSurfaceview)
                bd.layoutGlSurfaceView.addView(lineSurfaceview)
                isStarted = false
            }
        }

        bd.btnGravity.setOnClickListener {
        }
        bd.btnStop.setOnClickListener {
        }
        bd.btnStop.setOnClickListener {
            bd.layoutGlSurfaceView.isEnabled = false
        }
        //ビットマップ画像新規作成時以外は常にコメント。
        //ビットマップの生成にはActivityの継承が必要みたいなので、ビットマップに対する処理はメインアクティビティに配置する。
//        val mH = MeasureBmpLineHeight()
//        mH.measureBmpLineHeight(2)
    }

    private inner class SpinnerItemSelectedListener1 : AdapterView.OnItemSelectedListener {
        //　アイテムが選択された時
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?, position: Int, id: Long
        ) {
            val spinnerParent = parent as Spinner
            val tempo1item = spinnerParent.selectedItem as String
            //tempo2のリスト内容の設定
            tempo2 = when (tempo1item) {
                "Grave" -> tempo2list.filter { it in 30..44 } as MutableList<Int>
                "Largo" -> tempo2list.filter { it in 38..52 } as MutableList<Int>
                "Adagio" -> tempo2list.filter { it in 46..60 } as MutableList<Int>
                "Andante" -> tempo2list.filter { it in 56..76 } as MutableList<Int>
                "Moderate" -> tempo2list.filter { it in 66..92 } as MutableList<Int>
                "Allegretto" -> tempo2list.filter { it in 88..116 } as MutableList<Int>
                "Allegro" -> tempo2list.filter { it in 112..152 } as MutableList<Int>
                else -> tempo2list.filter { it in 56..76 } as MutableList<Int>
            }
            //spnTempo2スピナーの設定
            // spinner に adapter をセット
            val adapter2 =
                ArrayAdapter(applicationContext, R.layout.custom_spinner, tempo2).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            bd.spnTempo2.adapter = adapter2
        }

        //　アイテムが選択されなかった
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    private inner class SpinnerItemSelectedListener2 : AdapterView.OnItemSelectedListener {
        //　アイテムが選択された時
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?, position: Int, id: Long
        ) {
            val spinnerParent = parent as Spinner
            tempo = spinnerParent.selectedItem as Int
            timeChange()
        }

        //　アイテムが選択されなかった
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
    fun timeChange () {
        //リフレッシュレート60Hzを前提に一旦設計
        halfBeatDots = (1800f / tempo.toFloat()).toInt()
        oneBarDots = halfBeatDots * 2 * rhythm
        if (isStarted) {
            bd.layoutGlSurfaceView.removeView(glSurfaceview)
            bd.layoutGlSurfaceView.addView(glSurfaceview)
        } else {
            //ライン描画のサーフェスビューを表示する。
            bd.layoutGlSurfaceView.removeView(lineSurfaceview)
            bd.layoutGlSurfaceView.addView(lineSurfaceview)
        }
    }

    private inner class SpinnerItemSelectedListener3 : AdapterView.OnItemSelectedListener {
        //　アイテムが選択された時
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?, position: Int, id: Long
        ) {
            val spinnerParent = parent as Spinner
            val item3 = spinnerParent.selectedItem as String
            Log.i(tagMsg, "$item3")
        }

        //　アイテムが選択されなかった
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }


    companion object {
        private const val tagMsg = "My_MainActivity"
        private val tempo2list = listOf(
            30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 63, 66, 69, 72,
            76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 126, 132, 138, 144, 152
        )
        //BitmapはResourceとしてActivity内で呼び出す必要があるため、onCreate内で２拍子をデフォルトとして割り当てている。
        var bmpBeat: Bitmap? = null
        //ディスプレイサイズ
        var surfaceWidth = 0
        var surfaceHeight = 0
        //入力項目
        val rhythm = 2
        var tempo = 0
        //リフレッシュレート60Hzを前提に一旦設計
        var halfBeatDots = 0
        var oneBarDots = 0
    }
}
