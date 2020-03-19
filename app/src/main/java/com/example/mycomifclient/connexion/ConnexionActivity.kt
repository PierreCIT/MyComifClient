@file:Suppress("KotlinDeprecation", "KotlinDeprecation")

package com.example.mycomifclient.connexion

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mycomifclient.IS_SAFE_CONNEXION
import com.example.mycomifclient.MainActivity
import com.example.mycomifclient.R
import com.example.mycomifclient.database.*
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val PASSWORD_FORGOTTEN = 1

/**
 * Implementation of the "Connexion" activity
 */
class ConnexionActivity : AppCompatActivity() {

    //TODO: use basic okHttpClient when the API will be put in production
    private val retrofitHTTPServices = HTTPServices.create(isSafeConnexion = IS_SAFE_CONNEXION)

    private lateinit var userDAO: UserDAO
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var itemDAO: ItemDAO

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)

        //DAO permit to use the data stocked inside the database
        userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()
        transactionDAO = ComifDatabase.getAppDatabase(this).getTransactionDAO()
        itemDAO = ComifDatabase.getAppDatabase(this).getItemDAO()

        //Do NOT remove the question mark next line, as it permits to verify whether the database contains a user or not
        this.findViewById<TextView>(R.id.a_connexion_edit_text_email).text =
            userDAO.getFirst()?.email

        findViewById<Button>(R.id.a_connexion_button_connexion).setOnClickListener {

            disableButtons()
            showLoader()

            //Get the input data
            val id = this.findViewById<EditText>(R.id.a_connexion_edit_text_email).text.toString()
            val password =
                this.findViewById<EditText>(R.id.a_connexion_edit_text_password).text.toString()

            //Create the body of the HTTP request
            val authBody: JsonObject = createAuthBody(id, password)

            //Send this body to the API
            authenticate(authBody)
        }

        findViewById<Button>(R.id.a_connexion_button_password_forgotten).setOnClickListener {
            //Disable both buttons then start the activity
            disableButtons()
            val intent = Intent(this, PasswordForgottenActivity::class.java)
            this.startActivityForResult(intent, PASSWORD_FORGOTTEN)
        }
    }

    /**
     * Check the connectivity of the user device and display an alert box if no connexions were found
     * @param context Context of the calling activity
     * @return None
     */
    private fun checkConnectivity(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        if (!isConnected) {
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
            alertDialog?.show()
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        checkConnectivity(this)
        enableButtons()
    }

    private fun enableButtons() {
        findViewById<Button>(R.id.a_connexion_button_connexion).isEnabled = true
        findViewById<Button>(R.id.a_connexion_button_password_forgotten).isEnabled = true
    }

    private fun disableButtons() {
        findViewById<Button>(R.id.a_connexion_button_connexion).isEnabled = false
        findViewById<Button>(R.id.a_connexion_button_password_forgotten).isEnabled = false
    }

    private fun showLoader() {
        findViewById<ConstraintLayout>(R.id.constraint_layout_progress_bar_connexion).visibility =
            View.VISIBLE
    }

    private fun hideLoader() {
        findViewById<ConstraintLayout>(R.id.constraint_layout_progress_bar_connexion).visibility =
            View.INVISIBLE
    }

    /**
     * Create the authenticate request body
     * @param username User name (String)
     * @param password user password (String)
     * @return a JsonObject which represents an authenticate request body (JsonObject)
     */
    private fun createAuthBody(username: String, password: String): JsonObject {
        val serverBody = JsonObject()
        serverBody.addProperty("client_id", 1)
        serverBody.addProperty("client_secret", "secret")
        serverBody.addProperty("grant_type", "password")
        serverBody.addProperty("username", username)
        serverBody.addProperty("password", password)
        return serverBody
    }

    /**
     * Authenticate the user and handle response
     * @param authBody Authentication request body (JsonObject)
     * @return None
     * @see handle401Response
     * @see handleAuthenticationResponse
     */
    private fun authenticate(authBody: JsonObject) {
        if (checkConnectivity(this)) {
            enableButtons()
            hideLoader()
            return
        } else {
            retrofitHTTPServices.authenticate(authBody).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    when (response.raw().code()) {

                        200 -> handleAuthenticationResponse(response.body())

                        400 -> handle400response()

                        401 -> handle401Response()

                        else -> {
                            println("Error")
                            enableButtons()
                            hideLoader()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(baseContext, "Error: $t", Toast.LENGTH_LONG).show()
                    hideLoader()
                }
            })
        }
    }

    /**
     * Handle authentication response: Get user info or display error msg
     * @param body Response body (JsonObject?)
     * @return None
     */
    private fun handleAuthenticationResponse(body: JsonObject?) {
        //Retrieve the token from the body
        val token = body?.get("access_token")

        if (token == null) {
            Toast.makeText(
                this,
                resources.getString(R.string.error_server_data),
                Toast.LENGTH_LONG
            ).show()
        } else {
            //Create an empty UserEntity object with the token
            val userEntity = UserEntity(
                0,
                "",
                "",
                "",
                removeQuotes(token),
                0,
                0,
                0,
                0
            )

            //Clear the database, then add the UserEntity object created
            userDAO.nukeUserTable()
            userDAO.insert(userEntity)

            //Start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            this.finish()
        }
    }


    /**
     * Remove quotes from JsonElement
     * @param item Item from which you want to remove quotes (JsonElement)
     * @return Item substring (without quotes) (String)
     */
    private fun removeQuotes(item: JsonElement): String {
        return item.toString().substring(1, item.toString().length - 1)
    }

    /**
     * Handle 401 response by displaying Toast error message
     * @return None
     */
    private fun handle401Response() {
        Toast.makeText(
            baseContext,
            resources.getString(R.string.bad_id),
            Toast.LENGTH_LONG
        ).show()
        enableButtons()
        hideLoader()
    }

    /**
     * Handle 400 response by displaying Toast error message
     * @return None
     */
    private fun handle400response() {
        Toast.makeText(
            baseContext,
            resources.getString(R.string.error_server_data),
            Toast.LENGTH_LONG
        ).show()
        enableButtons()
        hideLoader()
    }
}
