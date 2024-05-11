package com.example.adulting101

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

// MainActivity
class CoursesListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var addCourseButton: Button
    private lateinit var addSubCourseButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        addCourseButton = findViewById(R.id.addCourseButton)
        addSubCourseButton = findViewById(R.id.addSubCourseButton)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("Course")

        // Retrieve course categories from Firebase
        val categoriesList = mutableListOf<CourseCategory>()
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriesList.clear()

                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.key
                    if (categoryName != null) {
                        categoriesList.add(CourseCategory(categoryName))
                    }
                }
                val adapter = CourseCategoryAdapter(categoriesList) { category ->
                    // Handle category item click
                    val intent = Intent(this@CoursesListActivity, SubCategoryActivity::class.java)
                    intent.putExtra("categoryName", category.name)
                    startActivity(intent)
                }
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read value.", error.toException())
            }
        })


        // Set click listener for the button
        addCourseButton.setOnClickListener {
            startActivity(Intent(this@CoursesListActivity, AddCourseActivity::class.java))
        }
        addSubCourseButton.setOnClickListener {
            startActivity(Intent(this@CoursesListActivity, AddNewSubCourseActivity::class.java))
        }
    }
}

