package com.addonovan.cse3330.model

import com.addonovan.cse3330.DbEngine
import java.sql.ResultSet

class Emotion : SqlEntity {

    companion object {

        val values = DbEngine.getEmotions()

        operator fun get(id: Int) = values.first { it.id == id }

    }

    var id: Int = 0

    lateinit var name: String

    lateinit var imageUrl: String

    //
    // Functions
    //

    override fun fromRow(row: ResultSet) {
        id = row.getInt("EmotionId")
        name = row.getString("EmotionName")
        imageUrl = row.getString("ImageURL")
    }
}
