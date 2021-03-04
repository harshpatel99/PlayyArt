package com.twogentle.wall.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.MainActivity
import com.twogentle.wall.R
import com.twogentle.wall.adapter.SelectCollectionsRecyclerAdapter
import com.twogentle.wall.extras.GridSpacingItemDecorator
import com.twogentle.wall.model.CollectionsData

class SelectCollectionActivity : AppCompatActivity() {

    lateinit var adapter: SelectCollectionsRecyclerAdapter

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_collection)

        val recyclerView = findViewById<RecyclerView>(R.id.selectCollectionRecyclerView)
        val doneCard = findViewById<MaterialCardView>(R.id.selectCollectionDoneCard)
        val progressBarView = findViewById<View>(R.id.selectCollectionProgressBar)
        shimmerFrameLayout = findViewById(R.id.selectCollectionShimmerLayout)

        val sp = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val selColArray = sp.getString("selectedCollections", "")
        val selectedCollections = selColArray!!.split(",")

        progressBarView.visibility = View.VISIBLE

        val data = ArrayList<CollectionsData>()

        val query = FirebaseFirestore.getInstance().collection("collections")

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result!!.documents) {
                    val collection = document.toObject(CollectionsData::class.java)
                    if(selectedCollections.contains(collection!!.title)) {
                        data.add(CollectionsData(1, collection.title, collection.imageUrl,true))
                    }else{
                        data.add(CollectionsData(1, collection.title, collection.imageUrl))
                    }
                }
                adapter.notifyDataSetChanged()
                shimmerFrameLayout.stopShimmerAnimation()
                shimmerFrameLayout.visibility = View.GONE
            }
            progressBarView.visibility = View.GONE
        }

        adapter = SelectCollectionsRecyclerAdapter(this, data)
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
        recyclerView.addItemDecoration(
            GridSpacingItemDecorator(
                2,
                35,
                true
            )
        )
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        doneCard.setOnClickListener {
            val selectedCollectionsBuffer = StringBuffer()
            val selectedCollectionsArray = adapter.getSelected()
            if (adapter.getSelected()!!.size > 2) {
                for (sel in selectedCollectionsArray!!) {
                    selectedCollectionsBuffer.append(sel.title).append(",")
                }

                val editor = sp.edit()
                editor.putString("selectedCollections", selectedCollectionsBuffer.toString())
                editor.apply()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Select minimum 3 collections", Toast.LENGTH_SHORT).show()
            }
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