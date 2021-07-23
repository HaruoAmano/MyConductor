package music.elsystem.myconductor

import android.content.res.Resources
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
import music.elsystem.myconductor.Common.bitmapX
import music.elsystem.myconductor.Common.bitmapY
import music.elsystem.myconductor.Common.gradationMultiplier
import music.elsystem.myconductor.Common.motionYMultiplier
import music.elsystem.myconductor.Common.radiusMultiplier
import music.elsystem.myconductor.Common.rhythm
import music.elsystem.myconductor.Common.surfaceHeight
import music.elsystem.myconductor.Common.surfaceWidth
import music.elsystem.myconductor.Common.tempo
import music.elsystem.myconductor.GraphicValue.dotSize
import music.elsystem.myconductor.GraphicValue.numberBitmapList
import music.elsystem.myconductor.GraphicValue.oneBarFrame
import music.elsystem.myconductor.databinding.ActivityMainBinding
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private val bd by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val ut = Util()
    var bmpBeat: Bitmap? = null

    //描画中かどうか（
    var isStarted = false

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
        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.beat_4, options)
        //描画に使用される数字のビットマップリストを生成する。
        setNumberList()
        //********* サーフェスビュークリック時の設定*****************************
        val lineSurfaceview = LineSurfaceView(this)
        val glSurfaceview = GlSurfaceView(this)
        //初期状態はライン描画のサーフェスビューを表示する。
        setOpglLineArray()
        bd.layoutGlSurfaceView.addView(lineSurfaceview)
        bd.layoutGlSurfaceView.setOnClickListener {
            //アニメーション描画を開始する。
            if (!isStarted) {
                bd.layoutGlSurfaceView.removeView(lineSurfaceview)
                setOpglArray()
                bd.layoutGlSurfaceView.addView(glSurfaceview)
                isStarted = true
            } else {
                //ライン描画のサーフェスビューを表示する。
                bd.layoutGlSurfaceView.removeView(glSurfaceview)
                setOpglLineArray()
                bd.layoutGlSurfaceView.addView(lineSurfaceview)
                isStarted = false
            }
        }
        //********* spnTempo1スピナーの設定（moderate,Allegro etc... )*********************
        val tempo1list = listOf(
            "test", "Grave", "Largo", "Adagio", "Andante",
            "Moderate", "Allegretto", "Allegro"
        )
        val adapter1 = ArrayAdapter(applicationContext, R.layout.custom_spinner, tempo1list).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        // spnTempo1 に adapter をセット
        bd.spnTempo1.adapter = adapter1
        bd.spnTempo1.setSelection(3)   //初期値をAndanteとする。
//        bd.spnTempo1.setSelection(0)   //テスト用（デバッグでドットを減らしたい時に使用する。）
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
                val tempo2list = listOf(
                    30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 63, 66, 69, 72,
                    76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 126, 132, 138, 144, 152,
                    200,300, 400,500,600, 1000
                )
                val tempo2 = when (tempo1item) {
                    "test" -> tempo2list.filter { it in 200..1000 } as MutableList<Int>
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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //********* spnRhythmスピナーの設定（2beat,4beat etc...) *********************
        val rhythmList = listOf(
            "2 beat", "3 beat", "4 beat"
        )
        // spinner に adapter をセット
        bd.spnBeat.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, rhythmList).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        bd.spnBeat.setSelection(2)
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
                        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.beat_2, options)

                    }
                    "3 beat" -> {
                        rhythm = 3
                        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.beat_3, options)
                    }
                    "4 beat" -> {
                        rhythm = 4
                        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.beat_4, options)
                    }
                }
                bitmapX = bmpBeat?.let { it.width } ?: 0
                bitmapY = bmpBeat?.let { it.height } ?: 0
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
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //********* spnDotSizeスピナーの設定（球の大きさ) *********************
        val dotSizeList = listOf(0.6f,0.8f,1.0f, 1.2f,1.5f,1.8f)
        // spinner に adapter をセット
        bd.spnDotSize.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, dotSizeList).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        bd.spnDotSize.setSelection(2)
        // リスナーを登録
        bd.spnDotSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                dotSize = spinnerParent.selectedItem as Float
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
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //********* spnGradationスピナーの設定（グラデーション（αの減衰具合）*************
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
        //opglOneBarDotsを更新する。（配列をオブジェクトしてから、上記対応をとったにもかかわらず
        //Out of Boundsが新たに発生）
        Log.i("Animation描画用配列作成開始！","tempo: $tempo, rhythm: $rhythm")
        oneBarFrame = 1
        val lpArray = LogicalPosArray(rhythm,tempo,motionYMultiplier)
        val animArray = AnimationArray(rhythm,tempo,radiusMultiplier,gradationMultiplier)
        if (lpArray.setLogicalPosArray(bmpBeat)
            && animArray.radiusArray()
            && animArray.gradationArray()) {
            oneBarFrame = ut.oneBarDots(tempo, rhythm)
        }
    }
    private fun setOpglLineArray () {
        //停止中のラインの頂点を作成する。
        val tempoAtLineDraw = 20
        Log.i("Line描画用配列作成開始！","tempo: $tempoAtLineDraw, rhythm: $rhythm")
        oneBarFrame = 1
        val lpArray = LogicalPosArray(rhythm, tempoAtLineDraw, 1.0)
        val animArray = AnimationArray(rhythm, tempoAtLineDraw, radiusMultiplier, gradationMultiplier)
        if (lpArray.setLogicalPosArray(bmpBeat)
            && animArray.radiusArray()
            && animArray.gradationArray()) {
            oneBarFrame = ut.oneBarDots(tempoAtLineDraw, rhythm)
        }
    }
    //描画に使用される数字のビットマップリストを生成する。
    fun setNumberList (){
        val resources: Resources = this.resources
        numberBitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.number_1red))
        numberBitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.number_2))
        numberBitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.number_3))
        numberBitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.number_4))
        numberBitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.number_5))
        numberBitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.number_6))
        numberBitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.number_7))
        numberBitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.number_8))
    }
    companion object {
        private const val tagMsg = "My_MainActivity"
        //*************** 拡張関数 *****************************************************
        fun Int.mPow (multiplier :Double = 2.0) = this.toDouble().pow(multiplier)
    }
}
