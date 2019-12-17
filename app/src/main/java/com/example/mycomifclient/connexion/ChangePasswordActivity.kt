package com.example.mycomifclient.connexion

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.R
import com.example.mycomifclient.database.ComifDatabase
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    //TODO: use basic okHttpClient when the API will be put in production
    private val retrofitHTTPServices = HTTPServices.create(isSafeConnexion = false)

    private val token = ComifDatabase.getAppDatabase(this).getUserDAO().getFirst().token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val submitButton = this.findViewById<Button>(R.id.a_change_password_button_change_password)
        submitButton.setOnClickListener {
            val oldPassword =
                this.findViewById<EditText>(R.id.a_change_password_edit_text_old_password)
                    .text.toString()
            val newPassword =
                this.findViewById<EditText>(R.id.a_change_password_edit_text_new_password)
                    .text.toString()
            val verifiedNewPassword =
                this.findViewById<EditText>(R.id.a_change_password_edit_text_verified_new_password)
                    .text.toString()
            if (newPassword.compareTo(verifiedNewPassword) == 0) {
                val body = buildResetPasswordBody(oldPassword, newPassword, verifiedNewPassword)
                retrofitHTTPServices.resetPassword(token, body)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(
                            call: Call<JsonObject>,
                            response: Response<JsonObject>
                        ) {
                            when (response.raw().code()) {

                                200 -> handleResponse200(response.body())

                                else -> handleBadResponse(response.body())
                            }
                        }

                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Toast.makeText(baseContext, "Error: $t", Toast.LENGTH_LONG).show()
                        }
                    })
            } else {
                Toast.makeText(baseContext, "New passwords must match", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun buildResetPasswordBody(
        oldPassword: String,
        newPassword: String,
        verifiedNewPassword: String
    ): JsonObject {
        val response = JsonObject()
        response.addProperty("old_password", oldPassword)
        response.addProperty("new_password", newPassword)
        response.addProperty("verified_new_password", verifiedNewPassword)
        return response
    }

    private fun handleBadResponse(body: JsonObject?) {
        //TODO: implement function that handles bad response
    }

    private fun handleResponse200(body: JsonObject?) {
        //TODO: implement function that handles accurate response

    }
}
