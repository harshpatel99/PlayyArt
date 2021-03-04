package com.twogentle.wall.adapter

import android.content.Context
import android.content.Intent
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
import com.twogentle.wall.R
import com.twogentle.wall.activity.WallActivity
import com.twogentle.wall.model.CategoriesData

class CategoriesRecyclerAdapter(
    private val context: Context,
    private val data: ArrayList<CategoriesData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_CATEGORY = 1
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
            TYPE_CATEGORY -> CategoryViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_categories_category, parent, false
                )
            )
            else -> CategoryViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_categories_category, parent, false
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
            TYPE_CATEGORY -> (holder as CategoryViewHolder).bindViews(context, data[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position].type) {
            CategoriesData.TYPE_TITLE -> TYPE_TITLE
            CategoriesData.TYPE_CATEGORY -> TYPE_CATEGORY
            else -> TYPE_CATEGORY
        }
    }

    class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(data: CategoriesData) {
            val titleTextView =
                itemView.findViewById<TextView>(R.id.listItemCategoriesTitleTextView)
            titleTextView.text = data.title
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindViews(context: Context, data: CategoriesData) {
            val imageView = itemView.findViewById<ImageView>(R.id.listItemCategoryImageView)
            val titleTextView = itemView.findViewById<TextView>(R.id.listItemCategoryTextView)
            val layout = itemView.findViewById<ConstraintLayout>(R.id.listItemCategoryCard)

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageView.width, imageView.height)
            Glide.with(context)
                .load(data.imageUrl)
                .apply(requestOptions)
                .placeholder(R.drawable.loading_dots)
                .centerCrop()
                .into(imageView)

            titleTextView.text = data.title

            layout.setOnClickListener {
                val intent = Intent(context, WallActivity::class.java)
                intent.putExtra("collectionName", data.collection)
                intent.putExtra("categoryName", data.title)
                context.startActivity(intent)
            }

        }
    }
}