package music.elsystem.myconductor.surfaceview.lineSureface

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import music.elsystem.myconductor.Common.RenderMode.Line
import music.elsystem.myconductor.Common.renderMode
import music.elsystem.myconductor.Common.rhythm
import music.elsystem.myconductor.Util
import music.elsystem.myconductor.surfaceview.LogicalPosList
import music.elsystem.myconductor.surfaceview.Shader.lineFragmentSource
import music.elsystem.myconductor.surfaceview.Shader.lineVertexSource
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LineRenderer() : GLSurfaceView.Renderer {
    private var mProgramId = 0
    private val mViewAndProjectionMatrix = FloatArray(16)
    private val ut = Util()
    private var oneBarFrame = ut.oneBarFrame(rhythm,20)
    val lp = LogicalPosList()
    //ドットのマッピング配列
    var logicalX: MutableList<Int> = mutableListOf()
    var logicalY: MutableList<Int> = mutableListOf()
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        val lpResult = lp.setDotLogicalPosList()
        logicalX = lpResult.first
        logicalY = lpResult.second
        //画面クリア時の色の設定。（０～1を指定する。）
        GLES20.glClearColor(0.1f, 0.3f, 0.3f, 1.0f)
        //バーテックスシェーダーのコンパイル
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        //sVertexShaderSourceは下のほうでコンパニオンオブジェクトとして定義されている。
        GLES20.glShaderSource(vertexShader, lineVertexSource)
        GLES20.glCompileShader(vertexShader)
        //フラグメントシェーダーのコンパイル
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        //sFragmentShaderSourceは下のほうでコンパニオンオブジェクトとして定義されている。
        GLES20.glShaderSource(fragmentShader, lineFragmentSource)
        GLES20.glCompileShader(fragmentShader)
        //シェーダのリンク
        //シェーダがコンパイル出来たら次にそれをプログラムにリンクさせる。
        //このプログラムの ID は後で使うことになるのでメンバ変数に保持しておく。
        mProgramId = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgramId, vertexShader)
        GLES20.glAttachShader(mProgramId, fragmentShader)
        GLES20.glLinkProgram(mProgramId)
        GLES20.glUseProgram(mProgramId)
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        Log.i("onSurfaceChanged", "")
        GLES20.glViewport(0, 0, width, height)
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
        Matrix.multiplyMM(mViewAndProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl10: GL10) {
        //画面クリア
        //最初に GLES20.glClearColor で設定した色で初期化される。
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //ワールド座標変換行列の生成～転送（頂点・カラーデータについてはdrawLine関数で行う。
        val worldMatrix = FloatArray(16)
        Matrix.setIdentityM(worldMatrix, 0)
        //シェーダ内変数へデータ転送
        val uniLoc1 = GLES20.glGetUniformLocation(mProgramId, "vpMatrix")
        val uniLoc2 = GLES20.glGetUniformLocation(mProgramId, "wMatrix")
        //アプリケーション内のメモリから ビュー座標変換・射影変換、ワールド変換をGPU へデータを転送するための処理。
        GLES20.glUniformMatrix4fv(uniLoc1, 1, false, mViewAndProjectionMatrix, 0)
        GLES20.glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0)
        //OpenGLでの頂点座標作成を指示*******************************************************************
        val vertices = FloatArray(oneBarFrame * 2)
        var vertexId = 0
        for (i in 0 until oneBarFrame) {
            vertices[vertexId++] = ut.coX(logicalX[i])
            vertices[vertexId++] = ut.coY(logicalY[i])
        }
        val attPositionLocation = GLES20.glGetAttribLocation(mProgramId, "position")
        GLES20.glEnableVertexAttribArray(attPositionLocation)
        GLES20.glVertexAttribPointer(
            attPositionLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            ut.convert(vertices)
        )
        //描画
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, oneBarFrame)
        GLES20.glDisableVertexAttribArray(attPositionLocation)
    }
}
