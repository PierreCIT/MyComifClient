@file:Suppress("Annotator")

package fr.comif.mycomifclient.connexion

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import fr.comif.mycomifclient.R
import fr.comif.mycomifclient.database.ComifDatabase
import fr.comif.mycomifclient.serverhandling.HTTPServices
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Implementation of the activity to change password
 */
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

        val submitButton =
            this.findViewById<Button>(R.id.a_change_password_button_change_password)
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
            val token = userDAO.getFirst()!!.token
            changePassword(token, body)
        }
    }

    /**
     * Sends an HTTP request to the API, and handle the response from the server according to its
     * HTTP code
     * @param token The authentication token of the user (String)
     * @param body The body of the request (JsonObject)
     */
    private fun changePassword(token: String, body: JsonObject) {
        retrofitHTTPServices.resetPassword("Bearer $token", body)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    when {

                        response.code() == 200 -> handlePositiveResponse()

                        response.code() >= 400 -> handleBadResponse(response)

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

    /**
     * Build the "reset pwd" request body
     * @param oldPassword Old password (String)
     * @param newPassword New password (String)
     * @param verifiedNewPassword Confirmation of new password (String)
     * @return The request body (JsonObject)
     */
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

    /**
     * Retrieve the errors sent by the API and display them, then erase the EditText elements
     * @return None
     */
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

    /**
     * Handle positive responses: close the activity when the pwd was successfully changed.
     * @return None
     */
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
