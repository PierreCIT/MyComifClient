package com.example.mycomifclient.fragmenttransaction

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mycomifclient.R

class TransactionAdapter(private val transactions: ArrayList<Transaction>) :
    RecyclerView.Adapter<TransactionViewHolder>() {

    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        this.parent = parent
        val row = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_transaction_item, parent, false)
        return TransactionViewHolder(row)
    }

    override fun getItemCount(): Int {
        return this.transactions.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val (date, hour, product, price) = this.transactions[position]

        val castedPriceToDouble = price.toDouble()

        if (castedPriceToDouble < 0.0) {
            holder.transactionPrice.background =
                ContextCompat.getDrawable(parent.context, R.drawable.custom_rectangle_negatif_cr10)
        } else {
            holder.transactionPrice.background =
                ContextCompat.getDrawable(parent.context, R.drawable.custom_rectangle_positive_cr10)
        }

        holder.transactionDate.text = date
        holder.transactionHour.text = hour
        holder.transactionProducts.text = mutableMapOfToString(product)
        holder.transactionPrice.text = "%.2f".format(castedPriceToDouble) + " €"
    }

    private fun mutableMapOfToString(map: MutableMap<String, Int>): String {
        var productStr = ""
        for ((item, quantity) in map) {
            productStr += if (item == parent.context.resources.getString(R.string.refill)) {
                item
            } else {
                "$item x$quantity\n"
            }
        }
        productStr = productStr.removeSuffix("\n")
        return productStr
    }
}