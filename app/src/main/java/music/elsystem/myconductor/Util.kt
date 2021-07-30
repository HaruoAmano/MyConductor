package music.elsystem.myconductor

import android.widget.Spinner
import android.widget.TextView
import music.elsystem.myconductor.Common.tempo
import music.elsystem.myconductor.Common.bitmapX
import music.elsystem.myconductor.Common.bitmapY
import music.elsystem.myconductor.Common.surfaceHeight
import music.elsystem.myconductor.Common.surfaceWidth
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
    fun halfBeatFrame(tempo: Int): Int {
        //リフレッシュレート60Hzを前提に一旦設計
        //tempo = 60とは１分間に60拍刻むということ。
        //リフレッシュレート60Hzであれば1分間に3600回画面が書き換わる（onDrawFrameが実行される）ということ。
        //したがって半拍分の画面書き換え回数は1800/tempoとなる。
        return (1800f / tempo.toFloat()).toInt()
    }
    fun oneBarFrame(tempo: Int, rhythm: Int): Int {
        return halfBeatFrame(tempo) * 2 * rhythm
    }
    fun changeTempo(newTempo: Int, textView:TextView,spinner:Spinner) {
        when {
            newTempo < 25 -> {
                textView.text = "25"
                tempo = 25
            }
            newTempo >= 144 -> {
                textView.text = "144"
                tempo = 144
            }
            else -> {
                textView.text = newTempo.toString()
                tempo = newTempo
            }
        }
        when (tempo) {
            in 25..42 -> spinner.setSelection(0) //"Grave"
            in 43..49 -> spinner.setSelection(1) //"Largo"
            in 50..53 -> spinner.setSelection(2) //"Lento"
            in 54..59 -> spinner.setSelection(3) //"Adagio"
            in 60..67 -> spinner.setSelection(4) //"Adagietto"
            in 68..83 -> spinner.setSelection(5) //"Andante"
            in 84..95 -> spinner.setSelection(6) //"Moderate"
            in 96..119 -> spinner.setSelection(7) //"Allegretto"
            in 120..144 -> spinner.setSelection(8) //"Allegro"
        }
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