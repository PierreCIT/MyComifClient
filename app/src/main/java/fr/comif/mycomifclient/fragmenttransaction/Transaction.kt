package fr.comif.mycomifclient.fragmenttransaction

import java.io.Serializable

/**
 * Implementation of a transaction object (Serializable)
 * @param date Date of the transaction (String)
 * @param hour Hour of the transaction (String)
 * @param product List of products bought during the transaction (MutableMap<String, Int>)
 * @param price Total price of the transaction (String)
 */
data class Transaction(
    val date: String,
    val hour: String,
    val product: MutableMap<String, Int>,
    val price: String
) : Serializable