package com.github.hadywalied.zakker.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonCreator

data class Zekr @JsonCreator constructor(
    @JsonProperty("zekr") val zekr: String?="",
    @JsonProperty("description") val description: String?="",
    @JsonProperty("count") val count: Int?=1,
    @JsonProperty("reference") val reference: String?="",
    @JsonProperty("search") val search: String?=""
)

data class Category(
    val name: String,
    val search: String
)

data class Azkar(
    val category: Category,
    val zikr: List<Zekr>
)