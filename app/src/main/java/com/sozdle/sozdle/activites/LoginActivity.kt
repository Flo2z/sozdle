package com.sozdle.sozdle.activites

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sozdle.sozdle.R
import com.sozdle.sozdle.util.HashUtil


class LoginActivity: AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE)

        val etUsername = findViewById<EditText>(R.id.login_et_username)
        val etPassword = findViewById<EditText>(R.id.login_et_password)

        val btnLogin = findViewById<Button>(R.id.login_btn_login)
        val btnToRegister = findViewById<Button>(R.id.login_btn_to_registration)

        val hasher = HashUtil()

        btnLogin.setOnClickListener {
            val hashedUsername:String = hasher.sha256(etUsername.text.toString())
            val hashedPassword:String = hasher.sha256(etPassword.text.toString())

            if(sharedPref.getBoolean(hashedUsername + "exists", false)){
                val savedPassword:String = sharedPref.getString(hashedUsername, "") ?: ""

                if(savedPassword == hashedPassword){
                    sharedPref.edit().putBoolean(hashedUsername + "isLoggedIn", true).apply()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else{
                    Toast.makeText(this, R.string.login_activity_incorrect_password, Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, R.string.login_activity_incorrect_username, Toast.LENGTH_SHORT).show()
            }
        }

        btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }
}