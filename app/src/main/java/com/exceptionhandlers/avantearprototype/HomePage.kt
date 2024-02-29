package com.exceptionhandlers.avantearprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomePage : AppCompatActivity() {


    lateinit var btnEnter : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)


        btnEnter = findViewById(R.id.btnEnter)

        btnEnter.setOnClickListener{
            startActivity(Intent(this, LiveViewActivity::class.java))
        }

    }
}