package com.twogentle.wall.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.twogentle.wall.MainActivity
import com.twogentle.wall.R
import com.twogentle.wall.extras.ExtraFunctions
import com.twogentle.wall.model.User
import kotlinx.android.synthetic.main.fragment_signup.*
import java.util.*
import kotlin.collections.ArrayList

class SignUpFragment : Fragment() {

    companion object {
        const val RC_SIGN_IN = 1001
    }

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        val emailSignUpInputLayout =
            view.findViewById<TextInputLayout>(R.id.authSignUpEmailInputLayout)
        val passwordSignUpInputLayout =
            view.findViewById<TextInputLayout>(R.id.authSignUpPasswordInputLayout)
        val confPasswordSignUpInputLayout =
            view.findViewById<TextInputLayout>(R.id.authSignUpConfPasswordInputLayout)

        val nameSignUpET = view.findViewById<TextInputEditText>(R.id.authSignUpNameEditText)
        val emailSignUpET = view.findViewById<TextInputEditText>(R.id.authSignUpEmailEditText)
        val passwordSignUpET =
            view.findViewById<TextInputEditText>(R.id.authSignUpPasswordEditText)
        val confPasswordSignUpET =
            view.findViewById<TextInputEditText>(R.id.authSignUpConfPasswordEditText)

        val progressBar = view.findViewById<View>(R.id.authSignUpProgressBar)

        val signUpButton =
            view.findViewById<TextView>(R.id.authSignUpTextView)
        val googleSignUpButtonBackground = view.findViewById<ImageView>(R.id.authSignInGoogleImageViewBackground)
        val googleSignUpButton = view.findViewById<ImageView>(R.id.authSignInGoogleImageView)

        signUpButton.setOnClickListener {

            progressBar.visibility = View.VISIBLE

            val name = nameSignUpET.text.toString()
            val email = emailSignUpET.text.toString()
            val pass = passwordSignUpET.text.toString()
            val confPass = confPasswordSignUpET.text.toString()

            when {
                name.isEmpty() -> {
                    authSignUpNameInputLayout.error = "Error"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    emailSignUpInputLayout.error = "Error"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
                pass.isEmpty() -> {
                    passwordSignUpInputLayout.error = "Error"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
                confPass.isEmpty() -> {
                    confPasswordSignUpInputLayout.error = "Error"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
                pass.length < 8 -> {
                    passwordSignUpInputLayout.error = "Password length should be > 8"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
                confPass.compareTo(pass) != 0 -> {
                    confPasswordSignUpInputLayout.error = "Passwords do not match"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
            }

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    progressBar.visibility = View.GONE

                    val timeNow = Calendar.getInstance().timeInMillis
                    val timeZone = TimeZone.getDefault().displayName

                    val newUser =
                        User(
                            name,
                            email,
                            User.SIGN_IN_TYPE_EMAIL,
                            timeZone,
                            timeNow,
                            ArrayList(),
                            50,
                            0,
                            0,
                            timeNow,
                            timeNow,
                            "",
                            User.USER_FREE
                        )

                    FirebaseMessaging.getInstance().subscribeToTopic("freeUser")

                    FirebaseFirestore.getInstance().collection("users")
                        .document(it.result!!.user!!.uid)
                        .set(newUser)
                        .addOnSuccessListener {
                            ExtraFunctions.firebaseToken()
                            activity!!.startActivity(Intent(activity, MainActivity::class.java))
                            activity!!.finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Error creating your account! Please try again",
                                Toast.LENGTH_LONG
                            ).show()
                        }


                } else {
                    Toast.makeText(
                        context,
                        "Registration Failed! Please Try again later",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.GONE
                }
            }

        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity!!, gso)

        googleSignUpButton.setOnClickListener {
            startActivityForResult(Intent(googleSignInClient.signInIntent), RC_SIGN_IN)
        }

        googleSignUpButtonBackground.setOnClickListener {
            startActivityForResult(Intent(googleSignInClient.signInIntent), RC_SIGN_IN)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                showToast("Welcome to PlayyArt")
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                showToast("Google sign in failed")
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credentials).addOnCompleteListener { outerTask ->
            if (outerTask.isSuccessful) {
                val timeNow = Calendar.getInstance().timeInMillis
                val timeZone = TimeZone.getDefault().displayName

                val firebaseUser = auth.currentUser
                val uid = outerTask.result!!.user!!.uid

                FirebaseFirestore.getInstance().collection("users").document(firebaseUser!!.uid)
                    .get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            val existingUser = it.result
                            if (existingUser.exists()) {
                                /*val sharedPreferences =
                                    context!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putInt(
                                    "subscription",
                                    it.result!!["userType"].toString().toInt()
                                )
                                editor.apply()*/
                                activity!!.startActivity(Intent(activity, MainActivity::class.java))
                                activity!!.finish()
                            } else {
                                val user = User(
                                    firebaseUser.displayName,
                                    firebaseUser.email,
                                    User.SIGN_IN_TYPE_GOOGLE,
                                    timeZone,
                                    timeNow,
                                    ArrayList(),
                                    User.DEFAULT_ART_POINTS,
                                    0,
                                    0,
                                    timeNow,
                                    timeNow,
                                    "",
                                    User.USER_FREE
                                )

                                /*val sharedPreferences =
                                    context!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putInt("subscription", User.USER_FREE)
                                editor.apply()*/

                                FirebaseMessaging.getInstance().subscribeToTopic("freeUser")

                                FirebaseFirestore.getInstance().collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        ExtraFunctions.firebaseToken()
                                        activity!!.startActivity(Intent(activity, MainActivity::class.java))
                                        activity!!.finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error creating your account! Please try again",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
                    }


            }
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}