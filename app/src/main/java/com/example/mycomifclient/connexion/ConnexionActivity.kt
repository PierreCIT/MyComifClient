@file:Suppress("KotlinDeprecation", "KotlinDeprecation")

package com.example.mycomifclient.connexion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.MainActivity
import com.example.mycomifclient.R
import com.example.mycomifclient.database.*
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val FIRST_CONNEXION = 1

class ConnexionActivity : AppCompatActivity() {

    //TODO: use basic okHttpClient when the API will be put in production
    private val retrofitHTTPServices = HTTPServices.create(isSafeConnexion = false)

    private lateinit var userDAO: UserDAO
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var itemDAO: ItemDAO

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)

        userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()
        transactionDAO = ComifDatabase.getAppDatabase(this).getTransactionDAO()
        itemDAO = ComifDatabase.getAppDatabase(this).getItemDAO()
        val user = userDAO.getFirst()

        this.findViewById<TextView>(R.id.a_connexion_edit_text_email).text = user?.email

        findViewById<Button>(R.id.a_connexion_button_connexion).setOnClickListener {
            findViewById<Button>(R.id.a_connexion_button_connexion).isEnabled = false
            val id = this.findViewById<EditText>(R.id.a_connexion_edit_text_email).text.toString()
            val password =
                this.findViewById<EditText>(R.id.a_connexion_edit_text_password).text.toString()
            val authBody: JsonObject = createAuthBody(id, password)
            authenticate(authBody)
        }
        findViewById<Button>(R.id.a_connexion_button_first_connexion).setOnClickListener {
            findViewById<Button>(R.id.a_connexion_button_first_connexion).isEnabled = false
            val intent = Intent(this, FirstConnexionActivity::class.java)
            this.startActivityForResult(intent, FIRST_CONNEXION)
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<Button>(R.id.a_connexion_button_connexion).isEnabled = true
        findViewById<Button>(R.id.a_connexion_button_first_connexion).isEnabled = true
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

    private fun authenticate(authBody: JsonObject) {

        retrofitHTTPServices.authenticate(authBody).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when (response.raw().code()) {

                    200 -> handleAuthenticationResponse(response.body())

                    401 -> handle401Response()

                    else -> println("Error")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(baseContext, "Error: $t", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getUser(token: String) {
        retrofitHTTPServices.getUser("Bearer $token")
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    when (response.raw().code()) {

                        200 -> handleGetUserResponse(response.body(), token)

                        401 -> reconnect()

                        else -> println("Error")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(baseContext, "Error: $t", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun handleAuthenticationResponse(body: JsonObject?) {
        val token = body?.get("access_token")
        if (token == null) {
            Toast.makeText(
                this,
                resources.getString(R.string.error_server_data),
                Toast.LENGTH_LONG
            ).show()
        } else {
            getUser(removeQuotes(token))
        }
    }

    private fun handleGetUserResponse(body: JsonObject?, token: String) {
        if (body != null) {
            val userEntity = UserEntity(
                body.get("id").asInt,
                removeQuotes(body.get("first_name")),
                removeQuotes(body.get("last_name")),
                removeQuotes(body.get("email")),
                token,
                body.get("balance").asInt
            )

            userDAO.nukeUserTable()
            userDAO.insert(userEntity)

            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            finish()
        }
    }

    private fun removeQuotes(item: JsonElement): String {
        return item.toString().substring(1, item.toString().length - 1)
    }

    private fun reconnect() {
        val intent = Intent(this, ConnexionActivity::class.java)
        this.startActivity(intent)
        finish()
    }

    private fun handle401Response() {
        Toast.makeText(
            this,
            resources.getString(R.string.bad_id),
            Toast.LENGTH_LONG
        ).show()
        findViewById<Button>(R.id.a_connexion_button_connexion).isEnabled = true
    }
}
