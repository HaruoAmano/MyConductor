package music.elsystem.myconductor

import android.util.Log
import music.elsystem.myconductor.MainActivity.Companion.bmpBeat
import music.elsystem.myconductor.MainActivity.Companion.halfBeatDots
import music.elsystem.myconductor.MainActivity.Companion.oneBarDots
import music.elsystem.myconductor.MainActivity.Companion.rhythm
import kotlin.math.pow

//テンポ・拍子・モーションYをインプットとして論理的頂点座標を作成する。
class LogicalPosition {
    //テンポ、（および打点の重み）に応じ論理的に（0～2999）マッピングされたY値の
    //配列を生成する。
    //この後、ビットマップの図形に合わせ縮尺する。
    private fun getMotionY() :IntArray{
        var motionYDotPosArray = IntArray(halfBeatDots)
        val multiplier = 2.0 //multiplier:乗数
        for (i in 0 until halfBeatDots) {
            //iのmultiplier乗を算出し最大値が2999となるように調整
            //べき乗の基数には1からhalfBeatDotsまでを代入する。
            motionYDotPosArray[i] = (2999.0 *
                    ((i+1).mPow(multiplier)  / halfBeatDots.mPow(multiplier))).toInt()
//            Log.i(tagMsg,"${motionYDotPosArray[i]}")
        }
        return motionYDotPosArray
    }
    //getMotionYで求めたY値を元に論理座標上のX,Yを指定されたリズム・テンポを元に算出する。
    fun getLogicalPosition() :Pair<IntArray,IntArray> {
        var motionYArray = getMotionY()
        //一小節分のエリアを確保する。
        var logicalX = IntArray(oneBarDots)
        var logicalY = IntArray(oneBarDots)
        //l:一小節分の配列に対するインデックス（getHalfbeatDots * 2 * rhythm）
        //m:１拍内のラインの数（rhythm * 2）
        //i:半拍分の配列に対するインデックス（getHalfbeatDots）
        //j:半拍内のYの位置に対するインデックス
        //k:Yに対応するXの位置
        var l = 0
        bmpBeat?.let {
            for (m in 0 until rhythm * 2) {
                val lineLength =
                    LineConst.lowestPosition[m] - LineConst.highestPosition[m]
                for (i in 0 until halfBeatDots) {
                    //表拍へ向かう時はmotionYDotPosArrayの順（上から下）だが、裏拍へ向かうときは下から上のためインデックスをひっくり返す。
                    var motionYDotPos: Int = if (m % 2 == 0) {
                        motionYArray[i]
                    } else {
                        motionYArray[(halfBeatDots - 1) - i]
                    }
                    //ビットマップの線に合わせYを縮尺し、論理的頂点Yを確保する。
                    logicalY[l] =
                        (motionYDotPos * (lineLength / 3000f)).toInt() + LineConst.highestPosition[m]
                    //ビットマップよりYに対応するXを走査する。
                    for (k in 0..2999) {
                        //bmpBeatがnullなら「拍子が選択されていない」旨を何らかの形でメッセージとして出す。
                        if ((it.getPixel(
                                k,
                                logicalY[l]
                            ) and 0xffffff).toString(16) == LineConst.strRgb[m]
                        ) {
                            //見つかった時点で論理的頂点Xを確保する。
                            logicalX[l] = k
                            break
                        }
                    }
//                    Log.i(tagMsg, "l:$l logicalX:${logicalX[l]} logicalY:${logicalY[l]}")
                    l++
                }
            }
        } ?: Log.i(tagMsg,"ビットマップデータがnullです！！")
        //２つの結果を戻り値にするためPairでまとめる。
        return Pair(logicalX,logicalY)
    }
    private fun Int.mPow (multiplier :Double = 2.0) = this.toDouble().pow(multiplier)

    companion object {
        private const val tagMsg = "My_mkLogicalPosition : "
    }
    //各拍子における、各表裏拍の最上点・最下点、
    //およびビットマップ上の色を管理する。
    object LineConst {
        val strRgb = listOf("ff0000", "ff7f00", "ff00", "ff7f")
        val highestPosition = listOf(157, 357, 359, 150)
        val lowestPosition = listOf(2850, 2846, 2406, 2405)

    }
}