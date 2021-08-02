package music.elsystem.myconductor

import android.media.SoundPool

object Common {
    //ディスプレイサイズ
    var surfaceWidth = 0
    var surfaceHeight = 0
    //ビットマップサイズ
    var bitmapX =0
    var bitmapY =0
    //リスナーインターフェースで使用する項目
    var rhythm = 4
    var tempo = 60
    var tactType = Tact.Normal.name
    var motionYMultiplier = 1.0
    var radiusMultiplier = 1.0
    var alphaMultiplier = 1.0
    //タップしたタイミングで最終拍のナンバーが表示されるのを回避するためのフラグ。
    var justTappedSw = true
    //タップしたタイミングで最終拍のサウンドが鳴らされるのを回避するためのフラグ。
    //サウンドについては裏拍も含め回避する必要があるためjustTappedSwに加え
    //当スイッチも使用する。
    var justTappedSoundSw = true
    //サウンド関連*****************************************************************
    //サウンドの裏拍数
    var offBeatNum = 1
    var lstResIdOnbeatAll: MutableList<Int>  = mutableListOf()
    var lstSpOnbeat: MutableList<Int>  = mutableListOf()
    var spOffbeatVoice = 0
    var spOffbeatVoice3 = 0
    var spOffbeatVoice4 = 0

    var soundPool: SoundPool? = null
    enum class SoundName {
        Voice,
        Click
    }
    enum class Tact {
        Heavy,
        Normal,
        Swing
    }
}
