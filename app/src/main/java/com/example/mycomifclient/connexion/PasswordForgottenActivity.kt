package com.example.mycomifclient.connexion

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.IS_SAFE_CONNEXION
import com.example.mycomifclient.R
import com.example.mycomifclient.database.ComifDatabase
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Implementation of the "First connexion" activity
 */
class PasswordForgottenActivity : AppCompatActivity() {

    private val retrofitHTTPServices = HTTPServices.create(isSafeConnexion = IS_SAFE_CONNEXION)
    private var email: String? = ComifDatabase.getAppDatabase(this).getUserDAO().getFirst()?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_forgotten)

        findViewById<EditText>(R.id.a_first_connexion_edit_text_email).setText(email)

        findViewById<ImageButton>(R.id.a_first_connexion_image_button_back).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.a_first_connexion_button_connexion).setOnClickListener {
            email = findViewById<EditText>(R.id.a_first_connexion_edit_text_email).text.toString()
            forgotPassword()
        }
    }

    /**
     * Display a popup to inform the user that its new pwd was sent by email
     * @return None
     */
    private fun displayPasswordMessage(message: String) {
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.OK
                ) { _, _ ->
                    finish()
                }
            }
            builder.setTitle(R.string.new_password)
            builder.setMessage(message)
            builder.create()
        }
        alertDialog?.show()
    }

    /*
     * This function send the request to the server, and handle the response according to its HTTP
     * error code
     * @return None
     */
    private fun forgotPassword() {
        retrofitHTTPServices.forgotPassword(buildForgotPasswordBody())
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    when (response.raw().code()) {

                        200 -> displayPasswordMessage(removeQuotes(response.body()?.get("status")))

                        500 -> displayPasswordMessage("There was an error while sending your mail. Contact an administrator")

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(baseContext, "Error: $t", Toast.LENGTH_LONG).show()
                }
            })
    }

    /*
     * Creates the body of the HTTP request
     */
    private fun buildForgotPasswordBody(): JsonObject {
        val response = JsonObject()
        response.addProperty("email", email)
        return response
    }

    private fun removeQuotes(item: JsonElement?): String {
        return item.toString().substring(1, item.toString().length - 1)
    }
}
