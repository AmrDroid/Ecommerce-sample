

package com.amrmustafa.ecommerce.db

import androidx.paging.DataSource
import androidx.room.*
import com.amrmustafa.ecommerce.model.Products

/**
 * Room data access object for accessing the [Products] table.
 */
@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<Products>)

    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProduct(): DataSource.Factory<Int, Products>

    @Query("SELECT * FROM products WHERE name LIKE :name ORDER BY  name ASC")
    fun getAllProductForFilter(name: String): DataSource.Factory<Int,Products>




}
