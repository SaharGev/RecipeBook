package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recipebook.R
import com.example.recipebook.db.BookEntity
import com.example.recipebook.repository.BookRepository
import kotlinx.coroutines.launch

class AddRecipeBookFragment : Fragment() {

    private lateinit var repository: BookRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_add_recipe_book, container, false)

        repository = BookRepository(requireContext())

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

        val privacyOptions = listOf("Select Privacy", "Private","Public")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            privacyOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrivacy.adapter = adapter

        spinnerPrivacy.setSelection(0)

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

            lifecycleScope.launch {
                repository.insertBook(
                    BookEntity(
                        title = bookName,
                        description = bookDescription,
                        isPublic = isPublic
                    )
                )

                Toast.makeText(requireContext(), "Book created successfully", Toast.LENGTH_SHORT).show()
                
                etBookName.text.clear()
                etBookDescription.text.clear()
                spinnerPrivacy.setSelection(0)
            }
        }

        return view
    }
}