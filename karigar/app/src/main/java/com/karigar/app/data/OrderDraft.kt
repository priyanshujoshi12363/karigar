package com.karigar.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

object OrderDraft {
    var categoryValue by mutableStateOf<String?>(null)
    var latitude by mutableStateOf(21.1458)
    var longitude by mutableStateOf(79.0882)
    var address by mutableStateOf("")
    var durationMinutes by mutableStateOf(60)

    const val RATE_PER_HOUR = 150
    const val PLATFORM_FEE = 2

    val workAmount: Int get() = ((RATE_PER_HOUR * durationMinutes) / 60.0).roundToInt()
    val total: Int get() = workAmount + PLATFORM_FEE

    fun reset() {
        categoryValue = null
        durationMinutes = 60
        address = ""
    }
}
