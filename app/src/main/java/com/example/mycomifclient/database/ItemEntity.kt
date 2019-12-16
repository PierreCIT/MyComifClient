package com.example.mycomifclient.database

import androidx.room.*

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

    @Query("SELECT * FROM ItemEntity WHERE transactionId = :transactionId")
    fun selectItems(transactionId: Int): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg itemEntity: ItemEntity)

    @Delete
    fun delete(itemEntity: ItemEntity)

    @Query("DELETE FROM ItemEntity")
    fun nukeItemTable()

}