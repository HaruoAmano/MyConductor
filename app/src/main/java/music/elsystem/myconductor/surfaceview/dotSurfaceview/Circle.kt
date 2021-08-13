package music.elsystem.myconductor.surfaceview.dotSurfaceview

import android.opengl.GLES20.*
import music.elsystem.myconductor.Util
import kotlin.math.cos
import kotlin.math.sin

class Circle {
    //円の頂点座標を作成する。
    private val util = Util()
    fun drawCircle(
        mProgramId: Int,
        x: Int,
        y: Int,
        divides: Int,          //n角形
        radius: Float,         //半径
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val vertices = FloatArray(divides * 2) //*2はx,yが対のため
        //頂点アトリビュートについてはそれを有効化する必要がある。
        for (i in 0 until divides * 2 step 2) {
            val theta1: Float = getRadian(divides, i)
            vertices[i] = (cos(theta1) * radius + util.coX(x))
            vertices[i + 1] = (sin(theta1) * radius + util.coY(y))
        }
        val dotColor = FloatArray(divides * 4) //*4はRGBA
        for (i in 0 until divides * 4 step 4) {
            dotColor[i] = red
            dotColor[i + 1] = green
            dotColor[i + 2] = blue
            dotColor[i + 3] = alpha
        }
        //アプリケーション内のメモリから GPU へデータを転送するための処理。
        //「このシェーダで描画します」という宣言を行う。glUserProgram() で、描画に使用するシェーダを切り替える。
        val attPositionLocation = glGetAttribLocation(mProgramId, "position")
        val attDotColorLocation = glGetAttribLocation(mProgramId, "dot_color")
        glEnableVertexAttribArray(attPositionLocation)
        glEnableVertexAttribArray(attDotColorLocation)
        glVertexAttribPointer(
            attPositionLocation,
            2,
            GL_FLOAT,
            false,
            0,
            util.convert(vertices)
        )
        glVertexAttribPointer(
            attDotColorLocation,
            4,
            GL_FLOAT,
            false,
            0,
            util.convert(dotColor)
        )
        val polygonNum = divides
        glDrawArrays(GL_TRIANGLE_FAN, 0, polygonNum)

        glDisableVertexAttribArray(attDotColorLocation)
        glDisableVertexAttribArray(attPositionLocation)
    }

    //求めたい頂点に対する角度を計算する。
    private fun getRadian(divides: Int, idx: Int): Float {
        return (kotlin.math.PI * 2 * idx / divides).toFloat()
    }
}