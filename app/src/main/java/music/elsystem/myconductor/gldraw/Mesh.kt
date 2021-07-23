package music.elsystem.myconductor.gldraw

import android.opengl.GLES20.*
import music.elsystem.myconductor.Common
import music.elsystem.myconductor.Util

class Mesh {
    //meshの頂点座標を作成する。（テスト時のみ使用。通常は使用しない。）
    fun drawMesh(mProgramId : Int) {
        val util = Util()
        //平行線メッシュ
        val meshVertex = FloatArray(72)
        var indexX = 1
        for (i in 0 until 36 step 4) {
            meshVertex[i] = util.coX(0)
            meshVertex[i + 1] = util.coY(Common.bitmapY / 10 * indexX)
            meshVertex[i + 2] = util.coX(Common.bitmapX)
            meshVertex[i + 3] = util.coY(Common.bitmapY / 10 * indexX)
            indexX++
        }
        //垂直線メッシュ
        var indexY = 1
        for (i in 36 until 72 step 4) {
            meshVertex[i] = util.coX(Common.bitmapX /10 * indexY)
            meshVertex[i + 1] = util.coY(0)
            meshVertex[i + 2] = util.coX(Common.bitmapX / 10 * indexY)
            meshVertex[i + 3] = util.coY(Common.bitmapY)
            indexY++
        }
        val attPositionLocation = glGetAttribLocation(mProgramId, "position")
        glEnableVertexAttribArray(attPositionLocation)
        //頂点座標の転送
        glVertexAttribPointer(
            attPositionLocation,
            2,
            GL_FLOAT,
            false,
            0,
            util.convert(meshVertex)
        )
        //カラー
        val color = floatArrayOf(0f, 1f, 0f, 1f) //rgba
        val uniLoc3 = glGetUniformLocation(mProgramId, "color")
        glUniform4fv(uniLoc3, 1, color, 0)
        //描画処理
        glDrawArrays(GL_LINES, 0, 36)
        glDisableVertexAttribArray(attPositionLocation)
    }
}