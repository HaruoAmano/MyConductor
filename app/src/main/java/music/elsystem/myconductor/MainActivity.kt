package music.elsystem.myconductor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import music.elsystem.myconductor.Common.RenderMode.*
import music.elsystem.myconductor.Common.SoundName.*
import music.elsystem.myconductor.Common.justTappedSw
import music.elsystem.myconductor.Common.justTappedSoundSw
import music.elsystem.myconductor.Common.noteNumPerBeat
import music.elsystem.myconductor.Common.surfaceHeight
import music.elsystem.myconductor.Common.surfaceWidth
import music.elsystem.myconductor.Common.Tact.*
import music.elsystem.myconductor.Common.bmpBeat
import music.elsystem.myconductor.Common.lstSpOnbeat
import music.elsystem.myconductor.Common.renderMode
import music.elsystem.myconductor.Common.rhythm
import music.elsystem.myconductor.Common.rhythmVariation
import music.elsystem.myconductor.Common.soundPool
import music.elsystem.myconductor.Common.spOffbeatVoice
import music.elsystem.myconductor.Common.spOffbeatVoice2
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.Common.tempo
import music.elsystem.myconductor.Common.voice
import music.elsystem.myconductor.GraphicValue.numberBitmapList
import music.elsystem.myconductor.databinding.ActivityMainBinding
import music.elsystem.myconductor.settings.HeavySettingActivity
import music.elsystem.myconductor.settings.LightSettingActivity
import music.elsystem.myconductor.surfaceview.dotSurfaceview.GlSurfaceView
import music.elsystem.myconductor.surfaceview.lineSureface.LineSurfaceView

class MainActivity : AppCompatActivity() {
    private lateinit var bd: ActivityMainBinding

    //描画中かどうか
    var isStarted = false

    //ナンバー読み込み用ビットマップリスト
    private val numberBitmapListAll: MutableList<Bitmap> = mutableListOf()

    //サーフェスビューの定義
    var lineSurfaceview: LineSurfaceView? = null
    var glSurfaceview: GlSurfaceView? = null

    //LogicalPosArrayをLine用に使用するかどうか。（ちゃんと設計すれば、この変数はいらないはず！）
    var line = true

    //テンポのタップ指定に使用する。
    private val handler: Handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bd = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bd.root)
        // "MyConductorPrefData"という名前でインスタンスを生成。書き込み先のデータ名称となる。
        val myConductorPrefData: SharedPreferences =
            getSharedPreferences("MyConductorPrefData", Context.MODE_PRIVATE)
        val editor = myConductorPrefData.edit()
        val ut = Util()
        //サーフェスビューの親ビューであるLinearLayoutより描画サイズを取得する。
        val observerSurfaceViewLayout: ViewTreeObserver = bd.layoutGlSurfaceView.viewTreeObserver
        observerSurfaceViewLayout.addOnGlobalLayoutListener {
            surfaceWidth = bd.layoutGlSurfaceView.width
            surfaceHeight = bd.layoutGlSurfaceView.height
        }
        //ヌルポを避けるため、とりあえずビットマップを設定する。
        //元画像のサイズを保持するようオプションで指定する。
        val options = BitmapFactory.Options()
        options.inScaled = false
        bmpBeat = BitmapFactory.decodeResource(resources, R.drawable.beat_4, options)
        //リズムに応じたナンバービットマップをセットする。
        loadBitmapNumber()
        //サウンドファイルを読み込みサウンドプールにセットする。
        setSoundList(voice, rhythm)
        setSoundPool(applicationContext, rhythm)
        //サーフェスビューの初期値としてラインを描画する。
        //モーションの描画か、ラインの描画か、セッティングの描画かを指定する。
        renderMode = Line.name
        lineSurfaceview = LineSurfaceView(this)
        bd.layoutGlSurfaceView.addView(lineSurfaceview)
        //タクトタイプの初期値はLight
        bd.btnHeavy.setBackgroundColor(Color.parseColor(COLOR_BUTTON_PALE))
        //タクト****************************************************************
        //Heavy
        bd.btnHeavy.setOnClickListener {
            tactType = Heavy.name
            bd.btnHeavy.setBackgroundColor(Color.parseColor(COLOR_BUTTON))
            bd.btnLight.setBackgroundColor(Color.parseColor(COLOR_BUTTON_PALE))
            bd.rgNoteNumPerOnebeat.visibility = View.VISIBLE
            if (isStarted) {
                updateGlSurface()
            }
        }
        //Heavy 詳細画面
        bd.btnHeavy.setOnLongClickListener {
            if (!isStarted) {
                tactType = Heavy.name
                bd.btnHeavy.setBackgroundColor(Color.parseColor(COLOR_BUTTON))
                bd.btnLight.setBackgroundColor(Color.parseColor(COLOR_BUTTON_PALE))
                bd.rgNoteNumPerOnebeat.visibility = View.VISIBLE
                val heavySettingIntent =
                    Intent(applicationContext, HeavySettingActivity::class.java)
                startActivityForResult(heavySettingIntent, 200)
            }
            true
        }
        //Light
        bd.btnLight.setOnClickListener {
            tactType = Light.name
            bd.btnHeavy.setBackgroundColor(Color.parseColor(COLOR_BUTTON_PALE))
            bd.btnLight.setBackgroundColor(Color.parseColor(COLOR_BUTTON))
            bd.rgNoteNumPerOnebeat.visibility = View.VISIBLE
            if (isStarted) {
                updateGlSurface()
            }
        }
        //Light詳細画面
        bd.btnLight.setOnLongClickListener {
            if (!isStarted) {
                tactType = Light.name
                bd.btnHeavy.setBackgroundColor(Color.parseColor(COLOR_BUTTON_PALE))
                bd.btnLight.setBackgroundColor(Color.parseColor(COLOR_BUTTON))
                bd.rgNoteNumPerOnebeat.visibility = View.VISIBLE
                val lightSettingIntent =
                    Intent(applicationContext, LightSettingActivity::class.java)
                startActivityForResult(lightSettingIntent, 200)
            }
            true
        }
        //サウンド　ラジオボタン*************************************************
        bd.rgSound.check(bd.rbVoice.id)
        bd.rgSound.setOnCheckedChangeListener { _, checkedId ->
            // checkedIdから、選択されたRadioButtonを取得
            when (checkedId) {
                bd.rbVoice.id -> {
                    voice = Voice.name
                }
                bd.rbClick.id -> {
                    voice = Click.name
                }
            }
            //メトロノーム音声
            setSoundList(voice, rhythm)
            setSoundPool(applicationContext, rhythm)
            if (isStarted) {
                updateGlSurface()
            }
        }
        //裏拍の刻み　NONE or２（裏） or ３（３連） or ４（裏裏）
        noteNumPerBeatUnable()
        bd.rgNoteNumPerOnebeat.check(bd.rbQuarterNote.id)
        bd.rbQuarterNote.setBackgroundResource(R.drawable.quarter_note_on)
        bd.rgNoteNumPerOnebeat.setOnCheckedChangeListener { _, checkedId ->
            noteNumPerBeatUnable()
            when (checkedId) {
                bd.rbQuarterNote.id -> {
                    noteNumPerBeat = 1
                    bd.rbQuarterNote.setBackgroundResource(R.drawable.quarter_note_on)
                }
                bd.rbEighthNote.id -> {
                    noteNumPerBeat = 2
                    bd.rbEighthNote.setBackgroundResource(R.drawable.eighth_note_on)
                }
                bd.rbTripletNote.id -> {
                    if (bd.tvTempo.text.toString().toInt() <= 60) {
                        noteNumPerBeat = 3
                        bd.rbTripletNote.setBackgroundResource(R.drawable.triplet_note_on)
                    }else{
                        Toast.makeText(this, "Can only be used, tempo is below 60", Toast.LENGTH_SHORT).show()
                        bd.rgNoteNumPerOnebeat.check(bd.rbQuarterNote.id)
                    }
                }
                bd.rbSixteenthNote.id -> {
                    if (bd.tvTempo.text.toString().toInt() <= 40) {
                        noteNumPerBeat = 4
                        bd.rbSixteenthNote.setBackgroundResource(R.drawable.sixteenth_note_on)
                    }else {
                        Toast.makeText(this, "Can only be used, tempo is below 40.", Toast.LENGTH_SHORT).show()
                        bd.rgNoteNumPerOnebeat.check(bd.rbEighthNote.id)
                    }
                }
            }
            if (isStarted) {
                updateGlSurface()
            }
        }
        //********* btn < >*********************************************************
        bd.btnTempoMinus.setOnClickListener {
            if (tactType == Swing.name) {
                val presentIndex = swingTempoTable.indexOf(tempo)
                if (presentIndex > 0) {
                    tempo = swingTempoTable[presentIndex - 1]
                }
            } else {
                val presentIndex = tempoTable.indexOf(tempo)
                if (presentIndex > 0) {
                    tempo = tempoTable[presentIndex - 1]
                }
            }
            ut.tempoChanged(
                tempo,
                bd.tvTempo,
                bd.tvTempoSign
            )
        }
        bd.btnTempoPlus.setOnClickListener {
            if (tactType == Swing.name) {
                val presentIndex = swingTempoTable.indexOf(tempo)
                if (presentIndex < swingTempoTable.lastIndex) {
                    tempo = swingTempoTable[presentIndex + 1]
                }
            } else {
                val presentIndex = tempoTable.indexOf(tempo)
                if (presentIndex < tempoTable.lastIndex) {
                    tempo = tempoTable[presentIndex + 1]
                }
            }
            ut.tempoChanged(
                tempo,
                bd.tvTempo,
                bd.tvTempoSign
            )
        }
        //*************************************************************************
        bd.tvTempo.setText(tempo.toString())
        bd.tvTempo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (bd.tvTempo.text.toString().toInt() > 60) {
                    if (bd.rbTripletNote.isChecked) {
                        bd.rgNoteNumPerOnebeat.check(bd.rbQuarterNote.id)
                    }
                }
                if (bd.tvTempo.text.toString().toInt() > 40) {
                    if (bd.rbSixteenthNote.isChecked) {
                        bd.rgNoteNumPerOnebeat.check(bd.rbEighthNote.id)
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        //********* Tapボタン ******************************************************
        val tapListener = TapOnClickListener(handler, bd.tvTempo, bd.tvTempoSign)
        bd.btnTap.setOnClickListener(tapListener)
        //********* spnRhythmスピナーの設定（2beat,4beat etc...) *********************
        val rhythmList = listOf(
            "2beat", "3beat", "4beat", "6beat"
        )
        // spinner に adapter をセット
        bd.spnBeat.adapter =
            ArrayAdapter(applicationContext, R.layout.custom_spinner, rhythmList).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        bd.spnBeat.setSelection(2)
        // リスナーを登録
        bd.spnBeat.onItemSelectedListener =
                //@@@@@@@@@クラスファイル化する！！！！！！！！！！！！！！！！！！！！！！！！
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
                        "2beat" -> {
                            rhythm = 2
                            rhythmVariation = 0
                            bmpBeat =
                                BitmapFactory.decodeResource(resources, R.drawable.beat_2, options)

                        }
                        "3beat" -> {
                            rhythm = 3
                            rhythmVariation = 0
                            bmpBeat =
                                BitmapFactory.decodeResource(resources, R.drawable.beat_3, options)
                        }
                        "4beat" -> {
                            rhythm = 4
                            rhythmVariation = 0
                            bmpBeat =
                                BitmapFactory.decodeResource(resources, R.drawable.beat_4, options)
                        }
                        "6beat" -> {
                            rhythm = 6
                            rhythmVariation = 0
                            bmpBeat =
                                BitmapFactory.decodeResource(resources, R.drawable.beat_6, options)
                        }
                    }
                    //拍子変更に関わる以下の項目を設定する。
                    if (isStarted) {
                        updateGlSurface()
                    } else {
                        bd.layoutGlSurfaceView.removeAllViews()
                        renderMode = Line.name
                        lineSurfaceview = LineSurfaceView(this@MainActivity)
                        bd.layoutGlSurfaceView.addView(lineSurfaceview)
                    }
                    //描画に使用される数字のビットマップリスト。
                    setNumberList()
                    //メトロノーム音声
                    setSoundList(voice, rhythm)
                    setSoundPool(applicationContext, rhythm)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        //********* サーフェスビュークリック時の設定*****************************
        //addView時はテンポ変更等を反映させるため必ずSurfaceViewの更新を行う。
        bd.layoutGlSurfaceView.setOnClickListener {
            //アニメーション描画を開始する。
            if (!isStarted) {
                //サウンドの再生はGlRendererから実行される。
                Log.i("@@@@", "${Motion.name}")
                bd.layoutGlSurfaceView.removeAllViews()
                renderMode = Motion.name
                glSurfaceview = GlSurfaceView(this)
                bd.layoutGlSurfaceView.addView(glSurfaceview)
                //再生中はスリープ状態にならないように設定する。
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                isStarted = true
                justTappedSw = true
                justTappedSoundSw = true
                bd.tvViewCap.text = getString(R.string.tapToStop)
                bd.btnTempoMinus.isEnabled = false
                bd.btnTempoMinus.setBackgroundColor(Color.GRAY)
                bd.tvTempo.isEnabled = false
                bd.btnTempoPlus.isEnabled = false
                bd.btnTempoPlus.setBackgroundColor(Color.GRAY)
                bd.spnBeat.isEnabled = false
                bd.spnBeat.setBackgroundColor(Color.GRAY)
                bd.btnTap.isEnabled = false
                bd.btnTap.setBackgroundColor(Color.GRAY)
                bd.tvTactCap.visibility = View.INVISIBLE
            } else {
                //ライン描画のサーフェスビューを表示する。
                bd.layoutGlSurfaceView.removeAllViews()
                renderMode = Line.name
                lineSurfaceview = LineSurfaceView(this)
                bd.layoutGlSurfaceView.addView(lineSurfaceview)
                //メトロノームを停止する。
                isStarted = false
                bd.tvViewCap.text = getString(R.string.tapToStart)
                bd.btnTempoMinus.isEnabled = true
                bd.btnTempoMinus.setBackgroundColor(Color.parseColor(COLOR_BUTTON))
                bd.tvTempo.isEnabled = true
                bd.btnTempoPlus.isEnabled = true
                bd.btnTempoPlus.setBackgroundColor(Color.parseColor(COLOR_BUTTON))
                bd.spnBeat.isEnabled = true
                bd.spnBeat.setBackgroundColor(Color.parseColor(COLOR_BUTTON))
                bd.btnTap.isEnabled = true
                bd.btnTap.setBackgroundColor(Color.parseColor(COLOR_BUTTON))
                bd.tvTactCap.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                bd.tvTempo.setText(tempo.toString())
                bd.layoutGlSurfaceView.removeAllViews()
                renderMode = Line.name
                lineSurfaceview = LineSurfaceView(this)
                bd.layoutGlSurfaceView.addView(lineSurfaceview)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //システムバーの消去
        //window.decorView.systemUiVisibilityはアンドロイド11では非推奨。
        // WindowInsetsControllerが推奨されるが現時点ではこのままとする。
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    private fun updateGlSurface() {
        bd.layoutGlSurfaceView.removeAllViews()
        renderMode = Motion.name
        glSurfaceview = GlSurfaceView(this)
        bd.layoutGlSurfaceView.addView(glSurfaceview)
        justTappedSw = true
        justTappedSoundSw = true
        isStarted = true
    }

    //描画に使用される数字のビットマップすべてをリスト読み込む。
    fun loadBitmapNumber() {
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
    private fun noteNumPerBeatUnable(){
        bd.rbQuarterNote.setBackgroundResource(R.drawable.quarter_note_off)
        bd.rbEighthNote.setBackgroundResource(R.drawable.eighth_note_off)
        bd.rbTripletNote.setBackgroundResource(R.drawable.triplet_note_off)
        bd.rbSixteenthNote.setBackgroundResource(R.drawable.sixteenth_note_off)
    }

    //音声ファイルを準備する。
    private fun setSoundList(voice: String, rhythm: Int) {
        //選択されたVoiceに対する音声ファイルを読み込む。
        val lstResIdOnbeatAll: MutableList<Int> = mutableListOf()
        when (voice) {
            Voice.name -> {
                lstResIdOnbeatAll.add(R.raw.one)
                lstResIdOnbeatAll.add(R.raw.two)
                lstResIdOnbeatAll.add(R.raw.three)
                lstResIdOnbeatAll.add(R.raw.four)
                lstResIdOnbeatAll.add(R.raw.five)
                lstResIdOnbeatAll.add(R.raw.six)
                lstResIdOnbeatAll.add(R.raw.seven)
                spOffbeatVoice = R.raw.and
                spOffbeatVoice2 = R.raw.wood_weak
            }
            Click.name -> {
                lstResIdOnbeatAll.add(R.raw.piin)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                spOffbeatVoice = R.raw.wood
                spOffbeatVoice2 = R.raw.wood_weak
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

    //setSoundListで用意されたサウンドリスト一拍目とするサウンドプールを生成する。
    private fun setSoundPool(context: Context, rhythm: Int) {
        SoundPool.Builder().run {
            val audioAttributes = AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            }
            setMaxStreams(4)
            setAudioAttributes(audioAttributes)
            build()
        }.also { soundPool = it }
        //表拍
        for (i in 0 until rhythm) {
            soundPool?.load(context, lstSpOnbeat[i], 1)?.let { lstSpOnbeat[i] = it }
        }
        //裏拍
        soundPool?.load(
            context,
            spOffbeatVoice, 1
        )?.let { spOffbeatVoice = it }
        //裏裏拍
        soundPool?.load(
            context,
            spOffbeatVoice2, 1
        )?.let { spOffbeatVoice2 = it }
    }

    companion object {
        val tempoTable = listOf(
            20, 21, 22, 23, 24, 25, 26, 28, 30, 32, 34,
            36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 63, 66, 69, 72, 76, 80,
            84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 126, 132, 138, 144, 152
        )
        val swingTempoTable = listOf(
            40, 42, 44, 46, 48, 50, 53, 57, 60, 64, 68, 72, 76, 82, 88, 94, 102, 114, 124, 136, 152
        )
        val COLOR_BUTTON = "#194B4B"
        val COLOR_BUTTON_PALE = "#5B7171"
    }
}
