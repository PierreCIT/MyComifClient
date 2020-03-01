package com.example.mycomifclient

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.mycomifclient.database.UserDAO
import com.example.mycomifclient.database.UserEntity
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Implementation of a fragment with main info (Home fragment)
 */
class HomeFragment(private var userDAO: UserDAO) : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var dayConsos: String
    private lateinit var weekConsos: String
    private lateinit var monthConsos: String

    private lateinit var user: UserEntity

    //TODO: use basic okHttpClient when the API will be put in production
    private val retrofitHTTPServices = HTTPServices.create(isSafeConnexion = false)

    private var balance = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        user = userDAO.getFirst()
        getUser()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * Updates all views of the fragment according to class data retrieved from API
     * @return None
     */
    private fun updateViews(
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

    /**
     * Interface for fragment interaction listener
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    /**
     * Get the user from API
     * @return None
     * @see MainActivity.reconnect
     * @see handleGetUserResponse
     */
    fun getUser() {
        val token = user.token
        retrofitHTTPServices.getUser("Bearer $token")
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    when (response.raw().code()) {

                        200 -> handleGetUserResponse(response.body(), token)

                        401 -> (activity as MainActivity).reconnect()

                        else -> println("Error")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(activity, "Error: $t", Toast.LENGTH_LONG).show()
                }
            })
    }

    /**
     * Handle response to the request for getting a specific user and close activity
     * @param body response body (JsonObject?)
     * @param token user token (String)
     * @return None
     */
    private fun handleGetUserResponse(body: JsonObject?, token: String) {
        if (body != null) {
            val userReturned = body.getAsJsonObject("user")
            val userEntity = UserEntity(
                userReturned.get("id").asInt,
                removeQuotes(userReturned.get("first_name")),
                removeQuotes(userReturned.get("last_name")),
                removeQuotes(userReturned.get("email")),
                token,
                userReturned.get("balance").asInt,
                body.get("expenses_day").asInt,
                body.get("expenses_week").asInt,
                body.get("expenses_month").asInt
            )

            userDAO.nukeUserTable()
            userDAO.insert(userEntity)

            updateViews(
                userEntity.firstName,
                userEntity.lastName,
                userEntity.balance / 100f,
                "%.2f".format(userEntity.dailyExpenses / 100f),
                "%.2f".format(userEntity.monthlyExpenses / 100f),
                "%.2f".format(userEntity.monthlyExpenses / 100f)
            )

            this.toggleViewStatus(View.INVISIBLE)
        }
    }

    private fun removeQuotes(item: JsonElement): String {
        return item.toString().substring(1, item.toString().length - 1)
    }

    /**
     * Display or Hide the progress bar and fragment views according to the current status
     * @param status Connexion status
     * @return None
     */
    private fun toggleViewStatus(status: Int) {
        view?.findViewById<ConstraintLayout>(R.id.constraint_layout_progress_bar)?.visibility =
            status
        if (status == View.INVISIBLE) {
            view?.findViewById<TextView>(R.id.f_home_text_view_user_name)?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.f_home_text_view_balance)?.visibility = View.VISIBLE
            view?.findViewById<TableRow>(R.id.table_row_stats)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<TextView>(R.id.f_home_text_view_user_name)?.visibility =
                View.INVISIBLE
            view?.findViewById<TextView>(R.id.f_home_text_view_balance)?.visibility = View.INVISIBLE
            view?.findViewById<TableRow>(R.id.table_row_stats)?.visibility = View.INVISIBLE
        }
    }
}
