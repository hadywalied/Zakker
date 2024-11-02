package com.github.hadywalied.zakker.application

import com.github.hadywalied.zakker.domain.Azkar
import com.github.hadywalied.zakker.domain.IAzkarRepository

class AzkarService(val azkarRepository: IAzkarRepository) {

    fun getAzkar(): List<Azkar> {
        return azkarRepository.getAzkar()
    }

}