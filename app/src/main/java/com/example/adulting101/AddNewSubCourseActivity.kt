package com.example.adulting101

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddNewSubCourseActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_VIDEO_PICK = 123
    }

    private lateinit var categoryEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var selectVideoButton: Button
    private lateinit var videoView: VideoView
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var videoUri: Uri? = null

    private lateinit var categorySpinner: Spinner
    private lateinit var database: DatabaseReference
    private lateinit var categoryList: MutableList<String>
    private lateinit var selectedCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_sub_course)

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        categoryEditText = findViewById(R.id.category_edit_text)
        titleEditText = findViewById(R.id.title_edit_text)
        descriptionEditText = findViewById(R.id.description_edit_text)
        saveButton = findViewById(R.id.save_button)
        selectVideoButton = findViewById(R.id.select_video_button)
        videoView = findViewById(R.id.video_main)
        categorySpinner = findViewById(R.id.category_spinner)

        database = FirebaseDatabase.getInstance().reference.child("Course")

        categoryList = mutableListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCategory = categoryList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        retrieveCategories()

        selectVideoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_VIDEO_PICK
                )
            } else {
                pickVideoFromGallery()
            }
        }

        saveButton.setOnClickListener {
            val category = categoryEditText.text.toString().trim()
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString()

            if (category.isEmpty() || title.isEmpty() || description.isEmpty() || videoUri == null) {
                if (videoUri == null) {
                    Toast.makeText(this, "Please select a video", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            // Upload the video to Firebase Storage
            uploadVideoToStorage(selectedCategory,category, title, description)
        }
    }

    private fun retrieveCategories() {
        val categoryListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                categoryList.clear()
                for (categorySnapshot in dataSnapshot.children) {
                    val categoryName = categorySnapshot.key
                    categoryName?.let { categoryList.add(it) }
                }
                if (categoryList.isNotEmpty()) {
                    selectedCategory = categoryList[0]
                }
                categorySpinner.adapter = ArrayAdapter(this@AddNewSubCourseActivity, android.R.layout.simple_spinner_item, categoryList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        }

        database.addListenerForSingleValueEvent(categoryListener)
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_VIDEO_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK && data != null) {
            videoUri = data.data
            videoView.setVideoURI(videoUri)
            videoView.start()
        }
    }

    private fun uploadVideoToStorage(selectedCategory: String,subcategory: String, title: String, description: String) {
        val storageRef = storage.reference
        val videoRef = storageRef.child("videos/${System.currentTimeMillis()}_${videoUri?.lastPathSegment}")

        val uploadTask = videoRef.putFile(videoUri!!)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get the download URL of the uploaded video
                videoRef.downloadUrl.addOnSuccessListener { uri ->
                    val videoUrl = uri.toString()

                    // Save the course data in Firestore with the video URL
                    saveCourseToFirestore(selectedCategory,subcategory, title, description, videoUrl)
                }.addOnFailureListener { exception ->
                    // Handle failure to get download URL
                    Toast.makeText(applicationContext, "Failed to upload video: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle failure to upload video
                Toast.makeText(applicationContext, "Failed to upload video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCourseToFirestore(selectedCategory: String,subcategory: String, title: String, description: String, videoUrl: String) {
        val course = Course(title, description, videoUrl)
        val database = FirebaseDatabase.getInstance().reference.child("Course").child(selectedCategory).child(subcategory)


        val newCourseRef = database.push()
        newCourseRef.setValue(course).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    applicationContext,
                    "Course saved successfully",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Failed to save course",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
