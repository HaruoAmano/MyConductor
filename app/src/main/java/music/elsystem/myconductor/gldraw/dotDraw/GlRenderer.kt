package music.elsystem.myconductor.gldraw.dotDraw

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import music.elsystem.myconductor.Common.Tact.*
import music.elsystem.myconductor.Common.alphaMultiplier
import music.elsystem.myconductor.Common.justTappedSw
import music.elsystem.myconductor.Common.radiusMultiplier
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.GraphicValue.dotSize
import music.elsystem.myconductor.Util
import music.elsystem.myconductor.gldraw.Circle
import music.elsystem.myconductor.gldraw.LogicalPosArray
import music.elsystem.myconductor.gldraw.Shader.dotFagmentSource
import music.elsystem.myconductor.gldraw.Shader.dotVertexSource
import music.elsystem.myconductor.gldraw.Shader.numberFragmentSource
import music.elsystem.myconductor.gldraw.Shader.numberVertexSource
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.pow

class GlRenderer(
    private val context: Context,
    private val rhythm: Int,
    private val tempo: Int,
    private val bmpBeat: Bitmap?,
    private val voice: String,
    private val motionYMultiplier: Double
) : GLSurfaceView.Renderer {
    //シェーダプログラムID
    private var dotProgramId = 0
    private var numberProgramId = 0
    //ヴュー＆プロジェクションマッピング行列
    private val mViewAndProjectionMatrix = FloatArray(16)
    //ドット描画クラス
    private val circle = Circle()
    //打点ナンバー描画クラス
    private val numDraw = NumDraw()
    //打点ナンバーのテクスチャ（ピクチャ）数
    private val textureArrayNum = IntArray(rhythm)
    //メッシュ描画クラス（テスト用）
    private val mesh = Mesh()
    //サウンドクラス
    private  var sound = Sound()
    //onDrawFrameが実行されるごとにインクリメントされる。一小節中の特定のドットを
    //指し示すために使用する。
    private var frameCount = 0
    //ドットサイズ変化割合の係数
    private var radiusCoefficient = 0.0
    //アルファ変化割合の係数
    private var alphaCoefficient = 0.0
    private val ut = Util()
    //UIで指定された一小節フレーム内の各要素を描画のに必要なフレーム数。
    private val halfBeatFrame = ut.halfBeatFrame(tempo)
    private val oneBeatFrame = ut.oneBeatFrame(tempo)
    private val oneBarFrame = ut.oneBarFrame(rhythm, tempo)
    val lp = LogicalPosArray(rhythm, tempo, motionYMultiplier)
    //ドットのマッピング配列
    var logicalX: MutableList<Int> = mutableListOf()
    var logicalY: MutableList<Int> = mutableListOf()
    //打点ナンバーのマッピング配列
    var numberPosXList: MutableList<Int> = mutableListOf()
    var numberPosYList: MutableList<Int> = mutableListOf()
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        val lpDotResult = lp.setDotLogicalPosList(bmpBeat)
        logicalX = lpDotResult.first
        logicalY = lpDotResult.second
        val lpNumResult = lp.setNumLogicalPosList()
        numberPosXList = lpNumResult.first
        numberPosYList = lpNumResult.second
        sound.setSoundList(voice,rhythm)
        sound.setSoundPool(context,rhythm)
        //画面クリア時の色の設定。（０～1を指定する。）
        glClearColor(0.3f, 0.3f, 1.0f, 1.0f)
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
        //上側のドットサイズを管理
        var ratioUpAndDown = 0.0f
        when (tactType) {
            Heavy.name -> {
                perOfHalfBeat = 0.7f
                ratioUpAndDown = 0.85f
            }
            Normal.name -> {
                perOfHalfBeat = 1.0f
                ratioUpAndDown = 0.0f
            }
            Swing.name -> {
                perOfHalfBeat = 0.5f
                ratioUpAndDown = 1.1f
            }

        }
        val lowerFrameNum = (halfBeatFrame * perOfHalfBeat).toInt()
        var l = 0
        //拍数分ループ
        for (i in 0 until rhythm) {
            //一拍のフレーム分ループ
            for (k in 0 until oneBeatFrame) {
                //radiusの大きさの基準となる係数を計算する。（最大値はここでは１）
                if (k / halfBeatFrame % 2 == 0) {
                    //表から裏へのライン
                    if (k % halfBeatFrame < lowerFrameNum) {
                        //下半分のドットに対する処理
                        radiusCoefficient =
                            (halfBeatFrame - (k % halfBeatFrame)).radiusPow() / halfBeatFrame.radiusPow()
//                            Log.i("@@@@", "i:$i 下半分radiusCoefficient: $radiusCoefficient")
                    } else {
                        //上半分のドットに対する処理
                        radiusCoefficient =
                            ((k % halfBeatFrame) * ratioUpAndDown).toInt()
                                .radiusPow() / halfBeatFrame.radiusPow()
//                            Log.i("@@@@", "i:$i 上半分radiusCoefficient: $radiusCoefficient")
                    }
                } else {
                    //裏から表へのライン
                    if (k % halfBeatFrame < (halfBeatFrame - lowerFrameNum)) {
                        //上半分のドットに対する処理
                        radiusCoefficient =
                            ((halfBeatFrame - (k % halfBeatFrame)) * ratioUpAndDown).toInt()
                                .radiusPow() / halfBeatFrame.radiusPow()
//                            Log.i("@@@@", "i:$i 上半分radiusCoefficient: $radiusCoefficient")
                    } else {
                        //下半分のドットに対する処理
                        radiusCoefficient =
                            (k % halfBeatFrame).radiusPow() / halfBeatFrame.radiusPow()
//                            Log.i("@@@@", "i:$i 下半分radiusCoefficient: $radiusCoefficient")
                    }
                }
                //alphaの基準となる係数を計算する。（最大値はここでは１）
                alphaCoefficient =
                    (((oneBarFrame - frameCount + l) % oneBarFrame).alphaPow() / oneBarFrame.alphaPow())
                circle.drawCircle(
                    dotProgramId,
                    logicalX[l],
                    logicalY[l],
                    32,
                    (radiusCoefficient * dotSize).toFloat() + 3.0f,
                    1f, 1f, 1f, alphaCoefficient.toFloat()
                )
                l += 1
            }

        }
        //メッシュの描画を行う（テスト用）場合に呼ぶ。********************************************
        //mesh.drawMesh(dotProgramId)
        //打点の数字を描画する。***************************************************************
        glUseProgram(numberProgramId)
        glUniformMatrix4fv(uniLoc1, 1, false, mViewAndProjectionMatrix, 0)
        glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0)
        var alpha = 0f
        var numberId = 0
        when (tactType) {
            Heavy.name -> {
                //Heavyの時は、打点の前からナンバーの表示が始まる。
                alpha = if ((frameCount / halfBeatFrame) % 2 == 0) {
                    //裏へ向かう拍
                    1f - ((frameCount % halfBeatFrame) / halfBeatFrame.toFloat()).pow(2.0f)
                } else {
                    //表へ向かう拍
                    ((frameCount % halfBeatFrame) / halfBeatFrame.toFloat()).pow(2.0f)
                }
                numberId = ((frameCount + halfBeatFrame) / oneBeatFrame) % rhythm
            }
            Normal.name -> {
                alpha = 1f - ((frameCount % oneBeatFrame) / oneBeatFrame.toFloat()).pow(5.0f)
                numberId = frameCount / oneBeatFrame
            }
            Swing.name -> {
                alpha = 1f - ((frameCount % oneBeatFrame) / oneBeatFrame.toFloat()).pow(5.0f)
                numberId = frameCount / oneBeatFrame
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
                alpha
            )
        }
        //メトロノームサウンドを鳴らす。**********************************************************
        sound.sound(halfBeatFrame,oneBeatFrame,frameCount)
        //**********************************************************************
        frameCount++
        if (frameCount >= oneBarFrame) {
            frameCount = 0
        }
    }

    companion object {
        //*************** 拡張関数 *****************************************************
        fun Int.radiusPow() = this.toDouble().pow(radiusMultiplier)
        fun Int.alphaPow() = this.toDouble().pow(alphaMultiplier)
    }
}
