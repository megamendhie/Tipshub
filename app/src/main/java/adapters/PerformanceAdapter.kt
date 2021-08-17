package adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.R
import java.util.*

class PerformanceAdapter(private val performanceList: ArrayList<Map<String, Any>>) : RecyclerView.Adapter<PerformanceAdapter.ListNewsViewHolder>() {
    var row: Map<String, Any> = HashMap()
    var TAG = "PerformanceAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNewsViewHolder {
        Log.i("News started", "onCreateViewHolder: started")
        val convertView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_tips_performance, parent, false)
        return ListNewsViewHolder(convertView)
    }

    override fun onBindViewHolder(holder: ListNewsViewHolder, position: Int) {
        row = performanceList[position]
        try {
            holder.txtType.text = getType(row["type"] as Int)
            holder.txtNOG.text = if (row["NOG"] as Long > 1) row["NOG"].toString() + " tips" else row["NOG"].toString() + " tip"
            holder.txtWG.text = row["WG"].toString() + " won"
            val i = row["WGP"] as Long
            holder.txtWGP.text = String.format(Locale.getDefault(), "%.1f%%", i.toDouble())
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    override fun getItemCount(): Int {
        Log.i(TAG, "getItemCount: " + performanceList.size)
        return performanceList.size
    }

    private fun getType(i: Int): String {
        when (i) {
            1 -> return "3-5 odd"
            2 -> return "6-10 odd"
            3 -> return "11-50 odd"
            4 -> return "50+ odd"
            5 -> return "Draws"
            6 -> return "Banker"
        }
        return ""
    }

    inner class ListNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtType: TextView
        var txtNOG: TextView
        var txtWG: TextView
        var txtWGP: TextView

        init {
            txtType = itemView.findViewById(R.id.txtType)
            txtNOG = itemView.findViewById(R.id.txtNOG)
            txtWG = itemView.findViewById(R.id.txtWG)
            txtWGP = itemView.findViewById(R.id.txtWGP)
        }
    }

    init {
        Log.i(TAG, "PerformanceAdapter: $performanceList")
    }
}