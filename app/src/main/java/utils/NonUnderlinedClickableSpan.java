package utils;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.sqube.tipshub.MemberProfileActivity;
import com.sqube.tipshub.MyProfileActivity;
import com.sqube.tipshub.R;

public class NonUnderlinedClickableSpan extends ClickableSpan {

    private int type;// 0-hashtag , 1- mention, 2- url link
    private String text;// Keyword or url
    private Context context;
    private String TAG = "NonUnderlinedSpan";

    NonUnderlinedClickableSpan(Context context, String text, int type) {
        this.text = text;
        this.type = type;
        this.context = context;
    }

    @Override
    public void onClick(View widget) {
        Log.d(TAG, "onClick: ok" + text);
        switch (type){
            case 0:
                break;
            case 1:
                String myUserId = FirebaseUtil.getFirebaseAuthentication().getCurrentUser().getUid();
                String username = text.substring(1);
                FirebaseUtil.getFirebaseFirestore().collection("profiles").whereEqualTo("a2_username", username)
                        .limit(1).get().addOnCompleteListener(task -> {
                            if(task.getResult()==null|| task.getResult().isEmpty()){
                                Toast.makeText(context, "unknown username", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String userId = task.getResult().getDocuments().get(0).getId();
                            if(userId.equals(myUserId)){
                                context.startActivity(new Intent(context, MyProfileActivity.class));
                            }
                            else{
                                Intent intent = new Intent(context, MemberProfileActivity.class);
                                intent.putExtra("userId", userId);
                                context.startActivity(intent);
                            }
                        });
                break;
            default:
                /*
                Uri webpage = Uri.parse(text);
                if (!text.startsWith("http://") && !text.startsWith("https://")) {
                    webpage = Uri.parse("http://" + text);
                }
                Intent i = new Intent(Intent.ACTION_VIEW, webpage);
                context.startActivity(i);
                */
                break;
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(false);
        ds.setColor(context.getResources().getColor(
                R.color.colorPrimaryDark));
        // ds.setTypeface(Typeface.DEFAULT_BOLD);
    }
}