package music.elsystem.myconductor

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class GlRenderer : GLSurfaceView.Renderer {
    private var mProgramId = 0
    private val mViewAndProjectionMatrix = FloatArray(16)
    private var frameCount = 0
    private val util = Util()

    //    private val radius = FloatArray(numOfDots)
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        //現在、軌跡の準備中
        //指揮の軌跡を用意する。
        //Y軸の取得
        //工事中。一旦trajectoryクラスが完成してから手当てする。

//        for (i in 0 until util.getHalfbeatDots()) {
//            var progStep =
//                (listPointX[(i + 1) % (rhythm * 2)] - listPointX[i]) / util.getHalfbeatDots()
//            for (j in 0 until util.getHalfbeatDots()) {
//                trajectoryX[i * util.getHalfbeatDots() + j] = listPointX[i] + progStep * (j + 1)
//            }
//        }

        //画面クリア時の色の設定。（０～1を指定する。）
        GLES20.glClearColor(0.3f, 0.3f, 1.0f, 1.0f)
        //バーテックスシェーダーのコンパイル
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        //sVertexShaderSourceは下のほうでコンパニオンオブジェクトとして定義されている。
        GLES20.glShaderSource(vertexShader, sVertexShaderSource)
        GLES20.glCompileShader(vertexShader)
        //フラグメントシェーダーのコンパイル
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        //sFragmentShaderSourceは下のほうでコンパニオンオブジェクトとして定義されている。
        GLES20.glShaderSource(fragmentShader, sFragmentShaderSource)
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
        //これは ビューポート変換 をする時の幅と高さを設定する。ここでは x, y 座標に 0 を、
        //幅と高さには width と height をそのまま渡しているので、Android の画面全体を
        //ビューポートとして設定していることになる。
        GLES20.glViewport(0, 0, width, height)
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
        //最初に GLES20.glClearColor で設定した色で初期化される。
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //描画*******************************************************************
        var l = 0
        for (i in 0 until util.getNumOfDots()) {
            l = (frameCount + i) % util.getNumOfDots()
            //工事中。一旦trajectoryクラスが完成してから手当てする。
//            drawCircle(
//                util.coX(trajectoryX[i]),
//                util.coY(trajectoryY[i]),
//                16, 20f,
//                1f, 1f, 1f, 1f
//            )
        }
        frameCount++
        if (frameCount >= util.getHalfbeatDots() * rhythm * 2) {
            frameCount = 0
        }
        //**********************************************************************
    }

    //円を描画
    fun drawCircle(
        x: Float,
        y: Float,
        divides: Int,          //n角形
        radius: Float,         //半径
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val vertices = FloatArray(divides * 3 * 2) //頂点の数はn角形の場合はn*3*2になる。
        var vertexId = 0
        for (i in 0 until divides) {
            val theta1: Float = getRadian(divides, i)
            val theta2: Float = getRadian(divides, i + 1)
            vertices[vertexId++] = x
            vertices[vertexId++] = y
            vertices[vertexId++] = (cos(theta1) * radius + x) as Float
            vertices[vertexId++] = (sin(theta1) * radius + y) as Float
            vertices[vertexId++] = (cos(theta2) * radius + x) as Float
            vertices[vertexId++] = (sin(theta2) * radius + y) as Float
        }
        val color = floatArrayOf(red, green, blue, alpha) //rgba
        //ワールド座標変換行列の生成
        val worldMatrix = FloatArray(16)
        Matrix.setIdentityM(worldMatrix, 0)
        //シェーダ内変数へデータ転送
        //バーテックスシェーダに定義されている３つの変数へアプリケーション内からデータを転送する必要があるが、
        //どの変数に対してデータを送信するかという識別子のようなものを取得している。
        val attPositionLocation = GLES20.glGetAttribLocation(mProgramId, "position")
        val uniLoc1 = GLES20.glGetUniformLocation(mProgramId, "vpMatrix")
        val uniLoc2 = GLES20.glGetUniformLocation(mProgramId, "wMatrix")
        val uniLoc3 = GLES20.glGetUniformLocation(mProgramId, "color")
        //attLoc1についてはそれを有効化する必要がある。
        GLES20.glEnableVertexAttribArray(attPositionLocation)
        //アプリケーション内のメモリから GPU へデータを転送するための処理。
        GLES20.glVertexAttribPointer(
            attPositionLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            util.convert(vertices)
        )
        GLES20.glUniformMatrix4fv(uniLoc1, 1, false, mViewAndProjectionMatrix, 0)
        GLES20.glUniformMatrix4fv(uniLoc2, 1, false, worldMatrix, 0)
        GLES20.glUniform4fv(uniLoc3, 1, color, 0)
        //描画
        //3つの頂点を持つポリゴンn個で構成されている。
        val polygonNum = divides * 3
        //最終的にはFUNで書くべし！
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, polygonNum)
        GLES20.glDisableVertexAttribArray(attPositionLocation)

    }

    private fun getRadian(divides: Int, idx: Int): Float {
        return (kotlin.math.PI * 2 * idx / divides).toFloat()
    }

    companion object {
        //バーテックスシェーダ
        //シェーダが実行される時はこの中のmain関数が呼び出される。
        //変数は 種別 型 変数名 という風に定義される。
        //  vpMatrix ビュー座標変換行列と射影変換行列を掛けあわせた 4x4 の行列
        //  wMatrix 4x4 のワールド座標変換行列
        //  position x, y, z 成分を持つ頂点座標
        const val sVertexShaderSource =
            "uniform mat4 vpMatrix;" +
                    "uniform mat4 wMatrix;" +
                    "attribute vec3 position;" +
                    "void main() {" +
                    "  gl_Position = vpMatrix * wMatrix * vec4(position, 1.0);" +
                    "}"

        //フラグメントシェーダ
        //今回のフラグメントシェーダでは変数は特に使わないので定義されていない。
        //このフラグメントシェーダでは一律赤色 vec4(1.0, 0.0, 0.0, 1.0) を出力するようにしている。
        //色は rgba の成分を 0 〜 1 の範囲で指定する。
        //gl_FragColor というのはバーテックスシェーダで出てきたのと同じで組み込みの変数になっていて、
        //この変数に色を代入するとその色をフラグメントの色として、次の処理に渡されていく。
        //※precision mediump float; の部分はおまじない的に考えてよい。
        const val sFragmentShaderSource = "precision mediump float;" +
                "uniform vec4 color;" +
                "void main() {" +
//                "  gl_FragColor = vec4(1.0f,0.0f,0.0f,1.0f);" +
                "  gl_FragColor = color;" +
                "}"
    }
}
