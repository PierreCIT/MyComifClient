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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.database.*
import com.example.mycomifclient.connexion.ConnexionActivity
import com.example.mycomifclient.fragmenttransaction.Transaction
import com.example.mycomifclient.fragmenttransaction.TransactionFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), HomeFragment.OnFragmentInteractionListener,
    TransactionFragment.OnFragmentInteractionListener {

    private val homeFragment = HomeFragment()
    private val transactionFragment = TransactionFragment()
    private val transactionList: ArrayList<Transaction> = ArrayList()

    private lateinit var userDAO: UserDAO
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var itemDAO: ItemDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(a_main_toolbar)

        userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()
        transactionDAO = ComifDatabase.getAppDatabase(this).getTransactionDAO()
        itemDAO = ComifDatabase.getAppDatabase(this).getItemDAO()

        userDAO.insert(UserEntity(1, "Emilie", "Bes", "", "", "", 0, "EI18"))
        transactionDAO.insert(TransactionEntity(0, "", ""))
        itemDAO.insert(ItemEntity(0, "Croissant", 2, 50))

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(homeFragment, "Home")
        adapter.addFragment(transactionFragment, "Transactions")
        a_main_view_pager.adapter = adapter
        tabs.setupWithViewPager(a_main_view_pager)

        // Test
        iniTransactionList()
        transactionFragment.setTransactionList(transactionList)
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

    private fun iniTransactionList() {
        val productMap1: MutableMap<String, Int> = mutableMapOf()
        val productMap2: MutableMap<String, Int> = mutableMapOf()
        productMap1["Oreo"] = 1
        productMap1["Coca"] = 2
        transactionList.add(Transaction("13/09/1997", "21:59", productMap1, "-7.56"))
        transactionList.add(Transaction("10/11/2019", "13:59", productMap1, "-7.56"))
        productMap2["Recharge"] = 1
        transactionList.add(Transaction("10/11/2019", "13:59", productMap2, "+25.00"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Toast.makeText(this@MainActivity, "Settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ConnexionActivity::class.java)
                this.startActivity(intent)
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
}
