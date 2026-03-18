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

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var selectedImageUri: Uri? = null

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                val savedUri = saveBitmapToCache(bitmap)
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

        tvSettingsUserName.text = "User Name"

        loadSavedProfileImage(imgSettingsProfile)

        btnChangePhoto.setOnClickListener {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnEditProfile.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Edit profile coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnSaveChanges.setOnClickListener {
            saveProfileImage()
            Toast.makeText(
                requireContext(),
                "Changes saved successfully",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnLogout.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun saveProfileImage() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("profile_image_uri", selectedImageUri?.toString())
            .apply()
    }

    private fun loadSavedProfileImage(imageView: ShapeableImageView) {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val savedImageUri = prefs.getString("profile_image_uri", null)

        if (!savedImageUri.isNullOrEmpty()) {
            selectedImageUri = Uri.parse(savedImageUri)

            Glide.with(this)
                .load(selectedImageUri)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView)
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.jpg")

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