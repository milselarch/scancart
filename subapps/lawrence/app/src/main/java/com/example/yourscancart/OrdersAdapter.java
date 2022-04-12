package com.example.yourscancart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class OrdersAdapter extends ArrayAdapter<Order>{
    public OrdersAdapter(Context context, ArrayList<Order> orders){
        super(context, R.layout.list_item,orders);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Order order = getItem(position);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent,false);
        TextView date = convertView.findViewById(R.id.date);
        TextView time = convertView.findViewById(R.id.delivery_status);
        TextView id = convertView.findViewById(R.id.order_id);
        ImageView item = convertView.findViewById(R.id.firstorder);

        date.setText(order.delivery_date);
        time.setText(order.delivery_status);
        id.setText(order.order_id);

        return convertView;
    }
}
