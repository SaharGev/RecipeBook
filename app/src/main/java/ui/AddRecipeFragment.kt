//ui/AddRecipeFragment
package com.example.recipebook.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.recipebook.R
import com.example.recipebook.viewmodel.RecipeViewModel
import java.io.File
import java.io.FileOutputStream
import android.Manifest
import android.widget.TextView
import com.example.recipebook.model.Recipe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading

class AddRecipeFragment : Fragment(R.layout.fragment_add_recipe) {

    private val viewModel: RecipeViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val selectedFriendPermissions = mutableMapOf<String, String>()
    private val userViewModel: com.example.recipebook.viewmodel.UserViewModel by viewModels()
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                view?.findViewById<ImageView>(R.id.imgRecipe)?.let { imageView ->
                    Glide.with(this)
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
                val savedUri = saveBitmapToCache(bitmap)
                selectedImageUri = savedUri

                view?.findViewById<ImageView>(R.id.imgRecipe)?.let { imageView ->
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
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Difficulty chip
        val difficultyChip = view.findViewById<TextView>(R.id.tvDifficulty)
        difficultyChip.setOnClickListener {
            val options = arrayOf("Easy", "Medium", "High")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Difficulty")
                .setItems(options) { _, which ->
                    difficultyChip.text = options[which]
                }
                .show()
        }

        // Privacy chip
        val tvShareWith = view.findViewById<TextView>(R.id.tvShareWith)
        val llFriendsList = view.findViewById<android.widget.LinearLayout>(R.id.llFriendsList)

        val privacyChip = view.findViewById<TextView>(R.id.tvPrivacy)

        privacyChip.setOnClickListener {
            val options = arrayOf("Public", "Private")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Privacy")
                .setItems(options) { _, which ->
                    privacyChip.text = options[which]

                    if (options[which] == "Public") {
                        tvShareWith.visibility = View.VISIBLE
                        llFriendsList.visibility = View.VISIBLE

                        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                        userViewModel.getFriends(uid) { friends ->
                            requireActivity().runOnUiThread {
                                llFriendsList.removeAllViews()
                                selectedFriendPermissions.clear()

                                if (friends.isEmpty()) {
                                    val tvNoFriends = android.widget.TextView(requireContext())
                                    tvNoFriends.text = "You have no friends yet"
                                    llFriendsList.addView(tvNoFriends)
                                    return@runOnUiThread
                                }

                                friends.forEach { friend ->
                                    val rowLayout = android.widget.LinearLayout(requireContext()).apply {
                                        orientation = android.widget.LinearLayout.HORIZONTAL
                                        gravity = android.view.Gravity.CENTER_VERTICAL
                                    }

                                    val checkBox = android.widget.CheckBox(requireContext()).apply {
                                        text = friend.username
                                        layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                    }

                                    val permissionSpinner = android.widget.Spinner(requireContext())
                                    permissionSpinner.visibility = View.GONE
                                    val permissions = listOf("view", "edit")
                                    val permAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, permissions)
                                    permAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    permissionSpinner.adapter = permAdapter

                                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                                        if (isChecked) {
                                            permissionSpinner.visibility = View.VISIBLE
                                            selectedFriendPermissions[friend.uid] = "view"
                                            permissionSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                                                override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, position: Int, id: Long) {
                                                    selectedFriendPermissions[friend.uid] = if (position == 0) "view" else "edit"
                                                }
                                                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                                            }
                                        } else {
                                            permissionSpinner.visibility = View.GONE
                                            selectedFriendPermissions.remove(friend.uid)
                                        }
                                    }

                                    rowLayout.addView(checkBox)
                                    rowLayout.addView(permissionSpinner)
                                    llFriendsList.addView(rowLayout)
                                }
                            }
                        }
                    } else {
                        tvShareWith.visibility = View.GONE
                        llFriendsList.visibility = View.GONE
                        selectedFriendPermissions.clear()
                    }
                }
                .show()
        }

        val recipeToEdit = arguments?.getParcelable<Recipe>("recipe")
        val selectedBookId = arguments?.getInt("bookId", 1) ?: 1

        val etName = view.findViewById<EditText>(R.id.etName)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etIngredients = view.findViewById<EditText>(R.id.etIngredients)
        val etInstructions = view.findViewById<EditText>(R.id.etInstructions)
        val etTime = view.findViewById<EditText>(R.id.etTime)
        val tvDifficulty = view.findViewById<TextView>(R.id.tvDifficulty)
        val tvPrivacy = view.findViewById<TextView>(R.id.tvPrivacy)

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnSelectImage = view.findViewById<Button>(R.id.btnSelectImage)
        val btnTakePhoto = view.findViewById<Button>(R.id.btnTakePhoto)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        if (recipeToEdit != null) {
            etName.setText(recipeToEdit.name)
            etDescription.setText(recipeToEdit.description)
            etIngredients.setText(recipeToEdit.ingredients)
            etInstructions.setText(recipeToEdit.instructions)
            etTime.setText(recipeToEdit.cookTime.toString())
            tvDifficulty.text = recipeToEdit.difficulty
            tvPrivacy.text = if (recipeToEdit.isPublic) "Public" else "Private"

            selectedImageUri = recipeToEdit.imageUri?.let { Uri.parse(it) }

            selectedImageUri?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(view.findViewById(R.id.imgRecipe))
            }

            btnSave.text = "Update Recipe"
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnTakePhoto.setOnClickListener {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnSave.setOnClickListener {

            val name = etName.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val ingredients = etIngredients.text.toString().trim()
            val instructions = etInstructions.text.toString().trim()

            val time = etTime.text.toString().toIntOrNull() ?: 0

            val difficulty = tvDifficulty.text.toString()
                .takeIf { it != "Difficulty" } ?: "Easy"

            val privacyText = tvPrivacy.text.toString()
            if (privacyText == "Privacy") {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please select privacy",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val isPublic = privacyText == "Public"

            //Validation
            if (name.isEmpty()) {
                etName.error = "Recipe name is required"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (ingredients.isEmpty()) {
                etIngredients.error = "Ingredients are required"
                etIngredients.requestFocus()
                return@setOnClickListener
            }

            if (instructions.isEmpty()) {
                etInstructions.error = "Instructions are required"
                etInstructions.requestFocus()
                return@setOnClickListener
            }

            if (time == 0) {
                etTime.error = "Please enter cooking time"
                etTime.requestFocus()
                return@setOnClickListener
            }

            if (tvDifficulty.text.toString() == "Difficulty") {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please select difficulty",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (recipeToEdit != null) {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

                showLoading()

                viewModel.updateRecipeByFields(
                    id = recipeToEdit.id,
                    bookId = recipeToEdit.id,
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    imageUri = selectedImageUri?.toString(),
                    cookTime = time,
                    difficulty = difficulty,
                    isPublic = isPublic,
                    sharedWith = selectedFriendPermissions.entries.joinToString(",") { "${it.key}:${it.value}" },
                    uid = uid,
                    onDone = {
                        requireActivity().runOnUiThread {
                            hideLoading()
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Recipe updated successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                    }
                )
            } else {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

                showLoading()

                val sharedWith = selectedFriendPermissions.entries.joinToString(",") { "${it.key}:${it.value}" }

                viewModel.addRecipe(
                    uid = uid,
                    bookId = selectedBookId,
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    imageUri = selectedImageUri?.toString(),
                    cookTime = time,
                    difficulty = difficulty,
                    isPublic = isPublic,
                    sharedWith = sharedWith,
                    onDone = {
                        requireActivity().runOnUiThread {
                            hideLoading()
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Recipe saved successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                    }
                )
            }
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "recipe_${System.currentTimeMillis()}.jpg")

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