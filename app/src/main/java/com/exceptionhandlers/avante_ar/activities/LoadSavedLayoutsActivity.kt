package com.exceptionhandlers.avante_ar.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exceptionhandlers.avante_ar.R
import com.exceptionhandlers.avante_ar.activities.adapters.RecyclerVwAdapterSvLayouts
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoadSavedLayoutsActivity : AppCompatActivity() {

    var titlesList = mutableListOf<String>()
    private lateinit var dbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_load_saved_layouts)


        dbRef = FirebaseDatabase.getInstance().getReference("SavedAnchors")
        val recView = findViewById<RecyclerView>(R.id.recView)
        recView.layoutManager = LinearLayoutManager(this)
        recView.adapter = RecyclerVwAdapterSvLayouts(titlesList)



    }


    private fun addToList(title : String){

        titlesList.add(title)

    }

}