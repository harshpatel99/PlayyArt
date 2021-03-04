package com.twogentle.wall.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.soundcloud.android.crop.Crop
import com.twogentle.wall.extras.BounceInterpolator
import com.twogentle.wall.R
import com.twogentle.wall.extras.ExtraFunctions
import com.twogentle.wall.model.Post
import com.twogentle.wall.model.User
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PostActivity : AppCompatActivity() {

    lateinit var firestore: FirebaseFirestore
    lateinit var data: Post
    private lateinit var isLiked: String
    private lateinit var isSaved: String
    private var width = 0
    private var height = 0

    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val uid = FirebaseAuth.getInstance().uid.toString()

        if (intent.extras == null) {
            FirebaseFirestore.getInstance().collection("posts")
                .orderBy("datePosted", Query.Direction.DESCENDING).limit(1)
                .get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (document in it.result!!.documents) {
                            val post = document.toObject(Post::class.java) as Post
                            data = post
                            isLiked = if (data.userLiked!!.toString().contains(uid)) {
                                "Liked"
                            } else {
                                "NotLiked"
                            }
                            isSaved = if (data.userSaved!!.toString().contains(uid)) {
                                "Saved"
                            } else {
                                "NotSaved"
                            }
                            postFun()
                        }
                    }
                }
        } else {
            data = intent.extras!!["postData"] as Post
            isLiked = intent.extras!!["postLiked"] as String
            isSaved = intent.extras!!["postSaved"] as String
            if (isLiked == "na") {
                isLiked = if (data.userLiked!!.toString().contains(uid)) {
                    "Liked"
                } else {
                    "NotLiked"
                }
                isSaved = if (data.userSaved!!.toString().contains(uid)) {
                    "Saved"
                } else {
                    "NotSaved"
                }
            }
            postFun()
        }

    }

    private fun postFun() {

        val imageView = findViewById<ImageView>(R.id.postContentImage)
        val backImageView = findViewById<ImageView>(R.id.postBackImageView)
        val bottomSheetBackgroundImageView =
            findViewById<ImageView>(R.id.postSheetBackgroundImageView)

        val artistName = findViewById<TextView>(R.id.postOwnerNameTextView)
        val artistProfilePic = findViewById<ImageView>(R.id.postArtistProfilePicImageView)

        val sheetCardView = findViewById<MaterialCardView>(R.id.postSheetCard)
        val viewTextView = findViewById<TextView>(R.id.postSheetViewsTextView)
        val likesTextView = findViewById<TextView>(R.id.postSheetLikesTextView)
        val categoryTextView = findViewById<TextView>(R.id.postSheetCategoryTextView)
        val collectionTextView = findViewById<TextView>(R.id.postSheetCollectionTextView)

        val likeFAB = findViewById<FloatingActionButton>(R.id.postSheetLikeFAB)
        val setAsFAB = findViewById<FloatingActionButton>(R.id.postSheetSetAsFAB)
        val shareFAB = findViewById<FloatingActionButton>(R.id.postSheetShareFAB)

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(this)
            .load(data.url)
            .apply(requestOptions)
            .placeholder(R.drawable.loading_dots)
            .into(imageView)

        Glide.with(this)
            .load(data.artistProfilePic)
            .apply(requestOptions)
            .placeholder(R.drawable.loading_dots)
            .into(artistProfilePic)

        val uid = FirebaseAuth.getInstance().uid

        if (isLiked == "Liked") {
            likeFAB.setImageResource(R.drawable.ic_heart_24_filled)
            likeFAB.tag = "Liked"
        } else {
            likeFAB.setImageResource(R.drawable.ic_heart_24_regular)
            likeFAB.tag = "NotLiked"
        }

        likeFAB.setOnClickListener {

            if (likeFAB.tag == "NotLiked") {
                likeFAB.setImageResource(R.drawable.ic_heart_24_filled)
                likeFAB.tag = "Liked"

                val anim = AnimationUtils.loadAnimation(this, R.anim.bounce)
                val interpolator =
                    BounceInterpolator(0.1, 20.0)
                anim.interpolator = interpolator
                likeFAB.startAnimation(anim)

                firestore.collection("posts").document(data.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "likes" to FieldValue.increment(1),
                            "userLiked" to FieldValue.arrayUnion(uid)
                        )
                    )
                firestore.collection("users").document(uid!!)
                    .update("likedCategories", FieldValue.arrayUnion(data.category))

                data.likes = data.likes!! + 1

            } else if (likeFAB.tag == "Liked") {
                likeFAB.setImageResource(R.drawable.ic_heart_24_regular)
                likeFAB.tag = "NotLiked"

                firestore.collection("posts").document(data.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "likes" to FieldValue.increment(-1),
                            "userLiked" to FieldValue.arrayRemove(uid)
                        )
                    )

                data.likes = data.likes!! - 1
            }

        }

        setAsFAB.setOnClickListener {

            if (isSaved != "NotSaved") {
                firestore.collection("posts").document(data.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "saves" to FieldValue.increment(1),
                            "userSaved" to FieldValue.arrayUnion(uid)
                        )
                    )
                firestore.collection("users").document(uid!!)
                    .update("likedCategories", FieldValue.arrayUnion(data.category))

            }

            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setView(R.layout.progress_dialog)
            dialog = builder.create()
            dialog.show()

            DownloadFromUrlTask().execute(data.url)

        }

        shareFAB.setOnClickListener {
            firestore.collection("posts").document(data.id!!)
                .update("shares", FieldValue.increment(1))
            val linkUrl = "https://play.google.com/store/apps/details?id=$packageName"
            val message = getString(R.string.share_app_message) + linkUrl
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(intent)
        }

        artistName.text = data.artistName
        viewTextView.text = data.views.toString()
        likesTextView.text = data.likes.toString()
        categoryTextView.text = data.category
        collectionTextView.text = data.collection

        backImageView.setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isUnlocked = sharedPreferences.getInt("subscription", User.USER_FREE)

        if (isUnlocked != User.USER_SUBSCRIBED_ARTS || isUnlocked != User.USER_SUBSCRIBED_FULL) {
            val bannerAdView = findViewById<AdView>(R.id.postSheetBannerAdView)

            MobileAds.initialize(this) {}
            val adRequest = AdRequest.Builder().build()
            bannerAdView.loadAd(adRequest)
        }

        firestore = FirebaseFirestore.getInstance()

        //val artistFacebook = findViewById<ImageView>(R.id.postSocialFacebook)
        //val ownerName = findViewById<TextView>(R.id.postOwnerNameTextView)
        //val artistTwitter = findViewById<ImageView>(R.id.postSocialTwitter)
        //val shareButton = findViewById<FloatingActionButton>(R.id.postSheetShareFAB)

        /*artistFacebook.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.artistSocial!!["facebook"])))
        }*/

        artistProfilePic.setOnClickListener {
            val intent = Intent(this, ArtistActivity::class.java)
            intent.putExtra("artistName", data.artistName)
            intent.putExtra("artistID", data.artistID)
            intent.putExtra("artistUsername", data.artistUsername)
            intent.putExtra("artistBio", data.artistBio)
            intent.putExtra("artistProfilePic", data.artistProfilePic)
            intent.putExtra("artistSocial", data.artistSocial)
            startActivity(intent)
        }

        artistName.setOnClickListener {
            val intent = Intent(this, ArtistActivity::class.java)
            intent.putExtra("artistName", data.artistName)
            intent.putExtra("artistID", data.artistID)
            intent.putExtra("artistUsername", data.artistUsername)
            intent.putExtra("artistBio", data.artistBio)
            intent.putExtra("artistProfilePic", data.artistProfilePic)
            intent.putExtra("artistSocial", data.artistSocial)
            startActivity(intent)
        }

        /*ownerName.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.artistSocial!!["instagram"])))
        }*/

        /*artistTwitter.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.artistSocial!!["twitter"])))
        }*/

        /*shareButton.setOnClickListener {
            val linkUrl = "https://play.google.com/store/apps/details?id=$packageName"
            val message = getString(R.string.share_app_message) + linkUrl
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(intent)
        }*/

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val cropUri = Crop.getOutput(data)
            val wallpaperManager = WallpaperManager.getInstance(this)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
            wallpaperManager.suggestDesiredDimensions(width, height)
            val inputStream = this.contentResolver.openInputStream(cropUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            wallpaperManager.setWallpaperOffsetSteps(1f, 1f)
            wallpaperManager.setBitmap(bitmap)
            Toast.makeText(this, "New wallpaper is set successfully", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        if (isChangingConfigurations) {
            Toast.makeText(this, "Deleting Cache", Toast.LENGTH_SHORT).show()
            ExtraFunctions.deleteTempFile(cacheDir)
        }

    }

    private inner class DownloadFromUrlTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg p0: String?): Boolean {

            val imageUrl = URL(p0[0])
            val connection = imageUrl.openConnection()
            connection.connectTimeout = 15000
            val bitmap = BitmapFactory.decodeStream(connection.getInputStream())
            val tempFile = File.createTempFile("temp", "jpg")
            val fos = BufferedOutputStream(FileOutputStream(tempFile))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            val display = windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            val fullWidth = point.x
            val fullHeight = point.y

            dialog.dismiss()

            val croppedFile = File.createTempFile("cropped", "jpg")
            Crop.of(Uri.fromFile(tempFile), Uri.fromFile(croppedFile))
                .withAspect(fullWidth, fullHeight).withMaxSize(fullWidth, fullHeight)
                .start(this@PostActivity)

            return true
        }
    }
}