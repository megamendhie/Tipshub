package adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.sqube.tipshub.R;
import com.sqube.tipshub.RepostActivity;

public class TestAdapter extends ArrayAdapter<String> {
    public TestAdapter(@NonNull Context context, String[] list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.post_view, parent, false);
        }

        listItem.findViewById(R.id.txtPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(), RepostActivity.class));
            }
        });
        return listItem;
    }
}
