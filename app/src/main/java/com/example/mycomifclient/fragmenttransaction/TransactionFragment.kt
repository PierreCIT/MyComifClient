package com.example.mycomifclient.fragmenttransaction

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycomifclient.R

/**
 * Implementation of the Transaction Fragment
 */
class TransactionFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var transactionList: ArrayList<Transaction> = ArrayList()
    private val adapter = TransactionAdapter(transactionList)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.f_transaction_recycler_view)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * Set the transaction list for the adapter
     * @param transactionList List of all the transactions to display (ArrayList<Transaction>)
     * @return None
     */
    fun setTransactionList(transactionList: ArrayList<Transaction>) {
        this.transactionList.clear()
        this.transactionList.addAll(transactionList)
        adapter.notifyDataSetChanged()
    }

    /**
     * Interface for the fragment interaction listener
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
