package com.relateos.keyboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConversationAdapter(
    private var conversations: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.textView.text = conversation
        holder.itemView.setOnClickListener { onItemClick(conversation) }
    }

    override fun getItemCount() = conversations.size

    fun updateData(newConversations: List<String>) {
        conversations = newConversations
        notifyDataSetChanged()
    }
}