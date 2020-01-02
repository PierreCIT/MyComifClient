package com.example.mycomifclient

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.mycomifclient.connexion.ConnexionActivity
import com.example.mycomifclient.database.*
import com.example.mycomifclient.fragmenttransaction.Transaction
import com.example.mycomifclient.fragmenttransaction.TransactionFragment
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val CONNEXION_STATUS_KEY = "CONNEXION_STATUS"

class MainActivity : AppCompatActivity(), HomeFragment.OnFragmentInteractionListener,
    TransactionFragment.OnFragmentInteractionListener {
    lateinit var sharedPref: SharedPreferences

    private val homeFragment = HomeFragment()
    private val transactionFragment = TransactionFragment()
    private val transactionList: ArrayList<Transaction> = ArrayList()

    private var adapter = ViewPagerAdapter(supportFragmentManager)

    private lateinit var userDAO: UserDAO
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var itemDAO: ItemDAO

    private val httpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    //TODO: use basic okHttpClient when the API will be put in production
    private val okHttpClient: OkHttpClient.Builder = UnsafeHTTPClient.getUnsafeOkHttpClient()

    private val serverBaseUrl = "https://dev.comif.fr"
    private val retrofit = Retrofit.Builder()
        .client(okHttpClient.build())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(serverBaseUrl)
        .build()
    private val retrofitHTTPServices = retrofit.create<HTTPServices>(HTTPServices::class.java)

    private lateinit var user: UserEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPref = getPreferences(Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)

        userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()
        transactionDAO = ComifDatabase.getAppDatabase(this).getTransactionDAO()
        itemDAO = ComifDatabase.getAppDatabase(this).getItemDAO()

        user = userDAO.getFirst()
        if (!::user.isInitialized) {
            reconnect()
        } else {
            getTransactions()

            setSharedPrefConnexionStatus(true)

            setContentView(R.layout.activity_main)
            setSupportActionBar(a_main_toolbar)

            adapter.addFragment(homeFragment, "Home")
            adapter.addFragment(transactionFragment, "Transactions")
            a_main_view_pager.adapter = adapter
            tabs.setupWithViewPager(a_main_view_pager)
        }
    }

    private fun checkConnectivity(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        //Alert Dialog box
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.OK
                ) { dialog, id ->
                    // User clicked OK button
                }
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
                startConnexionActivity()
                setSharedPrefConnexionStatus(false)
                userDAO.updateToken("")
                transactionDAO.nukeTransactionTable()
                itemDAO.nukeItemTable()
                true
            }
            R.id.action_information -> {
                //Toast.makeText(baseContext, "Not implemented yet", Toast.LENGTH_LONG).show()
                val intent = Intent(this, InfoActivity::class.java)
                this.startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startConnexionActivity() {
        val intent = Intent(this, ConnexionActivity::class.java)
        this.startActivity(intent)
        this.finish()
    }

    private fun checkConnexionStatus() {
        if (!sharedPref.getBoolean(CONNEXION_STATUS_KEY, false)) {
            startConnexionActivity()
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
        // TODO: Uncomment this line
        //checkConnexionStatus()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun setSharedPrefConnexionStatus(bool: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(CONNEXION_STATUS_KEY, bool)
        editor.apply()
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

    private fun getTransactions() {
        val user = userDAO.getFirst()
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

                        401 -> reconnect()

                        else -> println("Error")
                    }
                }

                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                    Toast.makeText(baseContext, "Error: $t", Toast.LENGTH_LONG).show()
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

    private fun removeQuotes(item: JsonElement): String {
        return item.toString().substring(1, item.toString().length - 1)
    }

    private fun createTransactionsList() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)
        val currentDate = Date().time
        val transactions = transactionDAO.getAll()

        //TODO: Fix the statistics, as they are not accurate actually
        var dayConsos = 0f
        var weekConsos = 0f
        var monthConsos = 0f

        transactions.forEach { transaction ->

            val itemsMap: MutableMap<String, Int> = mutableMapOf()
            val items = itemDAO.selectItems(transaction.transactionId)
            var totalTransactionPrice = 0f

            val date = transaction.date.split(' ')[0]
            val hour = transaction.date.split(' ')[1]
            val timeDiff =
                (currentDate - dateFormat.parse(transaction.date).time) / 1000f / 60f / 60f / 24f

            items.forEach { item ->
                itemsMap[item.itemName] = item.quantity
                totalTransactionPrice -= item.price * item.quantity / 100f
            }

            if (timeDiff <= 1 && transaction.type == "debit") {
                dayConsos += totalTransactionPrice
            }
            if (timeDiff <= 7 && transaction.type == "debit") {
                weekConsos += totalTransactionPrice
            }
            if (timeDiff <= 30 && transaction.type == "debit") {
                monthConsos += totalTransactionPrice
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
        transactionFragment.setTransactionList(transactionList)
        homeFragment.updateViews(
            user.firstName,
            user.lastName,
            user.balance / 100f,
            "%.2f".format(dayConsos),
            "%.2f".format(weekConsos),
            "%.2f".format(monthConsos)
        )
        homeFragment.toggleViewStatus(View.VISIBLE)
    }

    private fun reconnect() {
        finish()
        val intent = Intent(this, ConnexionActivity::class.java)
        this.startActivity(intent)
    }
}
