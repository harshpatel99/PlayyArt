package com.twogentle.wall.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.R
import com.twogentle.wall.activity.ArtistActivity
import com.twogentle.wall.extras.BounceInterpolator
import com.twogentle.wall.extras.ExtraFunctions
import com.twogentle.wall.model.Post
import kotlin.random.Random

class MediaRecyclerAdapter(
    private val context: Context,
    private val mediaObjects: ArrayList<Post>,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PlayerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_veritcal_video, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mediaObjects.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PlayerViewHolder).onBind(context,mediaObjects[position], requestManager)
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var mediaContainer: FrameLayout
        lateinit var mediaCoverImage: ImageView
        lateinit var progressBar: ProgressBar
        lateinit var requestManager: RequestManager
        lateinit var videoShare: ImageView
        lateinit var videoSave: ImageView
        private lateinit var userHandle: TextView
        private lateinit var parent: View

        fun onBind(
            context: Context,
            mediaObject: Post,
            requestManager :RequestManager
        ) {

            parent = itemView
            mediaContainer = itemView.findViewById(R.id.mediaContainer)
            mediaCoverImage = itemView.findViewById(R.id.ivMediaCoverImage)
            userHandle = itemView.findViewById(R.id.videoOwnerTextView)
            progressBar = itemView.findViewById(R.id.progressBar)
            this.requestManager = requestManager

            videoSave = itemView.findViewById(R.id.listItemTallVideoSave)
            videoShare = itemView.findViewById(R.id.listItemTallVideoShare)

            parent.tag = this
            userHandle.text = mediaObject.artistName
            requestManager
                .load(mediaObject.thumbnail)
                .into(mediaCoverImage)

            val firestore = FirebaseFirestore.getInstance()
            val uid = FirebaseAuth.getInstance().uid.toString()

            firestore.collection("posts").document(mediaObject.id!!)
                .update(
                    hashMapOf<String, Any>(
                        "random" to Random.nextLong(0,9999999)
                    )
                )

            if (mediaObject.userSaved!!.toString().contains(uid)) {
                videoSave.setImageResource(R.drawable.ic_bookmark_24_filled)
                videoSave.tag = "Saved"
            }

            videoSave.setOnClickListener {
                if (videoSave.tag == "NotSaved") {
                    videoSave.setImageResource(R.drawable.ic_bookmark_24_filled)
                    videoSave.tag = "Saved"

                    val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    val interpolator =
                        BounceInterpolator(0.1, 20.0)
                    anim.interpolator = interpolator
                    videoSave.startAnimation(anim)

                    firestore.collection("posts").document(mediaObject.id)
                        .update(
                            hashMapOf<String, Any>(
                                "saves" to FieldValue.increment(1),
                                "userSaved" to FieldValue.arrayUnion(uid)
                            )
                        )
                } else if (videoSave.tag == "Saved") {
                    videoSave.setImageResource(R.drawable.ic_bookmark_24_regular)
                    videoSave.tag = "NotSaved"

                    val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    val interpolator =
                        BounceInterpolator(0.1, 20.0)
                    anim.interpolator = interpolator
                    videoSave.startAnimation(anim)

                    firestore.collection("posts").document(mediaObject.id)
                        .update(
                            hashMapOf<String, Any>(
                                "saves" to FieldValue.increment(-1),
                                "userSaved" to FieldValue.arrayRemove(uid)
                            )
                        )
                }
            }

            videoShare.setOnClickListener {
                //playerWebView.pause()
                ExtraFunctions.shareVideo(context)
            }

            userHandle.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", mediaObject.artistName)
                intent.putExtra("artistID", mediaObject.artistID)
                intent.putExtra("artistUsername", mediaObject.artistUsername)
                intent.putExtra("artistBio", mediaObject.artistBio)
                intent.putExtra("artistProfilePic", mediaObject.artistProfilePic)
                intent.putExtra("artistSocial", mediaObject.artistSocial)
                context.startActivity(intent)
            }

        }
    }

}