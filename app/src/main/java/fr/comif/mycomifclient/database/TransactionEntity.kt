package fr.comif.mycomifclient.database

import androidx.room.*

/**
 * Implementation of a database object representation of a transaction
 * @param transactionId ID of the transaction (Integer)
 * @param type Type of the transaction (String)
 * @param date Date of the transaction (String)
 */
@Entity
data class TransactionEntity(
    @PrimaryKey
    val transactionId: Int,
    val type: String,
    val date: String
)

/**
 * Interface for the Transaction DAO
 */
@Dao
interface TransactionDAO {
    /**
     * Get all the transaction from the database
     * @return List of all transactions (List<TransactionEntity>)
     */
    @Query("SELECT * FROM TransactionEntity")
    fun getAll(): List<TransactionEntity>

    /**
     * Insert a transaction into the database (or replace it it already exists)
     * @param transactionEntity Transaction you want to insert into the database (TransactionEntity)
     * @return None
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg transactionEntity: TransactionEntity)

    /**
     * Delete a transaction entity from the database
     * @param transactionEntity Transaction you want to remove from the table
     * @return None
     */
    @Delete
    fun delete(transactionEntity: TransactionEntity)

    /**
     * Nuke the transaction table from the database
     * @return None
     */
    @Query("DELETE FROM TransactionEntity")
    fun nukeTransactionTable()
}