@file:Suppress("Annotator")

package com.example.mycomifclient.connexion

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.R
import com.example.mycomifclient.database.ComifDatabase
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    //TODO: use basic okHttpClient when the API will be put in production
    private val retrofitHTTPServices = HTTPServices.create(isSafeConnexion = false)

    private val userDAO = ComifDatabase.getAppDatabase(this).getUserDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        findViewById<ImageButton>(R.id.a_change_password_image_button_back).setOnClickListener {
            finish()
        }
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
            val body = buildResetPasswordBody(oldPassword, newPassword, verifiedNewPassword)
            val token = userDAO.getFirst().token
            retrofitHTTPServices.resetPassword("Bearer $token", body)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        when {
                            response.code() == 200 -> handlePositiveResponse()
                            response.code() == 422 || response.code() == 400 -> handleBadResponse(
                                response
                            )
                            else -> Toast.makeText(
                                baseContext,
                                resources.getString(R.string.err_loading_pwd),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Toast.makeText(baseContext, "Error: $t", Toast.LENGTH_LONG).show()
                    }
                })
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
        response.addProperty("new_password_confirmation", verifiedNewPassword)
        return response
    }

    private fun handleBadResponse(response: Response<JsonObject>) {
        val errorMessage = JSONObject(response.errorBody()!!.string())
        var errors = JSONArray()

        when {
            response.code() == 422 -> {
                errors = errorMessage.getJSONObject("errors").getJSONArray("new_password")
            }
            response.code() == 400 -> {
                errors = errorMessage.getJSONArray("errors")
            }
            else -> {
                setResult(Activity.RESULT_CANCELED, null)
                this.finish()
            }
        }

        var displayedError = ""

        for (i in 0 until errors.length()) {
            displayedError += "•"
            displayedError += errors[i]
            displayedError += "\n"
        }

        this.findViewById<EditText>(R.id.a_change_password_edit_text_old_password)
            .setText("")
        this.findViewById<EditText>(R.id.a_change_password_edit_text_new_password)
            .setText("")
        this.findViewById<EditText>(R.id.a_change_password_edit_text_verified_new_password)
            .setText("")
        this.findViewById<TextView>(R.id.a_change_password_text_view_response).text = displayedError
    }

    private fun handlePositiveResponse() {
        userDAO.updateToken("")
        Toast.makeText(
            baseContext,
            resources.getString(R.string.success_change_pwd),
            Toast.LENGTH_LONG
        ).show()
        setResult(Activity.RESULT_OK, null)
        this.finish()
    }
}
