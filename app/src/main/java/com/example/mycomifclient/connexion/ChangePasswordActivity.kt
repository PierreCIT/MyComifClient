package com.example.mycomifclient.connexion

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.R
import com.example.mycomifclient.database.ComifDatabase
import com.example.mycomifclient.serverhandling.HTTPServices
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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
            if (newPassword.compareTo(verifiedNewPassword) == 0 && newPassword.compareTo(oldPassword) != 0) {
                val body = buildResetPasswordBody(oldPassword, newPassword, verifiedNewPassword)
                val token = userDAO.getFirst().token
                retrofitHTTPServices.resetPassword("Bearer $token", body)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(
                            call: Call<JsonObject>,
                            response: Response<JsonObject>
                        ) {
                            when {
                                response.body()?.get("success") == null -> Toast.makeText(
                                    baseContext,
                                    resources.getString(R.string.err_loading_pwd),
                                    Toast.LENGTH_LONG
                                ).show()
                                response.body()!!.get("success").asBoolean -> handlePositiveResponse()
                                else -> handleBadResponse(response.body()!!)
                            }
                        }

                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Toast.makeText(baseContext, "Error: $t", Toast.LENGTH_LONG).show()
                        }
                    })
            } else if (newPassword.compareTo(verifiedNewPassword) != 0) {
                Toast.makeText(
                    baseContext,
                    resources.getString(R.string.match_new_pwd),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    baseContext,
                    resources.getString(R.string.new_pwd_diff),
                    Toast.LENGTH_LONG
                ).show()
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

    private fun handleBadResponse(body: JsonObject) {
        this.findViewById<EditText>(R.id.a_change_password_edit_text_old_password)
            .setText("")
        this.findViewById<EditText>(R.id.a_change_password_edit_text_new_password)
            .setText("")
        this.findViewById<EditText>(R.id.a_change_password_edit_text_verified_new_password)
            .setText("")
        // this.findViewById<TextView>(R.id.a_change_password_text_view_response).text =
        //     removeQuotes(body.get("message"))
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

    private fun removeQuotes(item: JsonElement): String {
        return item.toString().substring(1, item.toString().length - 1)
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
}
