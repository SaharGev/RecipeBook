package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipebook.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.textfield.TextInputLayout
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.db.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val userDao = DatabaseProvider.getDatabase(requireContext()).userDao()


        val etRegisterName = view.findViewById<TextInputEditText>(R.id.etRegisterName)
        val tilRegisterName = view.findViewById<TextInputLayout>(R.id.tilRegisterName)
        val etRegisterEmail = view.findViewById<TextInputEditText>(R.id.etRegisterEmail)
        val tilRegisterEmail = view.findViewById<TextInputLayout>(R.id.tilRegisterEmail)
        val etRegisterPhone = view.findViewById<TextInputEditText>(R.id.etRegisterPhone)
        val tilRegisterPhone = view.findViewById<TextInputLayout>(R.id.tilRegisterPhone)
        val etRegisterPassword = view.findViewById<TextInputEditText>(R.id.etRegisterPassword)
        val tilRegisterPassword = view.findViewById<TextInputLayout>(R.id.tilRegisterPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = view.findViewById<TextView>(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val username = etRegisterName.text?.toString()?.trim().orEmpty()
            tilRegisterName.error = null
            val email = etRegisterEmail.text?.toString()?.trim().orEmpty()
            tilRegisterEmail.error = null
            val phone = etRegisterPhone.text?.toString()?.trim().orEmpty()
            tilRegisterPhone.error = null
            val password = etRegisterPassword.text?.toString()?.trim().orEmpty()
            tilRegisterPassword.error = null

            if (username.isEmpty()) {
                tilRegisterName.error = "Username is required"
                etRegisterName.requestFocus()
                return@setOnClickListener
            }

            if (username.length < 3) {
                etRegisterName.error = "Username must be at least 3 characters"
                etRegisterName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                tilRegisterEmail.error = "Email is required"
                etRegisterEmail.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilRegisterEmail.error = "Invalid email format"
                etRegisterEmail.requestFocus()
                return@setOnClickListener
            }

            if (phone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches()) {
                tilRegisterPhone.error = "Invalid phone number"
                etRegisterPhone.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilRegisterPassword.error = "Password is required"
                etRegisterPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                tilRegisterPassword.error = "Password must be at least 6 characters"
                etRegisterPassword.requestFocus()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("fullName", username)
            }

            CoroutineScope(Dispatchers.IO).launch {
                val existingUserByUsername = userDao.getUserByUsername(username)
                val existingUserByPhone = if (phone.isNotEmpty()) userDao.getUserByPhone(phone) else null

                requireActivity().runOnUiThread {
                    if (existingUserByUsername != null) {
                        tilRegisterName.error = "Username already exists"
                        etRegisterName.requestFocus()
                        return@runOnUiThread
                    }

                    if (existingUserByPhone != null) {
                        tilRegisterPhone.error = "Phone number already exists"
                        etRegisterPhone.requestFocus()
                        return@runOnUiThread
                    }

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                val user = FirebaseAuth.getInstance().currentUser

                                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build()

                                user?.updateProfile(profileUpdates)

                                val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

                                CoroutineScope(Dispatchers.IO).launch {
                                    userDao.insertUser(
                                        UserEntity(
                                            uid = uid,
                                            username = username,
                                            email = email,
                                            phone = phone.ifEmpty { null }
                                        )
                                    )
                                }

                                findNavController().navigate(
                                    R.id.action_registerFragment_to_completeProfileFragment,
                                    bundle
                                )
                            } else {
                                tilRegisterEmail.error = "Email already exists"
                                etRegisterEmail.requestFocus()
                                tilRegisterPhone.error = null
                            }
                        }
                }
            }
        }

        tvGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
