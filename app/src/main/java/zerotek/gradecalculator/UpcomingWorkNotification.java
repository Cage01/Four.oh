package zerotek.gradecalculator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created by Mason on 5/13/2016.
 */
public class UpcomingWorkNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notificationIntent = new Intent(context, UpcomingWork.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(UpcomingWork.class);
        stackBuilder.addNextIntent(notificationIntent);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.alert_icon_2);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setLargeIcon(icon)
                        .setSmallIcon(R.drawable.notif)
                        .setColor(context.getResources().getColor(R.color.swipeDelete))
                        .setContentTitle("Four.oh")
                        .setContentText("You have an assignment due!")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(5, builder.build());


    }
}