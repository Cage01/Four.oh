package zerotek.gradecalculator.Other;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by Mason on 5/10/2016.
 */
public class DrawerOptions {

    public static void bugReport(final Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);


        builder.setTitle("Bug Report");

        final EditText subject = new EditText(context);
        subject.setHint("Summarize the issue");
        //  subject.setPadding(20, 20, 20, 0);

        final EditText message = new EditText(context);
        message.setHint("Some more detail...");

        layout.addView(subject);
        layout.addView(message);

        builder.setView(layout);


        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "support@zerotek.net", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report: " + subject.getText().toString());
                intent.putExtra(Intent.EXTRA_TEXT, message.getText().toString());
                context.startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
                dialog.dismiss();
            }
        })
                .create().show();

    }

    public static void newsletterSignup(Context context) {
        Uri uriUrl = Uri.parse("http://www.zerotek.net/signup");
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        context.startActivity(launchBrowser);
    }

    public static void contactDeveloper(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);


        builder.setTitle("Contact a Developer");


        final EditText subject = new EditText(context);
        subject.setHint("Subject");
        //  subject.setPadding(20, 20, 20, 0);

        final EditText message = new EditText(context);
        message.setHint("Message");

        layout.addView(subject);
        layout.addView(message);

        builder.setView(layout);


        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "mason.r@zerotek.net, ron.e@zerotek.net", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Four.oh Contact Dev: " + subject.getText().toString());
                intent.putExtra(Intent.EXTRA_TEXT, message.getText().toString());
                context.startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
                dialog.dismiss();
            }
        })
                .create().show();
    }


    public static void getOpenFacebookIntent(Context context) {

        try {
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/940301932721285"));
            context.startActivity(intent1);
        } catch (Exception e) {
            Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Zerotek-940301932721285"));
            context.startActivity(intent2);
        }
    }

}
