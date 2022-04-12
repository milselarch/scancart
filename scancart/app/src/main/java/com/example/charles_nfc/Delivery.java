package com.example.charles_nfc;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Delivery extends Fragment {
    FirebaseFirestore db;
    private final Integer userID = 1;
    ArrayList<Order> orders = new ArrayList<Order>();

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        CollectionReference orders_completed = db.collection("completed_orders");
        Query query = orders_completed.whereEqualTo("user_id", userID);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(
                        getContext(),
                        "Failed to load completed orders",
                        Toast.LENGTH_SHORT
                    ).show();
                }

                for (QueryDocumentSnapshot document: task.getResult()) {
                    // Log.d("DOCUMENT", document.toString());
                    Map<String, Object> documentData = document.getData();
                    String order_date = (String) documentData.get("delivery_date");
                    String order_status = (String) documentData.get("delivery_status");
                    String order_id = (String) documentData.get("order_id");
                    com.example.charles_nfc.Order order = new com.example.charles_nfc.Order(order_date, order_status, order_id);
                    orders.add(order);
                }

                Collections.sort(orders);
                Collections.reverse(orders);
                ListView orderlist = container.findViewById(R.id.orderlist);
                assert orders != null;

                Context context = getContext();
                if (context == null) { return; }

                OrdersAdapter ordersAdapter = new OrdersAdapter(context, orders);
                orderlist.setAdapter(ordersAdapter);
                orderlist.setClickable(true);
            }
        });

        return inflater.inflate(R.layout.fragment_delivery, container, false);
    }
}