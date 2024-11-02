package com.github.hadywalied.zakker.domain

interface IAzkarRepository {
    fun getAzkar(): List<Azkar>
}