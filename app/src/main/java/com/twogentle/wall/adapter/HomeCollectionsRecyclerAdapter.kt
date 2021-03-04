package com.twogentle.wall.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.twogentle.wall.MainActivity
import com.twogentle.wall.R
import com.twogentle.wall.fragments.CategoriesFragment
import com.twogentle.wall.model.CollectionsData

class HomeCollectionsRecyclerAdapter(
    private val context: Context,
    private val activity: MainActivity,
    private val data: ArrayList<CollectionsData>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_COLLECTION = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TITLE -> {
                TitleViewHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.list_item_categories_title, parent, false
                    )
                )
            }
            TYPE_COLLECTION -> CollectionsViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_home_collections_collection, parent, false
                )
            )
            else -> CollectionsViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_home_collections_collection, parent, false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_TITLE -> (holder as TitleViewHolder).bindViews(data[position])
            TYPE_COLLECTION -> (holder as CollectionsViewHolder).bindViews(context,activity, data[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position].type) {
            CollectionsData.TYPE_TITLE -> TYPE_TITLE
            CollectionsData.TYPE_COLLECTION -> TYPE_COLLECTION
            else -> TYPE_COLLECTION
        }
    }

    class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(data: CollectionsData) {
            val titleTextView =
                itemView.findViewById<TextView>(R.id.listItemCategoriesTitleTextView)
            titleTextView.text = data.title
        }
    }

    class CollectionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context,activity: MainActivity, data: CollectionsData) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemCollectionImageView)
            val layout = itemView.findViewById<ConstraintLayout>(R.id.listItemCollectionCard)

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageView.width, imageView.height)

            Glide.with(context)
                .load(data.imageUrl)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.loading_dots)
                .into(imageView)

            layout.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("collectionName",data.title)
                val fragment = CategoriesFragment()
                fragment.arguments = bundle
                MainActivity.changeFragment(activity,fragment,true)
            }

        }
    }
}