package com.example.mycomifclient.fragmenttransaction

import java.io.Serializable

data class Transaction(
    val date: String,
    val hour: String,
    val product: MutableMap<String, Int>,
    val price: String
) : Serializable