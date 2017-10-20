package com.olayinkapeter.toodoo.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Olayinka_Peter on 11/9/2016.
 */
public class ItemModel {
    private String id, item, label, dueDate, reminder;
    private Date dueDateValue, reminderValue;

    public ItemModel() {}

    public ItemModel(String id, String item, String label, String dueDate, Date dueDateValue, String reminder, Date reminderValue) {
        this.id = id;
        this.item = item;
        this.label = label;
        this.dueDate = dueDate;
        this.dueDateValue = dueDateValue;
        this.reminder = reminder;
        this.reminderValue = reminderValue;
    }

    public ItemModel(String id, String item, String label, String dueDate, String reminder) {
        this.id = id;
        this.item = item;
        this.label = label;
        this.dueDate = dueDate;
        this.reminder = reminder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getitem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Date getDueDateValue() {
        return dueDateValue;
    }

    public void setDueDateValue(Date dueDateValue) {
        this.dueDateValue = dueDateValue;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public Date getReminderValue() {
        return reminderValue;
    }

    public void setReminderValue(Date reminderValue) {
        this.reminderValue = reminderValue;
    }

    public Map<String,Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("item", item);
        result.put("label", label);
        result.put("dueDate", dueDate);
        result.put("dueDateValue", dueDateValue);
        result.put("dueDate", reminder);
        result.put("reminderValue", reminderValue);

        return result;
    }
}
