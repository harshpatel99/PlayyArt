package com.twogentle.wall.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.twogentle.wall.MainActivity
import com.twogentle.wall.extras.GridSpacingItemDecorator
import com.twogentle.wall.R
import com.twogentle.wall.adapter.WallRecyclerAdapter
import com.twogentle.wall.model.Post
import com.twogentle.wall.model.WallData
import org.json.JSONObject

class SavedFragment : Fragment() {

    lateinit var adapter: WallRecyclerAdapter
    lateinit var data : ArrayList<WallData>

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.profileSavedRecyclerView)
        shimmerFrameLayout = view.findViewById(R.id.savedShimmerLayout)

        val noItemView = view.findViewById<View>(R.id.savedNoItem)
        val noItemImageView = noItemView.findViewById<ImageView>(R.id.noItemImage)
        val noItemTextView = noItemView.findViewById<TextView>(R.id.noItemText)

        noItemImageView.setImageResource(R.drawable.ic_bookmark_24_regular)
        noItemTextView.text = getString(R.string.saved_empty_text)

        data = ArrayList()

        val query = FirebaseFirestore.getInstance().collection("posts")
            .whereArrayContains(
                "userSaved",
                FirebaseAuth.getInstance().currentUser!!.uid
            ).orderBy("datePosted", Query.Direction.DESCENDING)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
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
                            postType = when (post.contentType) {
                                "image" -> {
                                    Post.TYPE_POST
                                }
                                "video" -> {
                                    Post.TYPE_VIDEO
                                }
                                else -> Post.TYPE_UNAVAILABLE
                            }

                            data.add(WallData(postType, post.url!!, post.id!!, post))
                            count += 1
                        }
                    } else {
                        postType = when (post.contentType) {
                            "image" -> {
                                Post.TYPE_POST
                            }
                            "video" -> {
                                WallData.TYPE_VIDEO
                            }
                            else -> Post.TYPE_UNAVAILABLE
                        }

                        data.add(WallData(postType, post.url!!, post.id!!, post))
                        count += 1
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

        adapter = WallRecyclerAdapter(context!!, activity as MainActivity, data)
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
        recyclerView.addItemDecoration(
            GridSpacingItemDecorator(
                3,
                30,
                true
            )
        )
        recyclerView.layoutManager = GridLayoutManager(context, 3)

        return view
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
                        if (post.contentType == "video") {
                            FirebaseFirestore.getInstance().collection("posts").document(post.id!!)
                                .update(
                                    hashMapOf<String, Any>(
                                        "thumbnail" to displayUrl,
                                        "url" to videoUrl,
                                        "date" to System.currentTimeMillis()
                                    )
                                )
                            post.url = videoUrl
                        }else{
                            FirebaseFirestore.getInstance().collection("posts").document(post.id!!)
                                .update(
                                    hashMapOf<String, Any>(
                                        "url" to displayUrl,
                                        "date" to System.currentTimeMillis()
                                    )
                                )
                            post.url = displayUrl
                        }

                        val postType = if (post.contentType == "image") {
                            1
                        } else Post.TYPE_UNAVAILABLE
                        data.add(WallData(postType, post.url!!, post.id, post))
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