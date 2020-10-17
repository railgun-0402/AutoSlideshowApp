package jp.techacademy.sugawara.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.os.Handler
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    //写真URIの配列の長さは「８」
    val images_array = arrayListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //タイマの作成
        mTimer = Timer()

        //写真を保存する配列のインデックス番号
        var index : Int = 0

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }


        //最初の写真を表示
        imageView.setImageURI(images_array[index])

        //進むボタン押下
        go_button.setOnClickListener {
            /*
            スライドは8枚なので範囲は０〜７
            最後までいったら最初の写真に戻りたい
             */
            if (index == 7){
                index = 0
            } else {
                //1つ先の写真を表示する
                index += 1
            }
            //写真を表示
            imageView.setImageURI(images_array[index])
        }

        //戻るボタンを押下
        reverse_button.setOnClickListener {
            /*
            スライドは8枚なので範囲は０〜７
            最初まで戻ったら最後の写真へ。。
            */
            if (index == 0){
                index = 7
            } else {
                index -= 1
            }
            //写真表示
            imageView.setImageURI(images_array[index])

        }

        //再生ボタン押下
        restart_stop_button.setOnClickListener {
            //再生ボタン押下
            if (mTimer == null){
                //タイマー作成
                mTimer = Timer()

                /*
                * ２秒後に自動送りが開始
                * ２秒毎にスライドが流れる
                */
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            //配列を進めて表示
                            if (index == 7){
                                index = 0
                            } else {
                                index += 1
                            }
                            //写真を表示
                            imageView.setImageURI(images_array[index])
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで 2秒、ループの間隔を2秒に設定

                //押下すると停止ボタンに切り替わる
                restart_stop_button.text = "停止"

                //進むボタンと戻るボタンの押下を不可にする
                go_button.isEnabled = false
                reverse_button.isEnabled = false

                //停止ボタンを押下
            } else {
                //タイマを止める
                mTimer!!.cancel()

                //次に再生ボタンを押下した時、タイマーはnullである必要がある
                mTimer = null

                //再生ボタンに切り替える
                restart_stop_button.text = "再生"

                //進むボタンと戻るボタンを利用可能にする
                go_button.isEnabled = true
                reverse_button.isEnabled = true

            }


        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )


        if (cursor!!.moveToFirst()) {

            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                //写真の配列に加える
                images_array.add(imageUri)

            } while (cursor.moveToNext())
        }
        cursor.close()
    }

}


