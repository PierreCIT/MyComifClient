package com.example.mycomifclient.database

import androidx.room.*

@Entity
data class UserEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "first_name")
    val firstName: String,
    @ColumnInfo(name = "last_name")
    val lastName: String,
    val email: String,
    val password: String,
    val token: String,
    val balance: Int
)

@Dao
interface UserDAO {

    @Query("SELECT * FROM UserEntity")
    fun getAll(): UserEntity

    @Query("SELECT * FROM UserEntity LIMIT 1")
    fun getFirst(): UserEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg userEntity: UserEntity)

    @Query("DELETE FROM UserEntity")
    fun nukeTable()

    @Query("UPDATE UserEntity SET token = :token")
    fun updateToken(token: String)
}