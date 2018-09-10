package edu.uic.skatha2.clientapplication;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import edu.uic.skatha2.services.DailyCash;

/**
 * Created by sabask on 4/28/18.
 */

// Adapter for ListView
public class DataAdapter extends BaseAdapter{

    Parcelable[] data;
    Context context;
    public DataAdapter(Context context, Parcelable[] data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return data[position].hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                inflate(R.layout.list_items, parent, false);
        }

        TextView id = (TextView) convertView.findViewById(R.id.id);
        TextView year = (TextView) convertView.findViewById(R.id.year);
        TextView month = (TextView) convertView.findViewById(R.id.month);
        TextView day = (TextView) convertView.findViewById(R.id.day);
        TextView dayOfWeek = (TextView) convertView.findViewById(R.id.day_of_week);
        TextView cash = (TextView) convertView.findViewById(R.id.cash);

        DailyCash dailyCash = (DailyCash) data[position];
        id.setText(String.valueOf(position+1));
        year.setText(String.valueOf(dailyCash.getmYear()));
        month.setText(String.valueOf(dailyCash.getmMonth()));
        day.setText(String.valueOf(dailyCash.getmDay()));
        dayOfWeek.setText(String.valueOf(dailyCash.getmDayOfWeek()));
        cash.setText(String.valueOf(dailyCash.getmCash()));

        return convertView;
    }
}
