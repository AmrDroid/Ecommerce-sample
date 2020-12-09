package com.amrmustafa.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amrmustafa.ecommerce.R
import com.amrmustafa.ecommerce.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_splash)
        setContentView(binding.root)

        binding.start!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                binding.start!!.visibility=View.GONE
                binding.loading!!.visibility=View.VISIBLE

                //delay [SplashActivity] for 2sec
                Handler().postDelayed({
                    startActivity(Intent(applicationContext, ProductActivity::class.java))
                    finish()
                    //add fade transition when navigating from [SplashActivity] to [ProductActivity]
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                }, 2000)
            }

        })







    }


}
