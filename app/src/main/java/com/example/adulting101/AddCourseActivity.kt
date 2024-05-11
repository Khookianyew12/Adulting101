package com.example.adulting101

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddCourseActivity : AppCompatActivity() {

    private lateinit var courseNameEditText: EditText
    private lateinit var selectedImageUri: Uri
    private lateinit var imageViewSelectedImage: ImageView
    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        courseNameEditText = findViewById(R.id.edit_text_course_name)
        imageViewSelectedImage = findViewById(R.id.image_view_selected_image)
        val selectImageButton: Button = findViewById(R.id.button_select_image)
        val addButton: Button = findViewById(R.id.button_add_course)

        selectImageButton.setOnClickListener {
            // Launch the image picker
            getContent.launch("image/*")
        }

        addButton.setOnClickListener {
            val courseName = courseNameEditText.text.toString().trim()

            if (courseName.isNotEmpty()) {
                addCourseWithImage(courseName, selectedImageUri)
                addNotification(courseName)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please enter a course name",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Initialize the launcher for the image picker
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                imageViewSelectedImage.setImageURI(selectedImageUri)
                imageViewSelectedImage.visibility = ImageView.VISIBLE
            }
        }
    }

    private fun addNotification(courseName: String) {
        // Generate a unique course ID
        val courseId = generateCourseId()

        // Generate a random message
        val randomMessage = generateRandomMessage(courseName)

        // Get a reference to the "Notification" node
        val database = FirebaseDatabase.getInstance().reference.child("Notification")

        // Push a new child node under "Notification" with the generated course ID
        val notificationRef = database.child(courseId)

        // Store the course ID and message
        val notificationData = mapOf(
            "courseid" to courseName,
            "message" to randomMessage
        )

        // Set the course ID and message under the generated course ID node
        notificationRef.setValue(notificationData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Notification added successfully",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Failed to add notification",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun generateCourseId(): String {
        // Generate a unique ID using UUID
        return UUID.randomUUID().toString()
    }


    private fun generateRandomMessage(courseName: String): String {
        val messages = listOf(
            "Exciting news! We've just added a brand new $courseName course to our lineup. Come explore now!",
            "Hey there, hungry learners! We launched our new $courseName course and it is free to learn. Come find out now!",
            "New $courseName course is currently added. Come try it out now."
        )
        return messages.random()
    }


    private fun addCourseWithImage(courseName: String, imageUri: Uri?) {
        val database = FirebaseDatabase.getInstance().reference.child("Course")

        // Upload image to Firebase Storage
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("Photos").child("$courseName.jpg")
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Store course name and image URL under "Course" node
                    val courseData = mapOf("imageURL" to uri.toString())
                    database.child(courseName).setValue(courseData)
                        .addOnCompleteListener { task ->
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
            }.addOnFailureListener { exception ->
                // Handle errors
                Toast.makeText(
                    applicationContext,
                    "Failed to upload image: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // If no image is selected, show a message and return
            Toast.makeText(
                applicationContext,
                "Please select an image",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}


