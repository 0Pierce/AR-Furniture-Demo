package com.exceptionhandlers.avante_ar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

/*
*Name: HomePageActivity
*
* ========Authors:========
* Pierce, Amir, Isaac, Anson
*
*
* ========Description:========
* Simple landing activity, which lets the user to enter the AR view
* primarily used for testing, nothing more
*
*
*
* ========Primary Functions:========
*SetOnClick to facilitate activity start
*
*
*
* */

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)


        val btnEnter = findViewById<Button>(R.id.btnEnter)

        btnEnter.setOnClickListener{
            startActivity(Intent(this, LiveViewActivity::class.java))

        }

    }
}