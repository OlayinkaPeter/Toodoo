package com.olayinkapeter.toodoo.helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.olayinkapeter.toodoo.R;
import com.olayinkapeter.toodoo.toodooOptions.ToodooNote;

/**
 * Created by Olayinka_Peter on 2/11/2017.
 */

public class ReminderNotificationService extends Service {

    MediaPlayer mediaSong;
    boolean isRunning;
    String reminderStatus, todoItem, selectedDueDate;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // Get extra strings from the ReminderNotificationReceiver intent
        reminderStatus = intent.getExtras().getString("reminderStatus");
        todoItem = intent.getExtras().getString("todoItem");
        selectedDueDate = intent.getExtras().getString("selectedDueDate");
        Log.e("Reminder Status is ", reminderStatus);
        Log.e("Todo Item is ", todoItem);
        Log.e("DueDate is ", selectedDueDate);

        // put the notification here, test it out

        // notification
        // set up the notification service
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Intent intentToodooNote = new Intent(this.getApplicationContext(), ToodooNote.class);
        PendingIntent pendingIntentToodooNote = PendingIntent.getActivity(this, 0,
               intentToodooNote, 0);

        // make the notification parameters
        Notification notificationPopup = new Notification.Builder(this)
                .setContentTitle(todoItem)
                .setContentText("Due for " + selectedDueDate)
                .setSmallIcon(R.drawable.ic_red_circle)
                .setContentIntent(pendingIntentToodooNote)
                .setAutoCancel(true)
                .build();

        // this converts the extra strings from the intent
        // to start IDs, values 0 or 1
        assert reminderStatus != null;
        switch (reminderStatus) {
            case "reminder on":
                // set up the start command for the notification
                notificationManager.notify(0, notificationPopup);
                mediaSong = MediaPlayer.create(this, R.raw.reminder);
                mediaSong.start();
                break;
            case "reminder off":
                mediaSong.stop();
                mediaSong.reset();
                break;
            default:
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Log.e("on Destroy called", "ta da");
        super.onDestroy();
        this.isRunning = false;
    }
}
