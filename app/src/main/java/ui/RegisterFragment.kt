//ui/RegisterFragment
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
import androidx.fragment.app.viewModels
import com.example.recipebook.viewmodel.UserViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth
    private val userViewModel: UserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

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
                tilRegisterName.error = "Username must be at least 3 characters"
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

            userViewModel.getUserByUsername(username) { existingUserByUsername ->
                requireActivity().runOnUiThread {
                    if (existingUserByUsername != null) {
                        tilRegisterName.error = "Username already exists"
                        etRegisterName.requestFocus()
                        return@runOnUiThread
                    }

                    if (phone.isNotEmpty()) {
                        userViewModel.getUserByPhone(phone) { existingUserByPhone ->
                            requireActivity().runOnUiThread {
                                if (existingUserByPhone != null) {
                                    tilRegisterPhone.error = "Phone number already exists"
                                    etRegisterPhone.requestFocus()
                                    return@runOnUiThread
                                }

                                registerUser(
                                    username = username,
                                    email = email,
                                    phone = phone.ifEmpty { null },
                                    password = password,
                                    bundle = bundle
                                )
                            }
                        }
                    } else {
                        registerUser(
                            username = username,
                            email = email,
                            phone = null,
                            password = password,
                            bundle = bundle
                        )
                    }
                }
            }
        }

        tvGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun registerUser(
        username: String,
        email: String,
        phone: String?,
        password: String,
        bundle: Bundle
    ) {
        showLoading()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = FirebaseAuth.getInstance().currentUser

                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)

                    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

                    val newUser = com.example.recipebook.db.UserEntity(
                        uid = uid,
                        username = username,
                        email = email,
                        phone = phone
                    )

                    userViewModel.saveUserLocallyAndRemotely(newUser) {
                        requireActivity().runOnUiThread {
                            hideLoading()

                            findNavController().navigate(
                                R.id.action_registerFragment_to_completeProfileFragment,
                                bundle
                            )
                        }
                    }
                } else {
                    hideLoading()
                    requireActivity().runOnUiThread {
                        view?.findViewById<TextInputLayout>(R.id.tilRegisterEmail)?.error = "Email already exists"
                        view?.findViewById<TextInputEditText>(R.id.etRegisterEmail)?.requestFocus()
                        view?.findViewById<TextInputLayout>(R.id.tilRegisterPhone)?.error = null
                    }
                }
            }
    }
}
