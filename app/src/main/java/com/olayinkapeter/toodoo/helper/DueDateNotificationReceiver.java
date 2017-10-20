package com.olayinkapeter.toodoo.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Olayinka_Peter on 2/12/2017.
 */

public class DueDateNotificationReceiver extends BroadcastReceiver {

    String dueDateStatus, todoItem, selectedDueDate;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("On the Receiver", "Success");

        // Get extra strings from the ToodooNote intent
        dueDateStatus = intent.getExtras().getString("dueDateStatus");
        todoItem = intent.getExtras().getString("todoItem");
        selectedDueDate = intent.getExtras().getString("selectedDueDate");
        Log.e("Due Date Status is ", dueDateStatus);
        Log.e("Todo Item is ", todoItem);
        Log.e("DueDate is ", selectedDueDate);

        // Create an intent to the notification service
        Intent serviceIntent = new Intent(context, DueDateNotificationService.class);
        serviceIntent.putExtra("dueDateStatus", dueDateStatus);
        serviceIntent.putExtra("todoItem", todoItem);
        serviceIntent.putExtra("selectedDueDate", selectedDueDate);

        // Start the notification service
        context.startService(serviceIntent);
    }
}
