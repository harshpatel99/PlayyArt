package com.twogentle.wall.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.twogentle.wall.R

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_privacy_policy, container, false)

        val backButton = view.findViewById<ImageView>(R.id.privacyPolicyBackImageView)
        backButton.setOnClickListener {
            activity!!.finish()
        }

        return view
    }
}