package com.sozdle.sozdle.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.sozdle.sozdle.R

class InstructionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction)

        val instructionList = findViewById<ListView>(R.id.instructionList)

        val backButton = findViewById<MaterialButton>(R.id.back_button)

        backButton.setOnClickListener{
            finish()
        }
        val steps = listOf(
            "1. Сөздің ұзындығын және мүмкіндік санын таңдаңыз.",
            "2. 'Ойнау' батырмасын басыңыз.",
            "3. Пернетақта арқылы сөзді енгізіңіз.",
            "4. ✓ батырмасын басып сөзді жіберіңіз.",
            "5. Әріптер түспен белгіленеді:",
            "   - Жасыл: әріп орнында және дұрыс.",
            "   - Сары: әріп сөзде бар, бірақ орнында емес.",
            "   - Сұр: әріп сөзде жоқ.",
            "6. Барлық әріптерді дұрыс тапқанша немесе мүмкіндіктер біткенше жалғастырыңыз.",
            "7. Жаңа ойын бастау үшін нұсқауларға қайта оралыңыз."
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, steps)
        instructionList.adapter = adapter
    }
}
