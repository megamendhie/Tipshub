package adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.sqube.tipshub.R
import com.bumptech.glide.Glide
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import com.sqube.tipshub.models.Article

class NewsAdapter(private val data: List<Article>) : RecyclerView.Adapter<NewsAdapter.ListNewsViewHolder>() {
    private val customTab = CustomTabsIntent.Builder()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNewsViewHolder {
        val convertView: View = if (viewType == 0)
            LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        else
            LayoutInflater.from(parent.context).inflate(R.layout.item_news_small, parent, false)
        return ListNewsViewHolder(convertView)
    }

    override fun getItemViewType(position: Int): Int = position % 4

    override fun onBindViewHolder(holder: ListNewsViewHolder, position: Int) {
        val news = data[position]
        val customTabIntent = customTab.setToolbarColor(holder.itemView.context.resources.getColor(R.color.colorPrimary)).build()
        with(news){
            holder.description.text = description
            holder.title.text = title
            holder.time.text = publishedAt
            if (urlToImage != null) Glide.with(holder.galleryImage.context).load(urlToImage).into(holder.galleryImage)
            holder.crdContainer.setOnClickListener { customTabIntent.launchUrl(holder.itemView.context, Uri.parse(url)) }
        }
    }

    override fun getItemCount(): Int = data.size

    inner class ListNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val crdContainer: CardView = itemView.findViewById(R.id.crdContainer)
        val galleryImage: ImageView = itemView.findViewById(R.id.galleryImage)
        val description: TextView = itemView.findViewById(R.id.txtDescription)
        val title: TextView = itemView.findViewById(R.id.title)
        val time: TextView = itemView.findViewById(R.id.time)
    }
}