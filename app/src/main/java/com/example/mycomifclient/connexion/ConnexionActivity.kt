package com.example.mycomifclient.connexion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.mycomifclient.R

class ConnexionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)
        findViewById<Button>(R.id.a_connexion_button_connexion).setOnClickListener {
            // TODO: Link this button to the connexion functionality
        }
        findViewById<Button>(R.id.a_connexion_button_first_connexion).setOnClickListener {
            // TODO: Link this button the the first connexion activity
        }
    }
}
