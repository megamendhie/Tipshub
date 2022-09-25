package com.sqube.tipshub.adapters

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.activities.FullPostActivity
import com.sqube.tipshub.databinding.ItemPostBinding

class PostHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
    private var postId: String? = null

    fun setPostId(postId: String?) {
        this.postId = postId
    }

    init {
        binding.root.setOnClickListener { view: View? ->
            val intent = Intent(binding.root.context, FullPostActivity::class.java)
            intent.putExtra("postId", postId)
            binding.root.context.startActivity(intent)
        }
    }
}