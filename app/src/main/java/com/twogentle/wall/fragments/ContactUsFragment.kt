package com.twogentle.wall.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.R
import com.twogentle.wall.model.ContactUs
import java.util.*

class ContactUsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contact_us, container, false)

        val nameView = view.findViewById<View>(R.id.contactUsName)
        val emailView = view.findViewById<View>(R.id.contactUsEmail)
        val nameTextView = nameView.findViewById<TextView>(R.id.profileCardItemValue)
        val emailTextView = emailView.findViewById<TextView>(R.id.profileCardItemValue)
        val backButton = view.findViewById<ImageView>(R.id.contactUsBackImageView)
        val messageInputLayout =
            view.findViewById<TextInputLayout>(R.id.contactUsMessageInputLayout)
        val messageEditText = view.findViewById<TextInputEditText>(R.id.contactUsMessageEditText)
        val submit = view.findViewById<MaterialCardView>(R.id.contactUsSubmitCard)

        val name = arguments!!.getString("name")
        val email = arguments!!.getString("email")

        val contactUsEmailTitleTextView =
            emailView.findViewById<TextView>(R.id.profileCardItemTitle)
        contactUsEmailTitleTextView.text = context!!.getString(R.string.e_mail)

        val contactUsNameTitleTextView =
            nameView.findViewById<TextView>(R.id.profileCardItemTitle)
        contactUsNameTitleTextView.text = context!!.getString(R.string.name)

        nameTextView.text = name
        emailTextView.text = email

        backButton.setOnClickListener {
            activity!!.finish()
        }

        submit.setOnClickListener {

            val message = messageEditText.text.toString()

            if (message.isEmpty()) {
                messageInputLayout.error = "Please fill it."
                return@setOnClickListener
            }

            val contactUsData = ContactUs(name, email, message, Calendar.getInstance().timeInMillis)
            messageEditText.setText("")
            FirebaseFirestore.getInstance().collection("contactUs")
                .document().set(contactUsData)
                .addOnSuccessListener {
                    Snackbar.make(
                        submit,
                        "Submitted!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Snackbar.make(
                        submit,
                        "Error submitting! Please try again.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
        }

        return view
    }
}