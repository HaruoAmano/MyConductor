package music.elsystem.myconductor.surfaceview.dotSurfaceview

import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import music.elsystem.myconductor.Common.RenderMode.*
import music.elsystem.myconductor.Common.Tact.*
import music.elsystem.myconductor.Common.alphaMultiplier
import music.elsystem.myconductor.Common.dotSize
import music.elsystem.myconductor.Common.flashSW
import music.elsystem.myconductor.Common.justTappedSw
import music.elsystem.myconductor.Common.motionYMultiplier
import music.elsystem.myconductor.Common.offbeatDotSizeHeavy
import music.elsystem.myconductor.Common.offbeatDotSizeSwing
import music.elsystem.myconductor.Common.perOfHalfBeatSwing
import music.elsystem.myconductor.Common.radiusMultiplier
import music.elsystem.myconductor.Common.renderMode
import music.elsystem.myconductor.Common.rhythm
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.Common.tempo
import music.elsystem.myconductor.Util
import music.elsystem.myconductor.surfaceview.LogicalPosList
import music.elsystem.myconductor.surfaceview.Shader.dotFagmentSource
import music.elsystem.myconductor.surfaceview.Shader.dotVertexSource
import music.elsystem.myconductor.surfaceview.Shader.numberFragmentSource
import music.elsystem.myconductor.surfaceview.Shader.numberVertexSource
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow

//セッティングモードの時はナンバー描画ロジックは回避している。
class GlRenderer() : GLSurfaceView.Renderer, CoroutineScope by MainScope() {
    //シェーダプログラムID
    private var dotProgramId = 0
    private var numberProgramId = 0
    private var soundJob: Job? = null
    val scope = CoroutineScope(Dispatchers.IO + Job())

    //ヴュー＆プロジェクションマッピング行列
    private val mViewAndProjectionMatrix = FloatArray(16)

    //ドット描画クラス
    private val circle = Circle()

    //打点ナンバー描画クラス
    private val numDraw = NumDraw()

    //打点ナンバーのテクスチャ（ピクチャ）数
    private val textureArrayNum = IntArray(rhythm)

    //サウンドクラス
    private var sound = Sound()

    //onDrawFrameが実行されるごとにインクリメントされる。一小節中の特定のドットを
    //指し示すために使用する。
    private var frameCount = 0

    //ドットサイズ変化割合の係数
    private var radiusRadix = 0.0

    //アルファ変化割合の係数
    private var dotAlpha = 0.0
    private val ut = Util()

    //セッティングでの描画時はリズムを１とするが、共用変数rhythmに影響を与えないようにプライベートで定義し使用する。
    private var privateRhythm = 0

    //UIで指定された一小節フレーム内の各要素を描画のに必要なフレーム数。
    private var halfBeatFrame = 0
    private var oneBeatFrame = 0
    private var oneBarFrame = 0
    private val lp = LogicalPosList()

    //ドットのマッピング配列
    var logicalX: MutableList<Int> = mutableListOf()
    var logicalY: MutableList<Int> = mutableListOf()

    //打点ナンバーのマッピング配列
    var numberPosXList: MutableList<Int> = mutableListOf()
    var numberPosYList: MutableList<Int> = mutableListOf()
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        if (renderMode == Motion.name) {
            privateRhythm = rhythm
        } else {
            privateRhythm = 1
        }
        calcFrame()
        val lpDotResult = lp.setDotLogicalPosList()
        logicalX = lpDotResult.first
        logicalY = lpDotResult.second
        if (renderMode == Motion.name) {
            val lpNumResult = lp.setNumLogicalPosList()
            numberPosXList = lpNumResult.first
            numberPosYList = lpNumResult.second
        }
        //画面クリア時の色の設定。（０～1を指定する。）
        GLES20.glClearColor(0.07f, 0.2f, 0.18f, 1.0f)
        //ドットプログラムオブジェクトの準備************************************
        val dotVertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(dotVertexShader, dotVertexSource)
        glCompileShader(dotVertexShader)
        val dotFragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(dotFragmentShader, dotFagmentSource)
        glCompileShader(dotFragmentShader)
        //シェーダプログラムの生成
        dotProgramId = glCreateProgram()
        //プログラムオブジェクトを生成したら、リンクしたいシェーダを接続（アタッチ）する。
        glAttachShader(dotProgramId, dotVertexShader)
        glAttachShader(dotProgramId, dotFragmentShader)
        //2つのシェーダオブジェクトを関連付けたら、それらをリンクさせて使用可能な状態にする。
        glLinkProgram(dotProgramId)
        if (renderMode == Motion.name) {
            //ナンバープログラムオブジェクトの準備************************************
            val numberVertexShader = glCreateShader(GL_VERTEX_SHADER)
            glShaderSource(numberVertexShader, numberVertexSource)
            glCompileShader(numberVertexShader)
            val numberFragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
            glShaderSource(numberFragmentShader, numberFragmentSource)
            glCompileShader(numberFragmentShader)
            //シェーダプログラムの生成
            numberProgramId = glCreateProgram()
            //プログラムオブジェクトを生成したら、リンクしたいシェーダを接続（アタッチ）する。
            glAttachShader(numberProgramId, numberVertexShader)
            glAttachShader(numberProgramId, numberFragmentShader)
            //2つのシェーダオブジェクトを関連付けたら、それらをリンクさせて使用可能な状態にする。
            glLinkProgram(numberProgramId)
            //テクスチャ・オブジェクトの生成
            glGenTextures(rhythm, textureArrayNum, 0)
        }
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        //ビューの切り替えの都度、最初（アウフタクト）から描画し始める。
        frameCount = 0
        glViewport(0, 0, width, height)
        val projectionMatrix = FloatArray(16)
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.orthoM(
            projectionMatrix,
            0,
            -width / 2f,
            width / 2f,
            (-height / 2).toFloat(),
            (height / 2).toFloat(),
            0f,
            2f
        )
        //上記、ビュー座標変換 と 射影変換を掛け合わせてまとめている。
        Matrix.multiplyMM(mViewAndProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl10: GL10) {

        //メトロノームサウンドを鳴らす。**********************************************************
        soundJob = scope.launch {
            sound.sound(halfBeatFrame, oneBeatFrame, frameCount)
        }
        //画面クリア
        //最初に glClearColor で設定した色で初期化される。
        glClear(GL_COLOR_BUFFER_BIT)
        val worldMatrix = FloatArray(16)
        Matrix.setIdentityM(worldMatrix, 0)
        //シェーダ内変数へデータ転送
        val uniLoc1 = glGetUniformLocation(dotProgramId, "vpMatrix")
        val uniLoc2 = glGetUniformLocation(dotProgramId, "wMatrix")
        //アプリケーション内のメモリから ビュー座標変換・射影変換、ワールド変換をGPU へデータを転送するための処理。
        glUniformMatrix4fv(uniLoc1, 1, false, mViewAndProjectionMatrix, 0)
        glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0)
        //OpenGLでの頂点座標作成を指示*******************************************************************
        //！！！onDrawFrame自体が繰り返し処理（インデックスはframeCount）のため、ここでfor文を書いた場合
        //二重ループとなる点に留意すること！！！
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glUseProgram(dotProgramId)
        glUniformMatrix4fv(uniLoc1, 1, false, mViewAndProjectionMatrix, 0)
        glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0)
        //１フレーム分（パラパラ漫画一枚分）の描画を行う。
        //i:一小節分（パラパラ漫画一枚分）のインデックス
        //l:一小節（パラパラ漫画一枚）の中でどの点を強調するかに関わるインデックス

        //ドットサイズ（ライン内での変化の割合）に関するパラメータ
        //ドットサイズの打点からの折り返し地点を管理
        var perOfHalfBeat = 0.0f
        //裏拍のドットサイズを管理
        var offbeatDotSize = 0.0f
        when (tactType) {
            Heavy.name -> {
                perOfHalfBeat = 0.6f
                offbeatDotSize = offbeatDotSizeHeavy
            }
            Light.name -> {
                perOfHalfBeat = 1.0f
                offbeatDotSize = 0.0f
            }
        }
        val lowerFrameNum = (halfBeatFrame * perOfHalfBeat).toInt()
        var l = 0
        //拍数分ループ
        when (tactType) {
            Heavy.name -> radiusMultiplier = 3.0
            Light.name -> radiusMultiplier = motionYMultiplier.pow(3.0)
        }
        for (i in 0 until privateRhythm) {
            //一拍のフレーム分ループ
            for (k in 0 until oneBeatFrame) {
                //radiusの大きさの基準となる係数を計算する。（最大値はここでは１）
                if (k / halfBeatFrame % 2 == 0) {
                    //表から裏へのライン
                    if (k % halfBeatFrame < lowerFrameNum) {
                        //下半分のドットに対する処理
                        radiusRadix =
                            (halfBeatFrame - (k % halfBeatFrame)).radiusPow() / halfBeatFrame.radiusPow()
//                            Log.i("@@@@", "i:$i 下半分radiusCoefficient: $radiusCoefficient")
                    } else {
                        //上半分のドットに対する処理
                        radiusRadix =
                            ((k % halfBeatFrame) * offbeatDotSize).toInt()
                                .radiusPow() / halfBeatFrame.radiusPow()
//                            Log.i("@@@@", "i:$i 上半分radiusCoefficient: $radiusCoefficient")
                    }
                } else {
                    //裏から表へのライン
                    if (k % halfBeatFrame < (halfBeatFrame - lowerFrameNum)) {
                        //上半分のドットに対する処理
                        radiusRadix =
                            ((halfBeatFrame - (k % halfBeatFrame)) * offbeatDotSize).toInt()
                                .radiusPow().div(halfBeatFrame.radiusPow())
//                            Log.i("@@@@", "i:$i 上半分radiusCoefficient: $radiusCoefficient")
                    } else {
                        //下半分のドットに対する処理
                        radiusRadix =
                            (k % halfBeatFrame).radiusPow() / halfBeatFrame.radiusPow()
//                            Log.i("@@@@", "i:$i 下半分radiusCoefficient: $radiusCoefficient")
                    }
                }
                //alphaの基準となる係数を計算する。（最大値はここでは１）
                dotAlpha =
                    (((oneBarFrame - frameCount + l) % oneBarFrame).alphaPow() / oneBarFrame.alphaPow())
                circle.drawCircle(
                    dotProgramId,
                    logicalX[l],
                    logicalY[l],
                    20,
                    (radiusRadix * dotSize).toFloat() + 3.0f,
                    1f, 1f, 1f, dotAlpha.toFloat()
                )
//                Log.i("GlRenderer","logicalX:${logicalX.size}:${logicalX}")
//                Log.i("GlRenderer","logicalY:${logicalY.size}:${logicalY}")
                l += 1
            }
        }
        if (renderMode == Motion.name) {
            //打点の数字を描画する。***************************************************************
            glUseProgram(numberProgramId)
            glUniformMatrix4fv(uniLoc1, 1, false, mViewAndProjectionMatrix, 0)
            glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0)
            var numAlpha = 0f
            var numberId = 0
            when (tactType) {
                Heavy.name -> {
                    //Heavyの時は、打点の前からナンバーの表示が始まる。
                    numAlpha = if ((frameCount.div(halfBeatFrame)) % 2 == 0) {
                        //裏へ向かう拍
                        1f - ((frameCount % halfBeatFrame).div(halfBeatFrame.toFloat()).pow(2.0f))
                    } else {
                        //表へ向かう拍
                        ((frameCount % halfBeatFrame).div(halfBeatFrame.toFloat()).pow(2.0f))
                    }
                    numberId = ((frameCount + halfBeatFrame).div(oneBeatFrame)) % privateRhythm
                }
                Light.name, Swing.name -> {
                    numAlpha =
                        1f - ((frameCount % oneBeatFrame).div(oneBeatFrame.toFloat()).pow(5.0f))
                    numberId = frameCount.div(oneBeatFrame)
                }
            }
            //最初のタイミングで最終拍のナンバーが表示されるのを回避する。
            if (numberId > 0) {
                justTappedSw = false
            }
            if (!justTappedSw) {
                numDraw.drawNumber(
                    numberProgramId,
                    numberId,
                    textureArrayNum,
                    numberPosXList[numberId],
                    numberPosYList[numberId],
                    numAlpha
                )
            }
            //バックパネルのフラッシュを行う。
            if (flashSW) {
                var backFlash = 0f
                when (tactType) {
                    Heavy.name -> {
                        //Heavyの時は、打点の前からナンバーの表示が始まる。
                        if ((frameCount.div(halfBeatFrame)) in 1..2) {
                            backFlash = if ((frameCount.div(halfBeatFrame)) % 2 == 0) {
                                //裏へ向かう拍
                                1f - (frameCount % halfBeatFrame).div(halfBeatFrame.toFloat())
                            } else {
                                //表へ向かう拍
                                (frameCount % halfBeatFrame).div(halfBeatFrame.toFloat())
                            }
                        }
                    }
                    Light.name -> {
                        if ((frameCount.div(halfBeatFrame)) == 2) {
                            backFlash =
                                1f - (frameCount % halfBeatFrame).div(halfBeatFrame.toFloat())
                        }
                    }
                }
                glClearColor(0.3f, 0.3f + (0.2f * backFlash), 1.0f, 1.0f)
            }
            //**********************************************************************
        }
        frameCount++
        if (frameCount >= oneBarFrame) {
            frameCount = 0

        }
    }

    private fun calcFrame() {
        halfBeatFrame = ut.halfBeatFrame(tempo)
        oneBeatFrame = ut.oneBeatFrame(tempo)
        oneBarFrame = ut.oneBarFrame(privateRhythm, tempo)
    }

    companion object {
        //*************** 拡張関数 *****************************************************
        fun Int.radiusPow() = this.toDouble().pow(radiusMultiplier)
        fun Int.alphaPow() = this.toDouble().pow(alphaMultiplier)
    }
}
