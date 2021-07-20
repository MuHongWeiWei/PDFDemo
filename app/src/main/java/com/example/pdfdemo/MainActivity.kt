package com.example.pdfdemo


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //下載PDF到本地端
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val request = Request.Builder().url("https://badgameshow.com/laichao/test/test.pdf").build()
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {
                    writePDFQ(response.body!!)

                    runOnUiThread {
                        val intent = Intent(applicationContext, DigitalSignatureActivity::class.java)
                        intent.putExtra("ActivityAction", "once")
                        startActivityForResult(intent,5)
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            val intent = Intent(applicationContext, DigitalSignatureActivity::class.java)
            intent.putExtra("ActivityAction", "twice")
            startActivityForResult(intent,4)
        }
    }


    private fun writePDFQ(responseBody: ResponseBody): File {
        var length = 0L
        val file = File(filesDir, "test.pdf")

        val bis = BufferedInputStream(responseBody.byteStream())
        val buffer = ByteArray(1024)

        try {
            val fos = FileOutputStream(file)
            var bytes: Int = bis.read(buffer)
            while (bytes >= 0) {
                fos.write(buffer, 0, bytes)
                fos.flush()
                bytes = bis.read(buffer)
                length += bytes.toLong()
            }
            bis.close()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }
}