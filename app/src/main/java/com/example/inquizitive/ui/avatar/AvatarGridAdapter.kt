package com.example.inquizitive.ui.avatar

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.inquizitive.R

class AvatarGridAdapter(
    private val context: Context,
    private var avatars: List<Int>,
    private val itemClickListener: OnItemClickListener
) : BaseAdapter() {

    private var selectedPosition: Int = -1

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getCount(): Int {
        return avatars.size
    }

    override fun getItem(position: Int): Any {
        return avatars[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_grid_avatar, parent, false)

        val avatar = avatars[position]
        val viewHolder = ViewHolder(itemView)
        viewHolder.imageView.setImageResource(avatar)

        val frame = itemView.findViewById<FrameLayout>(R.id.fl_sign_up_avatar_container)
        frame.isSelected = position == selectedPosition

        itemView.setOnClickListener {
            selectedPosition = position
            itemClickListener.onItemClick(position)
            notifyDataSetChanged()
        }
        return itemView
    }

    fun updateAvatars(newAvatars: List<Int>) {
        avatars = newAvatars
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }

    private class ViewHolder(itemView: View) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_avatar)
    }
}