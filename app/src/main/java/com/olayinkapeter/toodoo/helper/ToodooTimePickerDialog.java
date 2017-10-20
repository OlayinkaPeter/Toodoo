package com.olayinkapeter.toodoo.helper;

import android.app.TimePickerDialog;
import android.content.Context;

/**
 * Created by Olayinka_Peter on 1/20/2017.
 */

public class ToodooTimePickerDialog extends TimePickerDialog {

    public ToodooTimePickerDialog(Context context, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, listener, hourOfDay, minute, is24HourView);
    }
}
