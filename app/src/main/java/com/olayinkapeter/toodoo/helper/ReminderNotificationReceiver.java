package com.olayinkapeter.toodoo.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Olayinka_Peter on 2/11/2017.
 */

public class ReminderNotificationReceiver extends BroadcastReceiver {

    String reminderStatus, todoItem, selectedDueDate;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("On the Receiver", "Success");

        // Get extra strings from the ToodooNote intent
        reminderStatus = intent.getExtras().getString("reminderStatus");
        todoItem = intent.getExtras().getString("todoItem");
        selectedDueDate = intent.getExtras().getString("selectedDueDate");
        Log.e("Reminder Status is ", reminderStatus);
        Log.e("Todo Item is ", todoItem);
        Log.e("DueDate is ", selectedDueDate);

        // Create an intent to the notification service
        Intent serviceIntent = new Intent(context, ReminderNotificationService.class);
        serviceIntent.putExtra("reminderStatus", reminderStatus);
        serviceIntent.putExtra("todoItem", todoItem);
        serviceIntent.putExtra("selectedDueDate", selectedDueDate);

        // Start the notification service
        context.startService(serviceIntent);
    }
}
