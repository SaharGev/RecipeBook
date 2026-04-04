//ui/LoginFragment
package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipebook.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.example.recipebook.db.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.os.bundleOf
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import androidx.fragment.app.viewModels
import com.example.recipebook.viewmodel.UserViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val userViewModel: UserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid

            showLoading()

            userViewModel.getOrFetchUser(uid) { user ->
                requireActivity().runOnUiThread {
                    if (user != null) {
                        hideLoading()

                        val navController = findNavController()
                        if (navController.currentDestination?.id != R.id.profileFragment) {
                            navController.navigate(R.id.profileFragment)
                        }
                    } else {
                        val fullName = currentUser.displayName.orEmpty()

                        hideLoading()

                        val navController = findNavController()
                        if (navController.currentDestination?.id != R.id.completeProfileFragment) {
                            navController.navigate(
                                R.id.completeProfileFragment,
                                bundleOf("fullName" to fullName)
                            )
                        }
                    }
                }
            }

            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val userDao = DatabaseProvider.getDatabase(requireContext()).userDao()

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnGoogleLogin = view.findViewById<Button>(R.id.btnGoogleLogin)

        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tvGoToRegister)

        val tilEmail = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPassword)

        btnLogin.setOnClickListener {
            val identifier = etEmail.text?.toString()?.trim().orEmpty()
            val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()
            val password = etPassword.text?.toString()?.trim().orEmpty()

            tilEmail.error = null
            tilPassword.error = null

            if (identifier.isEmpty()) {
                tilEmail.error = "Email or phone is required"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (isEmail) {
                auth.signInWithEmailAndPassword(identifier, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                        } else {
                            Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                userViewModel.getUserByPhone(identifier) { user ->
                    requireActivity().runOnUiThread {
                        if (user == null) {
                            tilEmail.error = "User not found"
                            etEmail.requestFocus()
                            return@runOnUiThread
                        }

                        auth.signInWithEmailAndPassword(user.email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                                } else {
                                    tilEmail.error = "Invalid email or password"
                                    etEmail.requestFocus()
                                }
                            }
                    }
                }
            }
        }

        btnGoogleLogin.setOnClickListener {
            showLoading()

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 100)
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(requireContext(), "Forgot password coming soon", Toast.LENGTH_SHORT).show()
        }

        tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            if (task.isSuccessful) {
                val account = task.result

                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                val userDao = DatabaseProvider.getDatabase(requireContext()).userDao()

                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {

                            val firebaseUser = auth.currentUser
                            val uid = firebaseUser?.uid ?: return@addOnCompleteListener
                            val firestore = FirebaseFirestore.getInstance()

                            userViewModel.getOrFetchUser(uid) { user ->
                                requireActivity().runOnUiThread {
                                    if (user != null) {
                                        val navController = findNavController()

                                        hideLoading()

                                        if (navController.currentDestination?.id != R.id.profileFragment) {
                                            navController.navigate(R.id.profileFragment)
                                        }
                                    } else {
                                        val fullName = firebaseUser.displayName.orEmpty()

                                        hideLoading()

                                        val navController = findNavController()
                                        if (navController.currentDestination?.id != R.id.completeProfileFragment) {
                                            navController.navigate(
                                                R.id.completeProfileFragment,
                                                bundleOf("fullName" to fullName)
                                            )
                                        }
                                    }
                                }
                            }

                        } else {
                            hideLoading()
                            Toast.makeText(requireContext(), "Firebase auth failed", Toast.LENGTH_SHORT).show()
                        }
                    }

            } else {
                hideLoading()
                Toast.makeText(requireContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}