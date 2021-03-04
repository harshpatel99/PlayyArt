package com.twogentle.wall.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.twogentle.wall.MainActivity
import com.twogentle.wall.R
import com.twogentle.wall.fragments.SelectAuthFragment

class AuthenticationActivity : AppCompatActivity() {

    companion object {
        fun changeFragment(
            fragment: Fragment,
            supportFragmentManager: FragmentManager,
            backStack: Boolean = false
        ) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.authContainer, fragment)
            if (backStack)
                transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser != null) {
            val sp = getSharedPreferences("pref", Context.MODE_PRIVATE)
            val selColArray = sp.getString("selectedCollections", "")

            if (selColArray!!.isEmpty()) {
                startActivity(Intent(this, SelectCollectionActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        val settings = firestoreSettings { isPersistenceEnabled = true }
        FirebaseFirestore.getInstance().firestoreSettings = settings

        setContentView(R.layout.activity_authentication)

        changeFragment(SelectAuthFragment(), supportFragmentManager)

    }

}