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
import com.twogentle.wall.R
import com.twogentle.wall.adapter.CategoriesRecyclerAdapter
import com.twogentle.wall.model.CategoriesData

class CategoriesFragment : Fragment() {

    lateinit var adapter: CategoriesRecyclerAdapter

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.categoriesRecyclerView)
        shimmerFrameLayout = view.findViewById(R.id.categoriesShimmerLayout)

        val collectionName = this.arguments!!.getString("collectionName")

        val data = ArrayList<CategoriesData>()
        data.add(CategoriesData(0, collectionName, ""))

        val query = FirebaseFirestore.getInstance().collection("categories")
            .whereEqualTo("collection",collectionName)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result!!.documents) {
                    val category = document.toObject(CategoriesData::class.java)
                    data.add(CategoriesData(1, category!!.title, category.imageUrl,collectionName))
                }
                adapter.notifyDataSetChanged()
            }
            shimmerFrameLayout.stopShimmerAnimation()
            shimmerFrameLayout.visibility = View.GONE
        }

        adapter = CategoriesRecyclerAdapter(context!!, data)
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