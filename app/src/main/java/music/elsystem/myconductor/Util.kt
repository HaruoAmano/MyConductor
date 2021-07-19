package music.elsystem.myconductor

import music.elsystem.myconductor.MainActivity.Companion.surfaceHeight
import music.elsystem.myconductor.MainActivity.Companion.surfaceWidth
import music.elsystem.myconductor.MainActivity.Companion.bitmapX
import music.elsystem.myconductor.MainActivity.Companion.bitmapY
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Util() {
    //左上に原点を置いてそこからビットマップのドットをの座標単位として図形を描き、
    //最終OpenGLに渡す段階で下記関数を使ってOpenGL座標に変換する。
    //下記関数の考え方としては、まず原点を中心に移動し、Display画面／論理座標で縮尺する。
    //co:convert origin

    fun coX(x: Int): Float {
        return (x - (bitmapX / 2f)) * (surfaceWidth/bitmapX.toFloat())
    }
    fun coY(y: Int): Float {
        return ((bitmapY -1 - y) - (bitmapY / 2f)) * (surfaceHeight/bitmapY.toFloat())
    }
    fun halfBeatDots(tempo: Int): Int {
        return (1800f / tempo.toFloat()).toInt()
    }
    fun oneBarDots(tempo: Int, rhythm: Int): Int {
        return halfBeatDots(tempo) * 2 * rhythm
    }

    //コーディングしたプリミティブ型を GPU に転送するためにバッファ型に
    // 変換するためのユーティリティクラスで、
    //頂点座標や頂点インデックスを GPU に転送する際に利用する。
    //FloatArray、ShortArrayともに関数名はconvertで同じ。
    //これにより引数によりそれに応じた関数が呼ばれる。（関数の多重定義）
    fun convert(data: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(data.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val floatBuffer = bb.asFloatBuffer()
        floatBuffer.put(data)
        floatBuffer.position(0)
        return floatBuffer
    }

    fun convert(data: ShortArray): ShortBuffer {
        val bb = ByteBuffer.allocateDirect(data.size * 2)
        bb.order(ByteOrder.nativeOrder())
        val shortBuffer = bb.asShortBuffer()
        shortBuffer.put(data)
        shortBuffer.position(0)
        return shortBuffer
    }

}