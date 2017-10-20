package com.olayinkapeter.toodoo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olayinkapeter.toodoo.R;
import com.olayinkapeter.toodoo.models.ToodooOptionsModel;

import java.util.List;

/**
 * Created by Olayinka_Peter on 6/19/2016.
 */
public class ToodooOptionsAdapter extends RecyclerView.Adapter<ToodooOptionsAdapter.MyViewHolder> {

    private List<ToodooOptionsModel> toodooOptionsList;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView optionIcon;
        public TextView optionTitle, optionValue;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            optionIcon = (ImageView) itemView.findViewById(R.id.option_icon);
            optionTitle = (TextView) itemView.findViewById(R.id.option_title);
            optionValue = (TextView) itemView.findViewById(R.id.option_value);
        }
    }

    public ToodooOptionsAdapter(List<ToodooOptionsModel> toodooOptionsList) {
        this.toodooOptionsList = toodooOptionsList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.toodoo_options_item, parent, false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ToodooOptionsModel ToodooOptionsModel = toodooOptionsList.get(position);
        holder.optionIcon.setImageResource(ToodooOptionsModel.getOptionIcon());
        holder.optionTitle.setText(ToodooOptionsModel.getOptionTitle());
        holder.optionValue.setText(ToodooOptionsModel.getOptionValue());
    }

    @Override
    public int getItemCount() {
        return toodooOptionsList.size();
    }
}
