package com.twogentle.wall.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.MainActivity
import com.twogentle.wall.R
import com.twogentle.wall.adapter.CollectionsRecyclerAdapter
import com.twogentle.wall.model.CollectionsData

class CollectionsFragment : Fragment() {

    lateinit var adapter: CollectionsRecyclerAdapter

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_collections, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.collectionsRecyclerView)
        shimmerFrameLayout = view.findViewById(R.id.collectionShimmerLayout)

        val data = ArrayList<CollectionsData>()
        data.add(CollectionsData(0, "Collections", ""))

        val query = FirebaseFirestore.getInstance().collection("collections")

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result!!.documents) {
                    val collection = document.toObject(CollectionsData::class.java)
                    data.add(CollectionsData(1, collection!!.title, collection.imageUrl))
                }
                adapter.notifyDataSetChanged()
            }
            shimmerFrameLayout.stopShimmerAnimation()
            shimmerFrameLayout.visibility = View.GONE
        }

        adapter = CollectionsRecyclerAdapter(context!!, activity!! as MainActivity, data)
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
        recyclerView.layoutManager = LinearLayoutManager(context)

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

}