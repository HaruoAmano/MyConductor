package music.elsystem.myconductor

import android.graphics.Bitmap
import android.util.Log
import music.elsystem.myconductor.Common.bitmapX
import music.elsystem.myconductor.Common.bitmapY
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.Common.Tact.*
import music.elsystem.myconductor.GraphicValue.logicalX
import music.elsystem.myconductor.GraphicValue.logicalY
import music.elsystem.myconductor.GraphicValue.numberPosXList
import music.elsystem.myconductor.GraphicValue.numberPosYList
import music.elsystem.myconductor.GraphicValue.TwoBeat
import music.elsystem.myconductor.GraphicValue.ThreeBeat
import music.elsystem.myconductor.GraphicValue.FourBeat
import music.elsystem.myconductor.GraphicValue.strRgb
import music.elsystem.myconductor.Util.Companion.mPow
import kotlin.math.cos
import kotlin.math.sin

//テンポ・拍子・モーションYをインプットとして論理的頂点座標を作成する。
class LogicalPosArray(
    private val rhythm: Int,
    private val tempo: Int,
    private val multiplier: Double
) {
    private val ut = Util()
    private val halfBeatDots = ut.halfBeatFrame(tempo)
    private val oneBarDots = ut.oneBarFrame(tempo, rhythm)

    //getMotionYで求めたY値を元に論理座標上のX,Yを指定されたリズム・テンポを元に算出する。
    fun setLogicalPosArray(bmpBeat: Bitmap?) {
        bmpBeat?.let {
//            Log.i("bmpBeat.height","${bmpBeat.height}")
//            Log.i("bmpBeat.width","${bmpBeat.width}")
            var motionYArray = getMotionY()
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
            numberPosXList = mutableListOf()
            numberPosYList = mutableListOf()
            //m:１小節のラインの数（rhythm * 2）
            //i:半拍分の配列に対するインデックス
            //k:Yに対応するXの位置
            Log.i("setLogicalPosArray", "rhythm: $rhythm")
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
                    //線の長さに合わせYを縮尺し、論理的頂点Yを確保する。
                    logicalY.add(
                        (motionYDotPos * (lineLength / bitmapY.toFloat())).toInt()
                                + highestPosition[m]
                    )
                    //ビットマップよりYに対応するXを走査する。
                    var getXSuccessSW = false //テスト用ロジック
                    for (k in 0 until bitmapX) {
                        if ((it.getPixel(k, logicalY[l]) and 0xffffff).toString(16) == strRgb[m]) {
                            //見つかった時点で論理的頂点Xを確保する。
                            logicalX.add(k)
                            getXSuccessSW = true  //テスト用ロジック
                            break
                        }
                    }
                    if (!getXSuccessSW) {
                        Log.i("x取り損ね", "m: $m Line目、logicalY:${logicalY[l]}")
                    }
//                    Log.i("setLogicalPosArray","logicalX:${logicalX[l]} logicalY:${logicalY[l]}")
                    l++
                }
            }
            for (n in 0 until oneBarDots step halfBeatDots) {
                if ((n / halfBeatDots) % 2 == 0) {
                    numberPosXList.add(logicalX[n])
                    numberPosYList.add(logicalY[n])
                }
            }
        } ?: Log.i(tagMsg, "ビットマップデータがnullです！！")
//        Log.i("numberPosXList", "$numberPosXList")
//        Log.i("numberPosYList", "$numberPosYList")
    }

    //テンポ、（および打点の重み）に応じ論理的にマッピングされたY値の
    //配列を生成する。
    //論理的なYの幅は1000とする。（現在のBitmapは1000x1000であるが、直接それとは関係ない）
    //この後、ビットマップの図形に合わせ縮尺する。
    //multiplier:乗数
    private fun getMotionY(): IntArray {
        val motionYDotPosArray = IntArray(halfBeatDots + 1)
        //0から最大点までの各Y位置を算出するが最大点は次のラインの始点で描画するため、
        //halfBeatDots+1が最大値となるよう比率を求める。
        when (tactType) {
            Heavy.name -> {
                var radian = 0.0
                for (i in 0..halfBeatDots) {
                    radian = ((kotlin.math.PI) * i / halfBeatDots)
                    motionYDotPosArray[i] = ((cos(radian) * (1000 - 1)).toInt() / 2) * -1 + 499
                }
            }
            Normal.name -> {
                for (i in 0..halfBeatDots) {
                    //べき乗の基数には1からhalfBeatDotsまでを代入する。
                    motionYDotPosArray[i] = ((1000 - 1) *
                            ((i).mPow(multiplier) / (halfBeatDots).mPow(multiplier))).toInt()
                }
            }
            Swing.name -> {
            }
        }
        return motionYDotPosArray
    }

    companion object {
        private const val tagMsg = "My_LogicalPosArray : "
    }
}