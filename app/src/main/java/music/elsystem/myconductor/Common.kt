package music.elsystem.myconductor

import java.sql.Timestamp

object Common {
    //ディスプレイサイズ
    var surfaceWidth = 0
    var surfaceHeight = 0
    //ビットマップサイズ
    var bitmapX =0
    var bitmapY =0
    //リスナーインターフェースで使用する項目
    var rhythm =4
    var tempo = 60
    var motionYMultiplier = 1.0
    var radiusMultiplier = 1.0
    var gradationMultiplier = 1.0
    //タップしたタイミングで最終拍のナンバーが表示されるのを回避するためのフラグ。
    var justTappedSw = true
    //サウンド関連*****************************************************************
    //サウンドの裏拍数
    var offBeatNum = 1
    var lstResIdOnbeatAll: MutableList<Int>  = mutableListOf()
    var lstSpOnbeat: MutableList<Int>  = mutableListOf()
    var spOffbeatVoice = 0
    var spOffbeatVoice2 = 0
    //サウンドとグラフィックの同期を取るためにグラフィック側から更新されるタイムスタンプ
    var graphicTimestamp = Timestamp(System.currentTimeMillis())
    //初期起動時、サウンドとグラフィックのおおよそのスピードを測定し差分割合算出に使用するためのタイムスタンプ
    var prefGraphTimestamp = Timestamp(System.currentTimeMillis())
    //サウンドとグラフィックの差分割合
    var machineTimelag = 0.0000f
}