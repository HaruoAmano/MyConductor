package music.elsystem.myconductor.surfaceview.dotSurfaceview

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import music.elsystem.myconductor.Common
import music.elsystem.myconductor.Common.RenderMode.Motion
import music.elsystem.myconductor.Common.RenderMode.Setting
import music.elsystem.myconductor.Common.Tact
import music.elsystem.myconductor.Common.justTappedSoundSw
import music.elsystem.myconductor.Common.offBeatNum
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.Common.SoundName.*
import music.elsystem.myconductor.Common.downBeatVolume
import music.elsystem.myconductor.Common.lstSpOnbeat
import music.elsystem.myconductor.Common.renderMode
import music.elsystem.myconductor.Common.soundPool
import music.elsystem.myconductor.Common.spOffbeatVoice
import music.elsystem.myconductor.Common.spOffbeatVoice2
import music.elsystem.myconductor.Common.subWeakBeatVolume
import music.elsystem.myconductor.Common.weakBeatVolume
import music.elsystem.myconductor.R


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
                    //メトロノームの最小時間単位を算出する。
                    val minInterval = oneBeatFrame / offBeatNum
                    //発音すべきフレームカウントとなった場合
                    if (frameCount % minInterval == 0) {
                        //表拍
                        if (frameCount % oneBeatFrame == 0) {
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
                        }
                        2f -> {
                            soundPool?.play(
                                spOffbeatVoice,
                                weakBeatVolume,
                                weakBeatVolume,
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
