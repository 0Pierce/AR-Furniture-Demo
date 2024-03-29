package com.exceptionhandlers.avante_ar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()


        val tvSignup = findViewById<TextView>(R.id.tvSignUp).setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))

        }




        val btnLogin = findViewById<Button>(R.id.btnLogin).setOnClickListener{
            val email = findViewById<EditText>(R.id.etxtEmailLogin).text.toString()
            val pass = findViewById<EditText>(R.id.etxtPassLogin).text.toString()
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            if(email.isNotEmpty() && pass.isNotEmpty()){


                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener{
                    startActivity(Intent(this, HomePageActivity::class.java))

                }

            }else{
                Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show()
            }
        }



    }
}