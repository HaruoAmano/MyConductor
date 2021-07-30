package music.elsystem.myconductor

import android.graphics.Bitmap
import android.util.Log
import music.elsystem.myconductor.GraphicValue.logicalX
import music.elsystem.myconductor.GraphicValue.logicalY
import music.elsystem.myconductor.GraphicValue.numberPosXList
import music.elsystem.myconductor.GraphicValue.numberPosYList
import music.elsystem.myconductor.GraphicValue.relativeYPosInLine
import music.elsystem.myconductor.GraphicValue.TwoBeat
import music.elsystem.myconductor.GraphicValue.ThreeBeat
import music.elsystem.myconductor.GraphicValue.FourBeat
import music.elsystem.myconductor.GraphicValue.strRgb
import music.elsystem.myconductor.MainActivity.Companion.mPow

//テンポ・拍子・モーションYをインプットとして論理的頂点座標を作成する。
class LogicalPosArray(private val rhythm: Int, private val tempo: Int, private val multiplier: Double) {
    private val ut = Util()
    private val halfBeatDots = ut.halfBeatFrame(tempo)
    private val oneBarDots = ut.oneBarFrame(tempo,rhythm)

    //テンポ、（および打点の重み）に応じ論理的にマッピングされたY値の
    //配列を生成する。
    //この後、ビットマップの図形に合わせ縮尺する。
    //multiplier:乗数
    private fun getMotionY(bmpBeatHeight: Int): IntArray {
//        Log.i("getMotionY", "halfBeatDots:$halfBeatDots")
        val motionYDotPosArray = IntArray(halfBeatDots + 1)
        //0から最大点までの各Y位置を算出するが最大点は次のラインの始点で描画するため、
        //halfBeatDots+1が最大値となるよう比率を求める。
        for (i in 0..halfBeatDots) {
            //べき乗の基数には1からhalfBeatDotsまでを代入する。
            motionYDotPosArray[i] = ((bmpBeatHeight - 1) *
                    ((i).mPow(multiplier) / (halfBeatDots).mPow(multiplier))).toInt()
//            Log.i("motionYDotPosArray", "i:$i,${motionYDotPosArray[i]}")
        }
        return motionYDotPosArray
    }

    //getMotionYで求めたY値を元に論理座標上のX,Yを指定されたリズム・テンポを元に算出する。
    //戻り地としてlogicalX、logicalYをセットで返したいため、戻り値はPairでまとめる。
    fun setLogicalPosArray(bmpBeat: Bitmap?) :Boolean{
        bmpBeat?.let {
//            Log.i("bmpBeat.height","${bmpBeat.height}")
//            Log.i("bmpBeat.width","${bmpBeat.width}")
            var motionYArray = getMotionY(bmpBeat.height)
            //拍ごとの各ラインの最上点・最下点のコンスト値を共通で扱えるよう
            // 変数highestPosition, lowestPositionにコピーする。
            var highestPosition = mutableListOf<Int>()
            var lowestPosition = mutableListOf<Int>()
            when (rhythm) {
                2 -> {
                    highestPosition = TwoBeat.highestPosition.toMutableList()
                    lowestPosition = TwoBeat.lowestPosition.toMutableList()
                }
                3 -> {
                    highestPosition = ThreeBeat.highestPosition.toMutableList()
                    lowestPosition = ThreeBeat.lowestPosition.toMutableList()
                }
                4 -> {
                    highestPosition = FourBeat.highestPosition.toMutableList()
                    lowestPosition = FourBeat.lowestPosition.toMutableList()
                }

            }
            //l:一小節分の配列に対するインデックス（halfbeatDots * 2 * rhythm）
            var l = 0
            //各リストの初期化
            logicalX = mutableListOf()
            logicalY = mutableListOf()
            relativeYPosInLine = mutableListOf()
            numberPosXList = mutableListOf()
            numberPosYList = mutableListOf()
            //m:１小節のラインの数（rhythm * 2）
            //i:半拍分の配列に対するインデックス
            //k:Yに対応するXの位置
            Log.i("setLogicalPosArray","rhythm: $rhythm")
            for (m in 0 until rhythm * 2) {
//                Log.i("setLogicalPosArray","なんライン目？: ${m + 1}")
                val lineLength =
                    lowestPosition[m] - highestPosition[m] + 1
                //ラインの最終ドットは次のラインの先頭とダブるため、次のラインに任す。
                for (i in 0 until halfBeatDots) {
                    //表拍へ向かう時はmotionYDotPosArrayの順（上から下）だが、
                        // 裏拍へ向かうときは下から上のためインデックスをひっくり返す。
                    var motionYDotPos: Int = if (m % 2 != 0) {
                        //表拍へ向かうライン
                        motionYArray[i]
                    } else {
                        //裏拍へ向かうライン
                        motionYArray[halfBeatDots - i]
                    }
                    //motionYDotPosを一小節分確保する（描画時のradiusの基数として使用）。
                    relativeYPosInLine.add(motionYDotPos.toFloat())
                    //線の長さに合わせYを縮尺し、論理的頂点Yを確保する。
                    logicalY.add((motionYDotPos * (lineLength / bmpBeat.height.toFloat())).toInt()
                            + highestPosition[m])
                    //ビットマップよりYに対応するXを走査する。
                    var getXSuccessSW = false //テスト用ロジック
                    for (k in 0 until bmpBeat.width) {
                        if ((it.getPixel(k, logicalY[l]) and 0xffffff).toString(16) == strRgb[m])
                        {
                            //見つかった時点で論理的頂点Xを確保する。
                            logicalX.add(k)
                            getXSuccessSW = true  //テスト用ロジック
                            break
                        }
                    }
                    if (!getXSuccessSW)  {
                        Log.i("x取り損ね", "m: $m Line目、logicalY:${logicalY[l]}")
                    }
//                    Log.i("setLogicalPosArray","logicalX:${logicalX[l]} logicalY:${logicalY[l]}")
                    l++
                }
            }
            //出来上がった座標リストから各拍の打点を抽出する。
//            Log.i("bmpBeat.width", "${bmpBeat.width}")
//            Log.i("tempo", "$tempo")
//            Log.i("index:l", "$l")
//            Log.i("logicalX", "$logicalX")
//            Log.i("logicalY", "$logicalY")
//            Log.i("halfBeatDots", "$halfBeatDots")
//            Log.i("oneBarDots", "$oneBarDots")
            for (n in 0 until oneBarDots step halfBeatDots){
                if ((n / halfBeatDots) % 2 == 0){
                    numberPosXList.add(logicalX[n])
                    numberPosYList.add(logicalY[n])
                }
            }
        } ?: Log.i(tagMsg, "ビットマップデータがnullです！！")
        Log.i("numberPosXList", "$numberPosXList")
        Log.i("numberPosYList", "$numberPosYList")
        return true
    }
    companion object {
        private const val tagMsg = "My_LogicalPosArray : "
    }
}