package com.example.charles_nfc;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Order implements Comparable<Order> {
    String delivery_date, delivery_status, order_id, image_url;
    final Pattern date_pattern = Pattern.compile("([0-9]*)-([0-9]*)-([0-9]*)");
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Order(String delivery_date, String delivery_status, String order_id){
        this.delivery_date = delivery_date;
        this.delivery_status = delivery_status;
        this.order_id = order_id;
    }

    public Promise<
        Pair<Bitmap, Integer>, Pair<Bitmap, Integer>, Object
    > asyncLoadImage() {
        Promise<
            Pair<Bitmap, Integer>, Pair<Bitmap, Integer>, Object
        > promise = new Promise<
            Pair<Bitmap, Integer>, Pair<Bitmap, Integer>, Object
        >() {
            @Override
            public void onReady(
                Pair<Bitmap, Integer> result,
                Resolver<Pair<Bitmap, Integer>> resolver
            ) {
                resolver.resolve(result);
            }
            @Override
            public void onError(Throwable error) { }
        };

        CollectionReference orders_completed = db.collection(
            "completed_orders"
        );
        Query query = orders_completed.whereEqualTo(
            "order_id", order_id
        );

        Resolvable<
            Pair<Bitmap, Integer>, Pair<Bitmap, Integer>
        >.Resolver<
            Pair<Bitmap, Integer>
        > resolver = promise.getResolver();

        query.limit(1).get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("RESOLVE_COMPLET","COMPLETE");
                if (!task.isSuccessful()) {
                    resolver.reject(new ParseException("Firebase call failed", 1));
                    return;
                }

                String imageUrl = null;
                QuerySnapshot snapshot = task.getResult();
                int numItems = 0;

                for (DocumentSnapshot document: snapshot) {
                    List<?> list = (List<?>) document.get("items");
                    Log.d("DOC_DATA", document.toString());
                    if (list == null) {
                        resolver.reject(new ParseException("empty firestore list", 1));
                        return;
                    }

                    numItems = list.size();
                    for (Object item: list) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> mapping = (Map<String, Object>) item;
                        imageUrl = (String) mapping.get("image_url");
                        break;
                    }

                    break;
                }

                if (imageUrl == null) {
                    resolver.reject(new ParseException("image url is null", 1));
                    return;
                }

                final Integer orderItems = numItems;
                final String finalUrl = imageUrl;
                // Log.d("RESOLVED_URL", finalUrl);

                new DownloadImageTask(new DownloadImageTask.customListener() {
                    @Override
                    public void postExecute(Bitmap image) {
                        resolver.resolve(new Pair<>(image, orderItems));
                    }
                }).execute(imageUrl);
            }
        });

        return promise;
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
