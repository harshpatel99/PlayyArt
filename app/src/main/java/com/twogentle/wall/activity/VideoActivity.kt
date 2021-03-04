package com.twogentle.wall.activity

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.R
import com.twogentle.wall.extras.BounceInterpolator
import com.twogentle.wall.extras.ExtraFunctions
import com.twogentle.wall.model.Post

class VideoActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var progressBar: ProgressBar
    private lateinit var data: Post
    private var currentPosition = 0

    companion object {
        const val PLAYBACK_TIME = "play_time"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        videoView = findViewById(R.id.videoPlayerView)

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(PLAYBACK_TIME)
        }

        val uid = FirebaseAuth.getInstance().uid.toString()
        data = intent.extras!!["videoData"] as Post
        var isSaved = intent.extras!!["videoSaved"] as String
        if(isSaved == "na"){
            isSaved = if (data.userSaved!!.toString().contains(uid)) {
                "Saved"
            } else {
                "NotSaved"
            }
        }

        progressBar = findViewById(R.id.videoProgressBar)
        val ownerTextView = findViewById<TextView>(R.id.videoOwnerTextView)
        val videoShare = findViewById<FloatingActionButton>(R.id.wallVideoShareFAB)
        val videoSave = findViewById<FloatingActionButton>(R.id.wallVideoSaveFAB)

        progressBar.visibility = View.VISIBLE

        ownerTextView.text = data.artistName
        ownerTextView.setOnClickListener {
            val intent = Intent(this, ArtistActivity::class.java)
            intent.putExtra("artistName", data.artistName)
            intent.putExtra("artistID", data.artistID)
            intent.putExtra("artistUsername", data.artistUsername)
            intent.putExtra("artistBio", data.artistBio)
            intent.putExtra("artistProfilePic", data.artistProfilePic)
            intent.putExtra("artistSocial", data.artistSocial)
            startActivity(intent)
        }


        val bannerAdView = findViewById<AdView>(R.id.videoBannerAdView)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        bannerAdView.loadAd(adRequest)

        videoView.setOnPreparedListener {
            it.setOnInfoListener { _, what, _ ->
                when(what){
                    MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                        progressBar.visibility = View.GONE
                    }
                    MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        progressBar.visibility = View.GONE
                    }
                }
                return@setOnInfoListener false
            }
        }

        if (isSaved == "Saved") {
            videoSave.setImageResource(R.drawable.ic_bookmark_24_filled)
            videoSave.tag = "Saved"
        }

        val firestore = FirebaseFirestore.getInstance()

        videoSave.setOnClickListener {
            if (videoSave.tag == "NotSaved") {
                videoSave.setImageResource(R.drawable.ic_bookmark_24_filled)
                videoSave.tag = "Saved"

                val anim = AnimationUtils.loadAnimation(this, R.anim.bounce)
                val interpolator =
                    BounceInterpolator(0.1, 20.0)
                anim.interpolator = interpolator
                videoSave.startAnimation(anim)

                firestore.collection("posts").document(data.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "saves" to FieldValue.increment(1),
                            "userSaved" to FieldValue.arrayUnion(uid)
                        )
                    )

            } else if (videoSave.tag == "Saved") {
                videoSave.setImageResource(R.drawable.ic_bookmark_24_regular)
                videoSave.tag = "NotSaved"

                val anim = AnimationUtils.loadAnimation(this, R.anim.bounce)
                val interpolator =
                    BounceInterpolator(0.1, 20.0)
                anim.interpolator = interpolator
                videoSave.startAnimation(anim)

                firestore.collection("posts").document(data.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "saves" to FieldValue.increment(-1),
                            "userSaved" to FieldValue.arrayRemove(uid)
                        )
                    )
            }
        }

        videoShare.setOnClickListener {
            ExtraFunctions.shareVideo(this)
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            videoView.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PLAYBACK_TIME, videoView.currentPosition)
    }

    private fun initializePlayer() {
        progressBar.visibility = VideoView.VISIBLE

        val videoUri: Uri = Uri.parse(data.url)
        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener {
            progressBar.visibility = VideoView.GONE
            if (currentPosition > 0) {
                videoView.seekTo(currentPosition)
            } else {
                videoView.seekTo(1)
            }

            // Start playing!
            videoView.start()
        }


        videoView.setOnCompletionListener {
            videoView.seekTo(0)
        }
    }

    private fun releasePlayer() {
        videoView.stopPlayback()
    }

}