package com.example.yourscancart;

import android.util.Log;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Order implements Comparable<Order> {
    String delivery_date, delivery_status, order_id, image_url;
    final Pattern date_pattern = Pattern.compile("([0-9]*)-([0-9]*)-([0-9]*)");

    public Order(String delivery_date, String delivery_status, String order_id){
        this.delivery_date = delivery_date;
        this.delivery_status = delivery_status;
        this.order_id = order_id;
        this.image_url = image_url;
    }

    public String get_delivery_date() {
        return delivery_date;
    }

    public void set_delivery_date(String delivery_date) {
        this.delivery_date = delivery_date;
    }

    public String get_delivery_status() {
        return delivery_status;
    }

    public void set_delivery_status(String delivery_status) {
        this.delivery_status = delivery_status;
    }

    public Date extract_date() throws ParseException {
        Matcher matcher = date_pattern.matcher(this.delivery_date);
        if (!matcher.find()) {
            throw new ParseException(this.delivery_date, 0);
        }

        String date_string = matcher.group(0);
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
        //Parsing the given String to Date object
        if (date_string == null) {
            throw new ParseException(this.delivery_date, 1);
        }

        try {
            Date date = formatter.parse(date_string);
            if (date == null) {
                throw new ParseException(this.delivery_date, 2);
            }

            return date;
        } catch (ParseException e) {
            Log.d("INVALID-DATE", this.delivery_date);
            throw e;
        }
    }

    @Override
    public int compareTo(Order order) {
        // Matcher matcher = date_pattern.matcher(order.delivery_date);
        Date current_date, other_date;

        try {
            other_date = order.extract_date();
        } catch (ParseException e) {
            // current order is considered bigger
            return 1;
        }

        try {
            current_date = this.extract_date();
        } catch (ParseException e) {
            // current order is considered bigger
            return -1;
        }

        return current_date.compareTo(other_date);
    }
}
