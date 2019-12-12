package com.example.mycomifclient.fragmenttransaction

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mycomifclient.R

class TransactionViewHolder(rootView: View) :
    RecyclerView.ViewHolder(rootView) {
    var transactionDate: TextView =
        rootView.findViewById(R.id.recycler_view_transaction_text_view_date)
    var transactionHour: TextView =
        rootView.findViewById(R.id.recycler_view_transaction_text_view_hour)
    var transactionProducts: TextView =
        rootView.findViewById(R.id.recycler_view_transaction_text_view_products_quantities)
    var transactionPrice: TextView =
        rootView.findViewById(R.id.recycler_view_transaction_text_view_price)
}