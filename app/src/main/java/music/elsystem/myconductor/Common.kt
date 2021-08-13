package music.elsystem.myconductor

import android.graphics.Bitmap
import android.media.SoundPool

object Common {
    //メインサーフェスビューサイズ
    var surfaceWidth = 0
    var surfaceHeight = 0
    //セッチングサーフェスビューサイズ
    var settingSurfaceWidth = 0
    var settingSurfaceHeight = 0
    //論理的座標空間のXY軸
    var logeicalSpaceX = 1000
    var logeicalSpaceY = 1000
    //メインとセッティングで共有する項目
    var rhythm = 4
    var bmpBeat: Bitmap? = null

    var tempo = 60
    var voice = SoundName.Voice.name
    var dotSize = 15.0f
    var motionYMultiplier = 1.0
    //裏拍のドットサイズ
    var offbeatDotSizeHeavy = 0.85f
    var offbeatDotSizeSwing = 1.0f
    //タクト＝Heavyの時の打点での滞留する度合
    var stayingFrameRate = 0.07f
    //タクト＝Swingの時のドットサイズの打点からの折り返し地点を管理
    var perOfHalfBeatSwing = 0.0f

    var renderMode = RenderMode.Line.name

    var soundPool: SoundPool? = null
    var lstSpOnbeat: MutableList<Int> = mutableListOf()
    var spOffbeatVoice = 0
    var spOffbeatVoice2 = 0


    var tactType = Tact.Light.name
    //radiusMultiplier,alphaMultiplier,flashSWについてはUI対象としていないためここの値で決定される。
    var radiusMultiplier = 4.0
    var alphaMultiplier = 5.0
    var flashSW = false
    //タップしたタイミングで最終拍のナンバーが表示されるのを回避するためのフラグ。
    var justTappedSw = true
    //タップしたタイミングで最終拍のサウンドが鳴らされるのを回避するためのフラグ。
    //サウンドについては裏拍も含め回避する必要があるためjustTappedSwに加え
    //当スイッチも使用する。
    var justTappedSoundSw = true
    //サウンド関連*****************************************************************
    //サウンドの裏拍数
    var offBeatNum = 1
    //サウンド・ボリューム
    var downBeatVolume = 1.0f
    var weakBeatVolume = 1.0f
    var subWeakBeatVolume = 1.0f


    enum class SoundName {
        Voice,
        Click
    }
    enum class Tact {
        Heavy,
        Light,
        Swing
    }
    enum class RenderMode {
        Motion,
        Line,
        Setting
    }
}
