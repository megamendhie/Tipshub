package adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sqube.tipshub.databinding.ItemTipsPerformanceBinding
import java.util.*

class PerformanceAdapter(private val performanceList: ArrayList<Map<String, Any>>) : RecyclerView.Adapter<PerformanceAdapter.ListNewsViewHolder>() {
    private var row: Map<String, Any> = HashMap()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNewsViewHolder {
        val binding = ItemTipsPerformanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListNewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListNewsViewHolder, position: Int) {
        row = performanceList[position]
        try {
            with(holder.binding){
                txtType.text = getType(row["type"] as Int)
                txtNOG.text = if (row["NOG"] as Long > 1) row["NOG"].toString() + " tips" else row["NOG"].toString() + " tip"
                txtWG.text = String.format(row["WG"].toString() + " won")
                val i = row["WGP"] as Long
                txtWGP.text = String.format(Locale.getDefault(), "%.1f%%", i.toDouble())
            }
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    override fun getItemCount(): Int {
        return performanceList.size
    }

    private fun getType(i: Int): String {
        return when (i) {
            1 -> "3-5 odd"
            2 -> "6-10 odd"
            3 -> "11-50 odd"
            4 -> "50+ odd"
            5 -> "Draws"
            6 -> "Banker"
            else -> ""
        }
    }

    inner class ListNewsViewHolder(val binding: ItemTipsPerformanceBinding) : RecyclerView.ViewHolder(binding.root)
}