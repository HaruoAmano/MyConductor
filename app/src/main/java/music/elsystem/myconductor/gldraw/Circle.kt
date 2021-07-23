package music.elsystem.myconductor.gldraw

import android.opengl.GLES20.*
import music.elsystem.myconductor.Util
import kotlin.math.cos
import kotlin.math.sin

class Circle {
    //円の頂点座標を作成する。
    private val util = Util()
    fun drawCircle(
        mProgramId : Int,
        x: Int,
        y: Int,
        divides: Int,          //n角形
        radius: Float,         //半径
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val vertices = FloatArray(divides * 3 * 2) //頂点の数はn角形の場合はn*3*2になる。
        //頂点アトリビュートについてはそれを有効化する必要がある。
        var vertexId = 0
        for (i in 0 until divides) {
            val theta1: Float = getRadian(divides, i)
            val theta2: Float = getRadian(divides, i + 1)
            vertices[vertexId++] = util.coX(x)
            vertices[vertexId++] = util.coY(y)
            vertices[vertexId++] = (cos(theta1) * radius + util.coX(x))
            vertices[vertexId++] = (sin(theta1) * radius + util.coY(y))
            vertices[vertexId++] = (cos(theta2) * radius + util.coX(x))
            vertices[vertexId++] = (sin(theta2) * radius + util.coY(y))
        }
        //アプリケーション内のメモリから GPU へデータを転送するための処理。
        val attPositionLocation = glGetAttribLocation(mProgramId, "position")
        glEnableVertexAttribArray(attPositionLocation)
        glVertexAttribPointer(
            attPositionLocation,
            2,
            GL_FLOAT,
            false,
            0,
            util.convert(vertices)
        )
        val color = floatArrayOf(red, green, blue, alpha) //rgba
        val uniLoc3 = glGetUniformLocation(mProgramId, "color")
        glUniform4fv(uniLoc3, 1, color, 0)
        //描画
        //3つの頂点を持つポリゴンn個で構成されている。
        val polygonNum = divides * 3
        //最終的にはFUNで書くべし！
        glDrawArrays(GL_TRIANGLES, 0, polygonNum)
        glDisableVertexAttribArray(attPositionLocation)

    }
    //求めたい頂点に対する角度を計算する。
    private fun getRadian(divides: Int, idx: Int): Float {
        return (kotlin.math.PI * 2 * idx / divides).toFloat()
    }
}