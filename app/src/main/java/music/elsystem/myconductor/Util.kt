package music.elsystem.myconductor

import music.elsystem.myconductor.MainActivity.Companion.surfaceHeight
import music.elsystem.myconductor.MainActivity.Companion.surfaceWidth
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Util() {
    //左上に原点を置いてそこから3000×3000（インデックスとしては0～2999）で区切った架空の座標（指揮軌道ビットマップに合わせる。）で図形を描き、
    //最終OpenGLに渡す段階で下記関数を使ってOpenGL座標に変換する。
    //下記関数の考え方としては、まず原点を中心に移動し、Display画面／論理座標で縮尺する。
    //co:convert origin
    val logicalWidth = 3000
    val logicalHeight = 3000
    fun coX(x: Int): Float {
        return (x - (logicalWidth / 2f)) * (surfaceWidth/logicalWidth.toFloat())
    }
    fun coY(y: Int): Float {
        return ((logicalHeight -1 - y) - (logicalHeight / 2f)) * (surfaceHeight/logicalHeight.toFloat())
    }
    fun halfBeatDots(tempo:Int) :Int {
        val halfBeatDots = (1800f / tempo.toFloat()).toInt()
        return halfBeatDots
    }
    fun  oneBarDots(tempo:Int, rhythm:Int) :Int {
        val oneBarDots = halfBeatDots(tempo) * 2 * rhythm
        return oneBarDots
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