package music.elsystem.myconductor.gldraw

import android.opengl.GLES20.*
import android.opengl.GLES32.GL_QUADS
import android.opengl.GLUtils
import music.elsystem.myconductor.GraphicValue
import music.elsystem.myconductor.Util

class NumDraw {
    private val util = Util()
    fun drawNumber(
        mProgramId: Int,
        numberId: Int,
        textureArrayNum: IntArray,
        x: Int,
        y: Int,
        alpha: Float
    ) {
        //頂点**************************************************************************
        val intervalY = 50
        val size = 50
        val vertices = floatArrayOf(
            util.coX(x), util.coY(y + size + intervalY),
            util.coX(x + size), util.coY(y + size + intervalY),
            util.coX(x + size), util.coY(y + intervalY),
            util.coX(x), util.coY(y + intervalY)
        )
        //テクスチャ**************************************************************
        val uniLocTex = glGetUniformLocation(mProgramId, "texture")
        //GPU内のテクスチャIDにバインドさせる処理。
        glBindTexture(GL_TEXTURE_2D, textureArrayNum[numberId])
        // テクスチャの拡大縮小時は線型フィルタリング
        //縮小
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR
        )
        //拡大
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            GL_LINEAR
        )
        val textCoords = floatArrayOf(
            0f,1f,    //左上
            1f,1f,    //左下
            1f,0f,      //右下
            0f,0f              //右上
        )
        //カラー**********************************************************************
        val uniLocColor = glGetUniformLocation(mProgramId, "color")
        //画像データのGPUへの転送。
        glUseProgram(mProgramId)
        GLUtils.texImage2D(GL_TEXTURE_2D,0, GraphicValue.numberBitmapList[numberId],0)
        glUniform1i(uniLocTex, 0)
        val color = floatArrayOf(1f, 1f, 1f, alpha) //rgba
        glUniform4fv(uniLocColor, 1, color, 0)
        //******************************************************************************
        //アプリケーション内のメモリから GPU へデータを転送するための処理。
        val attPositionLocation = glGetAttribLocation(mProgramId, "position")
        val attUvLocation = glGetAttribLocation(mProgramId, "uv")
        glEnableVertexAttribArray(attUvLocation)
        glEnableVertexAttribArray(attPositionLocation)
        glVertexAttribPointer(
            attPositionLocation,
            2,
            GL_FLOAT,
            false,
            0,
            util.convert(vertices)
        )
        glVertexAttribPointer(
            attUvLocation,
            2,
            GL_FLOAT,
            false,
            0,
            util.convert(textCoords)
        )
        //描画
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
        glDisableVertexAttribArray(attPositionLocation)

    }
}