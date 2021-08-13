package fragments

import adapters.NotificationAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.Query
import com.sqube.tipshub.R
import utils.FirebaseUtil

/**
 * A simple [Fragment] subclass.
 */
class NotificationFragment : Fragment() {
    private var rootView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment only if its null
        if (rootView == null) rootView = inflater.inflate(R.layout.fragment_notification, container, false)
        val notificationList: RecyclerView = rootView!!.findViewById(R.id.postList)
        notificationList.layoutManager = LinearLayoutManager(activity)
        val refresher: SwipeRefreshLayout = rootView!!.findViewById(R.id.refresher)
        refresher.setColorSchemeResources(R.color.colorPrimary)
        val user = FirebaseUtil.firebaseAuthentication?.currentUser
        val userId = user!!.uid
        val query = FirebaseUtil.firebaseFirestore?.collection("notifications")!!
                .orderBy("time", Query.Direction.DESCENDING).whereEqualTo("sendTo", userId).limit(40)
        val notificationAdapter = NotificationAdapter(query, userId)
        notificationList.adapter = notificationAdapter
        notificationAdapter.startListening()
        refresher.setOnRefreshListener {
            refresher.isRefreshing = true
            if (notificationAdapter != null) {
                notificationAdapter.stopListening()
                notificationAdapter.startListening()
            }
            refresher.isRefreshing = false
        }
        return rootView
    }
}