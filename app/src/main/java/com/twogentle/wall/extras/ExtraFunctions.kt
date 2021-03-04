package com.twogentle.wall.extras

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.twogentle.wall.R
import java.io.File
import java.lang.Exception
import java.net.InetAddress

class ExtraFunctions {

    companion object {
        fun firebaseToken() {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                if (it.isSuccessful) {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(FirebaseAuth.getInstance().uid.toString())
                        .update("token", it.result!!.token)
                }
            }
        }

        fun openWebsite(context: Context,url: String) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        fun openFacebook(context: Context,url: String) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        fun openInstagram(context: Context,url: String) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        fun openTwitter(context: Context,url: String) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        fun sharePost(activity: Activity) {
            val linkUrl = "https://play.google.com/store/apps/details?id=" + activity.packageName
            val message = activity.getString(R.string.share_post_message) + linkUrl
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)
            activity.startActivity(intent)
        }

        fun shareComic(context: Context) {
            val linkUrl = "https://play.google.com/store/apps/details?id=" + context.packageName
            val message = context.getString(R.string.share_comic_message) + linkUrl
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)
            context.startActivity(intent)
        }

        fun shareVideo(context: Context) {
            val linkUrl = "https://play.google.com/store/apps/details?id=" + context.packageName
            val message = context.getString(R.string.share_video_message) + linkUrl
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)
            context.startActivity(intent)
        }

        fun shareApp(context: Context) {
            val linkUrl = "https://play.google.com/store/apps/details?id=" + context.packageName
            val message = context.getString(R.string.share_app_message) + linkUrl
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)
            context.startActivity(intent)
        }

        fun openPlayStorePage(context: Context) {
            val packageName = context.packageName
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: android.content.ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }

        fun deleteTempFile(file: File) {
            if (file.isDirectory) {
                val files = file.listFiles()
                if (files != null) {
                    for (f in files) {
                        if (f.isDirectory) {
                            deleteTempFile(f)
                        } else {
                            f.delete()
                        }
                    }
                }
            }
        }
    }

}