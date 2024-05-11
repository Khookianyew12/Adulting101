package com.example.adulting101

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// SubCategoryActivity
class SubCategoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryName: String
    private lateinit var database: DatabaseReference
    private lateinit var addVideoButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_category)

        addVideoButton = findViewById(R.id.addVideoButton)
        // Get the category name passed
        categoryName = intent.getStringExtra("categoryName") ?: ""
        title = categoryName

        recyclerView = findViewById(R.id.subCategoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("Course").child(categoryName)

        // Retrieve subcategories for the selected category from Firebase
        val subcategoriesList = mutableListOf<SubCategory>()
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                subcategoriesList.clear()
                for (subcategorySnapshot in snapshot.children) {
                    if (subcategorySnapshot.key == "imageURL") {
                        // Skip if it's the imageURL node
                        continue
                    }
                    val subcategoryName = subcategorySnapshot.key
                    if (subcategoryName != null) {
                        // Add subcategory to the list
                        subcategoriesList.add(SubCategory(subcategoryName))
                    }
                }
                val adapter = SubCategoryAdapter(subcategoriesList) { subcategory ->
                    // Handle subcategory item click
                    // You can navigate to the content activity or fragment here
                    val intent = Intent(this@SubCategoryActivity, SubCategoryListActivity::class.java)
                    intent.putExtra("categoryName", categoryName)
                    intent.putExtra("subcategoryName", subcategory.name)
                    startActivity(intent)
                }
                recyclerView.adapter = adapter
            }




            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read value.", error.toException())
            }
        })

        // Set click listener for the button
        addVideoButton.setOnClickListener {
            val intent = Intent(this@SubCategoryActivity, AddVideoActivity::class.java)
            intent.putExtra("categoryName", categoryName)
            startActivity(intent)
        }
    }
}

