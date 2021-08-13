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

class NewsAdapter(private val activity: Activity, private val data: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<NewsAdapter.ListNewsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNewsViewHolder {
        var convertView: View? = null
        convertView = if (viewType == 0) LayoutInflater.from(parent.context).inflate(
                R.layout.item_news, parent, false) else LayoutInflater.from(parent.context).inflate(
                R.layout.item_news_small, parent, false)
        return ListNewsViewHolder(convertView)
    }

    override fun getItemViewType(position: Int): Int {
        return position % 4
    }

    override fun onBindViewHolder(holder: ListNewsViewHolder, position: Int) {
        val news = data[position]
        try {
            holder.description.text = news["description"]
            holder.title.text = news["title"]
            holder.time.text = news["publishedAt"]
            if (news["urlToImage"]!!.length > 5) Glide.with(activity)
                    .load(news["urlToImage"])
                    .into(holder.galleryImage)
            holder.crdContainer.setOnClickListener { v: View? ->
                val i = Intent(activity.applicationContext, NewsStoryActivity::class.java)
                i.putExtra("url", news["url"])
                activity.startActivity(i)
                activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
            }
        } catch (e: Exception) {
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ListNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var crdContainer: CardView
        var galleryImage: ImageView
        var description: TextView
        var title: TextView
        var time: TextView

        init {
            crdContainer = itemView.findViewById(R.id.crdContainer)
            galleryImage = itemView.findViewById(R.id.galleryImage)
            description = itemView.findViewById(R.id.txtDescription)
            title = itemView.findViewById(R.id.title)
            time = itemView.findViewById(R.id.time)
        }
    }
}