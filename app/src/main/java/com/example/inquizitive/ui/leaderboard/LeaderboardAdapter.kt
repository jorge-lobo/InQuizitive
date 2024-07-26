package com.example.inquizitive.ui.leaderboard

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ItemRvLeaderboardBinding
import com.example.inquizitive.utils.Utils

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    private var items: List<LeaderboardBindingItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRvLeaderboardBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position + 2)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<LeaderboardBindingItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemRvLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LeaderboardBindingItem, position: Int) {
            binding.tvItemPosition.text = position.toString()
            binding.tvItemUsername.text = item.user.username

            val formattedScore =
                item.result.score?.let { Utils.formatNumberWithThousandSeparator(it) }
            binding.tvItemScore.text =
                binding.root.context.getString(R.string.leaderboard_score_format, formattedScore)
        }
    }
}