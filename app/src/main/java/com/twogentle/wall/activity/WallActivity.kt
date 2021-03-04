package com.twogentle.wall.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import com.twogentle.wall.R
import com.twogentle.wall.adapter.WallRecyclerAdapter
import com.twogentle.wall.extras.GridSpacingItemDecorator
import com.twogentle.wall.model.Post
import com.twogentle.wall.model.User
import com.twogentle.wall.model.WallData
import org.json.JSONObject
import java.io.IOException

class WallActivity : AppCompatActivity() {

    lateinit var adapter: WallRecyclerAdapter
    lateinit var data: ArrayList<WallData>

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wall)

        val collectionName: String = intent.extras!!["collectionName"] as String
        val categoryName: String = intent.extras!!["categoryName"] as String

        val recyclerView = findViewById<RecyclerView>(R.id.wallRecyclerView)
        val backButton = findViewById<ImageView>(R.id.wallBackImageView)
        val titleText = findViewById<TextView>(R.id.wallTitleTextView)
        shimmerFrameLayout = findViewById(R.id.wallShimmerLayout)

        val noItemView = findViewById<View>(R.id.wallNoItem)
        val noItemImageView = noItemView.findViewById<ImageView>(R.id.noItemImage)
        val noItemTextView = noItemView.findViewById<TextView>(R.id.noItemText)

        noItemImageView.setImageResource(R.drawable.ic_round_category_24)
        noItemTextView.text = getString(R.string.wall_empty_text)

        titleText.text = categoryName

        backButton.setOnClickListener {
            finish()
        }


        data = ArrayList()

        val query = FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("collection", collectionName)
            .whereEqualTo("category", categoryName)
            .orderBy("datePosted", Query.Direction.DESCENDING)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                var count = 0

                for (document in it.result!!.documents) {
                    val post = document.toObject(Post::class.java)

                    var postType: Int

                    if (System.currentTimeMillis() - post!!.date!! > 259200000 && post.url!!.contains(
                            "instagram"
                        )
                    ) {
                        if (post.instagramUrl!!.isNotEmpty()) {
                            getInstaUrl(post, post.instagramUrl)
                            count += 1
                        } else {
                            val sharedPreferences =
                                getSharedPreferences("settings", Context.MODE_PRIVATE)
                            val isUnlocked =
                                sharedPreferences.getInt("subscription", User.USER_FREE)

                            if (isUnlocked == User.USER_SUBSCRIBED_ARTS || isUnlocked == User.USER_SUBSCRIBED_FULL) {
                                postType = if (post.contentType == "image") {
                                    1
                                } else Post.TYPE_UNAVAILABLE

                                data.add(WallData(postType, post.url!!, post.id!!, post))
                                count += 1
                            } else {
                                if (post.contentType == "image") {
                                    if (post.type!!.compareTo("free") == 0) {
                                        postType = 1
                                    } else if (post.type.compareTo("premium") == 0) {
                                        postType = 2
                                        if (post.userUnlocked!!.toString().contains(uid)) {
                                            postType = 1
                                        }
                                    } else postType = Post.TYPE_UNAVAILABLE

                                } else postType = Post.TYPE_UNAVAILABLE
                                data.add(WallData(postType, post.url!!, post.id!!, post))
                                count += 1
                            }
                        }

                    } else {

                        val sharedPreferences =
                            getSharedPreferences("settings", Context.MODE_PRIVATE)
                        val isUnlocked = sharedPreferences.getInt("subscription", User.USER_FREE)

                        if (isUnlocked == User.USER_SUBSCRIBED_ARTS || isUnlocked == User.USER_SUBSCRIBED_FULL) {
                            postType = if (post.contentType == "image") {
                                1
                            } else Post.TYPE_UNAVAILABLE

                            data.add(WallData(postType, post.url!!, post.id!!, post))
                            count += 1
                        } else {
                            if (post.contentType == "image") {
                                if (post.type!!.compareTo("free") == 0) {
                                    postType = 1
                                } else if (post.type.compareTo("premium") == 0) {
                                    postType = 2
                                    if (post.userUnlocked!!.toString().contains(uid)) {
                                        postType = 1
                                    }
                                } else postType = Post.TYPE_UNAVAILABLE

                            } else if (post.contentType == "video") {
                                postType = Post.TYPE_VIDEO
                            } else postType = Post.TYPE_UNAVAILABLE
                            data.add(WallData(postType, post.url!!, post.id!!, post))
                            count += 1
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                shimmerFrameLayout.stopShimmerAnimation()
                shimmerFrameLayout.visibility = View.GONE
                if (count == 0) {
                    noItemView.visibility = View.VISIBLE
                }
            }
        }

        adapter = WallRecyclerAdapter(this, this@WallActivity, data)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            GridSpacingItemDecorator(
                2,
                50,
                true
            )
        )
        recyclerView.layoutManager = GridLayoutManager(this, 2)

    }

    private fun getInstaUrl(post: Post, requestUrl: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$requestUrl/?__a=1")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                runOnUiThread {
                    Toast.makeText(this@WallActivity, "Error Playing Video", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onResponse(response: Response?) {
                val strResponse = response!!.body().string()
                val jsonContact = JSONObject(strResponse)
                val jsonQL = jsonContact["graphql"] as JSONObject
                val jsonMedia = jsonQL["shortcode_media"] as JSONObject
                val thumbnailUrl = jsonMedia["display_url"].toString()

                FirebaseFirestore.getInstance().collection("posts").document(post.id!!)
                    .update(
                        hashMapOf<String, Any>(
                            "url" to thumbnailUrl,
                            "date" to System.currentTimeMillis()
                        )
                    )

                runOnUiThread {
                    post.url = thumbnailUrl

                    var postType: Int

                    val sharedPreferences =
                        getSharedPreferences("settings", Context.MODE_PRIVATE)
                    val isUnlocked = sharedPreferences.getInt("subscription", User.USER_FREE)

                    if (isUnlocked == User.USER_SUBSCRIBED_ARTS || isUnlocked == User.USER_SUBSCRIBED_FULL) {
                        postType = if (post.contentType == "image") {
                            1
                        } else Post.TYPE_UNAVAILABLE

                        data.add(WallData(postType, post.url!!, post.id, post))
                    } else {
                        if (post.contentType == "image") {
                            if (post.type!!.compareTo("free") == 0) {
                                postType = 1
                            } else if (post.type.compareTo("premium") == 0) {
                                postType = 2
                                if (post.userUnlocked!!.toString()
                                        .contains(FirebaseAuth.getInstance().uid.toString())
                                ) {
                                    postType = 1
                                }
                            } else postType = Post.TYPE_UNAVAILABLE

                        } else postType = Post.TYPE_UNAVAILABLE
                        data.add(WallData(postType, post.url!!, post.id, post))
                    }
                }

            }


        })
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

}