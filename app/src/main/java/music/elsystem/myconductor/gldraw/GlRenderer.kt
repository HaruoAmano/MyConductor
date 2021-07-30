package music.elsystem.myconductor.gldraw

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import music.elsystem.myconductor.Common.justTappedSw
import music.elsystem.myconductor.Common.rhythm
import music.elsystem.myconductor.Common.graphicTimestamp
import music.elsystem.myconductor.GraphicValue.animGradation
import music.elsystem.myconductor.GraphicValue.animRadius
import music.elsystem.myconductor.GraphicValue.dotSize
import music.elsystem.myconductor.GraphicValue.halfBeatFrame
import music.elsystem.myconductor.GraphicValue.logicalX
import music.elsystem.myconductor.GraphicValue.logicalY
import music.elsystem.myconductor.GraphicValue.numberPosXList
import music.elsystem.myconductor.GraphicValue.numberPosYList
import music.elsystem.myconductor.GraphicValue.oneBarFrame
import music.elsystem.myconductor.GraphicValue.oneBeatFrame
import music.elsystem.myconductor.GraphicValue.relativeYPosInLine
import music.elsystem.myconductor.gldraw.Shader.dotFagmentSource
import music.elsystem.myconductor.gldraw.Shader.dotVertexSource
import music.elsystem.myconductor.gldraw.Shader.numberFragmentSource
import music.elsystem.myconductor.gldraw.Shader.numberVertexSource
import java.sql.Timestamp
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.pow

class GlRenderer : GLSurfaceView.Renderer {
    private var dotProgramId = 0
    private var numberProgramId = 0
    private val mViewAndProjectionMatrix = FloatArray(16)
    private var frameCount = 0
    private val circle = Circle()
    private val numDraw = NumDraw()
    private val mesh = Mesh()
    private val textureArrayNum = IntArray(rhythm)
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
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
        //これは ビューポート変換 をする時の幅と高さを設定する。ここでは x, y 座標に 0 を、
        //幅と高さには width と height をそのまま渡しているので、Android の画面全体を
        //ビューポートとして設定していることになる。
        glViewport(0, 0, width, height)
        val projectionMatrix = FloatArray(16)
        val viewMatrix = FloatArray(16)

        //次にバーテックスシェーダの座標変換の記事で扱った ビュー座標変換 と 射影変換 をするための変換行列を生成して、
        // その 2 つを掛け合わせてまとめている。
        //カメラの位置は (0, 0, 1)、カメラの注視点は (0, 0, 0)、そしてカメラの上方向は (0, 1, 0) になっているので、
        // 原点より少し手前の位置から原点を見ているカメラを配置していることになる。
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
        // そして射影変換については、
        // 原点を中心として幅が width、高さが height の長方形があって、その奥行は 2 の立方体をクリッピング空間としている。
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
        //ワールド座標変換・ヴューポート変換についてはドット・ナンバーで共用。
        //ワールド座標変換行列の生成～転送（頂点・カラーデータについてはdrawLine関数で行う。
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
        var l = 0
        //１フレーム分（パラパラ漫画一枚分）の描画を行う。
        //i:一小節分（パラパラ漫画一枚分）のインデックス
        //l:一小節（パラパラ漫画一枚）の中でどの点を強調するかに関わるインデックス
        for (i in 0 until oneBarFrame) {
            l = (oneBarFrame + frameCount - i) % oneBarFrame
            circle.drawCircle(
                dotProgramId,
                logicalX[i],
                logicalY[i],
                //animRadiusはドットの半径は球を大きくする勢い。
                //relativeYPosInLineはライン中の相対的な高さに連動し球の大きさを変える変数。
                32, ((animRadius[l] * relativeYPosInLine[i]) / 1000f * dotSize) + 1.0f,
                1f, 1f, 1f, animGradation[l]
            )
        }
        //メッシュの描画を行う（テスト用）場合に呼ぶ。********************************************
//        mesh.drawMesh(dotProgramId)
        //打点の数字を描画する。***************************************************************
        glUseProgram(numberProgramId)
        glUniformMatrix4fv(uniLoc1, 1, false, mViewAndProjectionMatrix, 0)
        glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0)
        val alpha = if ((frameCount / halfBeatFrame) % 2 == 0){
            //裏へ向かう拍
            1f - ((frameCount % halfBeatFrame) / halfBeatFrame.toFloat()).pow(5.0f)
        } else {
            //表へ向かう拍
            ((frameCount % halfBeatFrame) / halfBeatFrame.toFloat()).pow(5.0f)
        }
        val numberId = ((frameCount + halfBeatFrame) / oneBeatFrame) % rhythm
        //最初のタイミングで最終拍のナンバーが表示されるのを回避する。
        if (numberId > 0) {justTappedSw = false}
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
        //**********************************************************************
        frameCount++
        if (frameCount >= oneBarFrame) {
            frameCount = 0
            graphicTimestamp = Timestamp(System.currentTimeMillis())
            Log.i("graphicTimestamp","$graphicTimestamp")
        }
    }
}
