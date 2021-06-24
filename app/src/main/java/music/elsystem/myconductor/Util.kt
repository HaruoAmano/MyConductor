package music.elsystem.myconductor

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Util() {
    //リフレッシュレート60Hzを前提に一旦設計
    fun getHalfbeatDots () :Int {
        return 1800 / tempo
    }
    fun getNumOfDots() :Int{
        //一小節で打たれるドットの数
        return getHalfbeatDots() * 2 * rhythm
    }
    //左下に原点を置いてそこから1000×1000で区切った架空の座標で考える。
    //それを以下を使って実際のdpに変換する。
    //co:convert origin
    fun coX(x: Float): Float {
        val divisionX = surfaceWidth.toFloat() / 1000f
        return (x * divisionX - surfaceWidth.toFloat()/2)
    }

    fun coY(y: Float): Float {
        val divisionY = (surfaceHeight.toFloat() / 1000f)
        return (y * divisionY - surfaceHeight.toFloat()/2)
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