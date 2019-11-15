package com.example.mycomifclient.database.user

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDAO

    companion object {
        private var INSTANCE: UserDatabase? = null

        fun getAppDatabase(context: Context): UserDatabase {
            if (INSTANCE == null) {
                synchronized(UserDatabase::class) {
                    INSTANCE = Room
                        .databaseBuilder(
                            context.applicationContext,
                            UserDatabase::class.java,
                            "UserDB"
                        )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE as UserDatabase
        }
    }
}
