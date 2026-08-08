package com.newsme.app.data.model

data class News(
    val id: Int,
    val title: String,
    val details: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val link: String? = null,
    val author: String = "مراسل نيوز مي",
    val time: String = "الان"
)
