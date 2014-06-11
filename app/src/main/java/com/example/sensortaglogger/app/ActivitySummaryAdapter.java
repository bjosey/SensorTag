package com.example.sensortaglogger.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Brendan on 10/06/2014.
 */
public class ActivitySummaryAdapter extends BaseAdapter {

    private ArrayList<BehaviourSummary> summaries;
    private LayoutInflater mInflater;

    private Integer[] imgid = {
        R.drawable.sitting_icon,
        R.drawable.standing_icon,
        R.drawable.walking_icon,
        R.drawable.running_icon
    };

    public ActivitySummaryAdapter(Context context, ArrayList<BehaviourSummary> summaries) {
        this.summaries = summaries;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return summaries.size();
    }

    public Object getItem(int position) {
        return summaries.get(position);
    }

    public long getItemId(int position) {
        return position;
    }



    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_row_view, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.activityName);
            holder.txtLength = (TextView) convertView.findViewById(R.id.activityTime);
            holder.txtSteps = (TextView) convertView.findViewById(R.id.activitySteps);
            holder.imgPhoto = (ImageView) convertView.findViewById(R.id.photo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtName.setText(summaries.get(position).getName());
        holder.txtLength.setText(summaries.get(position).getLength());
        holder.txtSteps.setText(summaries.get(position).getNumSteps());
        holder.imgPhoto.setImageResource(imgid[position]);

        return convertView;
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtLength;
        TextView txtSteps;
        ImageView imgPhoto;
    }

}
