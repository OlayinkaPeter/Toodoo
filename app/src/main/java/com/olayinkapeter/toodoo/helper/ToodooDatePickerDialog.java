package com.olayinkapeter.toodoo.helper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

import com.olayinkapeter.toodoo.toodooOptions.ToodooNote;

/**
 * Created by Olayinka_Peter on 1/20/2017.
 */

public class ToodooDatePickerDialog extends DatePickerDialog {
    private CharSequence title;

    public ToodooDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    public void setPermanentTitle(CharSequence title) {
        this.title = title;
        setTitle(title);
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
        super.onDateChanged(datePicker, year, month, day);
//        setTitle(title);
    }
}
