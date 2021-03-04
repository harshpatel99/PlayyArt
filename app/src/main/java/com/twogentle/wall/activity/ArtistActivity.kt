package com.twogentle.wall.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.twogentle.wall.R
import com.twogentle.wall.adapter.ArtistWallRecyclerAdapter
import com.twogentle.wall.extras.ExtraFunctions
import com.twogentle.wall.extras.GridSpacingItemDecorator
import com.twogentle.wall.model.Post
import com.twogentle.wall.model.WallData

class ArtistActivity : AppCompatActivity() {

    lateinit var adapter: ArtistWallRecyclerAdapter
    lateinit var data: ArrayList<WallData>

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist)

        val artistID = intent.extras!!["artistID"] as String
        val artistName = intent.extras!!["artistName"] as String
        val artistUsername = intent.extras!!["artistUsername"] as String
        val artistBio = intent.extras!!["artistBio"] as String
        val artistProfilePicUrl = intent.extras!!["artistProfilePic"] as String
        val artistSocial = intent.extras!!["artistSocial"] as HashMap<*, *>

        val recyclerView = findViewById<RecyclerView>(R.id.artistProfileRecyclerView)
        val artistProfilePicImageView = findViewById<ImageView>(R.id.artistProfilePictureImageView)
        val artistIDTextView = findViewById<TextView>(R.id.artistProfileUsernameTextView)
        val artistNameTextView = findViewById<TextView>(R.id.artistProfileNameTextView)
        val artistBioTextView = findViewById<TextView>(R.id.artistProfileDescriptionTextView)
        val artistURL = findViewById<ImageView>(R.id.artistProfileLinkWebsiteImageView)
        val artistInstagram = findViewById<ImageView>(R.id.artistProfileLinkInstagramImageView)
        val artistFacebook = findViewById<ImageView>(R.id.artistProfileLinkFacebookImageView)
        val artistTwitter = findViewById<ImageView>(R.id.artistProfileLinkTwitterImageView)
        shimmerFrameLayout = findViewById(R.id.artistShimmerLayout)

        artistIDTextView.text = "@$artistUsername"
        artistNameTextView.text = artistName
        artistBioTextView.text = artistBio

        val requestOption = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(artistProfilePicImageView.width, artistProfilePicImageView.height)
        Glide.with(this)
            .load(artistProfilePicUrl)
            .apply(requestOption)
            .placeholder(R.drawable.loading_dots)
            .into(artistProfilePicImageView)

        if (artistSocial.containsKey("website")) {
            if (artistSocial["website"].toString().isNotEmpty())
                artistURL.setOnClickListener {
                    ExtraFunctions.openWebsite(this, artistSocial["website"] as String)
                }
            else
                artistURL.visibility = View.GONE
        } else
            artistURL.visibility = View.GONE

        if (artistSocial.containsKey("instagram")) {
            if (artistSocial["instagram"].toString().isNotEmpty())
                artistInstagram.setOnClickListener {
                    ExtraFunctions.openInstagram(this, artistSocial["instagram"] as String)
                }
            else
                artistInstagram.visibility = View.GONE
        } else
            artistInstagram.visibility = View.GONE

        if (artistSocial.containsKey("facebook")) {
            if (artistSocial["facebook"].toString().isNotEmpty())
                artistFacebook.setOnClickListener {
                    ExtraFunctions.openFacebook(this, artistSocial["facebook"] as String)
                }
            else
                artistFacebook.visibility = View.GONE
        } else
            artistFacebook.visibility = View.GONE

        if (artistSocial.containsKey("twitter")) {
            if (artistSocial["twitter"].toString().isNotEmpty())
                artistTwitter.setOnClickListener {
                    ExtraFunctions.openTwitter(this, artistSocial["twitter"] as String)
                }
            else
                artistTwitter.visibility = View.GONE
        } else
            artistTwitter.visibility = View.GONE

        data = ArrayList()

        val query = FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("artistID", artistID)
            .orderBy("datePosted", Query.Direction.DESCENDING)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                var count = 0
                for (document in it.result!!.documents) {
                    val post = document.toObject(Post::class.java)
                    var postType: Int

                    if (post!!.contentType == "image") {
                        if (post.type!!.compareTo("free") == 0) {
                            postType = WallData.TYPE_IMAGE
                        } else if (post.type.compareTo("premium") == 0) {
                            postType = WallData.TYPE_IMAGE_LOCKED
                            if (post.userUnlocked!!.toString().contains(uid)) {
                                postType = WallData.TYPE_IMAGE
                            }
                        } else postType = Post.TYPE_UNAVAILABLE

                    } else if (post.contentType == "video") {
                        postType = WallData.TYPE_VIDEO
                    } else postType = Post.TYPE_UNAVAILABLE

                    data.add(WallData(postType, post.url!!, post.id!!, post))
                    count += 1

                }
                adapter.notifyDataSetChanged()
                shimmerFrameLayout.stopShimmerAnimation()
                shimmerFrameLayout.visibility = View.GONE
            }

        }

        adapter = ArtistWallRecyclerAdapter(this, this, data)
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
        recyclerView.addItemDecoration(
            GridSpacingItemDecorator(
                3,
                30,
                true
            )
        )
        recyclerView.layoutManager = GridLayoutManager(this, 3)
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