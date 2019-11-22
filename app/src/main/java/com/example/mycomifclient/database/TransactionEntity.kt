package com.example.mycomifclient.database

import androidx.room.*

@Entity
data class TransactionEntity(
    @PrimaryKey
    val transactionId: Int,
    val type: String,
    val date: String
)

@Dao
interface TransactionDAO {

    @Query("SELECT * FROM TransactionEntity")
    fun getAll(): TransactionEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg transactionEntity: TransactionEntity)

    @Delete
    fun delete(transactionEntity: TransactionEntity)

}