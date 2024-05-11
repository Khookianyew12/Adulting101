package com.example.adulting101

import java.io.Serializable

data class Content(
    val id: String, // Add id property
    val title: String,
    val videoUrl: String,
    val description: String
) : Serializable