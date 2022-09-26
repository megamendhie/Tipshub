package com.sqube.tipshub.adapters

import android.content.DialogInterface
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ItemPostTipBinding
import com.sqube.tipshub.models.Tip
import java.util.*
import kotlin.collections.ArrayList

class PostTipsAdapter(var list: ArrayList<Tip>): RecyclerView.Adapter<PostTipsAdapter.PostTipHolder>() {

    fun updateTip(tip: Tip){
        list.add(tip)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostTipHolder {
        val binding = ItemPostTipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostTipHolder(binding)
    }

    override fun onBindViewHolder(holder: PostTipHolder, position: Int) {
        holder.bindView(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    inner class PostTipHolder(val binding: ItemPostTipBinding): RecyclerView.ViewHolder(binding.root){

        fun bindView(tip: Tip, position: Int){
            binding.edtRegion.setText(tip.region ?: "")
            binding.edtLeague.setText(tip.league ?: "")
            binding.edtHomeTeam.setText(tip.homeTeam ?: "")
            binding.edtAwayTeam.setText(tip.awayTeam ?: "")
            binding.edtPrediction.setText(tip.prediction ?: "")
            binding.edtTime.setText(tip.time ?: "")

            when(tip.status){
                "p" -> binding.spnStatus.setSelection(0)
                "w" -> binding.spnStatus.setSelection(1)
                else -> binding.spnStatus.setSelection(2)
            }

            binding.imgDelete.setOnClickListener { deleteTip(position) }
        }

        private fun deleteTip(position: Int) {
            val builder = AlertDialog.Builder(binding.root.context, R.style.CustomMaterialAlertDialog)
            builder.setTitle("Delete tip")
                .setCancelable(false)
                .setMessage("Do you want to delete tip?")
                .setPositiveButton("Yes") { _: DialogInterface?, i: Int ->
                    list.removeAt(position)
                    notifyDataSetChanged()
                }
                .setNegativeButton("Cancel") { _: DialogInterface?, i: Int -> }
                .show()
        }
    }
}