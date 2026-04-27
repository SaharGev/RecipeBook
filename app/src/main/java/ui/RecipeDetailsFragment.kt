// ui/RecipeDetailsFragment
package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.recipebook.R
import com.example.recipebook.model.Recipe
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import com.example.recipebook.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.graphics.Typeface

class RecipeDetailsFragment : Fragment(R.layout.fragment_recipe_details) {

    private val userViewModel: UserViewModel by viewModels()
    private val recipeViewModel: com.example.recipebook.viewmodel.RecipeViewModel by viewModels()

    private var currentRecipe: Recipe? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = RecipeDetailsFragmentArgs.fromBundle(requireArguments())
        currentRecipe = args.recipe

        val recipe = currentRecipe ?: return

        val tvTitle = view.findViewById<TextView>(R.id.tvRecipeTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvIngredients = view.findViewById<TextView>(R.id.tvIngredientsContent)
        val tvInstructions = view.findViewById<TextView>(R.id.tvInstructionsContent)
        val imgRecipe = view.findViewById<ImageView>(R.id.imgRecipe)
        val tvCookTime = view.findViewById<TextView>(R.id.tvCookTime)
        val tvDifficulty = view.findViewById<TextView>(R.id.tvDifficulty)
        val tvSharedWith = view.findViewById<TextView>(R.id.tvSharedWith)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
        val btnComments = view.findViewById<ImageView>(R.id.btnComments)

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        btnComments.setOnClickListener {
            val sheet = CommentsBottomSheetFragment()
            val bundle = Bundle().apply {
                putInt("recipeId", recipe.id)
            }
            sheet.arguments = bundle
            sheet.show(parentFragmentManager, "CommentsBottomSheet")
        }

        if (recipe.ownerUid != currentUid) {
            btnEdit.visibility = View.GONE
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnEdit.setOnClickListener {
            val action =
                RecipeDetailsFragmentDirections.actionRecipeDetailsFragmentToAddRecipeFragment(recipe)
            findNavController().navigate(action)
        }

        bindRecipe(recipe, currentUid, tvTitle, tvDescription, tvIngredients,
            tvInstructions, tvCookTime, tvDifficulty, imgRecipe, tvSharedWith)
    }

    private fun bindRecipe(
        recipe: Recipe,
        currentUid: String,
        tvTitle: TextView,
        tvDescription: TextView,
        tvIngredients: TextView,
        tvInstructions: TextView,
        tvCookTime: TextView,
        tvDifficulty: TextView,
        imgRecipe: ImageView,
        tvSharedWith: TextView
    ) {

        tvTitle.text = recipe.name
        tvDescription.text = recipe.description
        tvIngredients.text = "${recipe.ingredients}"
        tvInstructions.text = "${recipe.instructions}"
        tvCookTime.text = "⏱ ${recipe.cookTime} min"
        tvDifficulty.text = "🔥 ${recipe.difficulty}"

        recipe.imageUri?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imgRecipe)
        }

        if (!recipe.sharedWith.isNullOrEmpty() && recipe.ownerUid == currentUid) {

            val sharedUids = recipe.sharedWith.split(",").map { it.split(":")[0] }

            userViewModel.getFriends(currentUid) { friends ->
                activity?.runOnUiThread {

                    val sharedNames = friends
                        .filter { sharedUids.contains(it.uid) }
                        .map { it.username }

                    if (sharedNames.isNotEmpty()) {
                        tvSharedWith.visibility = View.VISIBLE

                        val text = "Shared with:\n${sharedNames.joinToString("\n")}"
                        val spannable = SpannableString(text)

                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            "Shared with:".length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        tvSharedWith.text = spannable
                    }
                }
            }
        }

        if (recipe.ownerUid != currentUid && recipe.ownerUid.isNotEmpty()) {

            userViewModel.getUserByUid(recipe.ownerUid) { owner ->
                activity?.runOnUiThread {
                    if (owner != null) {

                        tvSharedWith.visibility = View.VISIBLE

                        val text = "Shared by:\n${owner.username}"
                        val spannable = SpannableString(text)

                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            "Shared by:".length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        tvSharedWith.text = spannable
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val recipeId = currentRecipe?.id ?: return

        recipeViewModel.getRecipeById(recipeId) { updatedRecipe ->
            if (updatedRecipe != null) {
                activity?.runOnUiThread {
                    currentRecipe = updatedRecipe

                    view?.let {
                        bindRecipe(
                            updatedRecipe,
                            FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
                            it.findViewById(R.id.tvRecipeTitle),
                            it.findViewById(R.id.tvDescription),
                            it.findViewById(R.id.tvIngredientsContent),
                            it.findViewById(R.id.tvInstructionsContent),
                            it.findViewById(R.id.tvCookTime),
                            it.findViewById(R.id.tvDifficulty),
                            it.findViewById(R.id.imgRecipe),
                            it.findViewById(R.id.tvSharedWith)
                        )
                    }
                }
            }
        }
    }
}