package com.example.mycomifclient

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.database.*
import com.example.mycomifclient.connexion.ConnexionActivity
import com.example.mycomifclient.fragmenttransaction.Transaction
import com.example.mycomifclient.fragmenttransaction.TransactionFragment
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), HomeFragment.OnFragmentInteractionListener,
    TransactionFragment.OnFragmentInteractionListener {

    private val homeFragment = HomeFragment()
    private val transactionFragment = TransactionFragment()
    private val transactionList: ArrayList<Transaction> = ArrayList()

    private var adapter = ViewPagerAdapter(supportFragmentManager)

    private lateinit var userDAO: UserDAO
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var itemDAO: ItemDAO

    private val httpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val okHttpClient: OkHttpClient.Builder =
        OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor)
    private val SERVER_BASE_URL = "https://comif.fr"
    private val retrofit = Retrofit.Builder()
        .client(okHttpClient.build())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(SERVER_BASE_URL)
        .build()

    private val retrofitHTTPServices = retrofit.create<HTTPServices>(HTTPServices::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(a_main_toolbar)

        userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()
        transactionDAO = ComifDatabase.getAppDatabase(this).getTransactionDAO()
        itemDAO = ComifDatabase.getAppDatabase(this).getItemDAO()

        // INSERT A KNOWN USER TO TEST API REQUEST
        // userDAO.insert(UserEntity(ID, "", "", "hello@hello.com", "password", "", balanceValue))

        var user = userDAO.getFirst()
        val authBody: JsonObject

        if (user!= null) {
            authBody = createAuthBody(user.email, user.password)
        } else {
            authBody = createAuthBody("xxx@xxx.com", "password")
        }

        authenticate(authBody)

        adapter.addFragment(homeFragment, "Home")
        adapter.addFragment(transactionFragment, "Transactions")
        a_main_view_pager.adapter = adapter
        tabs.setupWithViewPager(a_main_view_pager)
    }

    private fun checkConnectivity(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        //Alert Dialog box
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.OK,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User clicked OK button
                    })
            }
            builder.setTitle(R.string.no_internet_connexion)
            builder.setMessage(R.string.offline_message)
            // Create the AlertDialog
            builder.create()
        }
        if (!isConnected) {
            alertDialog?.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                val intent = Intent(this, ConnexionActivity::class.java)
                this.startActivity(intent)
                this.finish()
                true
            }
            R.id.action_information -> {
                Toast.makeText(baseContext, "Not implemented yet", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResume() {
        super.onResume()
        checkConnectivity(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun authenticate(authBody: JsonObject) {

        retrofitHTTPServices.authenticate(authBody).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when (response.raw().code()) {

                    //TODO: Implement bearer token in the header of the POST request
                    200 -> handleAuthenticationResponse(response.body())

                    401 -> Toast.makeText(
                        baseContext,
                        "Unauthorised request",
                        Toast.LENGTH_LONG
                    ).show()

                    else -> println("Error")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: $t", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getTransactions() {
        val user = userDAO.getFirst()
        retrofitHTTPServices.getTransactions(
            user.id,
            "Bearer " + user.token
        )
            .enqueue(object : Callback<JsonArray> {
                override fun onResponse(
                    call: Call<JsonArray>,
                    response: Response<JsonArray>
                ) {
                    when (response.raw().code()) {

                        200 -> handleGetTransactionsResponse(response.body())

                        401 -> println("401")

                        else -> println("Error")
                    }
                }

                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: $t", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getUser() {
        val user  = userDAO.getFirst()
            retrofitHTTPServices.getUser(user.id, "Bearer " + user.token)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    when (response.raw().code()) {

                        200 -> handleGetUserResponse(response.body())

                        401 -> println("401")

                        else -> println("Error")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: $t", Toast.LENGTH_LONG).show()
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
                        "Recharge",
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

    private fun handleAuthenticationResponse(body: JsonObject?) {
        val token = body?.get("access_token")
        if (token == null) {
            Toast.makeText(
                this@MainActivity,
                "Error while recovering items from server. Please contact an administrator",
                Toast.LENGTH_LONG
            ).show()
        } else {
            userDAO.updateToken(removeQuotes(token))
            getUser()
        }
    }

    private fun handleGetUserResponse(body: JsonObject?) {
        if (body != null) {
            var user = userDAO.getFirst()
            val userEntity = UserEntity(
                body.get("id").asInt,
                removeQuotes(body.get("first_name")),
                removeQuotes(body.get("last_name")),
                removeQuotes(body.get("email")),
                user.password,
                user.token,
                body.get("balance").asInt
            )
            userDAO.nukeTable()
            userDAO.insert(userEntity)

            user = userDAO.getFirst()

            homeFragment.updateNameAndBalance(user.firstName, user.lastName, user.balance / 100f)

            getTransactions()
        }
    }

    private fun removeQuotes(item: JsonElement): String {
        return item.toString().substring(1, item.toString().length - 1)
    }

    private fun createAuthBody(username: String, password: String): JsonObject {
        val serverBody = JsonObject()
        serverBody.addProperty("client_id", 1)
        serverBody.addProperty("client_secret", "secret")
        serverBody.addProperty("grant_type", "password")
        serverBody.addProperty("username", username)
        serverBody.addProperty("password", password)
        return serverBody
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
            transactionList.add(Transaction(date, hour, itemsMap, totalTransactionPrice.toString()))
        }
        transactionList.reverse()
        transactionFragment.setTransactionList(transactionList)
    }
}
