package music.elsystem.myconductor.gldraw.dotDraw

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import music.elsystem.myconductor.Common.Tact
import music.elsystem.myconductor.Common.justTappedSoundSw
import music.elsystem.myconductor.Common.offBeatNum
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.Common.SoundName.*
import music.elsystem.myconductor.R


class Sound() {
    var lstResIdOnbeatAll: MutableList<Int>  = mutableListOf()
    var lstSpOnbeat: MutableList<Int>  = mutableListOf()
    var spOffbeatVoice = 0
    var spOffbeatVoice3 = 0
    var spOffbeatVoice4 = 0

    var soundPool: SoundPool? = null
    //メトロノームサウンドを鳴らす。**********************************************************
    fun sound(
        halfBeatFrame: Int,
        oneBeatFrame: Int,
        frameCount: Int
    ) {
        //最初のタイミングで最終拍が発音されるのを回避する。
        if (frameCount / oneBeatFrame > 0) {
            justTappedSoundSw = false
        }
        if (!justTappedSoundSw) {
            when (tactType) {
                Tact.Heavy.name, Tact.Normal.name -> {
                    //メトロノームの最小時間単位を算出する。
                    val minInterval = oneBeatFrame / offBeatNum
                    if (frameCount % minInterval == 0) {
                        //表拍
                        if (frameCount % oneBeatFrame == 0) {
                            soundPool?.play(
                                lstSpOnbeat[frameCount / oneBeatFrame],
                                1.0f,
                                1.0f,
                                1,
                                0,
                                1.0f
                            )
                        } else {
                            soundPool?.play(spOffbeatVoice, 0.8f, 0.8f, 1, 0, 1.0f)
                        }
                    }
                }
                Tact.Swing.name -> {
                    when ((frameCount % oneBeatFrame) / (halfBeatFrame / 2).toFloat()) {
                        0f -> {
                            soundPool?.play(
                                lstSpOnbeat[frameCount / oneBeatFrame],
                                1.0f,
                                1.0f,
                                1,
                                0,
                                1.0f
                            )
                        }
                        2f -> {
                            soundPool?.play(spOffbeatVoice, 0.8f, 0.8f, 1, 0, 1.0f)
                        }
                    }
                }
            }
        }
    }
    //音声ファイルを準備する。
    fun setSoundList(voice: String,rhythm: Int) {
        //選択されたVoiceに対する音声ファイルを読み込む。
        lstResIdOnbeatAll = mutableListOf()
        when (voice) {
            Voice.name -> {
                lstResIdOnbeatAll.add(R.raw.one)
                lstResIdOnbeatAll.add(R.raw.two)
                lstResIdOnbeatAll.add(R.raw.three)
                lstResIdOnbeatAll.add(R.raw.four)
                lstResIdOnbeatAll.add(R.raw.five)
                lstResIdOnbeatAll.add(R.raw.six)
                lstResIdOnbeatAll.add(R.raw.seven)
                spOffbeatVoice = R.raw.and
            }
            Click.name -> {
                lstResIdOnbeatAll.add(R.raw.piin)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                lstResIdOnbeatAll.add(R.raw.pon)
                spOffbeatVoice = R.raw.kattu
            }
        }
        //選択されたrhythmに応じて、実際に使用される表拍の音声リストを作成する。
        //４拍子であればfour[0],one[1],two[2],three[3]の様になる。
        lstSpOnbeat = mutableListOf()
        var j: Int
        for (i in 0 until rhythm) {
            j = (((rhythm - 1) + i) % rhythm)
            lstSpOnbeat.add(lstResIdOnbeatAll[j])
        }
    }

    fun setSoundPool(context:Context,rhythm:Int) {
        SoundPool.Builder().run {
            val audioAttributes = AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_MEDIA)
                build()
            }
            setMaxStreams(20)
            setAudioAttributes(audioAttributes)
            build()
        }.also { soundPool = it }
        //表拍
        for (i in 0 until rhythm) {
            soundPool?.load(context, lstSpOnbeat[i], 1)?.let { lstSpOnbeat[i] = it }
        }
        //裏拍
        soundPool?.load(context,
            spOffbeatVoice, 1)?.let { spOffbeatVoice = it }
    }
}
