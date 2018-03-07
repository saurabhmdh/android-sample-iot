package com.santiance.test.controllers.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.santiance.test.R;
import com.santiance.test.util.Constants;

import timber.log.Timber;

/**
 * Created by saurabh.khare on 2018/03/05.
 */

public class NotificationIntentReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1000;
    private static final String TAG = "NotificationIntentReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extraData = intent.getExtras();
        Boolean entering = extraData.getBoolean(Constants.BUNDLE_IN_RANGE, false);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(context.getString(android.R.string.dialog_alert_title))
                .setContentText(context.getString(entering ? R.string.check_in: R.string.check_out))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        Timber.i("Notification has been send for checkin = " + entering);
    }
}
