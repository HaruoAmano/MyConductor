package music.elsystem.myconductor

import android.graphics.Bitmap

object GraphicValue {
    var oneBarFrame = 0
    var dotSize = 0f
    //配列（ミュータブルリストとすることでobject（他モジュールから参照可能）でありながら可変とすることができる。
    var logicalX: MutableList<Int> = mutableListOf()
    var logicalY: MutableList<Int> = mutableListOf()
    var animRadius: MutableList<Float> = mutableListOf()
    //各ライン内でのYの相対位置（実際のBitMap合わせ縮尺する前の値）。描画時のradiusの基数として使用。
    var relativeYPosInLine: MutableList<Float> = mutableListOf()
    var animGradation: MutableList<Float> = mutableListOf()
    //描画に使用される数字のビットマップリスト
    var numberBitmapList: MutableList<Bitmap> = mutableListOf()
    var numberPosXList: MutableList<Int> = mutableListOf()
    var numberPosYList: MutableList<Int> = mutableListOf()
    //各拍子における、各表裏拍の最上点・最下点、
    //およびビットマップ上の色を管理する。
    //最終拍より描画を始める
    val strRgb = listOf(
        "cc0000", "ff9999",
        "6600", "66ff66",
        "99", "6666ff",
        "cccc00", "ffff00",
        "99cc", "66ffff",
        "6600cc", "ff99ff",
        "660000", "cc6666"
    )
    object TwoBeat {
        val highestPosition = listOf(50,50,66,68)
        val lowestPosition = listOf(711,850,849,711)
    }
    object ThreeBeat {
        val highestPosition = listOf(61,61,406,405,507,511)
        val lowestPosition = listOf(614,784,783,778,778,614)
    }
        object FourBeat {
        val highestPosition = listOf(29,28,94,96,268,267,280,284)
        val lowestPosition = listOf(590,810,810,739,739,733,731,590)
    }
}