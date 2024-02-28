package com.exceptionhandlers.avantearprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button


/*
* ===Authors:=== Exception Handlers
*Pierce, Amir, Isaac, Anson
*
* ===Description:===
*Simple homepage that will facilitate entering other activities for testing purposes
*
* ===Primary Components:===
*OnClick Listeners
*
* */
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