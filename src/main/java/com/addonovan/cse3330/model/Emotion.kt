package com.addonovan.cse3330.model

import com.addonovan.cse3330.DbEngine
import java.sql.ResultSet

class Emotion : SqlEntity {

    companion object {

        val LIKE: Emotion = DbEngine.getEmotionByName("Like")
        val ANGER: Emotion = DbEngine.getEmotionByName("Anger")
        val DISLIKE: Emotion = DbEngine.getEmotionByName("Dislike")
        val LOVE: Emotion = DbEngine.getEmotionByName("Love")

        val VALUES = listOf(LIKE, ANGER, DISLIKE, LOVE)

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
