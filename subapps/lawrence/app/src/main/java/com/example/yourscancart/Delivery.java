package com.example.yourscancart;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        ArrayList<Order> orders = new ArrayList<>();
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
                    Order order = new Order(order_date, order_status, order_id);
                    orders.add(order);
                }

                Collections.sort(orders);
                Collections.reverse(orders);
                ListView orderlist = container.findViewById(R.id.orderlist);
                OrdersAdapter ordersAdapter = new OrdersAdapter(getContext(), orders);
                orderlist.setAdapter(ordersAdapter);
                orderlist.setClickable(true);
            }
        });


        return inflater.inflate(R.layout.fragment_delivery, container, false);
    }
}