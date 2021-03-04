package com.twogentle.wall.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.R
import com.twogentle.wall.activity.PostActivity
import com.twogentle.wall.activity.VideoActivity
import com.twogentle.wall.model.WallData
import kotlin.collections.ArrayList

class ArtistWallRecyclerAdapter(
    private val context: Context,
    private val activity: AppCompatActivity? = null,
    private val data: ArrayList<WallData>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_IMAGE = 1
        private const val TYPE_IMAGE_LOCKED = 2
        private const val TYPE_VIDEO = 3
        private const val TYPE_UNAVAILABLE = 99
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TITLE -> {
                TitleViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.list_item_wall_title, parent, false
                    )
                )
            }
            TYPE_IMAGE -> PostViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_wall_post, parent, false
                )
            )
            TYPE_IMAGE_LOCKED -> PostLockedViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_wall_post_locked, parent, false
                )
            )
            TYPE_VIDEO -> VideoViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_wall_video, parent, false
                )
            )
            else -> UnavailableViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_wall_unavailable, parent, false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_TITLE -> (holder as TitleViewHolder).bindViews(data[position])
            TYPE_IMAGE -> (holder as PostViewHolder).bindViews(context, activity!!, data[position])
            TYPE_IMAGE_LOCKED -> (holder as PostLockedViewHolder).bindViews(
                context,
                activity!!,
                data[position]
            )
            TYPE_VIDEO -> (holder as VideoViewHolder).bindViews(
                context,
                activity!!,
                data[position]
            )
            else -> (holder as UnavailableViewHolder).bindViews(
                context
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position].type) {
            WallData.TYPE_TITLE -> TYPE_TITLE
            WallData.TYPE_IMAGE -> TYPE_IMAGE
            WallData.TYPE_IMAGE_LOCKED -> TYPE_IMAGE_LOCKED
            WallData.TYPE_VIDEO -> TYPE_VIDEO
            else -> TYPE_UNAVAILABLE
        }
    }

    class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(data: WallData) {
            val titleTextView = itemView.findViewById<TextView>(R.id.listItemWallTitleTextView)
            titleTextView.text = data.id
        }
    }

    class UnavailableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context) {
            val imageView =
                itemView.findViewById<ImageView>(R.id.listItemUnavailableUpdateImageView)

            imageView.setOnClickListener {
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
        }
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context, activity: AppCompatActivity, data: WallData) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemWallVideoImageView)
            val playVideo = itemView.findViewById<ImageView>(R.id.wallPostLockedVideoFAB)

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageView.width, imageView.height)
            Glide.with(context)
                .load(data.post!!.thumbnail)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.loading_dots)
                .into(imageView)

            playVideo.setOnClickListener {

                val uid = FirebaseAuth.getInstance().currentUser!!.uid

                var isSaved = "NotSaved"

                data.post!!.userSaved!!.forEach {
                    if (it == uid) {
                        isSaved = "Saved"
                    }
                }

                val intent = Intent(context, VideoActivity::class.java)
                intent.putExtra("videoData", data.post)
                intent.putExtra("videoSaved", isSaved)
                activity.startActivity(intent)
            }
        }
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context, activity: AppCompatActivity, data: WallData) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemWallPostImageView)

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageView.width, imageView.height)
            Glide.with(context)
                .load(data.url)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.loading_dots)
                .into(imageView)

            imageView.setOnClickListener {

                val uid = FirebaseAuth.getInstance().currentUser!!.uid

                var isLiked = "NotLiked"
                var isSaved = "NotSaved"

                data.post!!.userLiked!!.forEach {
                    if (it == uid) {
                        isLiked = "Liked"
                    }
                }

                data.post!!.userSaved!!.forEach {
                    if (it == uid) {
                        isSaved = "Saved"
                    }
                }

                val intent = Intent(activity, PostActivity::class.java)
                intent.putExtra("postData", data.post)
                intent.putExtra("postLiked", isLiked)
                intent.putExtra("postSaved", isSaved)
                activity.startActivity(intent)
            }
        }
    }

    class PostLockedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context, activity: AppCompatActivity, data: WallData) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemWallPostImageView)
            val lockImageView = itemView.findViewById<ImageView>(R.id.listItemWallPostLockImageView)
            val unlockByTextView =
                itemView.findViewById<TextView>(R.id.listItemWallPostUnlockByTextView)

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageView.width, imageView.height)
            Glide.with(context)
                .load(data.url)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.loading_dots)
                .into(imageView)

            var isUnlocked = false

            val firestore = FirebaseFirestore.getInstance()
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            var rewardedAd = RewardedAd(context, context.getString(R.string.video_unit_id))
            rewardedAd.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {})

            val adCallback = object : RewardedAdCallback() {
                override fun onUserEarnedReward(p0: RewardItem) {
                    isUnlocked = true
                    firestore.collection("posts")
                        .document(data.id)
                        .update(
                            "userUnlocked",
                            FieldValue.arrayUnion(uid)
                        )
                    Toast.makeText(context, "Post Unlocked!", Toast.LENGTH_SHORT).show()
                    lockImageView.visibility = View.GONE
                    unlockByTextView.visibility = View.GONE

                }

                override fun onRewardedAdClosed() {
                    rewardedAd = RewardedAd(context, context.getString(R.string.video_unit_id))
                    rewardedAd.loadAd(
                        AdRequest.Builder().build(),
                        object : RewardedAdLoadCallback() {})
                }
            }

            imageView.setOnClickListener {
                if (isUnlocked) {
                    var isLiked = "NotLiked"
                    var isSaved = "NotSaved"

                    data.post!!.userLiked!!.forEach {
                        if (it == uid) {
                            isLiked = "Liked"
                        }
                    }

                    data.post!!.userSaved!!.forEach {
                        if (it == uid) {
                            isSaved = "Saved"
                        }
                    }

                    val intent = Intent(context, PostActivity::class.java)
                    intent.putExtra("postData", data.post)
                    intent.putExtra("postLiked", isLiked)
                    intent.putExtra("postSaved", isSaved)
                    context.startActivity(intent)
                } else {
                    if (rewardedAd.isLoaded)
                        rewardedAd.show(activity, adCallback)
                    else
                        Toast.makeText(context, "Ad is being loaded!", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
}