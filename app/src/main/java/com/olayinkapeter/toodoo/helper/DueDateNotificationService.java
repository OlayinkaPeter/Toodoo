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

import com.olayinkapeter.toodoo.MainActivity;
import com.olayinkapeter.toodoo.R;

/**
 * Created by Olayinka_Peter on 2/12/2017.
 */

public class DueDateNotificationService extends Service {

    MediaPlayer mediaSong;
    boolean isRunning;
    String dueDateStatus, todoItem, selectedDueDate;
    String positionKey = "todoItemPosition";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // Get extra strings from the DueDateNotificationReceiver intent
        dueDateStatus = intent.getExtras().getString("dueDateStatus");
        todoItem = intent.getExtras().getString("todoItem");
        selectedDueDate = intent.getExtras().getString("selectedDueDate");
      //  Log.e("DueDate Status is ", dueDateStatus);
        Log.e("Todo Item is ", todoItem);
        Log.e("DueDate is ", selectedDueDate);

        // put the notification here, test it out

        // notification
        // set up the notification service
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Intent intentMain = new Intent(this.getApplicationContext(), MainActivity.class);
      //  intentMain.putExtra(positionKey, mTodoUUID);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(this, 0,
                intentMain, 0);

        // make the notification parameters
        Notification notificationPopup = new Notification.Builder(this)
                .setContentTitle(todoItem)
                .setContentText("Due for Today")
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentIntent(pendingIntentMain)
                .setAutoCancel(true)
                .build();

        // this converts the extra strings from the intent
        // to start IDs, values 0 or 1
        assert dueDateStatus != null;
        switch (dueDateStatus) {
            case "dueDate on":
                // set up the start command for the notification
                notificationManager.notify(1, notificationPopup);
                mediaSong = MediaPlayer.create(this, R.raw.duedate);
                mediaSong.start();
                break;
            case "dueDate off":
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
