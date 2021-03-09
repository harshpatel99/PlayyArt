package com.twogentle.wall.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.R
import com.twogentle.wall.activity.PostActivity
import com.twogentle.wall.extras.UnifiedNativeAdViewHolder
import com.twogentle.wall.model.Post
import com.twogentle.wall.viewholders.LoadingViewHolder
import kotlin.collections.ArrayList

class PopularRecyclerAdapter(
    private val context: Context,
    private val activity: FragmentActivity,
    private val data: ArrayList<Any>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_POST = 1
        private const val TYPE_POST_LOCKED = 3
        private const val TYPE_LOADING = 5

        private const val TYPE_NATIVE_AD = 9
        private const val TYPE_UNAVAILABLE = 99
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TITLE -> {
                TitleViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.list_item_popular_title, parent, false
                    )
                )
            }
            TYPE_POST -> PostViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_popular_post, parent, false
                )
            )
            TYPE_POST_LOCKED -> PostLockedViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_popular_post_locked, parent, false
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
            TYPE_POST -> (holder as PostViewHolder).bindViews(context, data[position] as Post)
            TYPE_POST_LOCKED -> (holder as PostLockedViewHolder).bindViews(
                context,
                activity,
                data[position] as Post
            )
            TYPE_LOADING -> (holder as LoadingViewHolder).bindViews()
            /*TYPE_NATIVE_AD -> {
                val nativeAd = data[position] as UnifiedNativeAd
                populateNativeAdView(nativeAd, (holder as UnifiedNativeAdViewHolder).getAdView())
            }*/
            else -> (holder as UnavailableViewHolder).bindViews(context)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when ((data[position] as Post).postType) {
            Post.TYPE_TITLE -> TYPE_TITLE
            Post.TYPE_POST -> TYPE_POST
            Post.TYPE_POST_LOCKED -> TYPE_POST_LOCKED
            Post.TYPE_LOADING -> TYPE_LOADING
            else -> TYPE_UNAVAILABLE
        }
    }

    class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(data: Post) {
            val titleTextView = itemView.findViewById<TextView>(R.id.listItemPopularTitleTextView)
            titleTextView.text = data.title
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

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context, data: Post) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemPopularPostImage)
            /*val likeButton = itemView.findViewById<ImageView>(R.id.listItemPopularPostLike)
            val saveButton = itemView.findViewById<ImageView>(R.id.listItemPopularPostSave)
            val setAsButton = itemView.findViewById<ImageView>(R.id.listItemPopularPostSetAs)*/

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageView.width, imageView.height)
            Glide.with(context)
                .load(data.url)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.loading_dots)
                .into(imageView)

            /*val uid = FirebaseAuth.getInstance().currentUser!!.uid

            if (data.userLiked!!.toString().contains(uid)) {
                likeButton.setImageResource(R.drawable.ic_round_star_24)
                likeButton.tag = "Liked"
            }

            if (data.userSaved!!.toString().contains(uid)) {
                saveButton.setImageResource(R.drawable.ic_round_bookmark_24)
                saveButton.tag = "Saved"
            }

            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("posts").document(data.id!!)
                .update("views", FieldValue.increment(1))

            likeButton.setOnClickListener {

                if (likeButton.tag == "NotLiked") {
                    likeButton.setImageResource(R.drawable.ic_round_star_24)
                    likeButton.tag = "Liked"

                    val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    val interpolator =
                        BounceInterpolator(0.1, 20.0)
                    anim.interpolator = interpolator
                    likeButton.startAnimation(anim)

                    firestore.collection("posts").document(data.id)
                        .update("likes", FieldValue.increment(1))
                    firestore.collection("posts").document(data.id)
                        .update("userLiked", FieldValue.arrayUnion(uid))
                    firestore.collection("users").document(uid)
                        .update("likedCategories", FieldValue.arrayUnion(data.category))

                    data.likes = data.likes!! + 1

                } else if (likeButton.tag == "Liked") {
                    likeButton.setImageResource(R.drawable.ic_round_star_border_24)
                    likeButton.tag = "NotLiked"

                    firestore.collection("posts").document(data.id)
                        .update("likes", FieldValue.increment(-1))
                    firestore.collection("posts").document(data.id)
                        .update("userLiked", FieldValue.arrayRemove(uid))

                    data.likes = data.likes!! - 1
                }

            }

            saveButton.setOnClickListener {

                if (saveButton.tag == "NotSaved") {
                    saveButton.setImageResource(R.drawable.ic_round_bookmark_24)
                    saveButton.tag = "Saved"

                    val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    val interpolator =
                        BounceInterpolator(0.1, 20.0)
                    anim.interpolator = interpolator
                    saveButton.startAnimation(anim)

                    firestore.collection("posts").document(data.id)
                        .update("saves", FieldValue.increment(1))
                    firestore.collection("posts").document(data.id)
                        .update("userSaved", FieldValue.arrayUnion(uid))
                    firestore.collection("users").document(uid)
                        .update("likedCategories", FieldValue.arrayUnion(data.category))

                    data.saves = data.saves!! + 1

                } else if (saveButton.tag == "Saved") {
                    saveButton.setImageResource(R.drawable.ic_round_bookmark_border_24)
                    saveButton.tag = "NotSaved"

                    firestore.collection("posts").document(data.id)
                        .update("saves", FieldValue.increment(-1))
                    firestore.collection("posts").document(data.id)
                        .update("userSaved", FieldValue.arrayRemove(uid))

                    data.likes = data.likes!! - 1
                }

            }

            setAsButton.setOnClickListener {
                firestore.collection("posts").document(data.id)
                    .update("shares", FieldValue.increment(1))
                val linkUrl = "https://play.google.com/store/apps/details?id=" + context.packageName
                val message = context.getString(R.string.share_post_message) + linkUrl
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, message)
                context.startActivity(intent)
            }*/

            imageView.setOnClickListener {
                val intent = Intent(context, PostActivity::class.java)
                intent.putExtra("postData", data)
                intent.putExtra("postLiked", "na")
                intent.putExtra("postSaved", "na")
                context.startActivity(intent)
            }
        }
    }

    class PostLockedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var rewardedAd: RewardedAd

        fun bindViews(
            context: Context,
            activity: FragmentActivity,
            postData: Post
        ) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemPopularPostLockedImage)
            val unlockByTextView =
                itemView.findViewById<TextView>(R.id.listItemWallPostUnlockByTextView)
            val lockImageView = itemView.findViewById<ImageView>(R.id.listItemWallPostLockImageView)

            val firestore = FirebaseFirestore.getInstance()

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

                Handler(Looper.getMainLooper()).postDelayed({
                    if (dialog.isShowing) {
                        dialog.dismiss()
                        Toast.makeText(
                            context,
                            "Loading Timeout! Please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }, 9000)

                val fullScreenContent = object : FullScreenContentCallback() {}

                RewardedAd.load(context,
                    context.getString(R.string.video_unit_id),
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(p0: RewardedAd) {
                            super.onAdLoaded(p0)
                            rewardedAd = p0
                            rewardedAd.fullScreenContentCallback = fullScreenContent

                            rewardedAd.show(activity) {
                                firestore.collection("posts")
                                    .document(postData.id!!)
                                    .update(
                                        "userUnlocked",
                                        FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser!!.uid)
                                    )
                                Toast.makeText(context, "Post Unlocked!", Toast.LENGTH_SHORT).show()
                                unlockByTextView.visibility = View.GONE
                                lockImageView.visibility = View.GONE

                                imageView.setOnClickListener {
                                    val intent = Intent(context, PostActivity::class.java)
                                    intent.putExtra("postData", postData)
                                    intent.putExtra("postLiked", "na")
                                    intent.putExtra("postSaved", "na")
                                    context.startActivity(intent)
                                }
                            }
                            dialog.dismiss()
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            Toast.makeText(
                                context,
                                "Unable to load ad! Please try again later.",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }

                    }
                )

            }

        }
    }

    /*private fun populateNativeAdView(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
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
    }*/
}