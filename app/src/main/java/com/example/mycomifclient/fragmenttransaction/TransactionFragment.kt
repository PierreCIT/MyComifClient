package com.example.mycomifclient.fragmenttransaction

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycomifclient.MainActivity
import com.example.mycomifclient.R
import com.example.mycomifclient.database.*
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Implementation of the Transaction Fragment
 */
class TransactionFragment(
    private val userDAO: UserDAO,
    private val transactionDAO: TransactionDAO,
    private val itemDAO: ItemDAO,
    private val retrofitHTTPServices: HTTPServices
) : Fragment() {

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
     * Interface for the fragment interaction listener
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    fun getTransactions() {
        val user = userDAO.getFirst()!!
        retrofitHTTPServices.getTransactions(
            "Bearer " + user.token
        )
            .enqueue(object : Callback<JsonArray> {
                override fun onResponse(
                    call: Call<JsonArray>,
                    response: Response<JsonArray>
                ) {
                    when (response.raw().code()) {

                        200 -> handleGetTransactionsResponse(response.body())

                        401 -> (activity as MainActivity).logoutFromApi()

                        else -> println("Error")
                    }
                }

                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                    Toast.makeText(activity, "Error: $t", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun handleGetTransactionsResponse(body: JsonArray?) {
        body?.forEach { bodyElement ->
            val transaction = bodyElement.asJsonObject
            val items = transaction.get("products").asJsonArray
            transactionDAO.insert(
                TransactionEntity(
                    transaction.get("id").asInt,
                    removeQuotes(transaction.get("type")),
                    removeQuotes(transaction.get("created_at"))
                )
            )
            if (removeQuotes(transaction.get("type")) == "credit") {
                itemDAO.insert(
                    ItemEntity(
                        transaction.get("id").asInt,
                        resources.getString(R.string.refill),
                        1,
                        transaction.get("value").asInt * -1
                    )
                )
            } else {
                items.forEach { itemsElement ->
                    val item = itemsElement.asJsonObject
                    itemDAO.insert(
                        ItemEntity(
                            transaction.get("id").asInt,
                            removeQuotes(item.get("name")),
                            item.get("pivot").asJsonObject.get("quantity").asInt,
                            item.get("pivot").asJsonObject.get("unit_price").asInt
                        )
                    )
                }
            }
        }
        createTransactionsList()
    }

    private fun createTransactionsList() {
        val transactions = transactionDAO.getAll()

        transactions.forEach { transaction ->

            val itemsMap: MutableMap<String, Int> = mutableMapOf()
            val items = itemDAO.selectItems(transaction.transactionId)
            var totalTransactionPrice = 0f

            val date = transaction.date.split(' ')[0]
            val hour = transaction.date.split(' ')[1]

            items.forEach { item ->
                itemsMap[item.itemName] = item.quantity
                totalTransactionPrice -= item.price * item.quantity / 100f
            }

            transactionList.add(
                Transaction(
                    date,
                    hour,
                    itemsMap,
                    totalTransactionPrice.toString()
                )
            )
        }
        transactionList.reverse()
        adapter.notifyDataSetChanged()
        toggleViewStatus(View.INVISIBLE)
    }

    private fun removeQuotes(item: JsonElement): String {
        return item.toString().substring(1, item.toString().length - 1)
    }

    /**
     * Display or Hide the progress bar and fragment views according to the current status
     * @param status Connexion status
     * @return None
     */
    fun toggleViewStatus(status: Int) {
        view?.findViewById<ConstraintLayout>(R.id.constraint_layout_progress_bar)?.visibility =
            status
        if (status == View.INVISIBLE) {
            view?.findViewById<RecyclerView>(R.id.f_transaction_recycler_view)?.visibility =
                View.VISIBLE
        } else {
            view?.findViewById<RecyclerView>(R.id.f_transaction_recycler_view)?.visibility =
                View.INVISIBLE
        }
    }
}
