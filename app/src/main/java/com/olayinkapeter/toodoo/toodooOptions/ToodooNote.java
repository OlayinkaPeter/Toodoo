package com.olayinkapeter.toodoo.toodooOptions;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.olayinkapeter.toodoo.adapters.ToodooOptionsAdapter;
import com.olayinkapeter.toodoo.helper.DueDateNotificationReceiver;
import com.olayinkapeter.toodoo.helper.RecyclerTouchListener;
import com.olayinkapeter.toodoo.helper.ReminderNotificationReceiver;
import com.olayinkapeter.toodoo.helper.ToodooDatePickerDialog;
import com.olayinkapeter.toodoo.helper.ToodooTimePickerDialog;
import com.olayinkapeter.toodoo.models.ItemModel;
import com.olayinkapeter.toodoo.MainActivity;
import com.olayinkapeter.toodoo.R;
import com.olayinkapeter.toodoo.models.ToodooOptionsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ToodooNote extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;

    private String mUserId;

    ImageView editIcon;
    EditText todoEditText;
    FloatingActionButton doneFAB;

    private List<ToodooOptionsModel> toodooOptionsList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ToodooOptionsAdapter mAdapter;

    private LabelDialog labelDialog;
    private ReminderDialog reminderDialog;

    public AlarmManager alarmManager;
    public PendingIntent pendingIntent;
    private Intent dueDateIntent;
    private Intent reminderIntent;

    Calendar reminderCalendar;

    String todoId, todoItem = "None", selectedLabel = "None", selectedDueDate = "No Due Date", selectedReminder = "No Reminder";
    Date selectedDueDateValue = null, selectedReminderValue = null;

    String reminderDateValue, reminderTimeValue;

    String IS_EXISTING_KEY = "IS_EXISTING_KEY", TODO_ID_KEY = "TODO_ID_KEY", TODO_ITEM_KEY = "TODO_ITEM_KEY", TODO_LABEL_KEY = "TODO_LABEL_KEY", TODO_DUEDATE_KEY = "TODO_DUEDATE_KEY", TODO_REMINDER_KEY = "TODO_REMINDER_KEY";
    String isExisting, getTodoId, getTodoItem, getTodoLabel, getTodoDueDate, getTodoReminder;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toodoo_note);

        // Show an X in place of <-
        final Drawable cross = getResources().getDrawable(R.drawable.ic_close);
        if (cross != null) {
            cross.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(cross);
        }

        editIcon = (ImageView) findViewById(R.id.editIcon);
        todoEditText = (EditText) findViewById(R.id.todoEditText);
        doneFAB = (FloatingActionButton) findViewById(R.id.doneFAB);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        // Get extra intents
        Intent intent = getIntent();
        // Checks for TodoItem existence. If TRUE, reuse and update ELSE create new.
        isExisting = intent.getExtras().getString(IS_EXISTING_KEY); // Checks for TodoItem existence. If TRUE, reuse and update ELSE create new.

        if (isExisting.equals("true")) {
            getTodoId = intent.getExtras().getString(TODO_ID_KEY);
            getTodoItem = intent.getExtras().getString(TODO_ITEM_KEY);
            getTodoLabel = intent.getExtras().getString(TODO_LABEL_KEY);
            getTodoDueDate = intent.getExtras().getString(TODO_DUEDATE_KEY);
            getTodoReminder = intent.getExtras().getString(TODO_REMINDER_KEY);

            todoEditText.setText(getTodoItem);
            todoId = getTodoId;
            todoItem = getTodoItem;
            selectedLabel = getTodoLabel;
            selectedDueDate = getTodoDueDate;
            selectedReminder = getTodoReminder;
        }

        if (isExisting.equals("camera") || isExisting.equals("voice")) {
            // Gets todoItem from ToodooCamera OR ToodooVoice if available, then sets the value
            getTodoItem = intent.getExtras().getString(TODO_ITEM_KEY);
            todoEditText.setText(getTodoItem);
            todoEditText.setEnabled(false);

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            // Show editIcon
            editIcon.setVisibility(View.VISIBLE);
            editIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    todoEditText.setEnabled(true);
                    editIcon.setVisibility(View.GONE);
                }
            });
        }

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mUserId = mFirebaseUser.getUid();

        doneFAB.setOnClickListener(this);

        mAdapter = new ToodooOptionsAdapter(toodooOptionsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        reminderCalendar = Calendar.getInstance();

        // initialize the alarm manager
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // create an intent to the DueDateNotificationReceiver class
        dueDateIntent = new Intent(this, DueDateNotificationReceiver.class);
        // create an intent to the ReminderNotificationReceiver class
        reminderIntent = new Intent(this, ReminderNotificationReceiver.class);

        prepareOptionsData();

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(ToodooNote.this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                ToodooOptionsModel ToodooOptionsModel = toodooOptionsList.get(position);
                switch (position) {
                    case 0:
                        labelDialog = new LabelDialog(ToodooNote.this);
                        labelDialog.show();
                        break;
                    case 1:
                        showDatePickerDialog();
                        break;
                    case 2:
                        reminderDialog = new ReminderDialog(ToodooNote.this);
                        reminderDialog.show();
                        break;
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    public void saveTodoItem() throws ParseException {
        todoItem = todoEditText.getText().toString();
        if (!todoItem.isEmpty()) {

            if (isExisting.equals("true")) {

                // Update Toodoo item in Firebase
                final ItemModel itemModel = new ItemModel(todoId, todoItem, selectedLabel, selectedDueDate, selectedDueDateValue, selectedReminder, selectedReminderValue);

                Map<String, Object> itemValues = itemModel.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/users/items/" + todoId, itemValues);
                mDatabase.updateChildren(childUpdates);

                if (!selectedDueDate.equals("No Due Date")) {
                    // Set DueDate Notification
                    setDueDateNotification(selectedDueDateValue);
                }

                if (!selectedReminder.equals("No Reminder")) {
                    // Set Reminder Notification
                    setReminderNotification(selectedReminderValue);
                }

                // Finish ToodooNote Activity and go to MainActivity
                Intent intent = new Intent(ToodooNote.this, MainActivity.class);
                startActivity(intent);

                finish();

            } else {
                // Get a Toodoo item id
                todoId = mDatabase.child("users").child(mUserId).child("items").push().getKey();

                // Save Toodoo item to Firebase using the id
                final ItemModel itemModel = new ItemModel(todoId, todoItem, selectedLabel, selectedDueDate, selectedDueDateValue, selectedReminder, selectedReminderValue);

                mDatabase.child("users").child(mUserId).child("items").child(todoId).setValue(itemModel);

                if (!selectedDueDate.equals("No Due Date")) {
                    // Set DueDate Notification
                    setDueDateNotification(selectedDueDateValue);
                }

                if (!selectedReminder.equals("No Reminder")) {
                    // Set Reminder Notification
                    setReminderNotification(selectedReminderValue);
                }

                // Finish ToodooNote Activity and go to MainActivity
                Intent intent = new Intent(ToodooNote.this, MainActivity.class);
                startActivity(intent);

                finish();
            }
        } else {
            Toast.makeText(ToodooNote.this, "You have not imputed anything", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareOptionsData() {
        toodooOptionsList.add(new ToodooOptionsModel(R.drawable.ic_label, "Label", selectedLabel));
        toodooOptionsList.add(new ToodooOptionsModel(R.drawable.ic_event_due, "Due date", selectedDueDate));
        toodooOptionsList.add(new ToodooOptionsModel(R.drawable.ic_alarm, "Reminder", selectedReminder));

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doneFAB:
                try {
                    saveTodoItem();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            Toast.makeText(ToodooNote.this, "Cancelled", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        final SimpleDateFormat simpleDayFormat = new SimpleDateFormat("EEE", Locale.US);
        final String currentDate = simpleDateFormat.format(calendar.getTime());

        final ToodooDatePickerDialog datePickerDialog = new ToodooDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, day);
                selectedDueDateValue = newCalendar.getTime();
                String selectedDate = simpleDateFormat.format(selectedDueDateValue);
                selectedDueDate = simpleDayFormat.format(selectedDueDateValue) + ", " + selectedDate;

                calendar.add(Calendar.DATE, 1);
                if (selectedDate.equals(currentDate)) {
                    selectedDueDate = "Today, " + selectedDate;
                } else if (selectedDate.equals(simpleDateFormat.format(calendar.getTime()))) {
                    selectedDueDate = "Tomorrow, " + selectedDate;
                }

                ToodooOptionsModel ToodooOptionsModel = toodooOptionsList.get(1);
                ToodooOptionsModel.setOptionValue(selectedDueDate);
                mAdapter.notifyDataSetChanged();
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        setTitle("Select a date");
        datePickerDialog.show();
    }

    public class LabelDialog extends Dialog {
        private Context context;
        private ListView labelList = null;
        private Button cancel;

        public LabelDialog(Context context) {
            super(context);
            this.context = context;
            // TODO Auto-generated constructor stub
        }

        public LabelDialog(Context context, int theme) {
            super(context, theme);
            this.context = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.label_dialog);
            labelList = (ListView) findViewById(R.id.label_list);
            cancel = (Button) findViewById(R.id.cancel);
            // ListView
            SimpleAdapter adapter = new SimpleAdapter(context, getLabelList(),
                    R.layout.label_list_item, new String[]{"label_img",
                    "label_value"}, new int[]{
                    R.id.label_img, R.id.label_value});
            labelList.setAdapter(adapter);
            //ListView
            labelList
                    .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int position, long arg3) {
                            HashMap<String, Object> label = getLabelList().get(position);
                            selectedLabel = (String) label.get("label_value");

                            ToodooOptionsModel ToodooOptionsModel = toodooOptionsList.get(0);
                            ToodooOptionsModel.setOptionValue(selectedLabel);
                            mAdapter.notifyDataSetChanged();
                            labelDialog.dismiss();
                        }
                    });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    labelDialog.dismiss();
                }
            });
        }

        private List<HashMap<String, Object>> getLabelList() {
            List<HashMap<String, Object>> labelList = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> map1 = new HashMap<String, Object>();
            map1.put("label_img", R.drawable.ic_red_circle);
            map1.put("label_value", "Work");
            labelList.add(map1);
            HashMap<String, Object> map2 = new HashMap<String, Object>();
            map2.put("label_img", R.drawable.ic_blue_circle);
            map2.put("label_value", "Personal");
            labelList.add(map2);
            HashMap<String, Object> map3 = new HashMap<String, Object>();
            map3.put("label_img", R.drawable.ic_grey_circle);
            map3.put("label_value", "Study");
            labelList.add(map3);
            HashMap<String, Object> map4 = new HashMap<String, Object>();
            map4.put("label_img", R.drawable.ic_yellow_circle);
            map4.put("label_value", "Shopping");
            labelList.add(map4);
            HashMap<String, Object> map5 = new HashMap<String, Object>();
            map5.put("label_img", R.drawable.ic_green_circle);
            map5.put("label_value", "Daily");
            labelList.add(map5);

            return labelList;
        }
    }

    public class ReminderDialog extends Dialog {
        private Context context;
        private Button okay, cancel;
        private EditText reminderDateText, reminderTimeText;

        Calendar calendar = Calendar.getInstance();

        public ReminderDialog(Context context) {
            super(context);
            this.context = context;
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.reminder_dialog);
            reminderDateText = (EditText) findViewById(R.id.reminderDateEditText);
            reminderTimeText = (EditText) findViewById(R.id.reminderTimeEditText);
            okay = (Button) findViewById(R.id.okay);
            cancel = (Button) findViewById(R.id.cancel);

            selectedReminderValue = calendar.getTime();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            SimpleDateFormat simpleDayFormat = new SimpleDateFormat("EEE", Locale.US);
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

            String reminderDate = simpleDateFormat.format(selectedReminderValue);
            reminderDateValue = simpleDayFormat.format(selectedReminderValue) + ", " + reminderDate;
            reminderTimeValue = simpleTimeFormat.format(selectedReminderValue);

            if (calendar.getTime().equals(calendar.getTime())) {
                reminderDateText.setText("Today");
                reminderDateValue = "Today";
            }
            reminderTimeText.setText(reminderTimeValue);

            reminderDateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToodooNote.this.reminderDatePickerDialog();
                    reminderDialog.dismiss();
                }
            });
            reminderTimeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToodooNote.this.reminderTimePickerDialog();
                    reminderDialog.dismiss();
                }
            });

            okay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedReminder = reminderDateValue + " @ " + reminderTimeValue;

                    ToodooOptionsModel ToodooOptionsModel = toodooOptionsList.get(2);
                    ToodooOptionsModel.setOptionValue(selectedReminder);
                    mAdapter.notifyDataSetChanged();
                    reminderDialog.dismiss();
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reminderDialog.dismiss();
                }
            });
        }
    }

    public void reminderDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd", Locale.US);
        final SimpleDateFormat simpleDayFormat = new SimpleDateFormat("EEE", Locale.US);

        final ToodooDatePickerDialog datePickerDialog = new ToodooDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                reminderCalendar.set(year, month, day);
                selectedReminderValue = reminderCalendar.getTime();
                String reminderDate = simpleDateFormat.format(selectedReminderValue);
                reminderDateValue = simpleDayFormat.format(selectedReminderValue) + ", " + reminderDate;

                // Today's date "Today"
                final String todayDate = simpleDateFormat.format(calendar.getTime());

                // Add one day to calendar to make it "Tomorrow"
                calendar.add(Calendar.DATE, 1);
                final String tomorrowDate = simpleDateFormat.format(calendar.getTime());

                if (reminderDate.equals(todayDate)) {
                    reminderDateValue = "Today";
                } else if (reminderDate.equals(tomorrowDate)) {
                    reminderDateValue = "Tomorrow";
                }

                ToodooNote.this.reminderTimePickerDialog();
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        setTitle("Select a date");
        datePickerDialog.show();
    }

    public void reminderTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        final SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

        final ToodooTimePickerDialog timePickerDialog = new ToodooTimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hour);
                reminderCalendar.set(Calendar.MINUTE, minute);
                selectedReminderValue = reminderCalendar.getTime();
                reminderTimeValue = simpleTimeFormat.format(selectedReminderValue);

                selectedReminder = reminderDateValue + " @ " + reminderTimeValue;

                ToodooOptionsModel ToodooOptionsModel = toodooOptionsList.get(2);
                ToodooOptionsModel.setOptionValue(selectedReminder);
                mAdapter.notifyDataSetChanged();
            }
        }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    public void setDueDateNotification(Date selectedDate) {
        // Put in extra string into alarmIntent
        dueDateIntent.putExtra("dueDateStatus", "dueDate on");
        dueDateIntent.putExtra("todoItem", todoItem);
        dueDateIntent.putExtra("selectedDueDate", selectedDueDate);

        // Create a pending intent that delays the intent
        // until the specified calendar time
        pendingIntent = PendingIntent.getBroadcast(ToodooNote.this, 0,
                dueDateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the alarm manager
        if (selectedDate != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, selectedDate.getTime(),
                    pendingIntent);
        }
    }

    public void setReminderNotification(Date selectedDate) {
        // Put in extra string into alarmIntent
        reminderIntent.putExtra("reminderStatus", "reminder on");
        reminderIntent.putExtra("todoItem", todoItem);
        reminderIntent.putExtra("selectedDueDate", selectedDueDate);

        // Create a pending intent that delays the intent
        // until the specified calendar time
        pendingIntent = PendingIntent.getBroadcast(ToodooNote.this, 0,
                reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the alarm manager
        if (selectedDate != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, selectedDate.getTime(),
                    pendingIntent);
        }
    }

    public Date dateStringToDate(String dateString) throws ParseException {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        Date newDate = simpleDateFormat.parse(dateString);
        return newDate;
    }

    public Date timeStringToDate(String timeString) throws ParseException {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.US);

        Date newTime = simpleDateFormat.parse(timeString);
        return newTime;
    }

    public void transformDateValuesForUpdate() {
        int dueDateSplit = selectedDueDate.indexOf(",");
        String selectedDueDateSubString = selectedDueDate.substring(dueDateSplit + 1).trim();
        try {
            selectedDueDateValue = dateStringToDate(selectedDueDateSubString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int reminderSplit = selectedReminder.indexOf("@");
        String selectedReminderSubString = selectedReminder.substring(reminderSplit + 1).trim();
        try {
            selectedReminderValue = timeStringToDate(selectedReminderSubString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}