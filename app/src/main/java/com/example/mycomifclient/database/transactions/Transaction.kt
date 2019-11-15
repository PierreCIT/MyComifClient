package com.example.mycomifclient.database.transactions

import androidx.room.*

@Entity
data class Transaction(
    val id: String,
    val type: TransactionType,
    val date: String,
    val products: List<Products>
)

data class Products(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Int
)

enum class TransactionType {
    debit,
    credit
}

@Dao
interface TransactionDAO {

    @Query("SELECT * FROM `Transaction`")
    fun getAll(): Transaction

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)
}