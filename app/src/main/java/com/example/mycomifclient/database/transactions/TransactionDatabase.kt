package com.example.mycomifclient.database.transactions

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class], version = 1)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun getTransactionDAO(): TransactionDAO

    companion object {
        private var INSTANCE: TransactionDatabase? = null

        fun getAppDatabase(context: Context): TransactionDatabase {
            if (INSTANCE == null) {
                synchronized(TransactionDatabase::class) {
                    INSTANCE = Room
                        .databaseBuilder(
                            context.applicationContext,
                            TransactionDatabase::class.java,
                            "UserDB"
                        )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE as TransactionDatabase
        }
    }
}
