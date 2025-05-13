package com.sozdle.sozdle.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CsvDownloadService : Service() {

    private val client = OkHttpClient()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val wordLength = intent?.getIntExtra("wordLength", 5) ?: 5

        scheduler.scheduleWithFixedDelay({
            val ip = "192.168.90.193"

            val url = "http://$ip:8080/api/words/csv/words_${wordLength}_letters.csv"
            downloadCsv(url, wordLength)

        }, 0, 15, TimeUnit.SECONDS)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduler.shutdownNow()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun downloadCsv(url: String, wordLength: Int) {
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CsvService", "Download failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    val inputStream: InputStream = response.body!!.byteStream()
                    val file = File(filesDir, "words_${wordLength}_letters.csv")
                    val fos = FileOutputStream(file)

                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                    }

                    fos.close()
                    inputStream.close()

                    Log.d("CsvService", "CSV saved to ${file.absolutePath}")
                } else {
                    Log.e("CsvService", "Response not successful: ${response.code}")
                }
            }
        })
    }
}