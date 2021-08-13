package adapters

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.sqube.tipshub.R
import com.bumptech.glide.Glide
import android.content.Intent
import android.view.View
import android.widget.ImageView
import com.sqube.tipshub.NewsStoryActivity
import androidx.cardview.widget.CardView
import android.widget.TextView
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap

class NewsAdapter(private val data: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<NewsAdapter.ListNewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNewsViewHolder {
        val convertView: View = if (viewType == 0)
            LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        else
            LayoutInflater.from(parent.context).inflate(R.layout.item_news_small, parent, false)
        return ListNewsViewHolder(convertView)
    }

    override fun getItemViewType(position: Int): Int {
        return position % 4
    }

    override fun onBindViewHolder(holder: ListNewsViewHolder, position: Int) {
        val news = data[position]
        with(news){
            holder.description.text = get("description")
            holder.title.text = get("title")
            holder.time.text = get("publishedAt")
            val url = get("urlToImage") ?: ""
            if (url.length > 5) Glide.with(holder.galleryImage.context).load(url).into(holder.galleryImage)
        }
        try {
            holder.crdContainer.setOnClickListener {
                val intent = Intent(it.context, NewsStoryActivity::class.java)
                intent.putExtra("url", news["url"])
                it.context.startActivity(intent)

                val activity = it.context as Activity
                activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
            }
        } catch (e: Exception) {
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ListNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val crdContainer: CardView = itemView.findViewById(R.id.crdContainer)
        val galleryImage: ImageView = itemView.findViewById(R.id.galleryImage)
        val description: TextView = itemView.findViewById(R.id.txtDescription)
        val title: TextView = itemView.findViewById(R.id.title)
        val time: TextView = itemView.findViewById(R.id.time)

    }
}