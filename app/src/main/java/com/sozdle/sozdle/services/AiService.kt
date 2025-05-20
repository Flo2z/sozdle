package com.sozdle.sozdle.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.util.concurrent.Executors
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiService : Service() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // Increased timeout
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val ip = "192.168.100.151"
    private val url = "http://$ip:8080/ai/generate" // Corrected URL without trailing '$'

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val word = intent?.getStringExtra("word")
        if (word != null) {
            // Construct a well-structured prompt
            val prompt = """
                Translate the following Kazakh word to Russian and English, and provide a definition in English.
                Return the response in JSON format with fields: 'word', 'translation_ru', 'translation_en', 'definition'.
                The word is: $word
            """.trimIndent()
            sendRequest(prompt)
        }
        return START_NOT_STICKY
    }

    private fun sendRequest(prompt: String) {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val jsonBody = JSONObject().apply {
            put("prompt", prompt)
        }.toString()

        val body = RequestBody.create(mediaType, jsonBody)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AiService", "Request failed", e)
                stopSelf()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { bodyStr ->
                        try {
                            val json = JSONObject(bodyStr)
                            val originalWord   = json.getString("word")
                            val translationRu  = json.getString("translation_ru")
                            val translationEn  = json.getString("translation_en")
                            val definition     = json.getString("definition")

                            // broadcast it
                            val broadcast = Intent("AI_RESPONSE").apply {
                                putExtra("word",           originalWord)
                                putExtra("translation_ru", translationRu)
                                putExtra("translation_en", translationEn)
                                putExtra("definition",     definition)
                            }
                            LocalBroadcastManager
                                .getInstance(this@AiService)
                                .sendBroadcast(broadcast)

                        } catch (e: Exception) {
                            Log.e("AiService", "JSON parsing error", e)
                        }
                    }
                } else {
                    Log.e("AiService", "Request failed: ${response.code}")
                }
                stopSelf()
            }

        })
    }
}