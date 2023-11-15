package cn.bit101.android.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terms")
data class TermEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val term: String,
)

fun String.toTermEntity(): TermEntity {
    return TermEntity(0, this)
}