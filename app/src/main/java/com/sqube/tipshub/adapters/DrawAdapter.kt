package com.sqube.tipshub.adapters

import com.sqube.tipshub.adapters.DrawAdapter.DrawViewHolder
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.DrawItemBinding
import com.sqube.tipshub.models.Draw
import java.util.*

class DrawAdapter : RecyclerView.Adapter<DrawViewHolder>() {
    private var list = ArrayList<Draw>()
    private var colorCode = R.color.colorAccent
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawViewHolder {
        val binding = DrawItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DrawViewHolder(binding)
    }

    fun setList(list: ArrayList<Draw>, code: String?) {
        this.list = list
        colorCode = setColor(code)
        notifyDataSetChanged()
    }

    private fun setColor(code: String?): Int {
        if (code != null) {
            when (code.toLowerCase()) {
                "r" -> return R.color.color_oxblood
                "br" -> return R.color.brown
                "p" -> return R.color.purple
            }
        }
        return R.color.colorAccent
    }

    override fun onBindViewHolder(holder: DrawViewHolder, position: Int) {
        holder.updateGame(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class DrawViewHolder(val binding: DrawItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun updateGame(draw: Draw) {
            with(binding){
                txtNum.text = draw.num.toString()
                txtMatch.text = draw.match
                if (draw.isWon) imgWon.visibility = View.VISIBLE else imgWon.visibility = View.INVISIBLE
                val txtNumBackground = txtNum.background as GradientDrawable
                txtNumBackground.setColor(itemView.context.resources.getColor(colorCode))
            }
        }
    }
}