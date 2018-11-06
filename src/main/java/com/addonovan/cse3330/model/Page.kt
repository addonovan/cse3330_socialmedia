package com.addonovan.cse3330.model

import java.sql.ResultSet

class Page : Account() {

    lateinit var name: String

    lateinit var description: String

    var viewCount: Int = 0

    override fun fromRow(row: ResultSet) {
        super.fromRow(row)
        name = row.getString("Name")
        description = row.getString("Description")
        viewCount = row.getInt("ViewCount")
    }

}
