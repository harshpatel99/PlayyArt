package com.twogentle.wall.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.R
import com.twogentle.wall.model.User
import java.util.*

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val profileInitialName = view.findViewById<TextView>(R.id.profileInitialTextView)
        val profileName = view.findViewById<TextView>(R.id.profileNameTextView)
        val profileTabLayout = view.findViewById<TabLayout>(R.id.profileTabLayout)
        val profileBadgeImageView = view.findViewById<ImageView>(R.id.profileBadgeImageView)
        val profileImageCardView = view.findViewById<MaterialCardView>(R.id.profileImageCardView)

        val query = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid!!)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                profileName.text = it.result!!["name"].toString()
                profileInitialName.text = it.result!!["name"].toString().substring(0, 1)
                    .toUpperCase(Locale.ROOT)

                when (it.result!!["subscription"]) {
                    User.USER_SUBSCRIBED_ARTS -> {
                        profileBadgeImageView.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.art_coin
                            )
                        )
                    }
                    User.USER_SUBSCRIBED_COMICS -> {
                        profileBadgeImageView.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.art_coin
                            )
                        )
                    }
                    User.USER_SUBSCRIBED_FULL -> {
                        profileBadgeImageView.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.art_coin
                            )
                        )
                        profileImageCardView.setCardBackgroundColor(ContextCompat.getColor(context!!,R.color.gold_member_color))
                    }
                }
            }
        }

        changeFragment(LikedFragment())

        profileTabLayout.addTab(
            profileTabLayout.newTab().setIcon(ContextCompat.getDrawable(context!!,R.drawable.ic_heart_24_filled))
        )
        profileTabLayout.addTab(
            profileTabLayout.newTab()
                .setIcon(ContextCompat.getDrawable(context!!,R.drawable.ic_bookmark_24_filled))
        )
        profileTabLayout.addTab(
            profileTabLayout.newTab()
                .setIcon(ContextCompat.getDrawable(context!!,R.drawable.ic_settings_24_filled))
        )

        profileTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> changeFragment(LikedFragment())
                    1 -> changeFragment(SavedFragment())
                    2 -> changeFragment(SettingsFragment())
                }
            }

        })

        return view
    }

    private fun changeFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.profileContainer, fragment)
        transaction.commit()
    }

}