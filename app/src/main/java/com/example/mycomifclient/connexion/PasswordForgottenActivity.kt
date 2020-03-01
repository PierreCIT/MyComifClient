package com.example.mycomifclient.connexion

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.R

/**
 * Implementation of the "First connexion" activity
 */
class PasswordForgottenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_forgotten)
        findViewById<ImageButton>(R.id.a_first_connexion_image_button_back).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.a_first_connexion_button_connexion).setOnClickListener {
            displayPasswordMessage()
        }
    }

    /**
     * Display a popup to inform the user that its new pwd was sent by email
     * @return None
     */
    private fun displayPasswordMessage() {
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
            builder.setMessage(R.string.password_by_email)
            builder.create()
        }
        alertDialog?.show()
    }
}
