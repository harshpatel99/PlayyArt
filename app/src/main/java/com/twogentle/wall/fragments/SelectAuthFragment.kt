package com.twogentle.wall.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.twogentle.wall.R
import com.twogentle.wall.activity.AuthenticationActivity

class SelectAuthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth_select, container, false)

        val signInOuterCard = view.findViewById<TextView>(R.id.authSignInOuterCard)
        val signUpOuterCard = view.findViewById<TextView>(R.id.authSignUpOuterCard)

        signInOuterCard.setOnClickListener {
            AuthenticationActivity.changeFragment(
                SignInFragment(),
                activity!!.supportFragmentManager,
                true
            )
        }

        signUpOuterCard.setOnClickListener {
            AuthenticationActivity.changeFragment(
                SignUpFragment(),
                activity!!.supportFragmentManager,
                true
            )
        }

        return view
    }
}