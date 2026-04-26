//ui/RecipeDetailsFragment
package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.recipebook.R
import com.example.recipebook.model.Recipe
import android.net.Uri
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import com.example.recipebook.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class RecipeDetailsFragment : Fragment(R.layout.fragment_recipe_details) {

    private val userViewModel: UserViewModel by viewModels()
    private val recipeViewModel: com.example.recipebook.viewmodel.RecipeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = RecipeDetailsFragmentArgs.fromBundle(requireArguments())
        val recipe = args.recipe

        if (recipe == null) return

        val tvTitle = view.findViewById<TextView>(R.id.tvRecipeTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvIngredients = view.findViewById<TextView>(R.id.tvIngredientsContent)
        val tvInstructions = view.findViewById<TextView>(R.id.tvInstructionsContent)
        val imgRecipe = view.findViewById<android.widget.ImageView>(R.id.imgRecipe)
        val tvCookTime = view.findViewById<TextView>(R.id.tvCookTime)
        val tvDifficulty = view.findViewById<TextView>(R.id.tvDifficulty)
        val tvPrivacy = view.findViewById<TextView>(R.id.tvPrivacy)
        tvPrivacy.visibility = View.GONE
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)

        val btnComments = view.findViewById<ImageView>(R.id.btnComments)

        btnComments.setOnClickListener {
            val sheet = CommentsBottomSheetFragment()

            val bundle = Bundle().apply {
                putInt("recipeId", recipe.id)
            }

            sheet.arguments = bundle
            sheet.show(parentFragmentManager, "CommentsBottomSheet")
        }

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        android.util.Log.d("DEBUG", "ownerUid: ${recipe.ownerUid}, currentUid: $currentUid")

        if (recipe.ownerUid != currentUid) {
            btnEdit.visibility = View.GONE
            tvPrivacy.visibility = View.GONE
        }

        if (recipe != null) {
            tvTitle.text = "${recipe.name} (ID: ${recipe.id})"
            tvDescription.text = recipe.description
            tvIngredients.text = "Ingredients: ${recipe.ingredients}"
            tvInstructions.text = "Instructions: ${recipe.instructions}"
            tvCookTime.text = "⏱ ${recipe.cookTime} min"
            tvDifficulty.text = "🔥 ${recipe.difficulty}"

            tvPrivacy.text = if (recipe.isPublic) {
                "🌍 Public"
            } else {
                "🔒 Private"
            }

            recipe.imageUri?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imgRecipe)
            }

            val tvSharedWith = view.findViewById<TextView>(R.id.tvSharedWith)

            if (!recipe.sharedWith.isNullOrEmpty() && recipe.ownerUid == currentUid) {
                val sharedUids = recipe.sharedWith.split(",").map { it.split(":")[0] }
                userViewModel.getFriends(currentUid) { friends ->
                    activity?.runOnUiThread {
                        val sharedNames = friends
                            .filter { sharedUids.contains(it.uid) }
                            .map { it.username }
                        if (sharedNames.isNotEmpty()) {
                            tvSharedWith.visibility = View.VISIBLE
                            val spannable = android.text.SpannableString("Shared with:\n${sharedNames.joinToString("\n")}")
                            spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Shared with:".length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            tvSharedWith.text = spannable
                        }
                    }
                }
            }

            if (recipe.ownerUid != currentUid && !recipe.ownerUid.isNullOrEmpty()) {
                userViewModel.getUserByUid(recipe.ownerUid) { owner ->
                    activity?.runOnUiThread {
                        if (owner != null) {
                            tvSharedWith.visibility = View.VISIBLE
                            val spannable = android.text.SpannableString("Shared by:\n${owner.username}")
                            spannable.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Shared by:".length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            tvSharedWith.text = spannable
                        }
                    }
                }
            }

        } else {
            tvTitle.text = "No recipe received"
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnEdit.setOnClickListener {
            if (recipe != null) {
                val action = RecipeDetailsFragmentDirections.actionRecipeDetailsFragmentToAddRecipeFragment(
                    recipe = recipe
                )
                findNavController().navigate(action)
            }
        }

        (requireActivity() as androidx.appcompat.app.AppCompatActivity)
            .supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
}