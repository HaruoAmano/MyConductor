package music.elsystem.myconductor

import android.util.Log
import kotlin.math.pow

class LogicalVerticalDots() {
    private val util = Util()
    //テンポ、（および打点の重み）に応じ論理的に（1000f～0F）マッピングされたY値の
    //配列を生成する。
    //verteces上のY値はTrajectoryクラスで拍ごとに縮尺し決定する。
    //また、配列は最初を最大値とする。（下に落ちていくイメージ）
    fun mkLogicalVDotsArray() :FloatArray{
        val logicalVDotsArray = FloatArray(util.getHalfbeatDots())
        for (i in 0 until util.getHalfbeatDots()) {
            //iの3乗を算出し最大値が1000となるように調整
            logicalVDotsArray[i] = (1000.0 -
                    ((i+1).toDouble().pow(1.0) *
                    (1000.0 / util.getHalfbeatDots().toDouble().pow(1.0)))).toFloat()
            Log.i(tagMsg,"${logicalVDotsArray[i]}")
        }
        return logicalVDotsArray
    }
    companion object {
        private const val tagMsg = "My_LogicalVerticalDots"
    }
}