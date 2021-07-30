package music.elsystem.myconductor

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import music.elsystem.myconductor.Common.bitmapX
import music.elsystem.myconductor.Common.bitmapY
import music.elsystem.myconductor.Common.gradationMultiplier
import music.elsystem.myconductor.Common.justTappedSw
import music.elsystem.myconductor.Common.lstResIdOnbeatAll
import music.elsystem.myconductor.Common.lstSpOnbeat
import music.elsystem.myconductor.Common.machineTimelag
import music.elsystem.myconductor.Common.motionYMultiplier
import music.elsystem.myconductor.Common.offBeatNum
import music.elsystem.myconductor.Common.radiusMultiplier
import music.elsystem.myconductor.Common.rhythm
import music.elsystem.myconductor.Common.spOffbeatVoice
import music.elsystem.myconductor.Common.spOffbeatVoice2
import music.elsystem.myconductor.Common.surfaceHeight
import music.elsystem.myconductor.Common.surfaceWidth
import music.elsystem.myconductor.Common.tempo
import music.elsystem.myconductor.GraphicValue.dotSize
import music.elsystem.myconductor.GraphicValue.halfBeatFrame
import music.elsystem.myconductor.GraphicValue.numberBitmapList
import music.elsystem.myconductor.GraphicValue.numberPosXList
import music.elsystem.myconductor.GraphicValue.oneBarFrame
import music.elsystem.myconductor.GraphicValue.oneBeatFrame
import music.elsystem.myconductor.databinding.ActivityMainBinding
import music.elsystem.myconductor.sound.Sound
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val bd by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val ut = Util()
    var bmpBeat: Bitmap? = null

    //描画中かどうか（
    var isStarted = false
    private val numberBitmapListAll: MutableList<Bitmap> = mutableListOf()
    private var tempoLabel = ""

    //テンポのタップ指定に使用する。
    private val handler: Handler = Handler(Looper.getMainLooper())

    //メトロノーム本体オブジェクト
    private val sound = Sound()
    var voice = "Voice"

    // Dispatcher.Main を指定しているためこのスコープで起動するコルーチンはメインスレッドで動作する。
    // Job として上で定義した job を渡しているので、すべてのコルーチンはこの job の子になる
    private val job by lazy { Job() }
    override var coroutineContext: CoroutineContext = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bd.root)
        // "MyConductorPrefData"という名前でインスタンスを生成。書き込み先のデータ名称となる。
        val myConductorPrefData: SharedPreferences =
            getSharedPreferences("MyConductorPrefData", Context.MODE_PRIVATE)
        val editor = myConductorPrefData.edit()
        bd.lMeasureSpeed.visibility = View.INVISIBLE
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
        loadBitmapNumber()
        //サウンドファイルを読み込みサウンドプールにセットする。
        loadSound(voice)
        sound.setSoundPool(this)
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
                //メトロノームを開始する。
                //再生中はスリープ状態にならないように設定する。
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//                job.start()
                launch(context = Dispatchers.Default) {
                    sound.start()
                }
                justTappedSw = true
                isStarted = true
            } else {
                Log.i("ストップタップ", "${numberPosXList}")
                //ライン描画のサーフェスビューを表示する。
                bd.layoutGlSurfaceView.removeView(glSurfaceview)
                setOpglLineArray()
                bd.layoutGlSurfaceView.addView(lineSurfaceview)
                //メトロノームを停止する。
                job.cancelChildren()
                isStarted = false
            }
        }
        //初回起動時はグラフィックとサウンドの同期のため測定を行う。
        if (!myConductorPrefData.getBoolean("TimelagSetSw",false)) {
            Log.i("prefTimestamp:Main", "初期測定開始")
            launch(context = Dispatchers.Default) {
                bd.lMeasureSpeed.visibility = View.VISIBLE
                sound.adjustStart()
                bd.lMeasureSpeed.visibility = View.INVISIBLE
                editor.putBoolean("TimelagSetSw",true)
                editor.putFloat("MachineTimelag",machineTimelag)
                editor.apply()
            }
        }
        //音色　ラジオボタン
        bd.rgSound.check(bd.rbVoice.id)
        bd.rgSound.setOnCheckedChangeListener{ _, checkedId ->
            // checkedIdから、選択されたRadioButtonを取得
            when (checkedId) {
                bd.rbVoice.id -> {
                    voice = "Voice"
                }
                bd.rbClick.id -> {
                    voice = "Click"
                }
            }
            loadSound(voice)
            Log.i("私は", "rgSound")
            sound.setSoundPool(this)
        }
        //裏拍の刻み　NONE or２ or ３
        bd.rgOffBeatNum.check(bd.rbNone.id)
        bd.rgOffBeatNum.setOnCheckedChangeListener{ _, checkedId ->
            when (checkedId) {
                bd.rbNone.id -> offBeatNum = 1
                bd.rbTwo.id -> offBeatNum = 2
                bd.rbThree.id -> offBeatNum = 3
            }
        }

        //********* spnTempoLabelスピナーの設定（moderate,Allegro etc... )*********************
        val tempoLabellist = listOf(
            "Grave", "Largo", "Lento", "Adagio", "Adagietto", "Andante",
            "Moderate", "Allegretto", "Allegro"
        )
        val adapter1 =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, tempoLabellist).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        // spnTempoLabel に adapter をセット
        bd.spnTempoLabel.adapter = adapter1
        bd.spnTempoLabel.setSelection(3)   //初期値をAndanteとする。
        // リスナーを登録
        bd.spnTempoLabel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val spinnerParent = parent as Spinner
                    tempoLabel = spinnerParent.selectedItem as String
                    bd.tvTempo.text = prepareTempo(tempoLabel).toString()
                    tempo = prepareTempo(tempoLabel)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        //********* btn < *********************************************************
        bd.btnTempoMm.setOnClickListener{
            ut.changeTempo(
                tempo - tempo / 25,
                bd.tvTempo,
                bd.spnTempoLabel
            )
        }
        bd.btnTempoM1.setOnClickListener{
            ut.changeTempo(
                tempo - 1,
                bd.tvTempo,
                bd.spnTempoLabel
            )
        }
        bd.btnTempoP1.setOnClickListener{
            ut.changeTempo(
                tempo + 1,
                bd.tvTempo,
                bd.spnTempoLabel
            )
        }
        bd.btnTempoPp.setOnClickListener{
            ut.changeTempo(
                tempo + tempo / 25,
                bd.tvTempo,
                bd.spnTempoLabel
            )
        }

        //********* Tapボタン ******************************************************
        val tapListener = TapOnClickListener(handler, bd.tvTempo, bd.spnTempoLabel)
        bd.btnTap.setOnClickListener(tapListener)
        //********* spnRhythmスピナーの設定（2beat,4beat etc...) *********************
        val rhythmList = listOf(
            "2 beat", "3 beat", "4 beat"
        )
        // spinner に adapter をセット
        bd.spnBeat.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, rhythmList).also{
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        bd.spnBeat.setSelection(2)
        // リスナーを登録
        bd.spnBeat.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
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
                            bmpBeat =
                                BitmapFactory.decodeResource(resources, R.drawable.beat_2, options)

                        }
                        "3 beat" -> {
                            rhythm = 3
                            bmpBeat =
                                BitmapFactory.decodeResource(resources, R.drawable.beat_3, options)
                        }
                        "4 beat" -> {
                            rhythm = 4
                            bmpBeat =
                                BitmapFactory.decodeResource(resources, R.drawable.beat_4, options)
                        }
                    }
                    bitmapX = bmpBeat?.let { it.width } ?: 0
                    bitmapY = bmpBeat?.let { it.height } ?: 0
                    //描画に使用される数字のビットマップリストを生成する。
                    setNumberList()
                    setOpglLineArray()
//                    loadSound(voice)
                    Log.i("私は", "spnBeat")
//                    sound.setSoundPool(context = this@MainActivity)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        //********* spnMotionYスピナーの設定（タクトの跳ね具合) *********************
        val motionYList = listOf(
            0.5, 0.8, 1.0, 1.2, 1.5, 1.8, 2.0, 2.5, 3.0
        )
        // spinner に adapter をセット
        bd.spnMotionY.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, motionYList).also{
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        bd.spnMotionY.setSelection(4)
        // リスナーを登録
        bd.spnMotionY.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
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
        val dotSizeList = listOf(0.6f, 0.8f, 1.0f, 1.2f, 1.5f, 1.8f)
        // spinner に adapter をセット
        bd.spnDotSize.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, dotSizeList).also{
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        bd.spnDotSize.setSelection(2)
        // リスナーを登録
        bd.spnDotSize.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
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
            0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7
        )
        // spinner に adapter をセット
        bd.spnRadius.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, radiusList).also{
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        bd.spnRadius.setSelection(4)
        // リスナーを登録
        bd.spnRadius.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
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
            0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 1.0, 1.5, 2.0
        )
        // spinner に adapter をセット
        bd.spnGradation.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, gradationList).also{
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        bd.spnGradation.setSelection(4)
        // リスナーを登録
        bd.spnGradation.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
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
    override fun onResume() {
        super.onResume()
//        Log.i("私は","onResume")
//        sound.setSoundPool(this)
    }

    override fun onPause() {
        super.onPause()
        job.cancelChildren()
        sound.soundPool?.release()
    }

    private fun setOpglArray() {
        //論理的頂点座標の作成。
        //実際にはBEAT,TEMPO変更時に呼び出すこととなる。
        //opgl変数はレンダーと変数を共有しているため、vertex等を書き換える前にopglOneBarDotsを
        //変更すると、配列数の不整合が生じOut of Boundsエラーとなる。
        //したがって描画を新しいリズム・テンポで作成し関連するopgl変数を変更してから
        //opglOneBarDotsを更新する。（配列をオブジェクトしてから、上記対応をとったにもかかわらず
        //Out of Boundsが新たに発生）
        Log.i("Animation描画用配列作成開始！", "tempo: $tempo, rhythm: $rhythm")
        oneBarFrame = 1
        val lpArray = LogicalPosArray(rhythm, tempo, motionYMultiplier)
        val animArray = AnimationArray(rhythm, tempo, radiusMultiplier, gradationMultiplier)
        if (lpArray.setLogicalPosArray(bmpBeat)
            && animArray.radiusArray()
            && animArray.gradationArray()
        ) {
            halfBeatFrame = ut.halfBeatFrame(tempo)
            oneBeatFrame = halfBeatFrame * 2
            oneBarFrame = ut.oneBarFrame(tempo, rhythm)
        }
    }

    private fun setOpglLineArray() {
        //停止中のラインの頂点を作成する。
        val tempoAtLineDraw = 20
        Log.i("Line描画用配列作成開始！", "tempo: $tempoAtLineDraw, rhythm: $rhythm")
        oneBarFrame = 1
        val lpArray = LogicalPosArray(rhythm, tempoAtLineDraw, 1.0)
        val animArray =
            AnimationArray(rhythm, tempoAtLineDraw, radiusMultiplier, gradationMultiplier)
        if (lpArray.setLogicalPosArray(bmpBeat)
            && animArray.radiusArray()
            && animArray.gradationArray()
        ) {
            oneBarFrame = ut.oneBarFrame(tempoAtLineDraw, rhythm)
        }
    }

    fun loadBitmapNumber() {
        //描画に使用される数字のビットマップすべてをリスト読み込む。
        numberBitmapListAll.add(BitmapFactory.decodeResource(resources, R.drawable.number_1))
        numberBitmapListAll.add(BitmapFactory.decodeResource(resources, R.drawable.number_2))
        numberBitmapListAll.add(BitmapFactory.decodeResource(resources, R.drawable.number_3))
        numberBitmapListAll.add(BitmapFactory.decodeResource(resources, R.drawable.number_4))
        numberBitmapListAll.add(BitmapFactory.decodeResource(resources, R.drawable.number_5))
        numberBitmapListAll.add(BitmapFactory.decodeResource(resources, R.drawable.number_6))
        numberBitmapListAll.add(BitmapFactory.decodeResource(resources, R.drawable.number_7))
        numberBitmapListAll.add(BitmapFactory.decodeResource(resources, R.drawable.number_8))
    }

    //選択されたrhythmに応じて、実際に描画に使用されるビットマップリストを作成する。
//４拍子であれば4[0],1[1],2[2],3[3]の様になる。
    fun setNumberList() {
        numberBitmapList = mutableListOf()
        var j: Int
        for (i in 0 until rhythm) {
            j = (((rhythm - 1) + i) % rhythm)
            numberBitmapList.add(numberBitmapListAll[j])
        }
    }

    //音声ファイルを準備する。
    fun loadSound(voice: String) {
        //選択されたVoiceに対する音声ファイルを読み込む。
        lstResIdOnbeatAll = mutableListOf()
        when (voice) {
            "Voice" -> {
                lstResIdOnbeatAll.add(R.raw.one)
                lstResIdOnbeatAll.add(R.raw.two)
                lstResIdOnbeatAll.add(R.raw.three)
                lstResIdOnbeatAll.add(R.raw.four)
                lstResIdOnbeatAll.add(R.raw.five)
                lstResIdOnbeatAll.add(R.raw.six)
                lstResIdOnbeatAll.add(R.raw.seven)
                spOffbeatVoice = R.raw.and
                spOffbeatVoice2 = R.raw.and
            }
            "Click" -> {
                lstResIdOnbeatAll.add(R.raw.piin)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                spOffbeatVoice = R.raw.kattu
                spOffbeatVoice2 = R.raw.kattu
            }
        }
        //選択されたrhythmに応じて、実際に使用される表拍の音声リストを作成する。
        //４拍子であればfour[0],one[1],two[2],three[3]の様になる。
        lstSpOnbeat = mutableListOf()
        var j: Int
        for (i in 0 until rhythm) {
            j = (((rhythm - 1) + i) % rhythm)
            lstSpOnbeat.add(lstResIdOnbeatAll[j])
        }
    }

    fun prepareTempo(tempoLabel: String): Int {
        return when (tempoLabel) {
            "Grave" -> 40
            "Largo" -> 46
            "Lento" -> 52
            "Adagio" -> 56
            "Adagietto" -> 63
            "Andante" -> 72
            "Moderate" -> 90
            "Allegretto" -> 108
            "Allegro" -> 132
            else -> 60
        }
    }

    companion object {
        private const val tagMsg = "My_MainActivity"

        //*************** 拡張関数 *****************************************************
        fun Int.mPow(multiplier: Double = 2.0) = this.toDouble().pow(multiplier)
    }
}
