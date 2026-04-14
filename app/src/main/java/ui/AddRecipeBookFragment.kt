package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipebook.R
import com.example.recipebook.db.BookEntity
import androidx.fragment.app.viewModels
import com.example.recipebook.viewmodel.AddRecipeBookViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading

class AddRecipeBookFragment : Fragment() {

    private val viewModel: AddRecipeBookViewModel by viewModels()
    private lateinit var booksList: List<BookEntity>
    private lateinit var recipesList: List<com.example.recipebook.db.RecipeEntity>

    private val selectedFriendPermissions = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_add_recipe_book, container, false)

        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        val headerCreate = view.findViewById<LinearLayout>(R.id.headerCreate)
        val contentCreate = view.findViewById<LinearLayout>(R.id.contentCreate)
        val arrowCreate = view.findViewById<TextView>(R.id.arrowCreate)

        headerCreate.setOnClickListener {
            if (contentCreate.visibility == View.GONE) {
                contentCreate.visibility = View.VISIBLE
                arrowCreate.text = "▲"
            } else {
                contentCreate.visibility = View.GONE
                arrowCreate.text = "▼"
            }
        }

        val headerAdd = view.findViewById<LinearLayout>(R.id.headerAdd)
        val contentAdd = view.findViewById<LinearLayout>(R.id.contentAdd)
        val arrowAdd = view.findViewById<TextView>(R.id.arrowAdd)

        headerAdd.setOnClickListener {
            if (contentAdd.visibility == View.GONE) {
                contentAdd.visibility = View.VISIBLE
                arrowAdd.text = "▲"
            } else {
                contentAdd.visibility = View.GONE
                arrowAdd.text = "▼"
            }
        }

        val etBookName = view.findViewById<EditText>(R.id.etBookName)
        val etBookDescription = view.findViewById<EditText>(R.id.etBookDescription)
        val spinnerPrivacy = view.findViewById<Spinner>(R.id.spinnerPrivacy)
        val btnCreateBook = view.findViewById<Button>(R.id.btnCreateBook)

        val spinnerBooks = view.findViewById<Spinner>(R.id.spinnerBooks)
        val spinnerRecipes = view.findViewById<Spinner>(R.id.spinnerRecipes)
        spinnerBooks.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (position == 0) {
                    val recipeNames = listOf("Select Recipe")

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        recipeNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerRecipes.adapter = adapter

                    return
                }

                val selectedBook = booksList[position - 1]

                viewModel.getAvailableRecipesForBook(selectedBook.id) { recipes ->
                    requireActivity().runOnUiThread {
                        recipesList = recipes

                        val recipeNames = mutableListOf("Select Recipe")
                        recipeNames.addAll(recipesList.map { it.name })

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            recipeNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerRecipes.adapter = adapter
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val btnAddRecipe = view.findViewById<Button>(R.id.btnAddRecipe)


        val privacyOptions = listOf("Select Privacy", "Private","Public")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            privacyOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrivacy.adapter = adapter

        spinnerPrivacy.setSelection(0)

        val tvShareWith = view.findViewById<TextView>(R.id.tvShareWith)
        val llFriendsList = view.findViewById<LinearLayout>(R.id.llFriendsList)

        spinnerPrivacy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                val selected = privacyOptions[position]
                if (selected == "Public") {
                    tvShareWith.visibility = View.VISIBLE
                    llFriendsList.visibility = View.VISIBLE

                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    viewModel.getFriends(uid) { friends ->
                        requireActivity().runOnUiThread {
                            llFriendsList.removeAllViews()
                            selectedFriendPermissions.clear()

                            if (friends.isEmpty()) {
                                val tvNoFriends = TextView(requireContext())
                                tvNoFriends.text = "You have no friends yet"
                                tvNoFriends.textSize = 14f
                                llFriendsList.addView(tvNoFriends)
                                return@runOnUiThread
                            }

                            friends.forEach { friend ->
                                val rowLayout = LinearLayout(requireContext()).apply {
                                    orientation = LinearLayout.HORIZONTAL
                                    gravity = android.view.Gravity.CENTER_VERTICAL
                                }

                                val checkBox = CheckBox(requireContext()).apply {
                                    text = friend.username
                                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                }

                                val permissionSpinner = Spinner(requireContext())
                                permissionSpinner.visibility = View.GONE
                                val permissions = listOf("view", "edit")
                                val permAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, permissions)
                                permAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                permissionSpinner.adapter = permAdapter


                                checkBox.setOnCheckedChangeListener { _, isChecked ->
                                    if (isChecked) {
                                        permissionSpinner.visibility = View.VISIBLE
                                        selectedFriendPermissions[friend.uid] = "view"
                                        permissionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                                                selectedFriendPermissions[friend.uid] = if (position == 0) "view" else "edit"
                                            }
                                            override fun onNothingSelected(parent: AdapterView<*>?) {}
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

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnCreateBook.setOnClickListener {
            val bookName = etBookName.text.toString().trim()
            val bookDescription = etBookDescription.text.toString().trim()
            val privacySelection = spinnerPrivacy.selectedItem.toString()

            if (bookName.isEmpty()) {
                etBookName.error = "Book name is required"
                etBookName.requestFocus()
                return@setOnClickListener
            }

            if (privacySelection == "Select Privacy") {
                Toast.makeText(requireContext(), "Please select privacy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isPublic = privacySelection == "Public"

            showLoading()
            viewModel.createBook(
                title = bookName,
                description = bookDescription,
                isPublic = isPublic,
                sharedWith = selectedFriendPermissions.entries.joinToString(",") { "${it.key}:${it.value}" }
            ) {
                requireActivity().runOnUiThread {
                    hideLoading()
                    Toast.makeText(requireContext(), "Book created successfully", Toast.LENGTH_SHORT).show()

                    etBookName.text.clear()
                    etBookDescription.text.clear()
                    spinnerPrivacy.setSelection(0)
                }
            }
        }

        btnAddRecipe.setOnClickListener {
            val selectedBookIndex = spinnerBooks.selectedItemPosition
            val selectedRecipeIndex = spinnerRecipes.selectedItemPosition

            if (selectedBookIndex == 0) {
                Toast.makeText(requireContext(), "Please select a book", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRecipeIndex == 0) {
                Toast.makeText(requireContext(), "Please select a recipe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedBook = booksList[selectedBookIndex - 1]
            val selectedRecipe = recipesList[selectedRecipeIndex - 1]

            showLoading()
            viewModel.addRecipeToBook(selectedRecipe, selectedBook.id) {
                requireActivity().runOnUiThread {
                    hideLoading()
                    recipesList = recipesList.filter { it.id != selectedRecipe.id }

                    val recipeNames = mutableListOf("Select Recipe")
                    recipeNames.addAll(recipesList.map { it.name })

                    val recipesAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        recipeNames
                    )
                    recipesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerRecipes.adapter = recipesAdapter

                    Toast.makeText(requireContext(), "Recipe added to book!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.getAllBooks { books ->
            booksList = books

            val bookTitles = mutableListOf("Select Book")
            bookTitles.addAll(books.map { it.title })

            val booksAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                bookTitles
            )
            booksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerBooks.adapter = booksAdapter
        }

        return view
    }
}