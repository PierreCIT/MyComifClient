package com.example.mycomifclient.fragmenttransaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mycomifclient.R

class TransactionAdapter(private val transactions: ArrayList<Transaction>) : RecyclerView.Adapter<TransactionViewHolder>() {

    lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        this.parent = parent
        val row = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_transaction_item, parent, false)
        return TransactionViewHolder(row)
    }

    override fun getItemCount(): Int {
        return this.transactions.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val (date, hour, product, price) = this.transactions[position]
        holder.transactionDate.text = date
        holder.transactionHour.text = hour
        holder.transactionProducts.text = mutableMapOfToString(product)
        holder.transactionPrice.text = "$price €"

        if(price.toDouble() < 0.0) {
            holder.transactionPrice.background = ContextCompat.getDrawable(parent.context, R.drawable.custom_rectangle_negatif_cr10)
        }
        else {
            holder.transactionPrice.background = ContextCompat.getDrawable(parent.context, R.drawable.custom_rectangle_positif_cr10)
        }
    }

    private fun mutableMapOfToString(map: MutableMap<String, Int>): String {
        var productStr = ""
        for ((item, quantity) in map) {
            productStr += if (item == "Recharge") {
                item
            } else {
                "$item x$quantity\n"
            }
        }
        productStr = productStr.removeSuffix("\n")
        return productStr
    }
}