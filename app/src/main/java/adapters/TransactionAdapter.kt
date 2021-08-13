package adapters

import adapters.TransactionAdapter.TransactionHolder
import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.sqube.tipshub.R
import models.Transaction

class TransactionAdapter(query: Query?, userID: String) : FirestoreRecyclerAdapter<Transaction, TransactionHolder>(FirestoreRecyclerOptions.Builder<Transaction>()
        .setQuery(query!!, Transaction::class.java)
        .build()) {
    private val TAG = "TransactionAdapter"
    private val userId: String
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: TransactionHolder, position: Int, model: Transaction) {
        Log.i(TAG, "onBindViewHolder: executed")
        holder.bindView(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        Log.i(TAG, "onCreateViewHolder: ")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionHolder(view)
    }

    inner class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgType: ImageView
        private val mTime: TextView
        private val mDescription: TextView
        private val mAmount: TextView
        fun bindView(model: Transaction) {
            val time = DateFormat.format("E, MMM dd yyyy h:mm a", model.time).toString()
            Log.i(TAG, "bindView: $time")
            mTime.text = time
            mDescription.text = model.description
            mAmount.text = model.amount
            when (model.type) {
                "deposit" -> {
                    mAmount.setTextColor(imgType.context.resources.getColor(R.color.check_green))
                    Glide.with(imgType.context).load(R.drawable.ic_circle_up).into(imgType)
                }
                "withdrawal" -> {
                    mAmount.setTextColor(imgType.context.resources.getColor(R.color.red))
                    Glide.with(imgType.context).load(R.drawable.ic_circle_down).into(imgType)
                }
                "subscription" -> {
                    mAmount.setTextColor(imgType.context.resources.getColor(R.color.brown))
                    Glide.with(imgType.context).load(R.drawable.ic_circle_right).into(imgType)
                }
            }
        }

        init {
            imgType = itemView.findViewById(R.id.imgType)
            mTime = itemView.findViewById(R.id.txtTime)
            mDescription = itemView.findViewById(R.id.txtDescription)
            mAmount = itemView.findViewById(R.id.txtAmount)
        }
    }

    init {
        Log.i(TAG, "PostAdapter: created")
        userId = userID
    }
}