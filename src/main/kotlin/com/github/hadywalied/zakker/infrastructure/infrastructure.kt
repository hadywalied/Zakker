package com.github.hadywalied.zakker.infrastructure

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.hadywalied.zakker.domain.Azkar
import com.github.hadywalied.zakker.domain.Category
import com.github.hadywalied.zakker.domain.IAzkarRepository
import com.github.hadywalied.zakker.domain.Zekr

class SqlAzkarRepository(
    private val databaseConfig: DatabaseConfig
) : IAzkarRepository {

    override fun getAzkar(): List<Azkar> {
        return databaseConfig.getConnection().use { connection ->
            val mapper = jacksonObjectMapper().apply {
                // Configure Jackson for more lenient parsing if needed
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(
                """
                    WITH RowsAsJson AS (
                        SELECT json_object(
                                       'zekr', zekr,
                                       'description', COALESCE(azkar.description, ''),
                                       'count', coalesce(azkar.count, 1),
                                       'reference', coalesce(azkar.reference, ''),
                                       'search', coalesce(azkar.search, '')
                               ) as row_data,
                               azkar.category,
                               c.search as category_search
                        FROM azkar
                        LEFT JOIN main.category c on c.category = azkar.category
                    )
                    SELECT
                        category,
                        category_search,
                         '[' || GROUP_CONCAT(row_data, ',') || ']' as rows
                    FROM RowsAsJson
                    GROUP BY category;
                """.trimIndent()
            )

            buildList {
                while (resultSet.next()) {
                    val categoryName = resultSet.getString("category")
                    val categorySearch = resultSet.getString("category_search")
                    val category = Category(categoryName, categorySearch)
                    val jsonArray = resultSet.getString("rows")
                    add(
                        Azkar(category = category, zikr = mapper.readValue<List<Zekr>>(jsonArray))
                    )
                }
            }
        }
    }

}