package music.elsystem.myconductor

import android.util.Log
import music.elsystem.myconductor.MainActivity.Companion.bmpBeat

//ビットマップデータより各拍および裏拍の最上点および最下点を取得する関数。
//結果はログ出力し、列挙型データに手で登録する。
//登録後は決して動かさない！！！！！！
class MeasureBmpLineHeight() {
    fun measureBmpLineHeight(beat: Int) {
        var intArgb = 0
        var strRgb = ""
        //現在6拍子をMAXと想定している。
        //この関数では、一拍表、裏といった数え方はせず、２拍子なら0,1,2,3と数える。
        //この関数では、一拍目表へのラインを一拍目表と呼ぶ。
        var startY = IntArray(12)
        var endY = IntArray(12)
        val rgbY = arrayOf(
            "ff0000",           //一拍目
            "ff7f00",
            "ff00",             //二拍目
            "ff7f",
            "ff",               //三拍目
            "7f00ff",
            "ffff00",           //四拍目
            "ffff7f",
            "ffff",             //五拍目
            "7fffff",
            "ff00ff",           //六拍目
            "ff7fff"
        )
        bmpBeat?.let {
            for (i in 0 until beat * 2) {
                for (y in 0..2999) {
                    for (x in 0..2999) {
                        intArgb = it.getPixel(x, y)
                        strRgb = (intArgb and 0xffffff).toString(16)
                        if (strRgb == rgbY[i]) {
                            if (startY[i] == 0) {
                                startY[i] = y
                            }
                            endY[i] = y
                        }
                    }
                }
                Log.i(tagMsg, "${(i + 2) / 2f}拍目最上点は ${startY[i]} 最下点は　${endY[i]}")
            }
        } ?: Log.i(tagMsg, "ビットマップデータがnullです！！")
    }

    companion object {
        private const val tagMsg = "My_mkLogicalPosition : "
    }
}