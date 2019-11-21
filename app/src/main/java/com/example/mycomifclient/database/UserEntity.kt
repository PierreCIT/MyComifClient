package com.example.mycomifclient.database

import androidx.room.*

@Entity
data class UserEntity(
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

    @Query("SELECT * FROM UserEntity")
    fun getAll(): UserEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg userEntity: UserEntity)

    @Delete
    fun delete(userEntity: UserEntity)
}