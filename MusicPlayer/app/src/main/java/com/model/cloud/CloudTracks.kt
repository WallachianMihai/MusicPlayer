package com.model.cloud

data class CloudTracks(
    val `data`: List<Data>,
    val next: String,
    val total: Int
)