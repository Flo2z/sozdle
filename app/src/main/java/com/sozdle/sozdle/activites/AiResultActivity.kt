package com.sozdle.sozdle.activites

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AiResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val word = intent.getStringExtra("word")
        val ru = intent.getStringExtra("translation_ru")
        val en = intent.getStringExtra("translation_en")
        val def = intent.getStringExtra("definition")

        val textView = TextView(this).apply {
            text = "Сөз: $word\nОрысша: $ru\nАғылшынша: $en\nАнықтама: $def"
            setPadding(32, 32, 32, 32)
            textSize = 18f
        }

        setContentView(textView)
    }
}
