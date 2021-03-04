package com.twogentle.wall.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.twogentle.wall.MainActivity
import com.twogentle.wall.R
import com.twogentle.wall.activity.SelectCollectionActivity
import com.twogentle.wall.adapter.PopularCategoryRecyclerAdapter
import com.twogentle.wall.adapter.PopularRecyclerAdapter
import com.twogentle.wall.extras.GridSpacingItemDecorator
import com.twogentle.wall.model.CategoriesData
import com.twogentle.wall.model.Post
import com.twogentle.wall.model.User

class PopularFragment : Fragment() {

    /*companion object {
        const val NUMBER_OF_ADS = 3
    }*/

    lateinit var adapter: PopularRecyclerAdapter
    var fetchLimit: Long = 20
    /*var isScrolling = false
    var isLastItemReached = false
    lateinit var lastVisible: DocumentSnapshot

    private lateinit var adLoader: AdLoader
    private val nativeAds = ArrayList<UnifiedNativeAd>()
    private var firstAdNotAdded = false*/

    val data = ArrayList<Any>()

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_popular, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.popularRecyclerView)
        //val progressBarView = view.findViewById<View>(R.id.popularProgressBar)
        shimmerFrameLayout = view.findViewById<ShimmerFrameLayout>(R.id.popularShimmerLayout)

        val sp = context!!.getSharedPreferences("pref", Context.MODE_PRIVATE)
        val selColArray = sp.getString("selectedCollections", "")

        if (selColArray!!.isEmpty()) {
            startActivity(Intent(activity, SelectCollectionActivity::class.java))
            activity!!.finish()
        }

        val sharedPreferences = activity!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isUnlocked = sharedPreferences.getInt("subscription", User.USER_FREE)

        if (isUnlocked != User.USER_SUBSCRIBED_ARTS || isUnlocked != User.USER_SUBSCRIBED_FULL) {
            val bannerAdView = view.findViewById<AdView>(R.id.popularBannerAdView)

            MobileAds.initialize(context) {}
            val adRequest = AdRequest.Builder().build()
            bannerAdView.loadAd(adRequest)
        }

        addCategories(view)

        val selectedCollections = ArrayList(selColArray.split(","))
        selectedCollections.add("Video")

        //progressBarView.visibility = View.VISIBLE

        //data.add(Post(0, "Popular"))

        val query = FirebaseFirestore.getInstance().collection("posts")
            .whereIn("collection", selectedCollections)
            .orderBy("likes", Query.Direction.DESCENDING)
            .limit(fetchLimit)

        /*FirebaseFirestore.getInstance().collection("polls").document("active")
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val poll = it.result!!.toObject(Poll::class.java)
                    data.add(1, poll!!)
                    adapter.notifyDataSetChanged()
                }
            }*/

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                for (document in it.result!!.documents) {
                    val post = document.toObject(Post::class.java)
                    var postType = 1
                    /*if (System.currentTimeMillis() - post!!.date!! > 259200000 && post.url!!.contains(
                            "instagram"
                        )
                    ) {
                        if (post.instagramUrl!!.isNotEmpty()) {
                            getInstaUrl(post, post.instagramUrl)
                        } else {
                            if (post.contentType == "image") {
                                if (post.type!!.compareTo("free") == 0) {
                                    postType = 1
                                } else if (post.type.compareTo("premium") == 0) {
                                    postType = 3
                                    if (post.userUnlocked!!.toString().contains(uid)) {
                                        postType = 1
                                    }
                                }
                            } else
                                postType = Post.TYPE_UNAVAILABLE
                        }
                    } else {*/
                    if (post!!.contentType == "image") {
                        if (post.type!!.compareTo("free") == 0) {
                            postType = 1
                        } else if (post.type.compareTo("premium") == 0) {
                            postType = 3
                            if (post.userUnlocked!!.toString().contains(uid)) {
                                postType = 1
                            }
                        }
                    } else
                        postType = Post.TYPE_UNAVAILABLE

                    post.postType = postType

                    data.add(post)
                    adapter.notifyDataSetChanged()
                    shimmerFrameLayout.stopShimmerAnimation()
                    shimmerFrameLayout.visibility = View.GONE
                }
                /*data.add(Post(5))
                adapter.notifyDataSetChanged()
                lastVisible = it.result.documents[it.result.size() - 1]
                progressBarView.visibility = View.GONE
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisibleItemPosition =
                            linearLayoutManager.findFirstVisibleItemPosition()
                        val visibleItemCount = linearLayoutManager.childCount
                        val totalItemCount = linearLayoutManager.itemCount

                        if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                            isScrolling = false
                            var isAdded = false
                            val innerQuery = FirebaseFirestore.getInstance().collection("posts")
                                .whereIn("collection", selectedCollections)
                                .orderBy("likes", Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(fetchLimit)

                            innerQuery.get().addOnCompleteListener { innerTask ->
                                if (innerTask.isSuccessful) {
                                    data.removeAt(data.size - 1)
                                    for (document in innerTask.result!!.documents) {
                                        val post = document.toObject(Post::class.java)
                                        var postType = 1
                                        if (System.currentTimeMillis() - post!!.date!! > 259200000 && post.url!!.contains(
                                                "instagram"
                                            )
                                        ) {
                                            if (post.instagramUrl!!.isNotEmpty()) {
                                                getInstaUrl(post, post.instagramUrl)
                                            } else {
                                                if (post.contentType == "image") {
                                                    if (post.type!!.compareTo("free") == 0) {
                                                        postType = 1
                                                    } else if (post.type.compareTo("premium") == 0) {
                                                        postType = 3
                                                        if (post.userUnlocked!!.toString()
                                                                .contains(uid)
                                                        ) {
                                                            postType = 1
                                                        }
                                                    }
                                                } else
                                                    postType = Post.TYPE_UNAVAILABLE
                                            }
                                        } else {
                                            if (post.contentType == "image") {
                                                if (post.type!!.compareTo("free") == 0) {
                                                    postType = 1
                                                } else if (post.type.compareTo("premium") == 0) {
                                                    postType = 3
                                                    if (post.userUnlocked!!.toString()
                                                            .contains(uid)
                                                    ) {
                                                        postType = 1
                                                    }
                                                }
                                            } else
                                                postType = Post.TYPE_UNAVAILABLE
                                        }

                                        post.postType = postType

                                        if (!data.contains(post)) {
                                            isAdded = true
                                            data.add(post)
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                    if (isAdded) {
                                        insertTwoAds(data.size - 8)
                                        adapter.notifyDataSetChanged()
                                        isAdded = false
                                    }
                                    data.add(Post(5))
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

                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            isScrolling = true
                        }
                    }
                })*/
            }
        }

        adapter = PopularRecyclerAdapter(context!!, activity!!, data)
        recyclerView.addItemDecoration(
            GridSpacingItemDecorator(
                2,
                50,
                true
            )
        )
        recyclerView.layoutManager = GridLayoutManager(activity!!, 2)
        recyclerView.adapter = adapter

        return view
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
        val categoryData = ArrayList<CategoriesData>()
        val adapter =
            PopularCategoryRecyclerAdapter(context!!, activity!! as MainActivity, categoryData)
        val recyclerView = view.findViewById<RecyclerView>(R.id.popularCategoryRecyclerView)
        val moreTextView = view.findViewById<TextView>(R.id.popularTitleCategorySeeMoreTextView)

        val sp = context!!.getSharedPreferences("pref", Context.MODE_PRIVATE)
        val selColArray = sp.getString("selectedCollections", "")

        if (selColArray!!.isEmpty()) {
            startActivity(Intent(activity, SelectCollectionActivity::class.java))
            activity!!.finish()
        }

        val selectedCollections = ArrayList(selColArray.split(","))

        val query = FirebaseFirestore.getInstance().collection("categories")
            .whereIn("collection", selectedCollections)
            .limit(8)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result!!.documents) {
                    val category = document.toObject(CategoriesData::class.java)
                    categoryData.add(
                        CategoriesData(
                            1,
                            category!!.title,
                            category.imageUrl,
                            category.collection
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
        }

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

    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        //loadNativeAds()
    }

    fun insertTwoAds(initIndex: Int) {
        if (nativeAds.size <= 0)
            return

        val indexOne = initIndex + 4

        if (indexOne < data.size) {
            data.add(indexOne, nativeAds[0])
        }
        if ((data.size - initIndex) > 6 && nativeAds.size > 1)
            data.add(nativeAds[1])
    }

    private fun loadNativeAds() {
        adLoader =
            AdLoader.Builder(context, getString(R.string.native_unit_id)).forUnifiedNativeAd {
                nativeAds.add(it)
                if (!firstAdNotAdded) {
                    if (!isDetached) {
                        if (data.size > 5) {
                            data.add(5, nativeAds[0])
                            adapter.notifyItemInserted(5)
                            firstAdNotAdded = true
                        }
                    }
                }
            }.withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()

        adLoader.loadAds(AdRequest.Builder().build(), NUMBER_OF_ADS)
    }

    private fun getInstaUrl(post: Post, requestUrl: String) {

        val stringRequest = StringRequest("$requestUrl/?__a=1",
            {
                try {
                    val jsonObject = JSONObject(it)
                    val graphQLArray = jsonObject.getJSONObject("graphql")
                    val shortMediaArray = graphQLArray.getJSONObject("shortcode_media")
                    val displayUrl = shortMediaArray.get("display_url").toString()

                    FirebaseFirestore.getInstance().collection("posts").document(post.id!!)
                        .update(
                            hashMapOf<String, Any>(
                                "url" to displayUrl,
                                "date" to System.currentTimeMillis()
                            )
                        )

                    activity!!.runOnUiThread {
                        post.url = displayUrl

                        val uid = FirebaseAuth.getInstance().uid.toString()
                        var postType = 1
                        if (post.contentType == "image") {
                            if (post.type!!.compareTo("free") == 0) {
                                postType = 1
                            } else if (post.type.compareTo("premium") == 0) {
                                postType = 3
                                if (post.userUnlocked!!.toString().contains(uid)) {
                                    postType = 1
                                }
                            }
                        } else if (post.contentType == "video")
                            postType = 4
                        else
                            postType = Post.TYPE_UNAVAILABLE

                        post.postType = postType
                        data.add(post)
                        adapter.notifyDataSetChanged()
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

    }*/
}