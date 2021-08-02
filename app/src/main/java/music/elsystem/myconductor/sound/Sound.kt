//package music.elsystem.myconductor.sound
//
//import android.content.Context
//import android.media.AudioAttributes
//import android.media.SoundPool
//import android.util.Log
//import music.elsystem.myconductor.Common.lstSpOnbeat
//import music.elsystem.myconductor.Common.rhythm
//import music.elsystem.myconductor.Common.spOffbeatVoice
//import music.elsystem.myconductor.Common.spOffbeatVoice2
//import music.elsystem.myconductor.Common.soundPool
//
//
//class Sound() {
//    fun setSoundPool(context: Context) {
//        SoundPool.Builder().run {
//            val audioAttributes = AudioAttributes.Builder().run {
//                setUsage(AudioAttributes.USAGE_MEDIA)
//                build()
//            }
//            setMaxStreams(20)
//            setAudioAttributes(audioAttributes)
//            build()
//        }.also { soundPool = it }
//        //表拍
//        Log.i("lstSpOnbeat", "$lstSpOnbeat")
//        for (i in 0 until rhythm) {
//            soundPool?.load(context, lstSpOnbeat[i], 1)?.let { lstSpOnbeat[i] = it }
//        }
//        //裏拍
//        soundPool?.load(context, spOffbeatVoice, 1)?.let { spOffbeatVoice = it }
//        //３連符の場合に裏拍連打で追いつかないケースが有るため裏拍を分ける。
//        soundPool?.load(context, spOffbeatVoice2, 1)?.let { spOffbeatVoice2 = it }
//    }
//}
