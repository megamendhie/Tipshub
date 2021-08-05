package adapters;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.sqube.tipshub.R;

import models.Transaction;

public class TransactionAdapter extends FirestoreRecyclerAdapter<Transaction, TransactionAdapter.TransactionHolder>{
    private final String TAG = "TransactionAdapter";
    private String userId;

    public TransactionAdapter(Query query, String userID) {
        super(new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build());

        Log.i(TAG, "PostAdapter: created");
        this.userId = userID;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindViewHolder(@NonNull TransactionHolder holder, final int position, @NonNull final Transaction model) {
        Log.i(TAG, "onBindViewHolder: executed");

        holder.bindView(model);
    }

    @Override
    public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionHolder(view);
    }

    class TransactionHolder extends RecyclerView.ViewHolder {
        private ImageView imgType;
        private TextView mTime, mDescription, mAmount;

        private TransactionHolder(View itemView) {
            super(itemView);
            imgType = itemView.findViewById(R.id.imgType);
            mTime = itemView.findViewById(R.id.txtTime);
            mDescription = itemView.findViewById(R.id.txtDescription);
            mAmount = itemView.findViewById(R.id.txtAmount);
        }

        void bindView(Transaction model){
            String time = DateFormat.format("E, MMM dd yyyy h:mm a", model.getTime()).toString();
            Log.i(TAG, "bindView: "+ time);
            mTime.setText(time);
            mDescription.setText(model.getDescription());
            mAmount.setText(model.getAmount());

            switch (model.getType()){
                case "deposit":
                    mAmount.setTextColor(imgType.getContext().getResources().getColor(R.color.check_green));
                    Glide.with(imgType.getContext()).load(R.drawable.ic_circle_up).into(imgType); break;
                case "withdrawal":
                    mAmount.setTextColor(imgType.getContext().getResources().getColor(R.color.red));
                    Glide.with(imgType.getContext()).load(R.drawable.ic_circle_down).into(imgType); break;
                case "subscription":
                    mAmount.setTextColor(imgType.getContext().getResources().getColor(R.color.brown));
                    Glide.with(imgType.getContext()).load(R.drawable.ic_circle_right).into(imgType); break;
            }
        }
    }
}