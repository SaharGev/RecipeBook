//ui/CompleteProfileFragment
package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.recipebook.R
import androidx.navigation.fragment.findNavController
import android.widget.EditText
import android.net.Uri
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.ImageView
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import com.example.recipebook.db.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.fragment.app.viewModels
import com.example.recipebook.viewmodel.UserViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading

class CompleteProfileFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private val userViewModel: UserViewModel by viewModels()
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

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri

                view?.findViewById<ImageView>(R.id.imgProfilePreview)?.let { imageView ->                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                }
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                val file = File(requireContext().filesDir, "profile_${System.currentTimeMillis()}.jpg")

                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                }

                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )

                selectedImageUri = uri

                view?.findViewById<ImageView>(R.id.imgProfilePreview)?.let { imageView ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_complete_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSelectProfileImage = view.findViewById<Button>(R.id.btnSelectProfileImage)
        val btnFinishProfile = view.findViewById<Button>(R.id.btnFinishProfile)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val fullName = arguments?.getString("fullName").orEmpty()
        val etCompleteName = view.findViewById<EditText>(R.id.etCompleteName)
        etCompleteName.setText(fullName)

        btnSelectProfileImage.setOnClickListener {
            val options = arrayOf("Camera", "Gallery")

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    } else {
                        pickImageLauncher.launch("image/*")
                    }
                }
                .show()
        }

        btnFinishProfile.setOnClickListener {

            val username = etCompleteName.text.toString().trim()

            if (username.isEmpty()) {
                etCompleteName.error = "Username is required"
                etCompleteName.requestFocus()
                return@setOnClickListener
            }

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()

            firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val prefs = requireContext().getSharedPreferences("profile_prefs", android.content.Context.MODE_PRIVATE)
                    val uid = firebaseUser?.uid.orEmpty()

                    prefs.edit()
                        .putString("profile_image_uri_$uid", selectedImageUri?.toString())
                        .apply()

                    val email = firebaseUser?.email.orEmpty()

                    showLoading()

                    val newUser = UserEntity(
                        uid = uid,
                        username = username,
                        email = email,
                        phone = null
                    )

                    userViewModel.saveUserLocallyAndRemotely(newUser) {
                        requireActivity().runOnUiThread {
                            hideLoading()

                            findNavController().navigate(R.id.action_completeProfileFragment_to_profileFragment)
                        }
                    }

                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}