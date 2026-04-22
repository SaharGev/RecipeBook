package com.example.recipebook.ui

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.recipebook.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.example.recipebook.viewmodel.UserViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import java.io.File
import java.io.FileOutputStream
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var selectedImageUri: Uri? = null
    private val userViewModel: UserViewModel by viewModels()

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                val savedUri = saveBitmapToFile(bitmap)
                selectedImageUri = savedUri
                view?.findViewById<ShapeableImageView>(R.id.imgSettingsProfile)?.let { imageView ->
                    Glide.with(this).load(savedUri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                view?.findViewById<ShapeableImageView>(R.id.imgSettingsProfile)?.let { imageView ->
                    Glide.with(this).load(uri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                }
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) takePhotoLauncher.launch(null)
            else com.google.android.material.snackbar.Snackbar.make(requireView(), "Camera permission denied", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgSettingsProfile = view.findViewById<ShapeableImageView>(R.id.imgSettingsProfile)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val btnSaveChanges = view.findViewById<Button>(R.id.btnSaveChanges)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val tilUsername = view.findViewById<TextInputLayout>(R.id.tilUsername)
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPhone = view.findViewById<TextInputLayout>(R.id.tilPhone)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        // Load user data
        userViewModel.getUserByUid(uid) { user ->
            activity?.runOnUiThread {
                etUsername.setText(user?.username ?: "")
                etEmail.setText(user?.email ?: "")
                etPhone.setText(user?.phone ?: "")
            }
        }

        loadSavedProfileImage(imgSettingsProfile)

        // Click on profile image to change photo
        view.findViewById<android.widget.ImageView>(R.id.imgEditPhoto).setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(arrayOf("Camera", "Gallery")) { _, which ->
                    if (which == 0) requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    else pickImageLauncher.launch("image/*")
                }
                .show()
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSaveChanges.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val newUsername = etUsername.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()

            // Basic validations
            if (newUsername.isEmpty()) {
                tilUsername.error = "Username is required"
                etUsername.requestFocus()
                return@setOnClickListener
            }

            if (newUsername.length < 3) {
                tilUsername.error = "Username must be at least 3 characters"
                tilUsername.requestFocus()
                return@setOnClickListener
            }

            if (newPhone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(newPhone).matches()) {
                tilPhone.error = "Invalid phone number"
                etPhone.requestFocus()
                return@setOnClickListener
            }

            showLoading()

            // Check if username already exists (by someone else)
            userViewModel.getUserByUsername(newUsername) { existingUser ->
                requireActivity().runOnUiThread {
                    if (existingUser != null && existingUser.uid != uid) {
                        hideLoading()
                        tilUsername.error = "Username already exists"
                        etUsername.requestFocus()
                        return@runOnUiThread
                    }

                    // Check if phone already exists (by someone else)
                    if (newPhone.isNotEmpty()) {
                        userViewModel.getUserByPhone(newPhone) { existingByPhone ->
                            requireActivity().runOnUiThread {
                                if (existingByPhone != null && existingByPhone.uid != uid) {
                                    hideLoading()
                                    tilPhone.error = "Phone number already exists"
                                    etPhone.requestFocus()
                                    return@runOnUiThread
                                }

                                // Check email
                                val newEmail = etEmail.text.toString().trim()
                                userViewModel.getUserByEmail(newEmail) { existingByEmail ->
                                    requireActivity().runOnUiThread {
                                        if (existingByEmail != null && existingByEmail.uid != uid) {
                                            hideLoading()
                                            tilEmail.error = "Email already exists"
                                            etEmail.requestFocus()
                                            return@runOnUiThread
                                        }
                                        saveUserChanges(uid, newUsername, newPhone)
                                    }
                                }
                            }
                        }
                    } else {
                        val newEmail = etEmail.text.toString().trim()
                        userViewModel.getUserByEmail(newEmail) { existingByEmail ->
                            requireActivity().runOnUiThread {
                                if (existingByEmail != null && existingByEmail.uid != uid) {
                                    hideLoading()
                                    tilEmail.error = "Email already exists"
                                    etEmail.requestFocus()
                                    return@runOnUiThread
                                }
                                saveUserChanges(uid, newUsername, newPhone)
                            }
                        }
                    }
                }
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().popBackStack(R.id.loginFragment, false)
        }
    }

    private fun loadSavedProfileImage(imageView: ShapeableImageView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        userViewModel.getUserByUid(uid) { user ->
            activity?.runOnUiThread {
                if (!user?.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(user?.profileImageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val file = File(requireContext().filesDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { it.apply { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, this); flush() } }
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
    }

    private fun saveUserChanges(uid: String, newUsername: String, newPhone: String) {
        userViewModel.getUserByUid(uid) { user ->
            if (user != null) {
                val updatedUser = user.copy(
                    username = newUsername,
                    phone = newPhone.ifEmpty { null }
                )

                if (selectedImageUri != null) {
                    userViewModel.uploadProfileImage(uid, selectedImageUri!!) { imageUrl ->
                        val finalUser = if (imageUrl.isNotEmpty()) updatedUser.copy(profileImageUrl = imageUrl) else updatedUser
                        userViewModel.saveUserLocallyAndRemotely(finalUser) {
                            requireActivity().runOnUiThread {
                                hideLoading()
                                com.google.android.material.snackbar.Snackbar.make(requireView(), "Changes saved successfully", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    userViewModel.saveUserLocallyAndRemotely(updatedUser) {
                        requireActivity().runOnUiThread {
                            hideLoading()
                            com.google.android.material.snackbar.Snackbar.make(requireView(), "Changes saved successfully", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                requireActivity().runOnUiThread { hideLoading() }
            }
        }
    }
}