package com.addonovan.cse3330.model

import java.sql.ResultSet

/**
 * The class model for the SQL `Page` entity, which is a specialization of the
 * [Account].
 */
class Page : Account() {

    /** The (non-unique) name of this page. */
    lateinit var name: String

    /** The description of this page. */
    lateinit var description: String

    /** The number of times this page has been viewed. */
    var viewCount: Int = 0

    override fun fromRow(row: ResultSet) {
        super.fromRow(row)
        name = row.getString("Name")
        description = row.getString("Description")
        viewCount = row.getInt("ViewCount")
    }

}
