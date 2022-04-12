package com.example.charles_nfc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrdersAdapter extends ArrayAdapter<Order>{
    final Pattern date_pattern = Pattern.compile("([0-9]*)-([0-9]*)-([0-9]*)");

    public OrdersAdapter(Context context, ArrayList<Order> orders){
        super(context, R.layout.list_item, orders);
    }

    @NonNull
    @Override
    public View getView(
        int position, @Nullable View convertView, @NonNull ViewGroup parent
    ) {
        Order order = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater
                .from(getContext())
                .inflate(R.layout.list_item, parent, false);
        }

        TextView date = convertView.findViewById(R.id.date);
        TextView time = convertView.findViewById(R.id.delivery_status);
        TextView id = convertView.findViewById(R.id.order_id);
        ImageView item = convertView.findViewById(R.id.firstorder);

        Matcher matcher = date_pattern.matcher(order.delivery_date);
        if (!matcher.find()) {
            date.setText(order.delivery_date);
        } else {
            String date_string = matcher.group(0);
            date.setText(date_string);
        }

        time.setText(order.delivery_status);
        id.setText(order.order_id);

        return convertView;
    }
}
