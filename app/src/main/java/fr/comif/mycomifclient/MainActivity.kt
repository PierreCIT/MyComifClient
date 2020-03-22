package fr.comif.mycomifclient

import android.app.Activity
import android.content.Context
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
import androidx.viewpager.widget.ViewPager
import com.google.gson.JsonObject
import fr.comif.mycomifclient.connexion.ChangePasswordActivity
import fr.comif.mycomifclient.connexion.ConnexionActivity
import fr.comif.mycomifclient.database.*
import fr.comif.mycomifclient.fragmenttransaction.TransactionFragment
import fr.comif.mycomifclient.serverhandling.HTTPServices
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Main activity
 */
class MainActivity : AppCompatActivity(), HomeFragment.OnFragmentInteractionListener,
    TransactionFragment.OnFragmentInteractionListener {

    //TODO: use basic okHttpClient when the API will be put in production
    private val retrofitHTTPServices = HTTPServices.create(isSafeConnexion = IS_SAFE_CONNEXION)

    private lateinit var userDAO: UserDAO
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var itemDAO: ItemDAO

    private lateinit var homeFragment: HomeFragment
    private lateinit var transactionFragment: TransactionFragment

    private var adapter = ViewPagerAdapter(supportFragmentManager)

    private var user: UserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()
        transactionDAO = ComifDatabase.getAppDatabase(this).getTransactionDAO()
        itemDAO = ComifDatabase.getAppDatabase(this).getItemDAO()

        //If there is no user inside the database, force the reconnection
        user = this.userDAO.getFirst()
        if (user == null || user!!.token.isBlank()) {
            logoutFromApplication()
        } else {
            homeFragment = HomeFragment(userDAO, retrofitHTTPServices)
            transactionFragment =
                TransactionFragment(userDAO, transactionDAO, itemDAO, retrofitHTTPServices)

            setContentView(R.layout.activity_main)
            setSupportActionBar(a_main_toolbar)

            adapter.addFragment(homeFragment, resources.getString(R.string.home))
            adapter.addFragment(transactionFragment, resources.getString(R.string.transactions))
            a_main_view_pager.adapter = adapter

            //Creates a listener that permit to know on which page the user is actually
            a_main_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    if (position == 0) {
                        transactionFragment.toggleViewStatus(View.VISIBLE)
                        homeFragment.getUser()
                    } else if (position == 1) {
                        transactionFragment.getTransactions()
                    }
                    checkConnectivity(this@MainActivity)
                }

            })
            tabs.setupWithViewPager(a_main_view_pager)
        }
    }

    /**
     * Check the connectivity of the user device and display an alert box if no connexions were found
     * @param context Context of the calling activity
     * @return None
     */
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
                ) { _, _ ->
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

    /**
     * Handles the menu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutFromApi()
                true
            }
            R.id.action_information -> {
                val intent = Intent(this, InfoActivity::class.java)
                this.startActivity(intent)
                true
            }
            R.id.change_password -> {
                val intent = Intent(this, ChangePasswordActivity::class.java)
                this.startActivityForResult(intent, CHANGE_PASSWORD)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Start the connexion activity and close the main activity
     * @return None
     */
    private fun startConnexionActivity() {
        val intent = Intent(this, ConnexionActivity::class.java)
        this.startActivity(intent)
        this.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onFragmentInteraction(uri: Uri) {
    }

    /**
     * Sends a HTTP request to logout from the API (it invalidates the token linked to this specific
     * connexion, and handle the response from the server according to its HTTP code
     */
    fun logoutFromApi() {
        val token = user?.token
        if (token == null) {
            logoutFromApplication()
        } else {
            retrofitHTTPServices.logoutFromApi(buildLogoutRequest(token))
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        when (response.raw().code()) {

                            200 -> logoutFromApplication()

                            401 -> Toast.makeText(
                                this@MainActivity,
                                getString(R.string.unsuccessful_logout),
                                Toast.LENGTH_LONG
                            ).show()

                            400 -> Toast.makeText(
                                this@MainActivity,
                                getString(R.string.invalid_request),
                                Toast.LENGTH_LONG
                            ).show()

                            else -> println("Error")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Error: $t", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }

    fun logoutFromApplication() {
        userDAO.updateToken("")
        transactionDAO.nukeTransactionTable()
        itemDAO.nukeItemTable()
        startConnexionActivity()
    }

    /**
     * Create the logout request body
     * @param token Bearer token of the user (String)
     * @return a JsonObject which represents an authenticate request body (JsonObject)
     */
    private fun buildLogoutRequest(token: String): JsonObject {
        val serverBody = JsonObject()
        serverBody.addProperty("token", token)
        serverBody.addProperty("token_type_hint", "access_token")
        return serverBody
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHANGE_PASSWORD) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    logoutFromApi()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(
                        baseContext,
                        resources.getString(R.string.op_cancelled),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        baseContext,
                        resources.getString(R.string.err_loading_pwd),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
