package com.example.mycomifclient.connexion

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mycomifclient.R

class FirstConnexionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_connexion)
        findViewById<ImageButton>(R.id.a_first_connexion_image_button_back).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.a_first_connexion_button_connexion).setOnClickListener {
            displayPasswordMessage(this)
        }
    }

    private fun displayPasswordMessage(context: Context) {
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.OK
                ) { dialog, id ->
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
