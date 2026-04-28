package com.soloretreat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "precept_logs")
data class PreceptLog(
    @PrimaryKey val date: LocalDate,
    val p1: Boolean? = null,
    val p2: Boolean? = null,
    val p3: Boolean? = null,
    val p4: Boolean? = null,
    val p5: Boolean? = null,
    val p6: Boolean? = null,
    val p7: Boolean? = null,
    val p8: Boolean? = null,
    val eveningReflection: String? = null
) {
    fun observanceList(): List<Boolean?> = listOf(p1, p2, p3, p4, p5, p6, p7, p8)

    fun observanceRate(): Float {
        val list = observanceList()
        val observed = list.count { it == true }
        return observed.toFloat() / list.size.toFloat()
    }
}