package com.twogentle.wall

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.twogentle.wall.fragments.*

class MainActivity : AppCompatActivity() {

    companion object {
        fun changeFragment(activity: MainActivity, fragment: Fragment, backStack: Boolean) {
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.homeContainer, fragment)
            if (backStack)
                transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .update("lastLogged", Calendar.getInstance().timeInMillis)*/

        MobileAds.initialize(this) {}

        val pointsImageView = findViewById<ImageView>(R.id.homePointsImageView)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavHome)

        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        pointsImageView.setOnClickListener {
            changeFragment(this, ProfileFragment(),false)
        }

        changeFragment(this, HomeFragment(),false)

    }

    override fun onStart() {
        super.onStart()

        val nameTextView = findViewById<TextView>(R.id.homeAppNameTextView)
        val logoImageView = findViewById<ImageView>(R.id.homeLogoImageView)
        //Handler().postDelayed({ logoImageView.visibility = View.VISIBLE }, 1000)
        Handler().postDelayed({ nameTextView.visibility = View.VISIBLE }, 2500)
        Handler().postDelayed({ nameTextView.visibility = View.GONE }, 7500)
        //Handler().postDelayed({ logoImageView.visibility = View.GONE }, 9000)
    }

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navHome -> {
                    val homeFragment = HomeFragment()
                    changeFragment(this, homeFragment,false)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navPopular -> {
                    val popularFragment = PopularFragment()
                    changeFragment(this, popularFragment,false)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navVideo -> {
                    val videoFragment = ExoPlayerFragment()
                    changeFragment(this, videoFragment,false)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navCategories -> {
                    val collectionFragment = CollectionsFragment()
                    changeFragment(this, collectionFragment,false)
                    return@OnNavigationItemSelectedListener true
                }

                /*R.id.navSettings -> {
                    val profileFragment = ProfileFragment()
                    changeFragment(this, profileFragment,false)
                    return@OnNavigationItemSelectedListener true
                }*/
            }
            false
        }

}