package adapters

import adapters.TransactionAdapter.TransactionHolder
import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ItemTransactionBinding
import models.Transaction

class TransactionAdapter(query: Query?) : FirestoreRecyclerAdapter<Transaction, TransactionHolder>(FirestoreRecyclerOptions.Builder<Transaction>()
        .setQuery(query!!, Transaction::class.java)
        .build()) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: TransactionHolder, position: Int, model: Transaction) {
        holder.bindView(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionHolder(binding)
    }

    inner class TransactionHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(model: Transaction) {
            with(model){
                val time = DateFormat.format("E, MMM dd yyyy h:mm a", time).toString()
                binding.txtTime.text = time
                binding.txtDescription.text = description
                binding.txtAmount.text = amount
            }
            when (model.type) {
                "deposit" -> {
                    binding.txtAmount.setTextColor(binding.imgType.context.resources.getColor(R.color.check_green))
                    Glide.with(binding.imgType.context).load(R.drawable.ic_circle_up).into(binding.imgType)
                }
                "withdrawal" -> {
                    binding.txtAmount.setTextColor(binding.imgType.context.resources.getColor(R.color.red))
                    Glide.with(binding.imgType.context).load(R.drawable.ic_circle_down).into(binding.imgType)
                }
                "subscription" -> {
                    binding.txtAmount.setTextColor(binding.imgType.context.resources.getColor(R.color.brown))
                    Glide.with(binding.imgType.context).load(R.drawable.ic_circle_right).into(binding.imgType)
                }
            }
        }
    }
}