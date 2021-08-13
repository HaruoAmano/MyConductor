package music.elsystem.myconductor.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SeekBar
import music.elsystem.myconductor.Common
import music.elsystem.myconductor.Common.RenderMode.*
import music.elsystem.myconductor.Common.bmpBeat
import music.elsystem.myconductor.Common.dotSize
import music.elsystem.myconductor.Common.downBeatVolume
import music.elsystem.myconductor.Common.lstSpOnbeat
import music.elsystem.myconductor.Common.motionYMultiplier
import music.elsystem.myconductor.Common.offbeatDotSizeHeavy
import music.elsystem.myconductor.Common.renderMode
import music.elsystem.myconductor.Common.settingSurfaceHeight
import music.elsystem.myconductor.Common.settingSurfaceWidth
import music.elsystem.myconductor.Common.soundPool
import music.elsystem.myconductor.Common.spOffbeatVoice
import music.elsystem.myconductor.Common.stayingFrameRate
import music.elsystem.myconductor.Common.subWeakBeatVolume
import music.elsystem.myconductor.Common.surfaceHeight
import music.elsystem.myconductor.Common.surfaceWidth
import music.elsystem.myconductor.Common.tempo
import music.elsystem.myconductor.Common.voice
import music.elsystem.myconductor.Common.weakBeatVolume
import music.elsystem.myconductor.MainActivity
import music.elsystem.myconductor.R
import music.elsystem.myconductor.databinding.ActivityHeavySettingBinding
import music.elsystem.myconductor.surfaceview.dotSurfaceview.GlSurfaceView


class HeavySettingActivity : AppCompatActivity() {
    private lateinit var bd: ActivityHeavySettingBinding
    var settingSurfaceView: GlSurfaceView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bd = ActivityHeavySettingBinding.inflate(layoutInflater)
        setContentView(bd.root)
        //描画中かどうか（
        var isSetStarted = false
        //サーフェスビューの親ビューであるLinearLayoutを生成する。
        val observerSurfaceViewLayout: ViewTreeObserver =
            bd.layoutSettingSurfaceView.viewTreeObserver
        observerSurfaceViewLayout.addOnGlobalLayoutListener {
            settingSurfaceWidth = bd.layoutSettingSurfaceView.width
            settingSurfaceHeight = bd.layoutSettingSurfaceView.height
        }
        //タクト・グラビティ*********************************************
        //プログレス設定
        bd.sbTactGravity.progress = (stayingFrameRate * 1000).toInt()
        //設定変更
        bd.sbTactGravity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                stayingFrameRate = (progress / 1000f)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isSetStarted) {
                    updateGlSurface()
                }
            }
        })
        //裏拍のドットサイズ*********************************************
        //プログレス設定
        bd.sbOffbeatDotSize.progress = (offbeatDotSizeHeavy * 100).toInt()
        //設定変更
        bd.sbOffbeatDotSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                offbeatDotSizeHeavy = (progress / 100f)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isSetStarted) {
                    updateGlSurface()
                }
            }
        })
        //ドットサイズ***************************************************
        //プログレス設定
        bd.sbDotSize.progress = dotSize.toInt()
        //設定変更
        bd.sbDotSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                dotSize = progress.toFloat()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isSetStarted) {
                    updateGlSurface()
                }
            }
        })
        //テンポ*******************************************************
        //プログレス設定
        bd.sbTempo.progress = tempo
        //設定変更
        bd.sbTempo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                bd.tvTempo.setText(progress.toString())
                tempo = progress
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isSetStarted) {
                    updateGlSurface()
                }
            }
        })
        //テンポ・テキストビューにtempoを設定
        bd.tvTempo.text = tempo.toString()
        //サウンド・ボリューム***********************************************
        //表拍
        //プログレス設定
        bd.sbDownBeatVolume.progress = (downBeatVolume * 10).toInt()
        //設定変更
        bd.sbDownBeatVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                downBeatVolume = progress / 10f
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isSetStarted) {
                    updateGlSurface()
                }
            }
        })
        //裏拍
        //プログレス設定
        bd.sbWeakBeatVolume.progress = (weakBeatVolume * 10).toInt()
        //設定変更
        bd.sbWeakBeatVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                weakBeatVolume = progress / 10f
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isSetStarted) {
                    updateGlSurface()
                }
            }
        })
        //裏裏拍
        //プログレス設定
        bd.sbSubweakBeatVolume.progress = (subWeakBeatVolume * 10).toInt()
        //設定変更
        bd.sbSubweakBeatVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                subWeakBeatVolume = progress / 10f
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isSetStarted) {
                    updateGlSurface()
                }
            }
        })
        //スタート・ストップ
        bd.layoutSettingSurfaceView.setOnClickListener {
            Log.i("@@@@", "$isSetStarted")
            if (!isSetStarted) {
                renderMode = Setting.name
                settingSurfaceView = GlSurfaceView(
                    applicationContext)
                bd.tvTap.visibility = View.INVISIBLE
                bd.layoutSettingSurfaceView.addView(settingSurfaceView)
                isSetStarted = true
            } else {
                bd.tvTap.visibility = View.VISIBLE
                bd.layoutSettingSurfaceView.removeAllViews()
                isSetStarted = false
            }
        }
        bd.btnBack.setOnClickListener {
            //メインアクティビティに戻った時に、テンポの表示を変更するが、そのトリガとして
            //RESULT_OKを返している。
            bd.layoutSettingSurfaceView.removeAllViews()
            val intent = Intent(applicationContext,MainActivity::class.java)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        //window.decorView.systemUiVisibilityはアンドロイド11では非推奨。
        // WindowInsetsControllerが推奨されるが現時点ではこのままとする。
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }
    private fun updateGlSurface(){
        bd.layoutSettingSurfaceView.removeAllViews()
        renderMode = Setting.name
        settingSurfaceView = GlSurfaceView(
            applicationContext)
        bd.layoutSettingSurfaceView.addView(settingSurfaceView)
    }
}