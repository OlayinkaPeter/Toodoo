package com.olayinkapeter.toodoo.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.olayinkapeter.toodoo.R;
import com.olayinkapeter.toodoo.models.ToodooListModel;
import com.olayinkapeter.toodoo.toodooOptions.ToodooNote;

import java.util.List;

/**
 * Created by Olayinka_Peter on 2016-12-29.
 */

public class ToodooListAdapter extends RecyclerView.Adapter<ToodooListAdapter.MyViewHolder> {

    private List<ToodooListModel> toodooList;
    private Context context;

    private String IS_EXISTING_KEY = "IS_EXISTING_KEY", TODO_ID_KEY = "TODO_ID_KEY", TODO_ITEM_KEY = "TODO_ITEM_KEY", TODO_LABEL_KEY = "TODO_LABEL_KEY", TODO_DUEDATE_KEY = "TODO_DUEDATE_KEY", TODO_REMINDER_KEY = "TODO_REMINDER_KEY";

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView todoItem, todoLabel, todoDueDate, todoReminder;
        public ImageView iconLabel;
        public LinearLayout layoutMain, layoutBackground, layoutDueDate, layoutLabel, layoutReminder;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            layoutBackground = (LinearLayout) itemView.findViewById(R.id.main_background_view);
            layoutMain = (LinearLayout) itemView.findViewById(R.id.main_view);
            layoutDueDate = (LinearLayout) itemView.findViewById(R.id.layout_duedate);
            layoutLabel = (LinearLayout) itemView.findViewById(R.id.layout_label);
            layoutReminder = (LinearLayout) itemView.findViewById(R.id.layout_reminder);
            todoItem = (TextView) itemView.findViewById(R.id.todoTask);
            todoLabel = (TextView) itemView.findViewById(R.id.todoLabel);
            todoDueDate = (TextView) itemView.findViewById(R.id.todoDueDate);
            todoReminder = (TextView) itemView.findViewById(R.id.todoTime);
            iconLabel = (ImageView) itemView.findViewById(R.id.iconLabel);
        }
    }

    public ToodooListAdapter(List<ToodooListModel> toodooList, Context context) {
        this.toodooList = toodooList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.toodoo_list_item, parent, false);

        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        ToodooListModel ToodooListModel = toodooList.get(position);

        if (ToodooListModel.getTodoItem().length() > 40) {
            String shortenedTodoItem = ToodooListModel.getTodoItem().substring(0, 40) + "...";
            holder.todoItem.setText(shortenedTodoItem);
        } else {
            holder.todoItem.setText(ToodooListModel.getTodoItem());
        }

        holder.todoLabel.setText(ToodooListModel.getTodoLabel());
        switch (ToodooListModel.getTodoLabel()) {
            case "Work":
                holder.iconLabel.setImageResource(R.drawable.ic_red_circle);
                break;
            case "Personal":
                holder.iconLabel.setImageResource(R.drawable.ic_blue_circle);
                break;
            case "Study":
                holder.iconLabel.setImageResource(R.drawable.ic_grey_circle);
                break;
            case "Shopping":
                holder.iconLabel.setImageResource(R.drawable.ic_yellow_circle);
                break;
            case "Daily":
                holder.iconLabel.setImageResource(R.drawable.ic_green_circle);
                break;
            case "None":
                holder.iconLabel.setImageResource(R.drawable.ic_transparent_circle);
                holder.todoLabel.setText(" ");
        }

        if (ToodooListModel.getTodoDueDate().contains("Today") || ToodooListModel.getTodoDueDate().contains("Tomorrow")) {
            int splitter = ToodooListModel.getTodoDueDate().indexOf(",");
            String splitTodoDueDate = ToodooListModel.getTodoDueDate().substring(0, splitter).trim();
            holder.todoDueDate.setText(splitTodoDueDate);
        } else {
            holder.todoDueDate.setText(ToodooListModel.getTodoDueDate());
        }

        holder.todoReminder.setText(ToodooListModel.getTodoReminder());

        holder.layoutBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Restored", Toast.LENGTH_SHORT).show();
            }
        });

        holder.layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToodooListModel ToodooListModel = toodooList.get(position);

                Intent intent = new Intent(context, ToodooNote.class);
                intent.putExtra(IS_EXISTING_KEY, "true");
                intent.putExtra(TODO_ID_KEY, ToodooListModel.getTodoId());
                intent.putExtra(TODO_ITEM_KEY, ToodooListModel.getTodoItem());
                intent.putExtra(TODO_LABEL_KEY, ToodooListModel.getTodoLabel());
                intent.putExtra(TODO_DUEDATE_KEY, ToodooListModel.getTodoDueDate());
                intent.putExtra(TODO_REMINDER_KEY, ToodooListModel.getTodoReminder());

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return toodooList.size();
    }
}
