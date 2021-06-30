package music.elsystem.myconductor

import android.util.Log
import music.elsystem.myconductor.MainActivity.Companion.oneBarDots
import kotlin.math.pow

//１フレーム内に描かれるドットの大きさ、色の濃さを当クラスで配列として規定し
//レンダラーに引き渡す。
class Gradation {
    private val multiplier = 0.5
    fun mkRadiusArray() :FloatArray {
        var radiusArray = FloatArray(oneBarDots)
        var j = 0
        for (i in 0 until oneBarDots) {
            if (i < oneBarDots) {
                //iのmultiplier乗を算出し最大値が30となるように調整
                radiusArray[i] = (30.0 / (i+1).toDouble().pow(multiplier)).toFloat()
            } else {
                radiusArray[i] = 0f
            }
//            Log.i("radiusArray", "$i : ${radiusArray[i]}")
        }
        return radiusArray
    }
}