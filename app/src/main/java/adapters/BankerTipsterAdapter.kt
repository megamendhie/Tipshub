package adapters

import com.sqube.tipshub.utils.FirebaseUtil.firebaseAuthentication
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sqube.tipshub.models.ProfileShort
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import adapters.BankerTipsterAdapter.BankerTipsterHolder
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import com.sqube.tipshub.R
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sqube.tipshub.databinding.ItemBankerTipsterBinding
import java.lang.Exception

class BankerTipsterAdapter(options: FirestoreRecyclerOptions<ProfileShort?>) : FirestoreRecyclerAdapter<ProfileShort, BankerTipsterHolder>(options) {
    private var myUserId = if (firebaseAuthentication!!.currentUser != null) firebaseAuthentication!!.currentUser!!.uid else ""

    override fun onBindViewHolder(holder: BankerTipsterHolder, position: Int, model: ProfileShort) {
        val userId = snapshots.getSnapshot(position).id
        model.a_userId = userId
        holder.setDisplay(model)
        with(holder.binding){
            imgDp.setOnClickListener {
                if (myUserId == userId) root.context.startActivity(Intent(root.context , MyProfileActivity::class.java))
                else {
                    val intent = Intent(root.context, MemberProfileActivity::class.java)
                    intent.putExtra("userId", userId)
                    root.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankerTipsterHolder {
        val binding = ItemBankerTipsterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BankerTipsterHolder(binding)
    }

    inner class BankerTipsterHolder(val binding: ItemBankerTipsterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setDisplay(profile: ProfileShort) {
            binding.txtUsername.text = profile.a2_username
            try {
                Glide.with(binding.imgDp.context).load(profile.b2_dpUrl)
                        .placeholder(R.drawable.dummy)
                        .error(getPlaceholderImage(profile.a_userId[0])).into(binding.imgDp)
            } catch (e: Exception) {
                Log.w("{PeopleAdapter", "onBindViewHolder: " + e.message)
            }
        }
    }
}