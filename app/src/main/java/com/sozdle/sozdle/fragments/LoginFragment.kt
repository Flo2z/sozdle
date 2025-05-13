package com.sozdle.sozdle.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import com.sozdle.sozdle.R
import com.sozdle.sozdle.activites.MainActivity
import com.sozdle.sozdle.services.CsvDownloadService
import com.sozdle.sozdle.util.HashUtil

class LoginFragment : Fragment() {
    private lateinit var sharedPref: SharedPreferences
    private val hasher = HashUtil()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedPref = requireActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE)

        val etUsername = view.findViewById<EditText>(R.id.login_et_username)
        val etPassword = view.findViewById<EditText>(R.id.login_et_password)
        val btnLogin = view.findViewById<Button>(R.id.login_btn_login)
        val btnToRegister = view.findViewById<Button>(R.id.login_btn_to_registration)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            val hashedUsername = hasher.sha256(username)
            val hashedPassword = hasher.sha256(password)

            if (sharedPref.getBoolean("${hashedUsername}exists", false)) {
                val savedPassword = sharedPref.getString(hashedUsername, "")
                if (savedPassword == hashedPassword) {
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    sharedPref.edit().putString("loggedInUser", hashedUsername).apply()
                    (activity as? MainActivity)?.menu()
                } else {
                    Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            }
        }

        btnToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegistrationFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
