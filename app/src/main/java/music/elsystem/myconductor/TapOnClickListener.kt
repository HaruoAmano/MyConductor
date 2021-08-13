package music.elsystem.myconductor

import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView

class TapOnClickListener(
    private val handler: Handler,
    private val editText: EditText,
    private val spinner: Spinner
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
            ut.changeTempo(6000 / count, editText, spinner)
        } else {
            handler.post(runnable)
        }
        count = 0
    }
}