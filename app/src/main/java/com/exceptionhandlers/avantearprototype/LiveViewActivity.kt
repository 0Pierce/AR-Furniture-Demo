package com.exceptionhandlers.avantearprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LiveViewActivity : AppCompatActivity() {


    lateinit var btnBack : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_view)


        btnBack = findViewById(R.id.btnBack)

        //Event handler
        btnBack.setOnClickListener{
            //Open the other activity
            startActivity(Intent(this, HomePage::class.java))

        }
    }
}