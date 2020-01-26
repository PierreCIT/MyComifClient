package com.example.mycomifclient.database

import androidx.room.*

/**
 * Implement an object representation of a transaction item
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = TransactionEntity::class,
        parentColumns = arrayOf("transactionId"),
        childColumns = arrayOf("transactionId"),
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["transactionId", "itemName"]
)
data class ItemEntity(
    val transactionId: Int,
    val itemName: String,
    val quantity: Int,
    val price: Int
)

@Dao
interface ItemDAO {

    /**
     * Get all the items of a given transaction
     * @param transactionId ID of the transaction (integer)
     * @return List of all the item of this transaction (List<ItemEntity>)
     */
    @Query("SELECT * FROM ItemEntity WHERE transactionId = :transactionId")
    fun selectItems(transactionId: Int): List<ItemEntity>

    /**
     * Insert a transaction item into the database (or replace it if it already exists in the table)
     * @param itemEntity Item you want to insert into the database (ItemEntity)
     * @return None
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg itemEntity: ItemEntity)

    /**
     * Delete a transaction item from the database
     * @param itemEntity Item you want to remove from the database (ItemEntity)
     * @return None
     */
    @Delete
    fun delete(itemEntity: ItemEntity)

    /**
     * Nuke the database table
     * @return None
     */
    @Query("DELETE FROM ItemEntity")
    fun nukeItemTable()

}