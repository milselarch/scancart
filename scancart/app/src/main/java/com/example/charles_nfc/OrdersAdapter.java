package com.example.charles_nfc;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;
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
        TextView numItemsView = convertView.findViewById(R.id.order_num_items);

        Promise<
            Pair<Bitmap, Integer>, Pair<Bitmap, Integer>, Object
        > loadImagePromise = order.asyncLoadImage();

        loadImagePromise.then(new Promise<
            Pair<Bitmap, Integer>, Object, Object
        >() {
            @Override
            public void onReady(
                Pair<Bitmap, Integer> result,
                Resolver<Object> resolver
            ) {
                Bitmap image = result.first;
                Integer orderItems = result.second;

                assert image != null;
                Log.d("RESIKVED_IMG", "asd");
                item.setImageBitmap(image);
                if (orderItems > 1) {
                    int hiddenItems = orderItems - 1;
                    numItemsView.setText(String.format(
                        "+%s", Integer.toString(hiddenItems)
                    ));
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.e("URL_LOAD_FAIL", error.toString());
            }
        });

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
