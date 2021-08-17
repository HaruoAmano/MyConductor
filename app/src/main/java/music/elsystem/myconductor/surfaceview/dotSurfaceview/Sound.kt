package music.elsystem.myconductor.surfaceview.dotSurfaceview

import android.util.Log
import music.elsystem.myconductor.Common.RenderMode.Setting
import music.elsystem.myconductor.Common.Tact
import music.elsystem.myconductor.Common.justTappedSoundSw
import music.elsystem.myconductor.Common.noteNumPerBeat
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.Common.downBeatVolumeHeavy
import music.elsystem.myconductor.Common.downBeatVolumeLight
import music.elsystem.myconductor.Common.lstSpOnbeat
import music.elsystem.myconductor.Common.renderMode
import music.elsystem.myconductor.Common.soundPool
import music.elsystem.myconductor.Common.spOffbeatVoice
import music.elsystem.myconductor.Common.spOffbeatVoice2
import music.elsystem.myconductor.Common.subWeakBeatVolumeHeavy
import music.elsystem.myconductor.Common.subWeakBeatVolumeLight
import music.elsystem.myconductor.Common.weakBeatVolumeHeavy
import music.elsystem.myconductor.Common.weakBeatVolumeLight


class Sound() {
    var downBeatVolume = 0f
    var weakBeatVolume = 0f
    var subWeakBeatVolume = 0f
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
            if (tactType == Tact.Heavy.name) {
                downBeatVolume = downBeatVolumeHeavy
                weakBeatVolume = weakBeatVolumeHeavy
                subWeakBeatVolume = subWeakBeatVolumeHeavy
            } else {
                downBeatVolume = downBeatVolumeLight
                weakBeatVolume = weakBeatVolumeLight
                subWeakBeatVolume = subWeakBeatVolumeLight
            }
        }
        //メトロノームの最小時間単位を算出する。
        val minInterval = oneBeatFrame / noteNumPerBeat
        //発音すべきフレームカウントとなった場合
        if (frameCount % minInterval == 0) {
            //表拍
            if (frameCount % oneBeatFrame == 0) {
//                Log.i(
//                    "$tactType 各拍頭",
//                    "${System.currentTimeMillis()},frameCount:$frameCount,oneBeatFrame:$oneBeatFrame,minInterval:$minInterval"
//                )
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
                        weakBeatVolume * 0.6f,
                        weakBeatVolume * 0.6f,
                        1,
                        0,
                        1.0f
                    )
                } else {
                    //裏裏拍の場合
                    soundPool?.play(
                        spOffbeatVoice2,
                        subWeakBeatVolume * 0.6f,
                        subWeakBeatVolume * 0.6f,
                        1,
                        0,
                        1.3f
                    )
                }
            }
        }
    }
}
