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
import com.example.recipebook.model.Recipe

class AddRecipeFragment : Fragment(R.layout.fragment_add_recipe) {

    private val viewModel: RecipeViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
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

        val recipeToEdit = arguments?.getParcelable<Recipe>("recipe")

        val etName = view.findViewById<EditText>(R.id.etName)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etIngredients = view.findViewById<EditText>(R.id.etIngredients)
        val etInstructions = view.findViewById<EditText>(R.id.etInstructions)

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnSelectImage = view.findViewById<Button>(R.id.btnSelectImage)
        val btnTakePhoto = view.findViewById<Button>(R.id.btnTakePhoto)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        if (recipeToEdit != null) {
            etName.setText(recipeToEdit.name)
            etDescription.setText(recipeToEdit.description)
            etIngredients.setText(recipeToEdit.ingredients)
            etInstructions.setText(recipeToEdit.instructions)

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
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnTakePhoto.setOnClickListener {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnSave.setOnClickListener {
            if (etName.text.toString().trim().isEmpty()) {
                etName.error = "Recipe name is required"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (etIngredients.text.toString().trim().isEmpty()) {
                etIngredients.error = "Ingredients are required"
                etIngredients.requestFocus()
                return@setOnClickListener
            }

            if (etInstructions.text.toString().trim().isEmpty()) {
                etInstructions.error = "Instructions are required"
                etInstructions.requestFocus()
                return@setOnClickListener
            }

            if (recipeToEdit != null) {
                viewModel.updateRecipe(
                    com.example.recipebook.db.RecipeEntity(
                        id = recipeToEdit.id,
                        bookId = 1,
                        name = etName.text.toString(),
                        description = etDescription.text.toString(),
                        ingredients = etIngredients.text.toString(),
                        instructions = etInstructions.text.toString(),
                        imageUri = selectedImageUri?.toString()
                    )
                )
            } else {
                viewModel.addRecipe(
                    bookId = 1,
                    name = etName.text.toString(),
                    description = etDescription.text.toString(),
                    ingredients = etIngredients.text.toString(),
                    instructions = etInstructions.text.toString(),
                    imageUri = selectedImageUri?.toString()
                )
            }

            android.widget.Toast.makeText(
                requireContext(),
                "Recipe saved successfully",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(R.id.homeFragment)        }
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