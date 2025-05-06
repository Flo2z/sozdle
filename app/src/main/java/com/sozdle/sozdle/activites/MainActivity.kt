package com.sozdle.sozdle.activites

import android.os.Bundle
import android.util.Log
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
    private var targetWord = "СӘЛЕМ"
    private val dictionary = listOf("СӘЛЕМ", "СӨЗДЕ", "БІЛІМ", "ТІЛЕК")
    private var keyboardLocked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupGameBoard()
        setupKeyboard()
    }

    private fun onGameWin(){
        showToast("Дұрыс таптың!")
        keyboardLocked = true
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
                if (button == null) {
                    Log.e("KeyboardError", "Button not found: keyboard_row${rowIndex}_$i")
                } else {
                    Log.d("KeyboardSetup", "Found: ${button.text}")
                }
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

    private fun onKeyPressed(letter: String) {
        if(!keyboardLocked){
            if (currentLetterIndex >= wordLength) return

            val rowId = resources.getIdentifier("game_layout_row${currentAttempt + 1}", "id", packageName)
            val currentRow = findViewById<LinearLayout>(rowId)
            val letterView = currentRow.getChildAt(currentLetterIndex) as TextView
            letterView.text = letter.uppercase()
            currentLetterIndex++

            if (currentLetterIndex == wordLength) {
                onSubmitWord()
            }
        }
    }

    private fun onBackspacePressed() {
        if(!keyboardLocked) {
            if (currentLetterIndex == 0) return

            currentLetterIndex--
            val rowId =
                resources.getIdentifier("game_layout_row${currentAttempt + 1}", "id", packageName)
            val currentRow = findViewById<LinearLayout>(rowId)
            val letterView = currentRow.getChildAt(currentLetterIndex) as TextView
            letterView.text = ""
        }
    }

    private fun onSubmitWord() {
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
                letterView.setBackgroundColor(getColor(R.color.correct_letter))
                targetCharCounts[guessedChar] = targetCharCounts[guessedChar]!! - 1
            }
            else if (targetCharCounts.getOrDefault(guessedChar, 0) > 0) {
                letterView.setBackgroundColor(getColor(R.color.wrong_placed_letter))
                targetCharCounts[guessedChar] = targetCharCounts[guessedChar]!! - 1
            } else {
                letterView.setBackgroundColor(getColor(R.color.incorrect_letter))
            }

            letterView.setTextColor(getColor(R.color.submit_word_letter_color))
        }

//        val rows = listOf(
//            Pair(1, 10),
//            Pair(2, 11),
//            Pair(3, 11),
//            Pair(4, 10)
//        )
//        for(z in 0 until wordLength) {
//            val guessedChar = guessWord[z]
//            val correctChar = targetWord[z]
//
//            for ((rowIndex, buttonCount) in rows) {
//                for (i in 1..buttonCount) {
//                    val buttonId =
//                        resources.getIdentifier("keyboard_row${rowIndex}_$i", "id", packageName)
//                    val button = findViewById<Button>(buttonId)
//                    if (button == null) {
//                        Log.e(
//                            "KeyboardError",
//                            "Coloring button not found: keyboard_row${rowIndex}_$i"
//                        )
//                    } else {
//                        Log.d("KeyboardSetup", "Coloring found: ${button.text}")
//                    }
//                    button?.setTextColor(getColor(R.color.submit_word_letter_color))
//                }
//            }
//        }
        if (guessWord == targetWord) {
            onGameWin()
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
