package com.example.mycomifclient.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, TransactionEntity::class, ItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ComifDatabase : RoomDatabase() {

    abstract fun getUserDAO(): UserDAO
    abstract fun getItemDAO(): ItemDAO
    abstract fun getTransactionDAO(): TransactionDAO

    companion object {
        private var INSTANCE: ComifDatabase? = null

        fun getAppDatabase(context: Context): ComifDatabase {
            if (INSTANCE == null) {
                synchronized(ComifDatabase::class) {
                    INSTANCE = Room
                        .databaseBuilder(
                            context.applicationContext,
                            ComifDatabase::class.java,
                            "Database"
                        )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE as ComifDatabase
        }
    }
}
