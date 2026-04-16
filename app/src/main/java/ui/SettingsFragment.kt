//ui/SettingsFragment
package com.example.recipebook.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.recipebook.R
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.viewModels
import com.example.recipebook.viewmodel.UserViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var selectedImageUri: Uri? = null
    private val userViewModel: UserViewModel by viewModels()

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                val savedUri = saveBitmapToFile(bitmap)
                selectedImageUri = savedUri

                view?.findViewById<ShapeableImageView>(R.id.imgSettingsProfile)?.let { imageView ->
                    Glide.with(this)
                        .load(savedUri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri

                view?.findViewById<ShapeableImageView>(R.id.imgSettingsProfile)?.let { imageView ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                }
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takePhotoLauncher.launch(null)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgSettingsProfile = view.findViewById<ShapeableImageView>(R.id.imgSettingsProfile)
        val tvSettingsUserName = view.findViewById<TextView>(R.id.tvSettingsUserName)
        val btnChangePhoto = view.findViewById<Button>(R.id.btnChangePhoto)
        val btnEditProfile = view.findViewById<Button>(R.id.btnEditProfile)
        val btnSaveChanges = view.findViewById<Button>(R.id.btnSaveChanges)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val currentUser = FirebaseAuth.getInstance().currentUser
        tvSettingsUserName.text = currentUser?.displayName ?: "User Name"

        loadSavedProfileImage(imgSettingsProfile)

        btnChangePhoto.setOnClickListener {
            val options = arrayOf("Camera", "Gallery")

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    } else {
                        pickImageLauncher.launch("image/*")
                    }
                }
                .show()
        }

        btnEditProfile.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Edit profile coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnSaveChanges.setOnClickListener {
            if (selectedImageUri != null) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

                showLoading()

                userViewModel.uploadProfileImage(uid, selectedImageUri!!) { imageUrl ->
                    if (imageUrl.isNotEmpty()) {
                        userViewModel.getUserByUid(uid) { user ->
                            if (user != null) {
                                val updatedUser = user.copy(profileImageUrl = imageUrl)
                                userViewModel.saveUserLocallyAndRemotely(updatedUser) {
                                    requireActivity().runOnUiThread {
                                        hideLoading()
                                        Toast.makeText(
                                            requireContext(),
                                            "Changes saved successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                "Failed to upload image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "No image selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnLogout.setOnClickListener {
            val context = requireContext()
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

            // Clear local Room database
//            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
//                com.example.recipebook.db.DatabaseProvider.clearAllData(requireContext())
//            }

            findNavController().popBackStack(R.id.loginFragment, false)
        }
    }

    private fun saveProfileImage() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        prefs.edit()
            .putString("profile_image_uri_$uid", selectedImageUri?.toString())
            .apply()
    }

    private fun loadSavedProfileImage(imageView: ShapeableImageView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        userViewModel.getUserByUid(uid) { user ->
            activity?.runOnUiThread {
                if (!user?.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(user?.profileImageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val file = File(requireContext().filesDir, "profile_${System.currentTimeMillis()}.jpg")

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
        }

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
    }
}