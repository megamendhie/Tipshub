package fragments

import adapters.NotificationAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.FragmentNotificationBinding
import utils.FirebaseUtil

/**
 * A simple [Fragment] subclass.
 */
class NotificationFragment : Fragment() {
    private lateinit var _binding: FragmentNotificationBinding
    private val binding get() = _binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)

        binding.postList.layoutManager = LinearLayoutManager(activity)
        binding.refresher.setColorSchemeResources(R.color.colorPrimary)
        val user = FirebaseUtil.firebaseAuthentication?.currentUser
        val userId = user!!.uid
        val query = FirebaseUtil.firebaseFirestore?.collection("notifications")!!
                .orderBy("time", Query.Direction.DESCENDING).whereEqualTo("sendTo", userId).limit(40)
        val notificationAdapter = NotificationAdapter(query, userId)
        binding.postList.adapter = notificationAdapter
        notificationAdapter.startListening()
        binding.refresher.setOnRefreshListener {
            binding.refresher.isRefreshing = true
            notificationAdapter.stopListening()
            notificationAdapter.startListening()
            binding.refresher.isRefreshing = false
        }
        return binding.root
    }
    fun scrollToTop(){
        binding.postList.smoothScrollToPosition(0)
    }
}