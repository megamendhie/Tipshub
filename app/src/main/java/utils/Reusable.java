package utils;

import android.app.Activity;
import android.content.Intent;
import android.text.format.DateFormat;
import android.widget.TextView;

import java.util.Date;

public final class Reusable {
    public Reusable(){}

    public void setTime(TextView txtTime, long messageTime){
        // Format the date before showing it
        final long diff = (new Date().getTime())  - messageTime;
        final long diffSeconds = diff / 1000 % 60;
        final long diffMinutes = diff / (60 * 1000) % 60;
        final long diffHours = diff / (60 * 60 * 1000) % 24;
        final long diffDays = diff / (24 * 60 * 60 * 1000);

        if(diffDays > 0){
            txtTime.setText(DateFormat.format("dd MMM  (h:mm a)", messageTime));
        }
        else if(diffHours > 23){
            txtTime.setText(DateFormat.format("dd MMM  (h:mm a)", messageTime));
        }
        else if(diffHours > 0){
            txtTime.setText(diffHours+"h ago");
        }
        else if(diffMinutes >0){
            txtTime.setText(diffMinutes==1? "1min":diffMinutes+"mins");
        }
        else if(diffSeconds > 8){
            txtTime.setText(diffSeconds+"s");
        }
        else{
            txtTime.setText("now");
        }
    }

    public String getSignature(){
        String Signature = "";
        String s = DateFormat.format("HH", new Date().getTime()).toString();
        int sign = Integer.parseInt(s);
        if(sign>=20){
            Signature = "f";
        }else if(sign>=16){
            Signature = "e";
        }else if(sign>=12){
            Signature = "d";
        }else if(sign>=8){
            Signature = "c";
        }else if(sign>=4){
            Signature = "b";
        }else{
            Signature = "a";
        }
        return Signature;
    }

    public void shareTips(Activity activity, String username, String post){
        String output = "@"+username+"'s post on Tipshub:\n\n"+ post + "\n\nApp link: http://bit.ly/SecuredTips" ;

        Intent share = new Intent(Intent.ACTION_SEND);share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, output);
        share.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        activity.startActivity(Intent.createChooser(share, "Share via:"));
    }

    public void shareComment(Activity activity, String username, String post){
        String output = "@"+username+"'s comment on Tipshub:\n\n"+ post + "\n\nApp link: http://bit.ly/SecuredTips" ;

        Intent share = new Intent(Intent.ACTION_SEND);share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, output);
        share.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        activity.startActivity(Intent.createChooser(share, "Share via:"));
    }

}
