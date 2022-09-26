package com.sqube.tipshub.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sqube.tipshub.adapters.PostTipsAdapter
import com.sqube.tipshub.databinding.ActivityPostTipsBinding
import com.sqube.tipshub.models.Tip
import com.sqube.tipshub.utils.TipsHolder

class PostTipsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostTipsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lstTips.layoutManager = LinearLayoutManager(this)

        val list = TipsHolder.tipsList
        val category = if(intent.getBooleanExtra("FREE", false)) "free_tips" else "vip_tips"

        val adapter = PostTipsAdapter(list)
        binding.lstTips.adapter = adapter
        binding.btnAdd.setOnClickListener { adapter.updateTip(Tip()) }
        binding.btnPost.setOnClickListener { postTips(category, adapter) }
    }

    private fun postTips(category: String, adapter: PostTipsAdapter) {
        val count = adapter.itemCount
        if(count==0)
            return
        for(i in 0 until count){
            val s = binding.lstTips.getChildAt(i)

        }
    }

}