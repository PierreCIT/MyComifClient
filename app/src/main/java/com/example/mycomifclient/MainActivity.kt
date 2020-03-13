package com.example.mycomifclient

import android.app.Activity
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
import androidx.viewpager.widget.ViewPager
import com.example.mycomifclient.connexion.ChangePasswordActivity
import com.example.mycomifclient.connexion.ConnexionActivity
import com.example.mycomifclient.database.*
import com.example.mycomifclient.fragmenttransaction.TransactionFragment
import kotlinx.android.synthetic.main.activity_main.*

const val CONNEXION_STATUS_KEY = "CONNEXION_STATUS"
const val CHANGE_PASSWORD = 1

/**
 * Main activity
 */
class MainActivity : AppCompatActivity(), HomeFragment.OnFragmentInteractionListener,
    TransactionFragment.OnFragmentInteractionListener {

    private lateinit var sharedPref: SharedPreferences

    private lateinit var userDAO: UserDAO
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var itemDAO: ItemDAO

    private lateinit var homeFragment: HomeFragment
    private lateinit var transactionFragment: TransactionFragment

    private var adapter = ViewPagerAdapter(supportFragmentManager)

    private var user: UserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPref = getPreferences(Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)

        userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()
        transactionDAO = ComifDatabase.getAppDatabase(this).getTransactionDAO()
        itemDAO = ComifDatabase.getAppDatabase(this).getItemDAO()

        user = this.userDAO.getFirst()
        if (user == null || user!!.token.isBlank()) {
            logout()
        } else {
            homeFragment = HomeFragment(userDAO)
            transactionFragment = TransactionFragment(userDAO, transactionDAO, itemDAO)

            setContentView(R.layout.activity_main)
            setSupportActionBar(a_main_toolbar)

            adapter.addFragment(homeFragment, resources.getString(R.string.home))
            adapter.addFragment(transactionFragment, resources.getString(R.string.transactions))
            a_main_view_pager.adapter = adapter
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_information -> {
                //Toast.makeText(baseContext, "Not implemented yet", Toast.LENGTH_LONG).show()
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
     * Set the shared preference connexion status var
     * @param bool Connexion status (True = connected; false = not connected) (Boolean)
     * @return None
     */
    private fun setSharedPrefConnexionStatus(bool: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(CONNEXION_STATUS_KEY, bool)
        editor.apply()
    }

    fun logout() {
        setSharedPrefConnexionStatus(false)
        userDAO.updateToken("")
        transactionDAO.nukeTransactionTable()
        itemDAO.nukeItemTable()
        startConnexionActivity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHANGE_PASSWORD) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    logout()
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
