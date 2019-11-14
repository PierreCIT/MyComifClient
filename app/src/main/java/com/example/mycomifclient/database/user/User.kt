package com.example.mycomifclient.database.user

import androidx.room.*

@Entity
data class User(
    @PrimaryKey
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val token: String,
    val balance: Int,
    val promotion: String
)

@Dao
interface UserDAO {

    @Query("SELECT * FROM User")
    fun getAll(): User

    @Insert
    fun insert(vararg user: User)

    @Delete
    fun delete(user: User)
}