package com.sozdle.sozdle.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sozdle.sozdle.R
import com.sozdle.sozdle.util.HashUtil

class RegistrationFragment : Fragment() {
    private lateinit var sharedPref: SharedPreferences
    private val hasher = HashUtil()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(false)

        sharedPref = requireActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE)

        val etUsername = view.findViewById<EditText>(R.id.registration_et_username)
        val etPassword = view.findViewById<EditText>(R.id.registration_et_password)
        val etConfirm = view.findViewById<EditText>(R.id.registration_et_confirm_password)
        val btnRegister = view.findViewById<Button>(R.id.registration_btn_register)
        val btnToLogin = view.findViewById<Button>(R.id.registration_btn_to_login)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val confirm = etConfirm.text.toString()

            if (password != confirm) {
                Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedUsername = hasher.sha256(username)
            val hashedPassword = hasher.sha256(password)

            if (sharedPref.getBoolean("${hashedUsername}exists", false)) {
                Toast.makeText(requireContext(), "User already exists", Toast.LENGTH_SHORT).show()
            } else {
                with(sharedPref.edit()) {
                    putBoolean("${hashedUsername}exists", true)
                    putString(hashedUsername, hashedPassword)
                    apply()
                }
                Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Go back to login
            }
        }

        btnToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
