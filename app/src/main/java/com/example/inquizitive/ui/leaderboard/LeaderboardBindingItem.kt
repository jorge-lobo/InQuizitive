package com.example.inquizitive.ui.leaderboard

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inquizitive.R
import com.example.inquizitive.data.result.Result
import com.example.inquizitive.data.user.User
import com.mikepenz.fastadapter.items.AbstractItem

class LeaderboardBindingItem(val result: Result, val user: User) :
    AbstractItem<LeaderboardBindingItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_id

    override val layoutRes: Int
        get() = R.layout.item_rv_leaderboard

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)

        val position = holder.bindingAdapterPosition + 1
        holder.position.text = position.toString()
        holder.userUsername.text = user.username
        holder.score.text = result.score.toString()
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)

        holder.position.text = null
        holder.userUsername.text = null
        holder.score.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var position: TextView = view.findViewById(R.id.tv_item_position)
        var userUsername: TextView = view.findViewById(R.id.tv_item_username)
        var score: TextView = view.findViewById(R.id.tv_item_score)
    }
}