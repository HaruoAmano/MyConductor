package music.elsystem.myconductor

import android.util.Log
import music.elsystem.myconductor.MainActivity.Companion.mPow

//１フレーム内に描かれるドットの大きさ、色の濃さを当クラスで配列として規定し
//レンダラーに引き渡す。
//最初が大きくてだんだん小さくなる配列（●＞----- こんな感じ）を作成する。
class AnimationArray(rhythm: Int, tempo: Int, val radiusMultiplier: Double = 0.4, val gradationMultiplier: Double = 0.4) {
    private val ut = Util()
    private val oneBarDots = ut.oneBarDots(tempo, rhythm)
    //ドットサイズの変更を定義する関数
    fun radiusArray(): FloatArray {
        var radiusArray = FloatArray(oneBarDots)
        var j = 0
        for (i in 0 until oneBarDots) {
            //iのmultiplier乗を算出し最大値が30となるように調整
            radiusArray[i] = (30.0 / (i + 1).mPow(radiusMultiplier)).let { if (it > 1.0) { it } else { 1.0 } }.toFloat()
        }
        return radiusArray
    }
    //グラデーションの変更を定義する関数
    fun gradationArray(): FloatArray {
        var gradationArray = FloatArray(oneBarDots)
        var j = 0
        for (i in 0 until oneBarDots) {
            //iのmultiplier乗を算出し最大値が30となるように調整
            gradationArray[i] = (0.7f * ((oneBarDots.mPow(gradationMultiplier) - i.mPow(gradationMultiplier)) / oneBarDots.mPow(gradationMultiplier))).toFloat() + 0.3f
//            Log.i(tagMsg,"gradationArray: ${gradationArray[i]}")
        }
        return gradationArray
    }
    companion object {
        private const val tagMsg = "My_MainActivity"
    }
}