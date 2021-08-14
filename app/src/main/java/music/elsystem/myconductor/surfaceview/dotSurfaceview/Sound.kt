package music.elsystem.myconductor.surfaceview.dotSurfaceview

import android.util.Log
import music.elsystem.myconductor.Common.RenderMode.Setting
import music.elsystem.myconductor.Common.Tact
import music.elsystem.myconductor.Common.justTappedSoundSw
import music.elsystem.myconductor.Common.offBeatNum
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.Common.downBeatVolumeHeavy
import music.elsystem.myconductor.Common.downBeatVolumeLight
import music.elsystem.myconductor.Common.downBeatVolumeSwing
import music.elsystem.myconductor.Common.lstSpOnbeat
import music.elsystem.myconductor.Common.renderMode
import music.elsystem.myconductor.Common.soundPool
import music.elsystem.myconductor.Common.spOffbeatVoice
import music.elsystem.myconductor.Common.spOffbeatVoice2
import music.elsystem.myconductor.Common.subWeakBeatVolumeHeavy
import music.elsystem.myconductor.Common.subWeakBeatVolumeLight
import music.elsystem.myconductor.Common.weakBeatVolumeHeavy
import music.elsystem.myconductor.Common.weakBeatVolumeLight
import music.elsystem.myconductor.Common.weakBeatVolumeSwing
import java.sql.Timestamp
import java.time.LocalTime


class Sound() {
    //メトロノームサウンドを鳴らす。**********************************************************
    fun sound(
        halfBeatFrame: Int,
        oneBeatFrame: Int,
        frameCount: Int
    ) {
        //最初のタイミングで最終拍が発音されるのを回避する。
        if (renderMode == Setting.name) {
            justTappedSoundSw = false
        } else {
            if (frameCount / oneBeatFrame > 0) {
                justTappedSoundSw = false
            }
        }
        if (!justTappedSoundSw) {
            when (tactType) {
                Tact.Heavy.name, Tact.Light.name -> {
                    var downBeatVolume =0f
                    var weakBeatVolume =0f
                    var subWeakBeatVolume=0f
                    if (tactType ==Tact.Heavy.name) {
                        downBeatVolume = downBeatVolumeHeavy
                        weakBeatVolume = weakBeatVolumeHeavy
                        subWeakBeatVolume= subWeakBeatVolumeHeavy
                    } else {
                        downBeatVolume = downBeatVolumeLight
                        weakBeatVolume = weakBeatVolumeLight
                        subWeakBeatVolume= subWeakBeatVolumeLight
                    }
                    //メトロノームの最小時間単位を算出する。
                    val minInterval = oneBeatFrame / offBeatNum
                    //発音すべきフレームカウントとなった場合
                    if (frameCount % minInterval == 0) {
                        //表拍
                        if (frameCount % oneBeatFrame == 0) {
                            Log.i("$tactType 各拍頭","${System.currentTimeMillis()}")
                            if (renderMode == Setting.name) {
                                soundPool?.play(
                                    lstSpOnbeat[1],
                                    downBeatVolume,
                                    downBeatVolume,
                                    1,
                                    0,
                                    1.0f
                                )
                            } else {
                                soundPool?.play(
                                    lstSpOnbeat[frameCount / oneBeatFrame],
                                    downBeatVolume,
                                    downBeatVolume,
                                    1,
                                    0,
                                    1.0f
                                )
                            }
                        } else {
                            //裏拍または３連の場合
                            if (frameCount % halfBeatFrame == 0 || frameCount % (oneBeatFrame / 3) == 0) {
                                soundPool?.play(
                                    spOffbeatVoice,
                                    weakBeatVolume,
                                    weakBeatVolume,
                                    1,
                                    0,
                                    1.0f
                                )
                            } else {
                                //裏裏拍の場合
                                soundPool?.play(
                                    spOffbeatVoice2,
                                    subWeakBeatVolume,
                                    subWeakBeatVolume,
                                    1,
                                    0,
                                    1.0f
                                )
                            }
                        }
                    }
                }
                Tact.Swing.name -> {
                    when ((frameCount % oneBeatFrame) / (halfBeatFrame / 2).toFloat()) {
                        0f -> {
                            Log.i("$tactType 各拍頭","${System.currentTimeMillis()}")
                            if (renderMode == Setting.name) {
                                soundPool?.play(
                                    lstSpOnbeat[1],
                                    downBeatVolumeSwing,
                                    downBeatVolumeSwing,
                                    1,
                                    0,
                                    1.0f
                                )
                            } else {
                                soundPool?.play(
                                    lstSpOnbeat[frameCount / oneBeatFrame],
                                    downBeatVolumeSwing,
                                    downBeatVolumeSwing,
                                    1,
                                    0,
                                    1.0f
                                )
                            }
                        }
                        2f -> {
                            soundPool?.play(
                                spOffbeatVoice,
                                weakBeatVolumeSwing,
                                weakBeatVolumeSwing,
                                1,
                                0,
                                1.0f
                            )
                        }
                    }
                }
            }
        }
    }
}
