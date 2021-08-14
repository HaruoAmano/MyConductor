package music.elsystem.myconductor

import android.graphics.Bitmap

object GraphicValue {

    //描画に使用される数字のビットマップリスト
    var numberBitmapList: MutableList<Bitmap> = mutableListOf()

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
    object OneBeat {
        val highestPosition = listOf(50,50)
        val lowestPosition = listOf(950,950)
    }
    object TwoBeat {
        val highestPosition = listOf(85,85,101,103)
        val lowestPosition = listOf(746,885,884,746)
    }

    object ThreeBeat {
        val highestPosition = listOf(61, 61, 406, 405, 507, 511)
        val lowestPosition = listOf(614, 784, 783, 778, 778, 614)
    }

    object FourBeat {
        val highestPosition = listOf(82,81,147,149,321,320,333,337)
        val lowestPosition = listOf(643,863,863,792,792,786,784,643)
    }
    object FiveBeat_2_3 {
        val highestPosition = listOf(111,110,170,170,171,171,452,451,552,554)
        val lowestPosition = listOf(661,833,833,542,541,829,827,822,824,663)
    }
    object FiveBeat_3_2 {
        val highestPosition = listOf(85,85,430,429,530,532,163,163,148,148)
        val lowestPosition = listOf(464,808,805,800,802,642,639,715,715,467)
    }
    object SixBeat {
        val highestPosition = listOf(175,174,229,230,408,407,345,345,377,377,433,433)
        val lowestPosition = listOf(693,824,824,776,776,730,730,755,756,754,754,691)
    }
}