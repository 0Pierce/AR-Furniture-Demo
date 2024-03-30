package com.exceptionhandlers.avante_ar.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.exceptionhandlers.avante_ar.R
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {


    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()

        val btnSignUp = findViewById<Button>(R.id.btnSignUp).setOnClickListener{

            //Gets the text of the fields
            val email = findViewById<EditText>(R.id.etxtEmail).text.toString()
            val pass = findViewById<EditText>(R.id.etxtPassSignUp).text.toString()
            val passConfirm = findViewById<EditText>(R.id.etxtPassSignUpConfirm).text.toString()

            //Field verification
            if(email.isNotEmpty() && pass.isNotEmpty() && passConfirm.isNotEmpty()){
                if(pass == passConfirm){
                    //Send the field text to firebase and creates a user
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomePageActivity::class.java))
                    }
                }else{
                    Toast.makeText(this, "Password don't match", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Field/s Empty", Toast.LENGTH_SHORT).show()
            }

        }



        val tvLogin = findViewById<TextView>(R.id.tvLoginUp).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))

        }

    }
}