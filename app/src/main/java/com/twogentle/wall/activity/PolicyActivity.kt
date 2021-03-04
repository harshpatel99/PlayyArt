package com.twogentle.wall.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.twogentle.wall.R
import com.twogentle.wall.fragments.AboutFragment
import com.twogentle.wall.fragments.ContactUsFragment
import com.twogentle.wall.fragments.PrivacyPolicyFragment

class PolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policy)

        val type = intent.getStringExtra("type")
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")

        when (type){
            "privacyPolicy" -> {
                changeFragment(PrivacyPolicyFragment())
            }
            "about" -> {
                changeFragment(AboutFragment())
            }
            "contactUs" -> {
                val fragment = ContactUsFragment()
                val bundle = Bundle()
                bundle.putString("name",name)
                bundle.putString("email",email)
                fragment.arguments = bundle
                changeFragment(fragment)
            }
        }


    }

    private fun changeFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.policyContainer, fragment)
        transaction.commit()
    }

}