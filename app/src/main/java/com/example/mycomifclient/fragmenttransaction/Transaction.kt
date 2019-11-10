package com.example.mycomifclient.fragmenttransaction

import java.io.Serializable

data class Transaction(
    val date: String,
    val hour: String,
    val product: MutableMap<String, Int> /* (Product, quantities) --> (Oreo, 3) */,
    val price: String
) : Serializable