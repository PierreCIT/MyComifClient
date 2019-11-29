package com.example.mycomifclient

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.*

class HomeFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var dayConsos: String
    private lateinit var weekConsos: String
    private lateinit var monthConsos: String

    private var balance = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    fun updateViews(
        firstName: String,
        lastName: String,
        balance: Float,
        dayConsos: String,
        weekConsos: String,
        monthConsos: String
    ) {

        this.firstName = firstName
        this.lastName = lastName
        this.balance = balance
        this.dayConsos = dayConsos
        this.weekConsos = weekConsos
        this.monthConsos = monthConsos

        val nameView = view?.findViewById<TextView>(R.id.f_home_text_view_user_name)
        val balanceView = view?.findViewById<TextView>(R.id.f_home_text_view_balance)
        val dayConsosView = view?.findViewById<TextView>(R.id.f_home_text_view_today_total)
        val weekConsosView = view?.findViewById<TextView>(R.id.f_home_text_view_this_week_total)
        val monthConsosView = view?.findViewById<TextView>(R.id.f_home_text_view_this_month_total)

        nameView?.text = String.format(
            resources.getString(
                R.string.first_last_name
            ), firstName, lastName.toUpperCase(
                Locale.FRANCE
            )
        )

        if (balance < 0) {
            balanceView?.text = String.format(
                resources.getString(R.string.euro_price), "", balance.toString()
            )
            balanceView?.background =
                (resources.getDrawable(R.drawable.custom_rectangle_negatif_cr10))
        } else {
            balanceView?.text = String.format(
                resources.getString(R.string.euro_price), "+", balance.toString()
            )
            balanceView?.background =
                (resources.getDrawable(R.drawable.custom_rectangle_positive_cr10))
        }
        dayConsosView?.text =
            String.format(resources.getString(R.string.euro_price), "", dayConsos)
        weekConsosView?.text =
            String.format(resources.getString(R.string.euro_price), "", weekConsos)
        monthConsosView?.text =
            String.format(resources.getString(R.string.euro_price), "", monthConsos)
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

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
