package com.example.adulting101

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditContentActivity : AppCompatActivity() {

    private lateinit var content: Content
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var selectVideoButton: Button
    private lateinit var saveButton: Button
    private lateinit var videoView: VideoView
    private var newVideoUri: Uri? = null
    private lateinit var categoryName: String // Declare categoryName
    private lateinit var subcategoryName: String // Declare subcategoryName

    companion object {
        private const val REQUEST_VIDEO_PICK = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_content)

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        selectVideoButton = findViewById(R.id.selectVideoButton)
        saveButton = findViewById(R.id.saveButton)
        videoView = findViewById(R.id.videoView)

        // Retrieve content details and category names from intent
        content = intent.getSerializableExtra("content") as Content
        categoryName = intent.getStringExtra("categoryName") ?: ""
        subcategoryName = intent.getStringExtra("subcategoryName") ?: ""

        // Populate EditText fields with existing content details
        titleEditText.setText(content.title)
        descriptionEditText.setText(content.description)

        // Display existing video
        videoView.setVideoURI(Uri.parse(content.videoUrl))
        videoView.start()

        selectVideoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_VIDEO_PICK)
        }

        saveButton.setOnClickListener {
            val updatedTitle = titleEditText.text.toString()
            val updatedDescription = descriptionEditText.text.toString()

            // Check if a new video was selected
            if (newVideoUri != null) {
                // Upload the new video to Firebase Storage
                uploadVideoToStorage(newVideoUri!!, updatedTitle, updatedDescription)
            } else {
                // If no new video was selected, update content details in Firebase
                updateContent(updatedTitle, updatedDescription, content.videoUrl)
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK && data != null) {
            newVideoUri = data.data
            // Display the selected video in VideoView
            videoView.setVideoURI(newVideoUri)
            videoView.start()
        }
    }

    private fun uploadVideoToStorage(videoUri: Uri, updatedTitle: String, updatedDescription: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("videos/${videoUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(videoUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val videoUrl = uri.toString()
                // Update content details in Firebase with the new video URL
                updateContent(updatedTitle, updatedDescription, videoUrl)
            }
        }.addOnFailureListener { exception ->
            // Handle failure
            Toast.makeText(this, "Failed to upload video: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateContent(updatedTitle: String, updatedDescription: String, newVideoUrl: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val contentRef = databaseRef
            .child("Course")
            .child(categoryName)
            .child(subcategoryName)
            .child(content.id)

        // Update content details
        contentRef.child("title").setValue(updatedTitle)
        contentRef.child("description").setValue(updatedDescription)
        contentRef.child("videoUrl").setValue(newVideoUrl)

        // Handle success
        // Display success message or navigate back to the previous screen
        Toast.makeText(this, "Content updated successfully", Toast.LENGTH_SHORT).show()
    }

}

