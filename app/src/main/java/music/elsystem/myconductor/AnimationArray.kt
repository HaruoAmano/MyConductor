package music.elsystem.myconductor

import music.elsystem.myconductor.GraphicValue.animGradation
import music.elsystem.myconductor.GraphicValue.animRadius
import music.elsystem.myconductor.MainActivity.Companion.mPow

//１フレーム内に描かれるドットの大きさ、色の濃さを当クラスで配列として規定し
//レンダラーに引き渡す。
//最初が大きくてだんだん小さくなる配列（●＞----- こんな感じ）を作成する。
class AnimationArray
    (rhythm: Int, tempo: Int, private val radiusMultiplier: Double = 0.4,
     private val gradationMultiplier: Double = 0.4) {
    private val ut = Util()
    private val oneBarDots = ut.oneBarFrame(tempo, rhythm)
    //ドットサイズの変更を定義する関数
    fun radiusArray() :Boolean{
        animRadius = mutableListOf()
        for (i in 0 until oneBarDots) {
            //iのmultiplier乗を算出し最大値が30、最小値が3となるように調整
            animRadius.add((30.0 / (i + 1).mPow(radiusMultiplier))
                .let { if (it > 1.0) { it } else { 1.0 } }.toFloat())
        }
        return true
    }
    //グラデーションの変更を定義する関数
    fun gradationArray():Boolean{
        animGradation = mutableListOf()
        for (i in 0 until oneBarDots) {
            //iのmultiplier乗を算出し最大値が1となるように調整
            animGradation.add(((oneBarDots.mPow(gradationMultiplier) - i.mPow(gradationMultiplier))
                    / oneBarDots.mPow(gradationMultiplier)).toFloat())
//            Log.i(tagMsg,"gradationArray: ${gradationArray[i]}")
        }
        return true
    }
    companion object {
        private const val tagMsg = "My_MainActivity"
    }
}