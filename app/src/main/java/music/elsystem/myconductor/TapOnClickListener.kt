package music.elsystem.myconductor

import android.os.Handler
import android.view.View
import android.widget.TextView
import music.elsystem.myconductor.Common.Tact.*
import music.elsystem.myconductor.Common.tactType
import music.elsystem.myconductor.Common.tempo
import music.elsystem.myconductor.MainActivity.Companion.swingTempoTable
import music.elsystem.myconductor.MainActivity.Companion.tempoTable

class TapOnClickListener(
    private val handler: Handler,
    private val tempoText: TextView,
    private val tempoSignText: TextView
) : View.OnClickListener {
    private val ut = Util()
    private var count = 0
    private var period = 10   //0.01秒単位
    private val runnable: Runnable = Runnable1()

    inner class Runnable1 : Runnable {
        override fun run() {
            count++
            handler.postDelayed(this, period.toLong())
            //400であればtempo=15まで測定可能
            if (count > 400) {
                //countが一定の値になった場合は停止する。
                count = 0
                handler.removeCallbacks(this)
            }
        }
    }

    override fun onClick(v: View) {
        //4秒以内に再度タップされた場合にテンポとして認識する。
        //4秒を超えた場合（または初回タップ時はrunnableを起動する）
        if (count > 0) {
            val temporaryTempo = 6000 / count
            //タップで取得したテンポ以下で一番近い値をtempoTableからセットする。
            if (tactType == Swing.name) {
                val temporaryList:List<Int> = swingTempoTable.takeWhile { it <= temporaryTempo }
                tempo = temporaryList.maxOrNull().let { it } ?: 40
            } else {
                val temporaryList:List<Int> = tempoTable.takeWhile { it <= temporaryTempo }
                tempo = temporaryList.maxOrNull().let { it } ?: 20
            }
            ut.tempoChanged(tempo, tempoText, tempoSignText)
        } else {
            handler.post(runnable)
        }
        count = 0
    }
}