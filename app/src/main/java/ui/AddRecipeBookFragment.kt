package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.recipebook.R
import com.example.recipebook.db.BookEntity
import androidx.fragment.app.viewModels
import com.example.recipebook.db.RecipeEntity
import com.example.recipebook.viewmodel.AddRecipeBookViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import com.google.android.material.snackbar.Snackbar

class AddRecipeBookFragment : Fragment() {

    private val viewModel: AddRecipeBookViewModel by viewModels()
    private val args: AddRecipeBookFragmentArgs by navArgs()

    private var booksList: List<BookEntity> = emptyList()
    private var recipesList: List<RecipeEntity> = emptyList()
    private val selectedFriendPermissions = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_recipe_book, container, false)

        val contentCreate = view.findViewById<LinearLayout>(R.id.contentCreate)
        val contentAdd = view.findViewById<LinearLayout>(R.id.contentAdd)
        val arrowCreate = view.findViewById<TextView>(R.id.arrowCreate)
        val arrowAdd = view.findViewById<TextView>(R.id.arrowAdd)
        val headerCreate = view.findViewById<LinearLayout>(R.id.headerCreate)
        val headerAdd = view.findViewById<LinearLayout>(R.id.headerAdd)
        val etBookName = view.findViewById<EditText>(R.id.etBookName)
        val etBookDescription = view.findViewById<EditText>(R.id.etBookDescription)
        val btnCreateBook = view.findViewById<Button>(R.id.btnCreateBook)
        val spinnerBooks = view.findViewById<Spinner>(R.id.spinnerBooks)
        val spinnerRecipes = view.findViewById<Spinner>(R.id.spinnerRecipes)
        val btnAddRecipe = view.findViewById<Button>(R.id.btnAddRecipe)
        val llFriendsList = view.findViewById<LinearLayout>(R.id.llFriendsList)
        val headerShareWith = view.findViewById<LinearLayout>(R.id.headerShareWith)
        val tvShareWithArrow = view.findViewById<TextView>(R.id.tvShareWithArrow)

        setupInitialVisibility(contentCreate, contentAdd, arrowCreate, arrowAdd)

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        headerCreate.setOnClickListener {
            toggleSection(contentCreate, arrowCreate)
        }

        headerAdd.setOnClickListener {
            toggleSection(contentAdd, arrowAdd)
        }

        headerShareWith.setOnClickListener {
            toggleSection(llFriendsList, tvShareWithArrow)
        }

        viewModel.getAllBooks { books ->
            requireActivity().runOnUiThread {
                booksList = books
                val bookTitles = mutableListOf("Select Book")
                bookTitles.addAll(books.map { it.title })

                val booksAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bookTitles)
                booksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerBooks.adapter = booksAdapter

                if (args.mode == "add" && args.selectedBookTitle.isNotEmpty()) {
                    val indexToSelect = books.indexOfFirst { it.title == args.selectedBookTitle }
                    if (indexToSelect != -1) {
                        spinnerBooks.setSelection(indexToSelect + 1)
                    }
                }
            }
        }

        spinnerBooks.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    resetRecipeSpinner(spinnerRecipes)
                    return
                }

                val selectedBook = booksList[position - 1]
                viewModel.getAvailableRecipesForBook(selectedBook.id) { recipes ->
                    requireActivity().runOnUiThread {
                        recipesList = recipes
                        val recipeNames = mutableListOf("Select Recipe")
                        recipeNames.addAll(recipesList.map { it.name })

                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recipeNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerRecipes.adapter = adapter
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadFriends(llFriendsList)

        btnCreateBook.setOnClickListener {
            val bookName = etBookName.text.toString().trim()
            if (bookName.isEmpty()) {
                etBookName.error = "Book name is required"
                return@setOnClickListener
            }

            showLoading()
            viewModel.createBook(
                title = bookName,
                description = etBookDescription.text.toString().trim(),
                isPublic = false,
                sharedWith = selectedFriendPermissions.entries.joinToString(",") { "${it.key}:${it.value}" }
            ) {
                requireActivity().runOnUiThread {
                    hideLoading()
                    Snackbar.make(requireView(), "Book created successfully", Snackbar.LENGTH_SHORT).show()
                    etBookName.text.clear()
                    etBookDescription.text.clear()
                }
            }
        }

        btnAddRecipe.setOnClickListener {
            val bookPos = spinnerBooks.selectedItemPosition
            val recipePos = spinnerRecipes.selectedItemPosition

            if (bookPos <= 0 || recipePos <= 0) {
                Snackbar.make(requireView(), "Please select both book and recipe", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedBook = booksList[bookPos - 1]
            val selectedRecipe = recipesList[recipePos - 1]

            showLoading()
            viewModel.addRecipeToBook(selectedRecipe.id, selectedBook.id)

            requireActivity().runOnUiThread {
                hideLoading()
                Snackbar.make(requireView(), "Recipe added to book!", Snackbar.LENGTH_SHORT).show()
                recipesList = recipesList.filter { it.id != selectedRecipe.id }
                updateRecipeSpinner(spinnerRecipes)
            }
        }

        return view
    }

    private fun setupInitialVisibility(create: View, add: View, arrowC: TextView, arrowA: TextView) {
        if (args.mode == "add") {
            add.visibility = View.VISIBLE
            arrowA.text = "▲"
            create.visibility = View.GONE
            arrowC.text = "▼"
        } else {
            create.visibility = View.VISIBLE
            arrowC.text = "▲"
            add.visibility = View.GONE
            arrowA.text = "▼"
        }
    }

    private fun toggleSection(content: View, arrow: TextView) {
        if (content.visibility == View.GONE) {
            content.visibility = View.VISIBLE
            arrow.text = "▲"
        } else {
            content.visibility = View.GONE
            arrow.text = "▼"
        }
    }

    private fun resetRecipeSpinner(spinner: Spinner) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("Select Recipe"))
        spinner.adapter = adapter
    }

    private fun updateRecipeSpinner(spinner: Spinner) {
        val names = mutableListOf("Select Recipe")
        names.addAll(recipesList.map { it.name })
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun loadFriends(container: LinearLayout) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewModel.getFriends(uid) { friends ->
            requireActivity().runOnUiThread {
                container.removeAllViews()
                if (friends.isEmpty()) {
                    container.addView(TextView(requireContext()).apply { text = "No friends found" })
                } else {
                    friends.forEach { friend ->
                        val cb = CheckBox(requireContext()).apply {
                            text = friend.username
                            setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) selectedFriendPermissions[friend.uid] = "view"
                                else selectedFriendPermissions.remove(friend.uid)
                            }
                        }
                        container.addView(cb)
                    }
                }
            }
        }
    }
}