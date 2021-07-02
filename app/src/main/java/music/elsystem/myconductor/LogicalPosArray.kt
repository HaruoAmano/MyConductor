package music.elsystem.myconductor

import android.graphics.Bitmap
import android.util.Log
import music.elsystem.myconductor.MainActivity.Companion.mPow

//テンポ・拍子・モーションYをインプットとして論理的頂点座標を作成する。
class LogicalPosArray (rhythm: Int, tempo: Int, private val multiplier: Double){
    val rhythm = rhythm
    val tempo = tempo
    //リフレッシュレート60Hzを前提に一旦設計
    private val halfBeatDots = (1800f / tempo.toFloat()).toInt()
    private val oneBarDots = halfBeatDots * 2 * rhythm
    //テンポ、（および打点の重み）に応じ論理的に（0～2999）マッピングされたY値の
    //配列を生成する。
    //この後、ビットマップの図形に合わせ縮尺する。
    //multiplier:乗数
    private fun getMotionY() :IntArray{
        Log.i(tagMsg,"halfBeatDots:$halfBeatDots")
        val motionYDotPosArray = IntArray(halfBeatDots + 1)
        //1から最大点2999までを算出するが最大点は次のラインの始点で描画するため、
        //halfBeatDots+1が最大値となるよう比率を求める。
        for (i in 0..halfBeatDots) {
            //べき乗の基数には1からhalfBeatDotsまでを代入する。
            motionYDotPosArray[i] = (2999.0 *
                    ((i).mPow(multiplier)  / (halfBeatDots).mPow(multiplier))).toInt()
//            Log.i(tagMsg,"${motionYDotPosArray[i]}")
        }
        return motionYDotPosArray
    }
    //getMotionYで求めたY値を元に論理座標上のX,Yを指定されたリズム・テンポを元に算出する。
    fun getLogicalPosArray(bmpBeat: Bitmap?) :Pair<IntArray,IntArray> {
        var motionYArray = getMotionY()
        //一小節分のエリアを確保する。
        var logicalX = IntArray(oneBarDots)
        var logicalY = IntArray(oneBarDots)
        //l:一小節分の配列に対するインデックス（getHalfbeatDots * 2 * rhythm）
        var l = 0
        bmpBeat?.let {
            var highestPosition = mutableListOf<Int>()
            var lowestPosition = mutableListOf<Int>()
            when (rhythm) {
                2 -> {
                    highestPosition = LineConst.twoBeat.highestPosition.toMutableList()
                    lowestPosition = LineConst.twoBeat.lowestPosition.toMutableList()
                }
                3 -> {
                    highestPosition = LineConst.twoBeat.highestPosition.toMutableList()
                    lowestPosition = LineConst.twoBeat.lowestPosition.toMutableList()
                }
                4 -> {
                    highestPosition = LineConst.fourBeat.highestPosition.toMutableList()
                    lowestPosition = LineConst.fourBeat.lowestPosition.toMutableList()
                }

            }
            //m:１小節のラインの数（rhythm * 2）
            //i:半拍分の配列に対するインデックス
            //j:半拍内のYの位置に対するインデックス
            //k:Yに対応するXの位置
            for (m in 0 until rhythm * 2) {
                val lineLength =
                    lowestPosition[m] - highestPosition[m] +1
//                for (i in 0 until halfBeatDots - 1) {     //-1の意味：ラインの最終ドットは次のラインの先頭とダブるため、次のラインに任す。
                for (i in 0 until halfBeatDots) {
                    //表拍へ向かう時はmotionYDotPosArrayの順（上から下）だが、裏拍へ向かうときは下から上のためインデックスをひっくり返す。
                    var motionYDotPos: Int = if (m % 2 == 0) {
                        //表拍へ向かうライン
                        motionYArray[i]
                    } else {
                        //裏拍へ向かうライン
                        motionYArray[(halfBeatDots) - i]
                    }
                    //線の長さに合わせYを縮尺し、論理的頂点Yを確保する。
                    logicalY[l] =
                        (motionYDotPos * (lineLength / 3000f)).toInt() + highestPosition[m]
                    //ビットマップよりYに対応するXを走査する。
                    for (k in 0..2999) {
                        //bmpBeatがnullなら「拍子が選択されていない」旨を何らかの形でメッセージとして出す。
                        if ((it.getPixel(k, logicalY[l]) and 0xffffff).toString(16) == LineConst.strRgb[m]) {
                            //見つかった時点で論理的頂点Xを確保する。
                            logicalX[l] = k
                            break
                        }
                    }
                    l++
                }
            }
        } ?: Log.i(tagMsg,"ビットマップデータがnullです！！")
        //２つの結果を戻り値にするためPairでまとめる。
        return Pair(logicalX,logicalY)
    }


    companion object {
        private const val tagMsg = "My_mkLogicalPosition : "
    }
    //各拍子における、各表裏拍の最上点・最下点、
    //およびビットマップ上の色を管理する。
    object LineConst {
        val strRgb = listOf("ff0000", "ff7f00", "ff00", "ff7f",
                            "ff","7f00ff","ffff00","ffff7f",
                            "ffff","7fffff","ff00ff","ff7fff")
        object twoBeat {
            val highestPosition = listOf(157, 357, 359, 150)
            val lowestPosition = listOf(2850, 2846, 2406, 2405)
        }
        object fourBeat {
            val highestPosition = listOf(152, 705, 704, 1224,1223,1269,1268,150)
            val lowestPosition = listOf(2850, 2849, 2634, 2633,2451,2453,2187,2186)
        }

    }
}