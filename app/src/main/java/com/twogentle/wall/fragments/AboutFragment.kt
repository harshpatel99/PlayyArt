package com.twogentle.wall.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.twogentle.wall.R

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        val packageInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
        val version = "Version " + packageInfo.versionName

        view.findViewById<TextView>(R.id.aboutVersionNumber).text = version

        val backButton = view.findViewById<ImageView>(R.id.aboutBackImageView)
        backButton.setOnClickListener {
            activity!!.finish()
        }

        return view
    }
}