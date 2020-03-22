package fr.comif.mycomifclient

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_info.*

/**
 * Implementation of the Information activity
 */
class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        val backB: ImageButton = a_info_image_button_back
        backB.setOnClickListener {
            this.finish()
        }
    }
}
