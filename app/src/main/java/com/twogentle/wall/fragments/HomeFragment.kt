package com.twogentle.wall.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.twogentle.wall.MainActivity
import com.twogentle.wall.R
import com.twogentle.wall.activity.SelectCollectionActivity
import com.twogentle.wall.adapter.HomeCollectionsRecyclerAdapter
import com.twogentle.wall.adapter.HomeRecyclerAdapter
import com.twogentle.wall.model.CollectionsData
import com.twogentle.wall.model.Post
import com.twogentle.wall.model.User

class HomeFragment : Fragment() {

    lateinit var adapter: HomeRecyclerAdapter
    val data = ArrayList<Any>()
    private val categoryData = ArrayList<CollectionsData>()
    var fetchLimit: Long = 8
    var isLastItemReached = false
    lateinit var lastVisible: DocumentSnapshot

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val sp = context!!.getSharedPreferences("pref", MODE_PRIVATE)
        val selColArray = sp.getString("selectedCollections", "")

        if (selColArray!!.isEmpty()) {
            startActivity(Intent(activity, SelectCollectionActivity::class.java))
            activity!!.finish()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.homeRecyclerView)
        val nestedScrollView = view.findViewById<NestedScrollView>(R.id.homeNestedScrollView)
        shimmerFrameLayout = view.findViewById(R.id.homeShimmerLayout)

        addCategories(view)

        val sharedPreferences = activity!!.getSharedPreferences("settings", MODE_PRIVATE)
        val isUnlocked = sharedPreferences.getInt("subscription", User.USER_FREE)

        if (isUnlocked != User.USER_SUBSCRIBED_ARTS || isUnlocked != User.USER_SUBSCRIBED_FULL) {
            val bannerAdView = view.findViewById<AdView>(R.id.homeBannerAdView)

            MobileAds.initialize(context) {}
            val adRequest = AdRequest.Builder().build()
            bannerAdView.loadAd(adRequest)
        }

        val query = FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("contentType", "image")
            .orderBy("datePosted", Query.Direction.DESCENDING)
            .limit(fetchLimit)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val uid = FirebaseAuth.getInstance().uid.toString()
                for (document in it.result!!.documents) {
                    val post = document.toObject(Post::class.java)
                    var postType = Post.TYPE_POST

                    if (post!!.contentType == "image") {
                        if (post.type!!.compareTo("free") == 0) {
                            postType = Post.TYPE_POST
                        } else if (post.type.compareTo("premium") == 0) {
                            postType = Post.TYPE_POST_LOCKED
                            if (post.userUnlocked!!.toString().contains(uid)) {
                                postType = Post.TYPE_POST
                            }
                        }
                    } else if (post.contentType == "video")
                        postType = Post.TYPE_VIDEO
                    else
                        postType = Post.TYPE_UNAVAILABLE

                    post.postType = postType
                    data.add(post)
                }
                data.add(Post(Post.TYPE_LOADING))
                adapter.notifyDataSetChanged()
                shimmerFrameLayout.stopShimmerAnimation()
                shimmerFrameLayout.visibility = View.GONE
                lastVisible = it.result.documents[it.result.size() - 1]
                nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
                    if(v.getChildAt(v.childCount - 1) != null){
                        if((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight)) && scrollY > oldScrollY){
                            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val visibleItemCount = linearLayoutManager.childCount
                            val totalItemCount = linearLayoutManager.itemCount
                            val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()

                            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount){
                                val innerQuery = FirebaseFirestore.getInstance().collection("posts")
                                    .whereEqualTo("contentType", "image")
                                    .orderBy("datePosted", Query.Direction.DESCENDING)
                                    .startAfter(lastVisible)
                                    .limit(fetchLimit)
                                innerQuery.get().addOnCompleteListener { innerTask ->
                                    if (innerTask.isSuccessful) {
                                        data.removeAt(data.size - 1)
                                        for (document in innerTask.result!!.documents) {
                                            val post = document.toObject(Post::class.java)
                                            var postType = Post.TYPE_POST

                                            if (post!!.contentType == "image") {
                                                if (post.type!!.compareTo("free") == 0) {
                                                    postType = Post.TYPE_POST
                                                } else if (post.type.compareTo("premium") == 0) {
                                                    postType = Post.TYPE_POST_LOCKED
                                                    if (post.userUnlocked!!.toString()
                                                            .contains(uid)
                                                    ) {
                                                        postType = Post.TYPE_POST
                                                    }
                                                }
                                            } else if (post.contentType == "video")
                                                postType = Post.TYPE_VIDEO
                                            else
                                                postType = Post.TYPE_UNAVAILABLE

                                            post.postType = postType
                                            data.add(post)
                                        }

                                        data.add(Post(Post.TYPE_LOADING))
                                        adapter.notifyDataSetChanged()

                                        if (innerTask.result.size() != 0)
                                            lastVisible =
                                                innerTask.result.documents[innerTask.result.size() - 1]
                                        else
                                            isLastItemReached = true
                                        if (innerTask.result.size() < fetchLimit) {
                                            isLastItemReached = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
                /*nestedScrollView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisibleItemPosition =
                            linearLayoutManager.findFirstVisibleItemPosition()
                        val visibleItemCount = linearLayoutManager.childCount
                        val totalItemCount = linearLayoutManager.itemCount

                        Log.w("Scroll", "Inside Outer Scrolled")

                        if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                            isScrolling = false
                            Log.w("Scroll", "Inside Scrolled")
                            val innerQuery = FirebaseFirestore.getInstance().collection("posts")
                                .whereEqualTo("contentType", "image")
                                .orderBy("datePosted", Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(fetchLimit)
                            innerQuery.get().addOnCompleteListener { innerTask ->
                                if (innerTask.isSuccessful) {
                                    data.removeAt(data.size - 1)
                                    for (document in innerTask.result!!.documents) {
                                        val post = document.toObject(Post::class.java)
                                        var postType = Post.TYPE_POST

                                        if (post!!.contentType == "image") {
                                            if (post.type!!.compareTo("free") == 0) {
                                                postType = Post.TYPE_POST
                                            } else if (post.type.compareTo("premium") == 0) {
                                                postType = Post.TYPE_POST_LOCKED
                                                if (post.userUnlocked!!.toString()
                                                        .contains(uid)
                                                ) {
                                                    postType = Post.TYPE_POST
                                                }
                                            }
                                        } else if (post.contentType == "video")
                                            postType = Post.TYPE_VIDEO
                                        else
                                            postType = Post.TYPE_UNAVAILABLE

                                        post.postType = postType
                                        data.add(post)
                                    }
                                    Log.w("Scroll", "Inside Second")
                                    Log.w("Scroll", "Last : " + it.result.size())

                                    data.add(Post(Post.TYPE_LOADING))
                                    adapter.notifyDataSetChanged()

                                    if (innerTask.result.size() != 0)
                                        lastVisible =
                                            innerTask.result.documents[innerTask.result.size() - 1]
                                    else
                                        isLastItemReached = true
                                    if (innerTask.result.size() < fetchLimit) {
                                        isLastItemReached = true
                                    }
                                } else {
                                    Log.w("Scroll", "Error : " + it.exception)
                                }
                            }
                        }
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            isScrolling = true
                        }
                    }
                })*/
            }
        }

        adapter = HomeRecyclerAdapter(context!!, activity as MainActivity, data)
        adapter.setHasStableIds(true)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()
        shimmerFrameLayout.startShimmerAnimation()
    }

    override fun onResume() {
        super.onResume()
        shimmerFrameLayout.startShimmerAnimation()
    }

    override fun onPause() {
        super.onPause()
        shimmerFrameLayout.stopShimmerAnimation()
    }

    private fun addCategories(view: View) {
        val adapter =
            HomeCollectionsRecyclerAdapter(context!!, activity!! as MainActivity, categoryData)
        val recyclerView = view.findViewById<RecyclerView>(R.id.homeCategoryRecyclerView)
        val moreTextView = view.findViewById<TextView>(R.id.homeTitleCategorySeeMoreTextView)

        val query = FirebaseFirestore.getInstance().collection("collections")

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result!!.documents) {
                    val collection = document.toObject(CollectionsData::class.java)
                    categoryData.add(CollectionsData(1, collection!!.title, collection.imageUrl))
                }
                adapter.notifyDataSetChanged()
            }
        }

        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        moreTextView.setOnClickListener {
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.homeContainer, CollectionsFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

}