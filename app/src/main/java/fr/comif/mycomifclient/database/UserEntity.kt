package fr.comif.mycomifclient.database

import androidx.room.*

/**
 * Implementation a database object representation of a User
 * @param id ID of the user (Int)
 * @param firstName First name of the user (String)
 * @param lastName Last name of the user (String)
 * @param email Email address of the user (String)
 * @param token Token of the user (String)
 * @param balance balance of the account of the user (Int)
 */
@Entity
data class UserEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "first_name")
    val firstName: String,
    @ColumnInfo(name = "last_name")
    val lastName: String,
    val email: String,
    val token: String,
    val balance: Int,
    val dailyExpenses: Int,
    val weeklyExpenses: Int,
    val monthlyExpenses: Int
)

/**
 * Interface for the User DAO
 */
@Dao
interface UserDAO {
    /**
     * Get the first user entity from the database
     * @return None
     */
    @Query("SELECT * FROM UserEntity LIMIT 1")
    fun getFirst(): UserEntity?

    /**
     * Insert (or replace if it already exists) a user entity into the database
     * @param userEntity User entity you want to insert into the database (UserEntity)
     * @return None
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg userEntity: UserEntity)

    /**
     * Nuke the User Table
     * @return None
     */
    @Query("DELETE FROM UserEntity")
    fun nukeUserTable()

    /**
     * Update the token in the user table
     * @param token New token (String)
     * @return None
     */
    @Query("UPDATE UserEntity SET token = :token")
    fun updateToken(token: String)
}