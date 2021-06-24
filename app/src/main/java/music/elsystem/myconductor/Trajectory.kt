package music.elsystem.myconductor

import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

//ここでYは上位で算出されたYの相対値（1000.0f～0.0f）とする。
//一旦、１小節分のvertexデータを作成すると想定して設計する。
class Trajectory {
    private val util = Util()
    fun getVertices(logicalY: FloatArray): FloatArray {
        val trajectoryArray = FloatArray(util.getHalfbeatDots() * 2)
//        when (rhythm) {
//            //２拍子
//            2 -> {
                //一拍目
                var vertexIdx = 0
                for (i in 0 until util.getHalfbeatDots()) {
                    //trajectoryXを算出する関数。
                    if (logicalY[i] >= 100.0f) {
                        trajectoryArray[vertexIdx] = util.coX(listPointX[0])   ///最終的にverticesにx,y,x,y,....となったものをこの関数として返す
                        trajectoryArray[vertexIdx+1] = util.coY(get1000zaY(logicalY[i]))
                    } else {
                        //下部は100を半径とする円弧として描く、インプットされたYよりピタゴラスの定理を使用してtrajectoryXを算出する。
                        trajectoryArray[vertexIdx] = util.coX(
                            (
                             //ここでNaNが発生している。
                             sqrt(
                                 100.0.pow(2.0)
                                         - (logicalY[i].toDouble() - 800.0).pow(2.0)
                             )
                             ).toFloat()) //800はさっさと変数化すること
                            trajectoryArray[vertexIdx+1] = util.coY(get1000zaY(logicalY[i]))
                    }
//                    Log.i(tagMsg,"i: $i logicalY: ${get1000zaY(logicalY[i])} x: ${trajectoryArray[vertexIdx]} y: ${trajectoryArray[vertexIdx+1]}")
                    vertexIdx +=2
//                }
//            }
        }
        return trajectoryArray
    }

    //インプットされたyから1000×1000座標上のtrajectoryYの要素を算出する。
    private fun get1000zaY(y: Float) :Float{
        //yの一拍目の最大幅を算出する。
        val maxY = listPointY[0] - listPointY[1]
        //yに対する縮尺比を算出する。（yの1000×1000座標の位置の算出に使用）
        var ratioY = maxY.toFloat() / 1000f
        var margin = 1000f - listPointY[0]
        return y * ratioY + margin
    }

    companion object {
        val listPointX = listOf(250f, 400f, 750f, 600f)
        val listPointY = listOf(900f, 100f, 800f, 200f)

        private const val tagMsg = "My_Trajectory"
    }
}