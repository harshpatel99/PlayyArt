package com.twogentle.wall.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.MainActivity
import com.twogentle.wall.extras.BounceInterpolator
import com.twogentle.wall.R
import com.twogentle.wall.activity.ArtistActivity
import com.twogentle.wall.activity.PostActivity
import com.twogentle.wall.activity.VideoActivity
import com.twogentle.wall.extras.ExtraFunctions
import com.twogentle.wall.extras.UnifiedNativeAdViewHolder
import com.twogentle.wall.model.Post
import com.twogentle.wall.model.User
import com.twogentle.wall.viewholders.LoadingViewHolder
import kotlin.collections.ArrayList

class HomeRecyclerAdapter(
    private val context: Context,
    private val activity: AppCompatActivity,
    private val data: ArrayList<Any>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_POST = 1
        private const val TYPE_POST_LOCKED = 3
        private const val TYPE_VIDEO = 4
        private const val TYPE_LOADING = 5

        private const val TYPE_NATIVE_AD = 9
        private const val TYPE_UNAVAILABLE = 99

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TITLE -> {
                TitleViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.list_item_home_title, parent, false
                    )
                )
            }
            TYPE_POST -> PostViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_home_post, parent, false
                )
            )
            TYPE_POST_LOCKED -> PostLockedViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_home_post_locked, parent, false
                )
            )
            TYPE_VIDEO -> VideoViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_home_video, parent, false
                )
            )
            TYPE_LOADING -> LoadingViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_loading, parent, false
                )
            )
            TYPE_NATIVE_AD -> UnifiedNativeAdViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.unified_native_ad, parent, false
                )
            )
            else -> UnavailableViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_unavailable, parent, false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_TITLE -> (holder as TitleViewHolder).bindViews(data[position] as Post)
            TYPE_POST -> (holder as PostViewHolder).bindViews(
                context,
                activity,
                data[position] as Post
            )
            TYPE_POST_LOCKED -> (holder as PostLockedViewHolder).bindViews(
                context,
                activity,
                data[position] as Post
            )
            TYPE_VIDEO -> (holder as VideoViewHolder).bindViews(
                context,
                activity as MainActivity,
                data[position] as Post
            )
            TYPE_LOADING -> (holder as LoadingViewHolder).bindViews()
            TYPE_NATIVE_AD -> {
                val nativeAd = data[position] as UnifiedNativeAd
                populateNativeAdView(nativeAd, (holder as UnifiedNativeAdViewHolder).getAdView())
            }
            else -> (holder as UnavailableViewHolder).bindViews(context)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is UnifiedNativeAd)
            TYPE_NATIVE_AD
        else
            when ((data[position] as Post).postType) {
                Post.TYPE_TITLE -> TYPE_TITLE
                Post.TYPE_POST -> TYPE_POST
                Post.TYPE_POST_LOCKED -> TYPE_POST_LOCKED
                Post.TYPE_VIDEO -> TYPE_VIDEO
                Post.TYPE_LOADING -> TYPE_LOADING
                Post.TYPE_NATIVE_AD -> TYPE_NATIVE_AD
                else -> TYPE_UNAVAILABLE
            }
    }

    class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(data: Post) {
            val titleTextView = itemView.findViewById<TextView>(R.id.listItemHomeTitleTextView)
            titleTextView.text = data.title
        }
    }

    class UnavailableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context) {
            val imageView =
                itemView.findViewById<ImageView>(R.id.listItemUnavailableUpdateImageView)

            imageView.setOnClickListener {
                ExtraFunctions.openPlayStorePage(context)
            }
        }
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindViews(
            context: Context,
            activity: MainActivity,
            data: Post
        ) {
            val parent = itemView.findViewById<ConstraintLayout>(R.id.listItemHomeParent)
            val thumbnailImageView =
                itemView.findViewById<ImageView>(R.id.listItemHomeVideoPlayerThumbnail)

            val profilePictureCard = itemView.findViewById<MaterialCardView>(R.id.listItemHomePostPictureCard)
            val profilePictureImageView = itemView.findViewById<ImageView>(R.id.artistProfilePictureImageView)
            val profileNameTextView = itemView.findViewById<TextView>(R.id.listItemHomePostProfileNameTextView)
            val videoPlay = itemView.findViewById<ImageView>(R.id.listItemHomeVideoPlayImageView)
            val videoSave = itemView.findViewById<ImageView>(R.id.listItemHomeVideoSave)
            val videoShare = itemView.findViewById<ImageView>(R.id.listItemHomeVideoShare)

            profileNameTextView.text = data.artistName
            val requestOption = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(profilePictureImageView.width, profilePictureImageView.height)
            Glide.with(context)
                .load(data.artistProfilePic)
                .apply(requestOption)
                .placeholder(R.drawable.loading_dots)
                .into(profilePictureImageView)

            profilePictureCard.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", data.artistName)
                intent.putExtra("artistID", data.artistID)
                intent.putExtra("artistUsername", data.artistUsername)
                intent.putExtra("artistBio", data.artistBio)
                intent.putExtra("artistProfilePicUrl", data.artistProfilePic)
                intent.putExtra("artistSocial", data.artistSocial)
                activity.startActivity(intent)
            }

            profileNameTextView.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", data.artistName)
                intent.putExtra("artistID", data.artistID)
                intent.putExtra("artistUsername", data.artistUsername)
                intent.putExtra("artistBio", data.artistBio)
                intent.putExtra("artistProfilePic", data.artistProfilePic)
                intent.putExtra("artistSocial", data.artistSocial)
                activity.startActivity(intent)
            }

            videoPlay.setOnClickListener {
                playVideoInDialog(context, activity, data, parent, videoSave)
            }

            thumbnailImageView.setOnClickListener {
                playVideoInDialog(context, activity, data, parent, videoSave)
            }

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(thumbnailImageView.width, thumbnailImageView.height)
            Glide.with(context)
                .load(data.thumbnail)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.loading_dots)
                .into(thumbnailImageView)

            thumbnailImageView.setOnClickListener {
                val intent = Intent(context, VideoActivity::class.java)
                intent.putExtra("videoData", data)
                intent.putExtra("videoSaved", videoSave.tag.toString())
                activity.startActivity(intent)
            }

            videoPlay.setOnClickListener {
                val intent = Intent(context, VideoActivity::class.java)
                intent.putExtra("videoData", data)
                intent.putExtra("videoSaved", videoSave.tag.toString())
                activity.startActivity(intent)
            }

            val firestore = FirebaseFirestore.getInstance()
            val uid = FirebaseAuth.getInstance().uid.toString()

            if (data.userSaved!!.toString().contains(uid)) {
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

                    val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
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
                ExtraFunctions.shareVideo(context)
            }
        }

        private fun playVideoInDialog(
            context: Context,
            activity: MainActivity,
            data: Post,
            parent: ConstraintLayout,
            videoSave: ImageView
        ) {
            val builder = AlertDialog.Builder(activity)
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.layout_video, parent, false)

            val videoView = dialogView.findViewById<VideoView>(R.id.layoutVideoPlayerView)
            val progressBar = dialogView.findViewById<ProgressBar>(R.id.layoutVideoProgressBar)
            val infoView = dialogView.findViewById<ImageView>(R.id.layoutVideoInfo)

            val profilePictureCard = itemView.findViewById<MaterialCardView>(R.id.listItemHomePostPictureCard)
            val profileNameTextView = itemView.findViewById<TextView>(R.id.listItemHomePostProfileNameTextView)

            profileNameTextView.text = data.artistName

            profilePictureCard.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", data.artistName)
                intent.putExtra("artistID", data.artistID)
                intent.putExtra("artistUsername", data.artistUsername)
                intent.putExtra("artistBio", data.artistBio)
                intent.putExtra("artistProfilePic", data.artistProfilePic)
                intent.putExtra("artistSocial", data.artistSocial)
                activity.startActivity(intent)
            }

            profileNameTextView.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", data.artistName)
                intent.putExtra("artistID", data.artistID)
                intent.putExtra("artistUsername", data.artistUsername)
                intent.putExtra("artistBio", data.artistBio)
                intent.putExtra("artistProfilePic", data.artistProfilePic)
                intent.putExtra("artistSocial", data.artistSocial)
                activity.startActivity(intent)
            }

            val videoUri: Uri = Uri.parse(data.url)
            videoView.setVideoURI(videoUri)
            videoView.start()

            val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val isUnlocked = sharedPreferences.getInt("subscription", User.USER_FREE)

            if (isUnlocked != User.USER_SUBSCRIBED_ARTS || isUnlocked != User.USER_SUBSCRIBED_FULL) {
                val bannerAdView = dialogView.findViewById<AdView>(R.id.layoutVideoBannerAdView)
                MobileAds.initialize(context) {}
                val adRequest = AdRequest.Builder().build()
                bannerAdView.loadAd(adRequest)
            }

            videoView.setOnPreparedListener {
                progressBar.visibility = VideoView.GONE
                videoView.seekTo(1)
                videoView.start()
            }
            videoView.setOnCompletionListener {
                videoView.seekTo(0)
                videoView.start()
            }

            videoView.setOnPreparedListener {
                it.setOnInfoListener { _, what, _ ->
                    when (what) {
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
            videoView.setZOrderOnTop(true)

            builder.setView(dialogView)
            builder.setOnCancelListener {
                videoView.stopPlayback()
            }
            val dialog = builder.show()
            val window = dialog.window
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window.setDimAmount(0.8f)
            }
            dialog.setOnCancelListener {
                window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }

            infoView.setOnClickListener {
                val intent = Intent(context, VideoActivity::class.java)
                intent.putExtra("videoData", data)
                intent.putExtra("videoSaved", videoSave.tag.toString())
                activity.startActivity(intent)
                dialog.dismiss()
            }

        }
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context, activity: FragmentActivity, data: Post) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemHomePostImage)
            val likeButton = itemView.findViewById<ImageView>(R.id.listItemHomePostLike)
            val saveButton = itemView.findViewById<ImageView>(R.id.listItemHomePostSave)
            val shareButton = itemView.findViewById<ImageView>(R.id.listItemHomePostSetAs)

            val profilePictureCard = itemView.findViewById<MaterialCardView>(R.id.listItemHomePostPictureCard)
            val profileNameTextView = itemView.findViewById<TextView>(R.id.listItemHomePostProfileNameTextView)
            val profilePictureImageView = itemView.findViewById<ImageView>(R.id.artistProfilePictureImageView)

            profileNameTextView.text = data.artistName

            val requestOption = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(profilePictureImageView.width, profilePictureImageView.height)
            Glide.with(context)
                .load(data.artistProfilePic)
                .apply(requestOption)
                .placeholder(R.drawable.loading_dots)
                .into(profilePictureImageView)

            profilePictureCard.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", data.artistName)
                intent.putExtra("artistID", data.artistID)
                intent.putExtra("artistUsername", data.artistUsername)
                intent.putExtra("artistBio", data.artistBio)
                intent.putExtra("artistProfilePic", data.artistProfilePic)
                intent.putExtra("artistSocial", data.artistSocial)
                activity.startActivity(intent)
            }

            profileNameTextView.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", data.artistName)
                intent.putExtra("artistID", data.artistID)
                intent.putExtra("artistUsername", data.artistUsername)
                intent.putExtra("artistBio", data.artistBio)
                intent.putExtra("artistProfilePic", data.artistProfilePic)
                intent.putExtra("artistSocial", data.artistSocial)
                activity.startActivity(intent)
            }


            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageView.width, imageView.height)
            Glide.with(context)
                .load(data.url)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.loading_dots)
                .into(imageView)

            val uid = FirebaseAuth.getInstance().currentUser!!.uid

            checkLikeAndSave(likeButton, saveButton, data, uid)

            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("posts").document(data.id!!)
                .update("views", FieldValue.increment(1))

            likeButton.setOnClickListener {
                likePost(context, likeButton, firestore, data, uid)
            }

            saveButton.setOnClickListener {
                savePost(context, saveButton, firestore, data, uid)
            }

            shareButton.setOnClickListener {
                firestore.collection("posts").document(data.id)
                    .update("shares", FieldValue.increment(1))
                ExtraFunctions.sharePost(activity)
            }

            imageView.setOnClickListener {
                openPost(context, data, likeButton, saveButton)
            }

        }

        private fun checkLikeAndSave(
            likeButton: ImageView,
            saveButton: ImageView,
            data: Post,
            uid: String
        ) {
            if (data.userLiked!!.toString().contains(uid)) {
                likeButton.setImageResource(R.drawable.ic_heart_24_filled)
                likeButton.tag = "Liked"
            }

            if (data.userSaved!!.toString().contains(uid)) {
                saveButton.setImageResource(R.drawable.ic_bookmark_24_filled)
                saveButton.tag = "Saved"
            }
        }

        private fun likePost(
            context: Context,
            likeButton: ImageView,
            firestore: FirebaseFirestore,
            data: Post,
            uid: String
        ) {
            if (likeButton.tag == "NotLiked") {
                likeButton.setImageResource(R.drawable.ic_heart_24_filled)
                likeButton.tag = "Liked"

                val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                val interpolator =
                    BounceInterpolator(0.1, 20.0)
                anim.interpolator = interpolator
                likeButton.startAnimation(anim)

                firestore.collection("posts").document(data.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "likes" to FieldValue.increment(1),
                            "userLiked" to FieldValue.arrayUnion(uid)
                        )
                    )
                firestore.collection("users").document(uid)
                    .update("likedCategories", FieldValue.arrayUnion(data.category))

                data.likes = data.likes!! + 1

            } else if (likeButton.tag == "Liked") {
                likeButton.setImageResource(R.drawable.ic_heart_24_regular)
                likeButton.tag = "NotLiked"

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

        private fun savePost(
            context: Context,
            saveButton: ImageView,
            firestore: FirebaseFirestore,
            data: Post,
            uid: String
        ) {
            if (saveButton.tag == "NotSaved") {
                saveButton.setImageResource(R.drawable.ic_bookmark_24_filled)
                saveButton.tag = "Saved"

                val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                val interpolator =
                    BounceInterpolator(0.1, 20.0)
                anim.interpolator = interpolator
                saveButton.startAnimation(anim)

                firestore.collection("posts").document(data.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "saves" to FieldValue.increment(1),
                            "userSaved" to FieldValue.arrayUnion(uid)
                        )
                    )
                firestore.collection("users").document(uid)
                    .update("likedCategories", FieldValue.arrayUnion(data.category))

                data.saves = data.saves!! + 1

            } else if (saveButton.tag == "Saved") {
                saveButton.setImageResource(R.drawable.ic_bookmark_24_regular)
                saveButton.tag = "NotSaved"

                firestore.collection("posts").document(data.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "saves" to FieldValue.increment(-1),
                            "userSaved" to FieldValue.arrayRemove(uid)
                        )
                    )

                data.likes = data.likes!! - 1
            }
        }

        private fun openPost(
            context: Context,
            data: Post,
            likeButton: ImageView,
            saveButton: ImageView
        ) {
            val intent = Intent(context, PostActivity::class.java)
            intent.putExtra("postData", data)
            intent.putExtra("postLiked", likeButton.tag.toString())
            intent.putExtra("postSaved", saveButton.tag.toString())
            context.startActivity(intent)
        }

    }

    class PostLockedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(
            context: Context,
            activity: FragmentActivity,
            postData: Post
        ) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemHomePostLockedImage)
            val unlockPostCard = itemView.findViewById<MaterialCardView>(R.id.listItemHomeUnlockPostLockedCard)
            val videoFab = itemView.findViewById<TextView>(R.id.homePostLockedVideoTextView)
            val unlockTextView = itemView.findViewById<TextView>(R.id.homePostLockedUnlockText)
            val saveButton = itemView.findViewById<ImageView>(R.id.listItemHomePostLockedSave)
            val likeButton = itemView.findViewById<ImageView>(R.id.listItemHomePostLockedLike)
            val shareButton = itemView.findViewById<ImageView>(R.id.listItemHomePostLockedSetAs)

            val profilePictureCard = itemView.findViewById<MaterialCardView>(R.id.listItemHomePostPictureCard)
            val profileNameTextView = itemView.findViewById<TextView>(R.id.listItemHomePostProfileNameTextView)
            val profilePictureImageView = itemView.findViewById<ImageView>(R.id.artistProfilePictureImageView)

            val requestOption = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(profilePictureImageView.width, profilePictureImageView.height)
            Glide.with(context)
                .load(postData.artistProfilePic)
                .apply(requestOption)
                .placeholder(R.drawable.loading_dots)
                .into(profilePictureImageView)

            profileNameTextView.text = postData.artistName

            profilePictureCard.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", postData.artistName)
                intent.putExtra("artistID", postData.artistID)
                intent.putExtra("artistUsername", postData.artistUsername)
                intent.putExtra("artistBio", postData.artistBio)
                intent.putExtra("artistProfilePic", postData.artistProfilePic)
                intent.putExtra("artistSocial", postData.artistSocial)
                activity.startActivity(intent)
            }

            profileNameTextView.setOnClickListener {
                val intent = Intent(context, ArtistActivity::class.java)
                intent.putExtra("artistName", postData.artistName)
                intent.putExtra("artistID", postData.artistID)
                intent.putExtra("artistUsername", postData.artistUsername)
                intent.putExtra("artistBio", postData.artistBio)
                intent.putExtra("artistProfilePic", postData.artistProfilePic)
                intent.putExtra("artistSocial", postData.artistSocial)
                activity.startActivity(intent)
            }

            var rewardedAd = RewardedAd(context, context.getString(R.string.video_unit_id))
            val firestore = FirebaseFirestore.getInstance()

            val uid = FirebaseAuth.getInstance().uid.toString()

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageView.width, imageView.height)
            Glide.with(context)
                .load(postData.url)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.loading_dots)
                .into(imageView)

            imageView.setOnClickListener {

                val builder = AlertDialog.Builder(context)
                builder.setCancelable(false)
                builder.setView(R.layout.progress_dialog_loading_ad)
                val dialog = builder.create()
                dialog.show()

                Handler().postDelayed({
                    if (dialog.isShowing) {
                        dialog.dismiss()
                        Toast.makeText(
                            context,
                            "Loading Timeout! Please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }, 9000)

                val adCallback = object : RewardedAdCallback() {
                    override fun onUserEarnedReward(p0: RewardItem) {

                        firestore.collection("posts")
                            .document(postData.id!!)
                            .update(
                                "userUnlocked",
                                FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser!!.uid)
                            )
                        Toast.makeText(context, "Post Unlocked!", Toast.LENGTH_SHORT).show()
                        unlockTextView.visibility = View.GONE

                        imageView.setOnClickListener {
                            val intent = Intent(activity, PostActivity::class.java)
                            intent.putExtra("postData", postData)
                            intent.putExtra("postLiked", likeButton.tag.toString())
                            intent.putExtra("postSaved", saveButton.tag.toString())
                            activity.startActivity(intent)
                        }
                    }
                }

                rewardedAd.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
                    override fun onRewardedAdFailedToLoad(p0: LoadAdError?) {
                        Toast.makeText(
                            context,
                            "Unable to load ad! Please try again later.",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }

                    override fun onRewardedAdLoaded() {
                        rewardedAd.show(activity, adCallback)
                        dialog.dismiss()
                    }
                })
            }

            shareButton.setOnClickListener {
                firestore.collection("posts").document(postData.id!!)
                    .update("shares", FieldValue.increment(1))
                ExtraFunctions.sharePost(activity)
            }

            likeButton.setOnClickListener {

                if (likeButton.tag == "NotLiked") {
                    likeButton.setImageResource(R.drawable.ic_heart_24_filled)
                    likeButton.tag = "Liked"

                    val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    val interpolator =
                        BounceInterpolator(0.1, 20.0)
                    anim.interpolator = interpolator
                    likeButton.startAnimation(anim)

                    firestore.collection("posts").document(postData.id!!)
                        .update(
                            hashMapOf<String, Any>(
                                "likes" to FieldValue.increment(1),
                                "userLiked" to FieldValue.arrayUnion(uid)
                            )
                        )
                    firestore.collection("users").document(uid)
                        .update("likedCategories", FieldValue.arrayUnion(postData.category))

                    postData.likes = postData.likes!! + 1

                } else if (likeButton.tag == "Liked") {
                    likeButton.setImageResource(R.drawable.ic_heart_24_regular)
                    likeButton.tag = "NotLiked"

                    firestore.collection("posts").document(postData.id!!)
                        .update(
                            hashMapOf<String, Any>(
                                "likes" to FieldValue.increment(-1),
                                "userLiked" to FieldValue.arrayRemove(uid)
                            )
                        )

                    postData.likes = postData.likes!! - 1
                }

            }

            saveButton.setOnClickListener {

                if (saveButton.tag == "NotSaved") {
                    saveButton.setImageResource(R.drawable.ic_bookmark_24_filled)
                    saveButton.tag = "Saved"

                    val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    val interpolator =
                        BounceInterpolator(0.1, 20.0)
                    anim.interpolator = interpolator
                    saveButton.startAnimation(anim)

                    firestore.collection("posts").document(postData.id!!)
                        .update(
                            hashMapOf<String, Any>(
                                "saves" to FieldValue.increment(1),
                                "userSaved" to FieldValue.arrayUnion(uid)
                            )
                        )
                    firestore.collection("users").document(uid)
                        .update("likedCategories", FieldValue.arrayUnion(postData.category))

                    postData.saves = postData.saves!! + 1

                } else if (saveButton.tag == "Saved") {
                    saveButton.setImageResource(R.drawable.ic_bookmark_24_regular)
                    saveButton.tag = "NotSaved"

                    firestore.collection("posts").document(postData.id!!)
                        .update(
                            hashMapOf<String, Any>(
                                "saves" to FieldValue.increment(-1),
                                "userSaved" to FieldValue.arrayRemove(uid)
                            )
                        )

                    postData.likes = postData.likes!! - 1
                }

            }

            videoFab.setOnClickListener {

                val builder = AlertDialog.Builder(context)
                builder.setCancelable(false)
                builder.setView(R.layout.progress_dialog_loading_ad)
                val dialog = builder.create()
                dialog.show()

                Handler().postDelayed({
                    if (dialog.isShowing) {
                        dialog.dismiss()
                        Toast.makeText(
                            context,
                            "Loading Timeout! Please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }, 9000)

                val adCallback = object : RewardedAdCallback() {
                    override fun onUserEarnedReward(p0: RewardItem) {

                        firestore.collection("posts")
                            .document(postData.id!!)
                            .update(
                                "userUnlocked",
                                FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser!!.uid)
                            )

                        Toast.makeText(context, "Post Unlocked!", Toast.LENGTH_SHORT).show()
                        saveButton.visibility = View.VISIBLE
                        likeButton.visibility = View.VISIBLE
                        videoFab.visibility = View.GONE
                        unlockPostCard.visibility = View.GONE
                        unlockTextView.visibility = View.GONE

                        imageView.setOnClickListener {
                            val intent = Intent(activity, PostActivity::class.java)
                            intent.putExtra("postData", postData)
                            intent.putExtra("postLiked", likeButton.tag.toString())
                            intent.putExtra("postSaved", saveButton.tag.toString())
                            activity.startActivity(intent)
                        }
                    }

                    override fun onRewardedAdClosed() {
                        rewardedAd = RewardedAd(context, context.getString(R.string.video_unit_id))
                        rewardedAd.loadAd(
                            AdRequest.Builder().build(),
                            object : RewardedAdLoadCallback() {})
                    }
                }

                rewardedAd.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
                    override fun onRewardedAdFailedToLoad(p0: LoadAdError?) {
                        Toast.makeText(
                            context,
                            "Unable to load ad! Please try again later.",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }

                    override fun onRewardedAdLoaded() {
                        rewardedAd.show(activity, adCallback)
                        dialog.dismiss()
                    }
                })
            }

        }

    }

    private fun populateNativeAdView(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction

        val icon = nativeAd.icon
        if (icon == null)
            adView.iconView.visibility = View.INVISIBLE
        else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null)
            adView.priceView.visibility = View.INVISIBLE
        else {
            (adView.priceView as TextView).text = nativeAd.price
            adView.priceView.visibility = View.VISIBLE
        }

        if (nativeAd.store == null)
            adView.storeView.visibility = View.INVISIBLE
        else {
            (adView.storeView as TextView).text = nativeAd.store
            adView.storeView.visibility = View.VISIBLE
        }

        if (nativeAd.starRating == null)
            adView.starRatingView.visibility = View.INVISIBLE
        else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null)
            adView.advertiserView.visibility = View.INVISIBLE
        else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }

}