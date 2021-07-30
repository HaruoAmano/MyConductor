package music.elsystem.myconductor.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.delay
import music.elsystem.myconductor.Common.justTappedSw
import music.elsystem.myconductor.Common.lstSpOnbeat
import music.elsystem.myconductor.Common.offBeatNum
import music.elsystem.myconductor.Common.prefGraphTimestamp
import music.elsystem.myconductor.Common.rhythm
import music.elsystem.myconductor.Common.spOffbeatVoice
import music.elsystem.myconductor.Common.spOffbeatVoice2
import music.elsystem.myconductor.Common.tempo
import music.elsystem.myconductor.Common.graphicTimestamp
import music.elsystem.myconductor.Common.machineTimelag
import java.sql.Timestamp


class Sound() {
    var soundPool: SoundPool? = null
    fun setSoundPool(context: Context) {
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
        Log.i("lstSpOnbeat", "$lstSpOnbeat")
        for (i in 0 until rhythm) {
            soundPool?.load(context, lstSpOnbeat[i], 1)?.let { lstSpOnbeat[i] = it }
        }
        //裏拍
        soundPool?.load(context, spOffbeatVoice, 1)?.let { spOffbeatVoice = it }
        //３連符の場合に裏拍連打で追いつかないケースが有るため裏拍を分ける。
        soundPool?.load(context, spOffbeatVoice2, 1)?.let { spOffbeatVoice2 = it }
    }

    suspend fun start() {
        var soundTimestamp: Timestamp
        var interval: Long
        var timeLag = 0L
        while (true) {
            interval = (60000 / (tempo * offBeatNum)).toLong()//テンポをインターバルに変換
            for (i in 0 until rhythm * offBeatNum) {
                //最初のタイミングで最終拍の音がなるのを回避する。
                //justTappedSwは描画側で制御
                if (!justTappedSw) {
                    when (i % offBeatNum) {
                        //表拍
                        0 -> soundPool?.play(
                            lstSpOnbeat[i / offBeatNum],
                            1.0f,
                            1.0f,
                            1,
                            0,
                            1.0f
                        )
                        //裏拍
                        1 -> soundPool?.play(spOffbeatVoice, 0.0f, 0.8f, 1, 0, 1.0f)
                        //３連裏拍
                        2 -> soundPool?.play(spOffbeatVoice2, 0.0f, 0.8f, 1, 0, 1.0f)
                    }
                }
                Log.i("interval - timeLag", "${interval - timeLag}")
                delay(interval - timeLag)
            }
            soundTimestamp = Timestamp(System.currentTimeMillis())
            Log.i("soundTimestamp", "$soundTimestamp")
            timeLag = (soundTimestamp.time - graphicTimestamp.time) / (rhythm * offBeatNum)
            Log.i("時間差", "$timeLag")
        }
    }

    suspend fun adjustStart() {
        Log.i("prefTimestamp:Sound", "処理開始")
        var soundTimestamp: Timestamp
        soundTimestamp = Timestamp(System.currentTimeMillis())
        Log.i("prefTimestamp:Sound　前", "$soundTimestamp")
        val offBeatNum = 2
        //tempo=60で裏拍ありを４拍刻むため、全体で４秒かかる。（周期は0.5秒となる）

        for (i in 0 until rhythm * offBeatNum) {
            when (i % offBeatNum) {
                //表拍
                0 -> soundPool?.play(
                    lstSpOnbeat[i / offBeatNum], 0.0f, 0.0f, 1, 0, 1.0f
                )
                //裏拍
                1 -> soundPool?.play(spOffbeatVoice, 0.0f, 0.0f, 1, 0, 1.0f)
                //３連裏拍
                2 -> soundPool?.play(spOffbeatVoice2, 0.0f, 0.0f, 1, 0, 1.0f)
            }
            delay((1000 / offBeatNum.toFloat()).toLong())
        }
        soundTimestamp = Timestamp(System.currentTimeMillis())
        //サウンド側が先に終了した場合に備え、２秒間の待ちを設ける。
        delay(2000)
        machineTimelag = (soundTimestamp.time - prefGraphTimestamp.time) / 4000f
        Log.i("prefTimestamp:Sound", "$soundTimestamp")
        Log.i("prefTimestamp:Sound", "初期測定時間差　${prefGraphTimestamp.time - soundTimestamp.time}")
        Log.i("prefTimestamp:Sound", "machineTimelag　$machineTimelag")

    }
}
