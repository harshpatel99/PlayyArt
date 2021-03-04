package com.twogentle.wall.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.twogentle.wall.R
import com.twogentle.wall.adapter.MediaRecyclerAdapter

import com.twogentle.wall.extras.ExoPlayerRecyclerView
import com.twogentle.wall.model.Post
import org.json.JSONObject


class ExoPlayerFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var mRecyclerView: ExoPlayerRecyclerView
    private lateinit var mediaObjectList: ArrayList<Post>
    private var mAdapter: MediaRecyclerAdapter? = null
    private var firstTime = true

    private var itemPosition = 0
    private var interstitialAdPosition = 5
    private val adPosition = 15

    private var isAdPlayable = true
    private var isAdLoading = false
    private var isAdSkipped = false
    private lateinit var rewardedAd: RewardedAd
    private lateinit var adCallback: RewardedAdCallback
    private lateinit var adTimerTextView: TextView
    private lateinit var interstitialAd: InterstitialAd

    var fetchLimit: Long = 5
    var isScrolling = false
    var isLastItemReached = false
    lateinit var lastVisible: DocumentSnapshot

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video_vertical, container, false)

        mRecyclerView = view.findViewById(R.id.exoPlayerRecyclerView)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        adTimerTextView = view.findViewById(R.id.adTimerTextView)
        progressBar = view.findViewById(R.id.playyProgressBar)

        progressBar.visibility = View.VISIBLE

        interstitialAd = InterstitialAd(context)
        interstitialAd.adUnitId =
            getString(R.string.interstitial_video_unit_id)

        mediaObjectList = ArrayList()
        prepareVideoList()
        //set data object
        PagerSnapHelper().attachToRecyclerView(mRecyclerView)
        mRecyclerView.setMediaObjects(mediaObjectList)
        mAdapter = MediaRecyclerAdapter(context!!, mediaObjectList, initGlide())
        //Set Adapter
        mRecyclerView.adapter = mAdapter

        return view
    }

    private fun initGlide(): RequestManager {
        val options = RequestOptions()
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    override fun onStart() {
        if (Util.SDK_INT < 24)
            mRecyclerView.onPlayPlayer()
        super.onStart()
    }

    override fun onResume() {
        if (Util.SDK_INT > 24)
            mRecyclerView.onPlayPlayer()
        super.onResume()
    }

    override fun onPause() {
        if (Util.SDK_INT < 24)
            mRecyclerView.onPausePlayer()
        super.onPause()
    }

    override fun onStop() {
        if (Util.SDK_INT > 24)
            mRecyclerView.onPausePlayer()
        super.onStop()
    }

    override fun onDestroy() {
        mRecyclerView.releasePlayer()
        super.onDestroy()
    }

    private fun prepareVideoList() {

        val query = FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("contentType", "video")
            .orderBy("random", Query.Direction.DESCENDING)
            .limit(fetchLimit)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result!!.documents) {
                    val post = document.toObject(Post::class.java)
                    post!!.postType = Post.TYPE_VIDEO

                    if (post.url!!.isEmpty()) {
                        getInstaUrl(post, post.instagramUrl!!)
                    } else if (System.currentTimeMillis() - post.date!! > 86400000 && post.url!!.contains(
                            "instagram"
                        )
                    ) {
                        if (post.instagramUrl!!.isNotEmpty()) {
                            getInstaUrl(post, post.instagramUrl)
                        } else {
                            mediaObjectList.add(post)
                            mAdapter!!.notifyDataSetChanged()
                        }
                    } else {
                        mediaObjectList.add(post)
                        mAdapter!!.notifyDataSetChanged()
                    }

                    progressBar.visibility = View.GONE

                    /*mediaObjectList.add(post)
                    mAdapter!!.notifyDataSetChanged()*/
                }
                if (firstTime) {
                    Handler().postDelayed({
                        mRecyclerView.playVideo(false)
                    }, 100)
                    firstTime = false
                }

                if (it.result!!.size() > 0)
                    lastVisible = it.result.documents[it.result.size() - 1]

                mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisibleItemPosition =
                            linearLayoutManager.findFirstVisibleItemPosition()
                        val visibleItemCount = linearLayoutManager.childCount
                        val totalItemCount = linearLayoutManager.itemCount

                        itemPosition = linearLayoutManager.findFirstVisibleItemPosition()

                        if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                            isScrolling = false
                            val innerQuery = FirebaseFirestore.getInstance().collection("posts")
                                .whereEqualTo("contentType", "video")
                                .orderBy("random", Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(fetchLimit)

                            innerQuery.get().addOnCompleteListener { innerTask ->
                                if (innerTask.isSuccessful) {
                                    for (document in innerTask.result!!.documents) {
                                        val post = document.toObject(Post::class.java)
                                        post!!.postType = Post.TYPE_VIDEO

                                        if (post.url!!.isEmpty()) {
                                            getInstaUrl(post, post.instagramUrl!!)
                                        } else if (System.currentTimeMillis() - post.date!! > 86400000 && post.url!!.contains(
                                                "instagram"
                                            )
                                        ) {
                                            if (post.instagramUrl!!.isNotEmpty()) {
                                                getInstaUrl(post, post.instagramUrl)
                                            } else {
                                                mediaObjectList.add(post)
                                                mAdapter!!.notifyDataSetChanged()
                                            }
                                        } else {
                                            mediaObjectList.add(post)
                                            mAdapter!!.notifyDataSetChanged()
                                        }

                                        /*mediaObjectList.add(post)
                                        mAdapter!!.notifyDataSetChanged()*/
                                    }
                                    if (firstTime) {
                                        Handler().postDelayed({
                                            mRecyclerView.playVideo(false)
                                        }, 100)
                                        firstTime = false
                                    }

                                    if (innerTask.result!!.size() > 0)
                                        lastVisible =
                                            innerTask.result.documents[innerTask.result.size() - 1]
                                }
                            }
                        }
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            isScrolling = true
                        } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                            if (itemPosition % interstitialAdPosition == 1 && itemPosition % adPosition != 11) {
                                interstitialAd.loadAd(AdRequest.Builder().build())
                                interstitialAd.adListener = object : AdListener() {
                                    override fun onAdClosed() {
                                        super.onAdClosed()
                                        mRecyclerView.onPlayPlayer()
                                    }
                                }
                            }

                            if (itemPosition % adPosition == 11) {
                                rewardedAd =
                                    RewardedAd(
                                        activity,
                                        getString(R.string.video_unit_id)
                                    )

                                adCallback = object : RewardedAdCallback() {
                                    override fun onUserEarnedReward(p0: RewardItem) {

                                    }

                                    override fun onRewardedAdClosed() {
                                        rewardedAd = RewardedAd(
                                            activity,
                                            getString(R.string.video_unit_id)
                                        )
                                        rewardedAd.loadAd(
                                            AdRequest.Builder().build(),
                                            object : RewardedAdLoadCallback() {})
                                        mRecyclerView.onPlayPlayer()
                                    }
                                }
                                if (!isAdLoading) {
                                    isAdLoading = true
                                    rewardedAd.loadAd(
                                        AdRequest.Builder().build(),
                                        object : RewardedAdLoadCallback() {
                                            override fun onRewardedAdFailedToLoad(p0: LoadAdError?) {
                                                Toast.makeText(
                                                    context,
                                                    "Unable to load ad!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                isAdLoading = false
                                            }

                                            override fun onRewardedAdLoaded() {
                                                isAdLoading = false
                                            }
                                        })
                                }
                            }

                            if (itemPosition % adPosition == 0 && isAdPlayable && itemPosition != 0) {
                                adTimerTextView.visibility = View.VISIBLE
                                val t =
                                    getString(R.string.ad_in) + " 5s"
                                adTimerTextView.text = t
                                object : CountDownTimer(5000, 1000) {
                                    override fun onFinish() {
                                        if (rewardedAd.isLoaded && !isAdSkipped) {
                                            rewardedAd.show(activity, adCallback)
                                            isAdLoading = false
                                            mRecyclerView.onPausePlayer()
                                        }
                                        adTimerTextView.visibility = View.GONE
                                        isAdPlayable = false
                                        Handler().postDelayed({
                                            isAdPlayable = true
                                        }, 100000)
                                    }

                                    override fun onTick(p0: Long) {
                                        val time =
                                            getString(R.string.ad_in) + " " + p0 / 1000 + "s"
                                        adTimerTextView.text = time
                                    }
                                }.start()
                            }

                            if (itemPosition % interstitialAdPosition == 0 && itemPosition % adPosition != 0 && isAdPlayable && itemPosition != 0) {
                                adTimerTextView.visibility = View.VISIBLE
                                val t =
                                    getString(R.string.ad_in) + " 5s"
                                adTimerTextView.text = t
                                object : CountDownTimer(5000, 1000) {
                                    override fun onFinish() {
                                        if (interstitialAd.isLoaded && !isAdSkipped) {
                                            mRecyclerView.onPausePlayer()
                                            interstitialAd.show()
                                        }
                                        adTimerTextView.visibility = View.GONE
                                        isAdPlayable = false
                                        Handler().postDelayed({
                                            isAdPlayable = true
                                        }, 180000)
                                    }

                                    override fun onTick(p0: Long) {
                                        val time =
                                            getString(R.string.ad_in) + " " + p0 / 1000 + "s"
                                        adTimerTextView.text = time
                                    }
                                }.start()
                            }
                        }

                        /**if (itemPosition == 16 && !sp.getBoolean("isReviewSubmitted",false)) {
                        var reviewInfo: ReviewInfo
                        val manager = ReviewManagerFactory.create(this@MainActivity)
                        manager.requestReviewFlow().addOnCompleteListener { request ->
                        if (request.isSuccessful) {
                        reviewInfo = request.result
                        manager.launchReviewFlow(this@MainActivity, reviewInfo).addOnCompleteListener { _ ->
                        editor = sp.edit()
                        editor.putBoolean("isReviewSubmitted",true)
                        editor.apply()
                        }
                        }

                        }*/
                    }
                })
            }
        }
    }

    private fun getInstaUrl(post: Post, requestUrl: String) {

        val stringRequest = StringRequest("$requestUrl/?__a=1",
            {
                try {
                    val jsonObject = JSONObject(it)
                    val graphQLArray = jsonObject.getJSONObject("graphql")
                    val shortMediaArray = graphQLArray.getJSONObject("shortcode_media")
                    val displayUrl = shortMediaArray.get("display_url").toString()
                    val videoUrl = shortMediaArray.get("video_url").toString()

                    activity!!.runOnUiThread {

                        FirebaseFirestore.getInstance().collection("posts")
                            .document(post.id!!)
                            .update(
                                hashMapOf<String, Any>(
                                    "url" to videoUrl,
                                    "thumbnail" to displayUrl,
                                    "date" to System.currentTimeMillis()
                                )
                            )

                        post.url = videoUrl
                        post.thumbnail = displayUrl
                        mediaObjectList.add(post)
                        mAdapter!!.notifyDataSetChanged()
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }, {

            })

        if(context != null) {
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(stringRequest)
        }

    }

}