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
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private val bd by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var bmpBeat: Bitmap? = null
    //リスナーインターフェースで使用する項目
    var rhythm = 4
    var tempo = 60
    var motionYMultiplier = 1.0
    var radiusMultiplier = 1.0
    var gradationMultiplier = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bd.root)
        //サーフェスビューの親ビューであるLinearLayoutを生成する。
        val observerSurfaceViewLayout: ViewTreeObserver = bd.layoutGlSurfaceView.viewTreeObserver
        observerSurfaceViewLayout.addOnGlobalLayoutListener {
            Log.i(tagMsg, "---ViewTreeObserver---!")
            Log.i(tagMsg, "Surfaceview Width: ${bd.layoutGlSurfaceView.width}")
            Log.i(tagMsg, "Surfaceview Height: ${bd.layoutGlSurfaceView.height}")
            surfaceWidth = bd.layoutGlSurfaceView.width
            surfaceHeight = bd.layoutGlSurfaceView.height
        }
        //ヌルポを避けるため、とりあえずビットマップを設定する。
        val options = BitmapFactory.Options()
        options.inScaled = false
        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.fourbeat, options)
        //********* サーフェスビュークリック時の設定*****************************
        val lineSurfaceview = LineSurfaceView(this)
        val glSurfaceview = GlSurfaceView(this)
        bd.layoutGlSurfaceView.setOnClickListener {
            if (!isStarted) {
                bd.layoutGlSurfaceView.removeView(lineSurfaceview)
                setOpglArray()
                bd.layoutGlSurfaceView.addView(glSurfaceview)
                isStarted = true
            } else {
                //ライン描画のサーフェスビューを表示する。
                bd.layoutGlSurfaceView.removeView(glSurfaceview)
                setOpglArray()
                bd.layoutGlSurfaceView.addView(lineSurfaceview)
                isStarted = false
            }
        }
        //********* spnTempo1スピナーの設定（moderate,Allegro etc... )*********************
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
        bd.spnTempo1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                val tempo1item = spinnerParent.selectedItem as String
                //tempo2のリスト内容の設定
                val tempo2 = when (tempo1item) {
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
                //テンポの中ほどをデフォルトにする。
                bd.spnTempo2.setSelection(4)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //********* spnTempo2スピナーの設定（60,108 etc.....) *********************
        //spnTempo2スピナーの設定
        // リスナーを登録
        bd.spnTempo2.onItemSelectedListener  = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                tempo = spinnerParent.selectedItem as Int
                setOpglArray()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //********* spnRhythmスピナーの設定（2beat,4beat etc...) *********************
        val rhythmList = listOf(
            "2 beat", "4 beat"
        )
        // spinner に adapter をセット
        bd.spnBeat.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, rhythmList).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        // リスナーを登録
        bd.spnBeat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val options = BitmapFactory.Options()
                options.inScaled = false
                val spinnerParent = parent as Spinner
                when (spinnerParent.selectedItem as String) {
                    "2 beat" -> {
                        rhythm = 2
                        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.twobeat, options)

                    }
                    "3 beat" -> {
                        rhythm = 3
                    }
                    "4 beat" -> {
                        rhythm = 4
                        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.fourbeat, options)
                    }
                }
                bitmapX = bmpBeat?.let { it.width } ?: 0
                bitmapY = bmpBeat?.let { it.height } ?: 0
                setOpglArray()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //********* spnMotionYスピナーの設定（タクトの跳ね具合) *********************
        val motionYList = listOf(
            0.5,0.8,1.0, 1.2,1.5,1.8,2.0,2.5,3.0
        )
        // spinner に adapter をセット
        bd.spnMotionY.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, motionYList).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        bd.spnMotionY.setSelection(4)
        // リスナーを登録
        bd.spnMotionY.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                motionYMultiplier = spinnerParent.selectedItem as Double
                setOpglArray()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //********* spnRadiusスピナーの設定（ドットが大きくなる速さ) *********************
        val radiusList = listOf(
            0.1,0.2,0.3,0.4,0.5,0.6,0.7
        )
        // spinner に adapter をセット
        bd.spnRadius.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, radiusList).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        bd.spnRadius.setSelection(4)
        // リスナーを登録
        bd.spnRadius.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                radiusMultiplier = spinnerParent.selectedItem as Double
                setOpglArray()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //********* spnGradationスピナーの設定（ドットが大きくなる速さ) *********************
        val gradationList = listOf(
            0.1,0.2,0.3,0.4,0.5,0.6,0.7,1.0,1.5,2.0
        )
        // spinner に adapter をセット
        bd.spnGradation.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, gradationList).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        bd.spnGradation.setSelection(4)
        // リスナーを登録
        bd.spnGradation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                gradationMultiplier = spinnerParent.selectedItem as Double
                setOpglArray()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
    //********************** onCreate終了 ***********************************************
    private fun setOpglArray () {
        //論理的頂点座標の作成。
        //実際にはBEAT,TEMPO変更時に呼び出すこととなる。

        //opgl変数はレンダーと変数を共有しているため、vertex等を書き換える前にopglOneBarDotsを
        //変更すると、配列数の不整合が生じOut of Boundsエラーとなる。
        //したがって描画を新しいリズム・テンポで作成し関連するopgl変数を変更してから
        //opglOneBarDotsを更新する。
        opglOneBarDots = 1
        val lpArray = LogicalPosArray(rhythm,tempo,motionYMultiplier)
        //LogicalPositionはlogicalXとlogicalYの２つの戻り値を返すためPairでまとめられている。
        opglLogicalX = lpArray.setLogicalPosArray(bmpBeat).first.toMutableList()
        opglLogicalY = lpArray.setLogicalPosArray(bmpBeat).second.toMutableList()
        val animArray = AnimationArray(rhythm,tempo,radiusMultiplier,gradationMultiplier)
        opglAnimRadius = animArray.radiusArray().toMutableList()
        opglAnimGradation = animArray.gradationArray().toMutableList()
        opglOneBarDots = ut.oneBarDots(tempo,rhythm)
    }

    companion object {
        private val ut = Util()
        private const val tagMsg = "My_MainActivity"
        private val tempo2list = listOf(
            30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 63, 66, 69, 72,
            76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 126, 132, 138, 144, 152
        )
        //ディスプレイサイズ
        var surfaceWidth = 0
        var surfaceHeight = 0
        //ビットマップサイズ
        var bitmapX =0
        var bitmapY =0
        //描画中かどうか（
        var isStarted = false
        //*************** OpenGlへ渡すための変数 *********************************************
        var opglOneBarDots = 0
        //配列（ミュータブルリストとすることでobject（他モジュールから参照可能）でありながら可変とすることができる。
        var opglLogicalX: MutableList<Int> = mutableListOf()
        var opglLogicalY: MutableList<Int> = mutableListOf()
        var opglAnimRadius: MutableList<Float> = mutableListOf()
        var opglAnimGradation: MutableList<Float> = mutableListOf()

        //*************** 拡張関数 *****************************************************
        fun Int.mPow (multiplier :Double = 2.0) = this.toDouble().pow(multiplier)
    }
}
