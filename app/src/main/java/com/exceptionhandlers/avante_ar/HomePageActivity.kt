package com.exceptionhandlers.avante_ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView

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

        val card1 = findViewById<CardView>(R.id.homeCard1)
        val card2 = findViewById<CardView>(R.id.homeCard2)
        val card3 = findViewById<CardView>(R.id.homeCard3)
        val card4 = findViewById<CardView>(R.id.homeCard4)

        //val btnEnter = findViewById<Button>(R.id.btnEnter)

//        btnEnter.setOnClickListener{
//            startActivity(Intent(this, LiveViewActivity::class.java))
//
//        }

        card1.setOnClickListener {
            val scaleDownX = ObjectAnimator.ofFloat(card1, "scaleX", 0.9f)
            val scaleDownY = ObjectAnimator.ofFloat(card1, "scaleY", 0.9f)
            scaleDownX.duration = 100
            scaleDownY.duration = 100

            val scaleUpX = ObjectAnimator.ofFloat(card1, "scaleX", 1f)
            val scaleUpY = ObjectAnimator.ofFloat(card1, "scaleY", 1f)
            scaleUpX.duration = 100
            scaleUpY.duration = 100

            scaleUpX.startDelay = 100
            scaleUpY.startDelay = 100

            val scaleDown = AnimatorSet()
            scaleDown.play(scaleDownX).with(scaleDownY)
            val scaleUp = AnimatorSet()
            scaleUp.play(scaleUpX).with(scaleUpY)

            scaleDown.start()
            scaleDown.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    scaleUp.start()

                    // Delay the start of the activity so the anim can play
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@HomePageActivity, LiveViewActivity::class.java))
                    }, 100) // milliseconds
                }
            })
        }
        card2.setOnClickListener{
            val scaleDownX = ObjectAnimator.ofFloat(card2, "scaleX", 0.9f)
            val scaleDownY = ObjectAnimator.ofFloat(card2, "scaleY", 0.9f)
            scaleDownX.duration = 100
            scaleDownY.duration = 100

            val scaleUpX = ObjectAnimator.ofFloat(card2, "scaleX", 1f)
            val scaleUpY = ObjectAnimator.ofFloat(card2, "scaleY", 1f)
            scaleUpX.duration = 100
            scaleUpY.duration = 100

            scaleUpX.startDelay = 100
            scaleUpY.startDelay = 100

            val scaleDown = AnimatorSet()
            scaleDown.play(scaleDownX).with(scaleDownY)
            val scaleUp = AnimatorSet()
            scaleUp.play(scaleUpX).with(scaleUpY)

            scaleDown.start()
            scaleDown.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    scaleUp.start()
                }
            })

        }

        card3.setOnClickListener{
            val scaleDownX = ObjectAnimator.ofFloat(card3, "scaleX", 0.9f)
            val scaleDownY = ObjectAnimator.ofFloat(card3, "scaleY", 0.9f)
            scaleDownX.duration = 100
            scaleDownY.duration = 100

            val scaleUpX = ObjectAnimator.ofFloat(card3, "scaleX", 1f)
            val scaleUpY = ObjectAnimator.ofFloat(card3, "scaleY", 1f)
            scaleUpX.duration = 100
            scaleUpY.duration = 100

            scaleUpX.startDelay = 100
            scaleUpY.startDelay = 100

            val scaleDown = AnimatorSet()
            scaleDown.play(scaleDownX).with(scaleDownY)
            val scaleUp = AnimatorSet()
            scaleUp.play(scaleUpX).with(scaleUpY)

            scaleDown.start()
            scaleDown.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    scaleUp.start()
                }
            })

        }

        card4.setOnClickListener{
            val scaleDownX = ObjectAnimator.ofFloat(card4, "scaleX", 0.9f)
            val scaleDownY = ObjectAnimator.ofFloat(card4, "scaleY", 0.9f)
            scaleDownX.duration = 100
            scaleDownY.duration = 100

            val scaleUpX = ObjectAnimator.ofFloat(card4, "scaleX", 1f)
            val scaleUpY = ObjectAnimator.ofFloat(card4, "scaleY", 1f)
            scaleUpX.duration = 100
            scaleUpY.duration = 100

            scaleUpX.startDelay = 100
            scaleUpY.startDelay = 100

            val scaleDown = AnimatorSet()
            scaleDown.play(scaleDownX).with(scaleDownY)
            val scaleUp = AnimatorSet()
            scaleUp.play(scaleUpX).with(scaleUpY)

            scaleDown.start()
            scaleDown.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    scaleUp.start()
                }
            })

        }

    }





}