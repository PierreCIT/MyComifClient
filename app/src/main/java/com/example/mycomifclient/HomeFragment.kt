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
    lateinit var firstName: String
    lateinit var lastName: String
    var balance: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val balanceView = view.findViewById<TextView>(R.id.f_home_text_view_solde)
        val nameView = view.findViewById<TextView>(R.id.f_home_text_view_user_name)
        nameView.text = String.format(
            resources.getString(R.string.first_last_name), firstName, lastName.toUpperCase(
                Locale.FRANCE
            )
        )

        if (balance < 0) {
            balanceView.text = String.format(
                resources.getString(R.string.balance), "-", balance.toString()
            )
            balanceView.background =
                (resources.getDrawable(R.drawable.custom_rectangle_negatif_cr10))
        } else {
            balanceView.text = String.format(
                resources.getString(R.string.balance), "+", balance.toString()
            )
            balanceView.background =
                (resources.getDrawable(R.drawable.custom_rectangle_positif_cr10))
        }
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
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
