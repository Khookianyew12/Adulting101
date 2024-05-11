package com.example.adulting101

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class AddVideoActivity : AppCompatActivity() {

    private lateinit var selectedCategory: String
    private lateinit var categorySpinner: Spinner
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var selectVideoButton: Button
    private lateinit var saveButton: Button
    private lateinit var videoView: VideoView
    private var videoUri: Uri? = null

    private lateinit var categoryName: String
    private lateinit var database: DatabaseReference
    private lateinit var categoryList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_video)

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        selectVideoButton = findViewById(R.id.selectVideoButton)
        saveButton = findViewById(R.id.saveButton)
        videoView = findViewById(R.id.videoView)
        categorySpinner = findViewById(R.id.categorySpinner)

        // Get the category name passed
        categoryName = intent.getStringExtra("categoryName") ?: ""
        title = categoryName

        database = FirebaseDatabase.getInstance().reference.child("Course").child(categoryName)

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
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_VIDEO_PICK)
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || videoUri == null) {
                Toast.makeText(this, "Please fill all fields and select a video", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the video data to Firebase
            saveVideoToFirebase(selectedCategory, title, description, videoUri!!)
        }
    }

    private fun retrieveCategories() {
        val categoryListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                categoryList.clear()
                for (categorySnapshot in dataSnapshot.children) {
                    val categoryName = categorySnapshot.key
                    // Exclude "imageURL" from the category list
                    if (categoryName != "imageURL") {
                        categoryName?.let { categoryList.add(it) }
                    }
                }
                if (categoryList.isNotEmpty()) {
                    selectedCategory = categoryList[0]
                }
                categorySpinner.adapter = ArrayAdapter(this@AddVideoActivity, android.R.layout.simple_spinner_item, categoryList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        }

        database.addListenerForSingleValueEvent(categoryListener)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK && data != null) {
            videoUri = data.data
            videoView.setVideoURI(videoUri)
            videoView.start()
        }
    }

    private fun saveVideoToFirebase(category: String, title: String, description: String, videoUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("videos/${videoUri.lastPathSegment}")

        val uploadTask = storageRef.putFile(videoUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val videoUrl = uri.toString()
                val course = Course(title, description, videoUrl)

                val courseRef = database.child(category).push()
                courseRef.setValue(course).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Course saved successfully", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Failed to save course", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(applicationContext, "Failed to upload video: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REQUEST_VIDEO_PICK = 123
    }
}
