@file:Suppress("KotlinDeprecation", "KotlinDeprecation")

package com.example.mycomifclient.connexion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.MainActivity
import com.example.mycomifclient.R
import com.example.mycomifclient.database.ComifDatabase
import com.example.mycomifclient.database.UserDAO
import com.example.mycomifclient.database.UserEntity
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ConnexionActivity : AppCompatActivity() {

    private val httpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val okHttpClient: OkHttpClient.Builder =
        OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor)
    private val serverBaseUrl = "https://comif.fr"
    private val retrofit = Retrofit.Builder()
        .client(okHttpClient.build())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(serverBaseUrl)
        .build()
    private val retrofitHTTPServices = retrofit.create<HTTPServices>(HTTPServices::class.java)

    private lateinit var id: String
    private lateinit var password: String

    private lateinit var userDAO: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)

        userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()
        val user = userDAO.getFirst()

        this.findViewById<TextView>(R.id.a_connexion_edit_text_email).text = user?.email
        this.findViewById<TextView>(R.id.a_connexion_edit_text_password).text =
            user?.password

        findViewById<Button>(R.id.a_connexion_button_connexion).setOnClickListener {
            findViewById<Button>(R.id.a_connexion_button_connexion).isEnabled = false
            id = this.findViewById<EditText>(R.id.a_connexion_edit_text_email).text.toString()
            password =
                this.findViewById<EditText>(R.id.a_connexion_edit_text_password).text.toString()
            val authBody: JsonObject = createAuthBody(id, password)
            authenticate(authBody)
        }
        findViewById<Button>(R.id.a_connexion_button_first_connexion).setOnClickListener {
            findViewById<Button>(R.id.a_first_connexion_button_connexion).isEnabled = false
            val intent = Intent(this, FirstConnexionActivity::class.java)
            this.startActivity(intent)
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

                    401 -> Toast.makeText(
                        this@ConnexionActivity,
                        "Wrong credentials, please try again",
                        Toast.LENGTH_LONG
                    ).show()

                    else -> println("Error")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@ConnexionActivity, "Error: $t", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getUser() {
        val user = userDAO.getFirst()
        retrofitHTTPServices.getUser(218, "Bearer " + user.token)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    when (response.raw().code()) {

                        200 -> handleGetUserResponse(response.body())

                        401 -> reconnect()

                        else -> println("Error")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@ConnexionActivity, "Error: $t", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun handleAuthenticationResponse(body: JsonObject?) {
        val token = body?.get("access_token")
        if (token == null) {
            Toast.makeText(
                this,
                "Error while recovering data from server. Please contact an administrator",
                Toast.LENGTH_LONG
            ).show()
        } else {
            userDAO.insert(UserEntity(1, "", "", id, password, removeQuotes(token), 0))
            getUser()
        }
    }

    private fun handleGetUserResponse(body: JsonObject?) {
        if (body != null) {
            val user = userDAO.getFirst()
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
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
        }
    }

    private fun removeQuotes(item: JsonElement): String {
        return item.toString().substring(1, item.toString().length - 1)
    }

    private fun reconnect() {
        val intent = Intent(this, ConnexionActivity::class.java)
        this.startActivity(intent)
    }
}
