package com.sozdle.sozdle.activites

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sozdle.sozdle.R

class MainActivity : AppCompatActivity() {

    private val maxAttempts = 6
    private var wordLength = 5
    private var currentAttempt = 0
    private var currentLetterIndex = 0
    private var targetWord = "ӘЛЕМ"
    private val dictionary = listOf("ӘЛЕМ", "СӨЗДЕ", "БІЛІМ", "ТІЛЕК")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupGameBoard()
    }

    private fun setupGameBoard() {
        for (row in 1..maxAttempts) {
            val rowId = resources.getIdentifier("game_layout_row$row", "id", packageName)
            val rowLayout = findViewById<LinearLayout>(rowId)
            rowLayout.removeAllViews()

            for (i in 0 until wordLength) {
                val letterView = TextView(this)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                params.marginEnd = 4 // optional spacing
                letterView.layoutParams = params
                letterView.gravity = Gravity.CENTER
                letterView.textSize = 24f
                letterView.setBackgroundResource(R.drawable.letter_background)
                letterView.text = ""
                rowLayout.addView(letterView)
            }
        }
    }

    private fun setupKeyboard() {
        val rows = listOf(
            Pair(1, 10),
            Pair(2, 11),
            Pair(3, 11),
            Pair(4, 10)
        )

        for ((rowIndex, buttonCount) in rows) {
            for (i in 1..buttonCount) {
                val buttonId = resources.getIdentifier("keyboard_row${rowIndex}_$i", "id", packageName)
                val button = findViewById<Button>(buttonId)

                button?.setOnClickListener {
                    val text = button.text.toString()

                    when (text.lowercase()) {
                        "del", "←" -> onBackspacePressed()
                        "ok", "enter", "✓" -> onSubmitWord()
                        else -> onKeyPressed(text)
                    }
                }
            }
        }
    }

    fun onKeyPressed(letter: String) {
        if (currentLetterIndex >= wordLength) return

        val rowId = resources.getIdentifier("game_layout_row${currentAttempt + 1}", "id", packageName)
        val currentRow = findViewById<LinearLayout>(rowId)
        val letterView = currentRow.getChildAt(currentLetterIndex) as TextView
        letterView.text = letter.uppercase()
        currentLetterIndex++
    }

    fun onBackspacePressed() {
        if (currentLetterIndex == 0) return

        currentLetterIndex--
        val rowId = resources.getIdentifier("game_layout_row${currentAttempt + 1}", "id", packageName)
        val currentRow = findViewById<LinearLayout>(rowId)
        val letterView = currentRow.getChildAt(currentLetterIndex) as TextView
        letterView.text = ""
    }

    fun onSubmitWord() {
        if (currentLetterIndex < wordLength) return

        val rowId = resources.getIdentifier("game_layout_row${currentAttempt + 1}", "id", packageName)
        val currentRow = findViewById<LinearLayout>(rowId)
        val guess = StringBuilder()

        for (i in 0 until wordLength) {
            val letterView = currentRow.getChildAt(i) as TextView
            guess.append(letterView.text.toString())
        }

        val guessWord = guess.toString().uppercase()
        if (!dictionary.contains(guessWord)) {
            showToast("Сөз табылмады!")
            return
        }

        val targetCharCounts = mutableMapOf<Char, Int>()
        targetWord.forEach { c ->
            targetCharCounts[c] = targetCharCounts.getOrDefault(c, 0) + 1
        }

        for (i in 0 until wordLength) {
            val letterView = currentRow.getChildAt(i) as TextView
            val guessedChar = guessWord[i]
            val correctChar = targetWord[i]

            if (guessedChar == correctChar) {
                letterView.setBackgroundColor(getColor(R.color.green))
                targetCharCounts[guessedChar] = targetCharCounts[guessedChar]!! - 1
            }
        }

        for (i in 0 until wordLength) {
            val letterView = currentRow.getChildAt(i) as TextView
            val guessedChar = guessWord[i]
            val correctChar = targetWord[i]

            // already colored green, skip
            if (guessedChar == correctChar) continue

            if (targetCharCounts.getOrDefault(guessedChar, 0) > 0) {
                letterView.setBackgroundColor(getColor(R.color.yellow))
                targetCharCounts[guessedChar] = targetCharCounts[guessedChar]!! - 1
            } else {
                letterView.setBackgroundColor(getColor(R.color.gray))
            }
        }

        if (guessWord == targetWord) {
            showToast("Дұрыс таптың!")
            return
        }

        currentAttempt++
        currentLetterIndex = 0

        if (currentAttempt >= maxAttempts) {
            showToast("Сөз: $targetWord")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
