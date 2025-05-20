package com.sozdle.sozdle.activites
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import okhttp3.*
import okhttp3.Request
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import com.sozdle.sozdle.R
import com.sozdle.sozdle.activities.InstructionActivity
import com.sozdle.sozdle.fragments.LoginFragment
import com.sozdle.sozdle.services.CsvDownloadService
import java.io.InputStream
import java.net.URL
import java.util.Timer
import java.util.concurrent.Executors
import kotlin.concurrent.schedule
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Callable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private lateinit var  sharedPref:SharedPreferences

    private fun addBorderToTextView(tv: TextView, colorStateList: ColorStateList) {
        val border = GradientDrawable()
        border.shape = GradientDrawable.RECTANGLE
        border.setColor(colorStateList.defaultColor) // Background color inside the border

        // Extract the default color from the ColorStateList
        border.setStroke(4, Color.BLACK)

        border.cornerRadius = 8f // Optional: rounded corner
        tv.background = border
    }

    private fun showPopupMenu(anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_clear_prefs -> {
                    getSharedPreferences("UserInfo", MODE_PRIVATE).edit().clear().apply()
                    Toast.makeText(this, "Өшірілді", Toast.LENGTH_SHORT).show()
                    finish()
                    true
                }
                R.id.menu_exit -> {
                    finish()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
    private fun game(wordsOfLength: List<String>, wordLength: Int, maxGuesses: Int) {
        supportActionBar?.show()
        setContentView(R.layout.game)
        val input = findViewById<TableLayout>(R.id.input)

        var chosenWord = wordsOfLength.random()
        Log.w("chosenWord", "${chosenWord}")
        val inputLabelsList = Array(maxGuesses) { Array(wordLength) { TextView(this) } }

        for (rowNum in 0 until maxGuesses) {
            val inputRow = TableRow(this)
            val inputLabels = inputLabelsList[rowNum]

            val labelSize = 1000 / maxOf(wordLength, maxGuesses)
            val padding = labelSize.floorDiv(50)
            for (i in 0 until wordLength) {
                val label = inputLabels[i]
                label.width = (labelSize - padding)
                label.height = (labelSize - padding)
                label.textSize = (labelSize / 3).toFloat()
                label.gravity = Gravity.CENTER
                label.setTypeface(null, Typeface.BOLD)
                inputLabels[i].setTextColor(getColorStateList(R.color.black))
                label.setBackgroundResource(R.color.white)
                addBorderToTextView(label, getColorStateList(R.color.white))

                val params = TableRow.LayoutParams(labelSize, labelSize)
                params.setMargins(padding, padding, padding, padding)
                label.layoutParams = params

                inputRow.addView(label)
            }
            inputRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            inputRow.gravity = Gravity.CENTER_HORIZONTAL
            input.addView(inputRow)
        }

        var rowNum = 0

        val inputStrings = Array(maxGuesses) { CharArray(wordLength) }

        var inputLen = 0
        var inputString = inputStrings[rowNum]
        var inputLabels = inputLabelsList[rowNum]

        val keyboard = findViewById<LinearLayout>(R.id.keyboard)
        val keyToButton = emptyMap<Char, Button>().toMutableMap()
        fun makeKeyRow(keys: String) {
            val row = LinearLayout(this)

            row.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            row.gravity = Gravity.CENTER
            row.setPadding(0, 0, 0, 0)
            for (character in keys) {
                val key = Button(this)
                key.backgroundTintList = getColorStateList(R.color.keyboard_key_background_color)
                key.text = character.toString()
                key.setTextColor(getColorStateList(R.color.black))
                key.textSize = 34f
                key.textAlignment = Button.TEXT_ALIGNMENT_CENTER
                key.setPadding(0, 0, 0, 0)
                key.setTypeface(null, Typeface.BOLD)
                key.layoutParams = LinearLayout.LayoutParams(
                    105,
                    160
                )
                row.addView(key)
                keyToButton[character] = key
            }

            keyboard.addView(row)
        }
        makeKeyRow("әіңғүұқөһ")
        makeKeyRow("йукенгшзх")
        makeKeyRow("ывапролдж")
        makeKeyRow("ячсмитб←✓")


        fun newGame() {
            for (labels in inputLabelsList) {
                for (label in labels) {
                    label.text = ""
                    label.setTextColor(getColorStateList(R.color.black))
                    label.backgroundTintList = getColorStateList(R.color.white)
                }
            }
            for (list in inputStrings) {
                list.fill('\u0000')
            }
            for (button in keyToButton.values) {
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
                button.backgroundTintList = getColorStateList(R.color.keyboard_key_background_color)
            }
            rowNum = 0
            inputLen = 0
            inputString = inputStrings[rowNum]
            inputLabels = inputLabelsList[rowNum]
            chosenWord = wordsOfLength.random()
        }

        var stopInputs = false
        for ((key, button) in keyToButton)
            button.setOnClickListener {
                if (stopInputs) {
                    return@setOnClickListener
                }
                if (key == '✓') {
                    if (inputLen != wordLength) {
                        return@setOnClickListener
                    }
                    // not valid word
                    if (!wordsOfLength.contains(inputString.joinToString(separator = ""))) {
                        Toast.makeText(this, "дұрыс сөз емес", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    for (i in 0 until rowNum) {
                        if (inputStrings[i].contentEquals(inputString)) {
                            // Already entered
                            Toast.makeText(
                                this,
                                "ол созди жаздын",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    }

                    var allCorrect = true

                    for (i in 0 until wordLength) {
                        val guess = inputString[i]
                        val correctCharacter = chosenWord[i]

                        inputLabels[i].setTextColor(getColorStateList(R.color.white))
                        keyToButton[guess]!!.setTextColor(getColorStateList(R.color.white))
                        if (guess == correctCharacter) {
                            inputLabels[i].backgroundTintList =
                                getColorStateList(R.color.correct_letter)
                            keyToButton[guess]!!.backgroundTintList = getColorStateList(R.color.correct_letter)
                        } else {
                            allCorrect = false
                            if (chosenWord.contains(guess)) {
                                inputLabels[i].backgroundTintList =
                                    getColorStateList(R.color.wrong_placed_letter)
                                keyToButton[guess]!!.let {
                                    if (it.backgroundTintList != getColorStateList(R.color.correct_letter)) {
                                        it.backgroundTintList = getColorStateList(R.color.wrong_placed_letter)
                                    }
                                }
                            } else {
                                inputLabels[i].backgroundTintList=
                                    getColorStateList(R.color.incorrect_letter)
                                keyToButton[guess]!!.let {
                                    if (it.backgroundTintList == getColorStateList(R.color.keyboard_key_background_color)) {
                                        it.backgroundTintList = getColorStateList(R.color.incorrect_letter)
                                    }
                                }
                            }
                        }
                    }

                    if (allCorrect) {
                        // Won
                        stopInputs = true
                        Toast.makeText(this, "мықты", Toast.LENGTH_LONG).show()
                        Timer().schedule(5000) {
                            newGame()
                            stopInputs = false
                        }
                        return@setOnClickListener
                    }

                    if (rowNum + 1 == maxGuesses) {
                        // Out of guesses
                        stopInputs = true

                        runOnUiThread {
                            AlertDialog.Builder(this)
                                .setTitle("ойын бітті")
                                .setMessage("таппадын, сөз осындай $chosenWord")
                                .setPositiveButton("жана ойын") { _, _ ->
                                    newGame()
                                    stopInputs = false
                                }
                                .setNegativeButton("шығу") { _, _ ->
                                    finish()
                                }
                                .setCancelable(false)
                                .show()
                        }

                        return@setOnClickListener
                    }


                    // New row
                    rowNum += 1
                    inputString = inputStrings[rowNum]
                    inputLen = 0
                    inputLabels = inputLabelsList[rowNum]

                } else if (key == '←') {
                    if (inputLen != 0) {
                        inputLen -= 1
                        inputLabels[inputLen].text = ""
                        inputString[inputLen] = '\u0000'
                    }
                } else {
                    if (inputLen == wordLength) {
                        return@setOnClickListener
                    }
                    inputLabels[inputLen].text = key.uppercase()
                    inputString[inputLen] = key
                    inputLen += 1
                }
            }
    }
    fun menu() {
        supportActionBar?.hide()
        setContentView(R.layout.menu)

        val wordLengthBar = findViewById<Slider>(R.id.wordLengthBar)
        val textLengthLabel = findViewById<TextView>(R.id.wordLength)

        textLengthLabel.text = wordLengthBar.value.toInt().toString()
        wordLengthBar.addOnChangeListener { _, value, _ ->
            textLengthLabel.text = value.toInt().toString()
        }

        val maxGuessesBar = findViewById<Slider>(R.id.maxGuessesBar)
        val maxGuessesLabel = findViewById<TextView>(R.id.maxGuesses)
        maxGuessesLabel.text = maxGuessesBar.value.toInt().toString()
        maxGuessesBar.addOnChangeListener { _, value, _ ->
            maxGuessesLabel.text = value.toInt().toString()
        }

        val playButton = findViewById<Button>(R.id.play)
        playButton.setOnClickListener {
            val wordLength = wordLengthBar.value.toInt()
            val maxGuesses = maxGuessesBar.value.toInt()

            // Stop and restart service with new word length
            val serviceIntent = Intent(this, CsvDownloadService::class.java)
            serviceIntent.putExtra("wordLength", wordLength)
            startService(serviceIntent)

            // Load words from downloaded CSV file
            val file = File(applicationContext.filesDir, "words_${wordLength}_letters.csv")
            words = if (file.exists()) {
                file.readText()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.length == wordLength }
            } else {
                Log.w("Menu", "CSV file not found, fallback to empty list.")
                emptyList()
            }

            game(words, wordLength, maxGuesses)
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showPopupMenu(logoutButton)
        }

        val instructionButton = findViewById<Button>(R.id.instructionButton)
        instructionButton.setOnClickListener {
            val intent = Intent(this, InstructionActivity::class.java)
            startActivity(intent)
        }
    }

//        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)
//        deleteAccountButton.setOnClickListener {
//            val prefs = getSharedPreferences("UserInfo", MODE_PRIVATE)
//            val loggedInUser = prefs.getString("loggedInUser", null)
//
//            if (loggedInUser != null) {
//                prefs.edit()
//                    .remove(loggedInUser)
//                    .remove("${loggedInUser}exists")
//                    .remove("loggedInUser")
//                    .apply()
//
//                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
//
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container, LoginFragment())
//                    .commit()
//            } else {
//                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    private lateinit var words: List<String>

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu -> {
            menu()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)

        // Load LoginFragment on startup
        sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        if(sharedPref.contains("loggedInUser")){

            if(sharedPref.getString("loggedInUser", "empty").equals("empty")){
                //do nothing
            }
            else{
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
            }

        }
        else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
        }



}