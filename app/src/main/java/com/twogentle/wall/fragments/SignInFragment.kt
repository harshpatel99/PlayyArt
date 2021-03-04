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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import java.util.*
import kotlin.collections.ArrayList

class SignInFragment : Fragment() {

    companion object {
        const val RC_SIGN_IN = 1001
    }

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signin, container, false)

        val emailSignInInputLayout =
            view.findViewById<TextInputLayout>(R.id.authSignInEmailInputLayout)
        val passwordSignInInputLayout =
            view.findViewById<TextInputLayout>(R.id.authSignInPasswordInputLayout)

        val emailSignInET = view.findViewById<TextInputEditText>(R.id.authSignInEmailEditText)
        val passwordSignInET = view.findViewById<TextInputEditText>(R.id.authSignInPasswordEditText)

        val progressBar = view.findViewById<View>(R.id.authSignInProgressBar)

        val signInButton = view.findViewById<TextView>(R.id.authSignInTextView)
        val googleSignInButtonBackground =
            view.findViewById<ImageView>(R.id.authSignInGoogleImageViewBackground)
        val googleSignInButton = view.findViewById<ImageView>(R.id.authSignInGoogleImageView)

        val forgotPass = view.findViewById<TextView>(R.id.authForgotPasswordTextView)

        signInButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val email = emailSignInET.text.toString()
            val pass = passwordSignInET.text.toString()

            when {
                email.isEmpty() -> {
                    emailSignInInputLayout.error = "Error"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
                pass.isEmpty() -> {
                    passwordSignInInputLayout.error = "Error"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
                pass.length < 8 -> {
                    passwordSignInInputLayout.error = "Password length should be > 6"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
            }


            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(it.result!!.user!!.uid)
                        .get().addOnCompleteListener { innerTask ->
                            progressBar.visibility = View.GONE
                            if (innerTask.isSuccessful) {
                                ExtraFunctions.firebaseToken()
                                activity!!.startActivity(
                                    Intent(
                                        activity,
                                        MainActivity::class.java
                                    )
                                )
                                activity!!.finish()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Sign in failed! Please Try again later",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                } else {
                    Toast.makeText(
                        context,
                        "Sign in failed! Please Try again later",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.GONE
                }
            }
        }

        forgotPass.setOnClickListener {

            val email = emailSignInET.text.toString()

            when {
                email.isEmpty() -> {
                    emailSignInInputLayout.error = "Please fill it."
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful) {
                    val builder = MaterialAlertDialogBuilder(context!!, R.style.DialogTheme)
                    builder.setTitle("Forgot Password?")
                    builder.setMessage("Password reset email is sent to your registered email address. Please check your email.")
                    builder.setPositiveButton(
                        "Continue"
                    ) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    builder.show()
                } else {
                    Toast.makeText(
                        context,
                        "Error in sending password reset email. Please enter your registered email address.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            val builder = MaterialAlertDialogBuilder(context!!, R.style.DialogTheme)
            builder.setTitle("Forgot Password?")
            builder.setMessage("Password reset email is sent to your registered email id. Please check your email.")
            builder.setPositiveButton(
                "Cancel"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }

        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity!!, gso)

        googleSignInButton.setOnClickListener {
            startActivityForResult(Intent(googleSignInClient.signInIntent), RC_SIGN_IN)
        }

        googleSignInButtonBackground.setOnClickListener {
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

                                activity!!.startActivity(
                                    Intent(
                                        activity,
                                        MainActivity::class.java
                                    )
                                )
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
                                        activity!!.startActivity(
                                            Intent(
                                                activity,
                                                MainActivity::class.java
                                            )
                                        )
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