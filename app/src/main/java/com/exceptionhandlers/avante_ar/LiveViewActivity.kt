package com.exceptionhandlers.avante_ar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
/*
*Name: LiveViewActivity
*
* ========Authors:========
* Pierce, Amir, Isaac, Anson
*
*
* ========Description:========
* The primary AR Activity which will handle AR functionality/capabilities
* All AR related classes/APIs will culminate here
* TBD
*
*
*
*
* ========Primary Functions:========
*Hold LiveView fragment which creates a AR viewport
* TBD
*
*

* */
class LiveViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_view)

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener{
            startActivity(Intent(this, HomePageActivity::class.java))

        }

    }
}