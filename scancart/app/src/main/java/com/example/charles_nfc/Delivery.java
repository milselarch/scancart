package com.example.charles_nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    ArrayList<Order> orders;
    private final UserAccount account = UserAccount.getAccount();
    private FirebaseAuth firebaseAuth;
    private Integer userID;

    void loadUser() {
        if (!account.isLoggedIn()) {
            Activity activity = getActivity();
            if (activity == null) { return; }
            account.logout(activity.getApplicationContext());
            firebaseAuth.signOut();
            startActivity(new Intent(
                    activity, MainActivity.class
            ));
        } else {
            userID = account.getUserID();
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);
        this.loadUser();

        orders = new ArrayList<Order>();

        db = FirebaseHandler.getInstanceDatabase();
        CollectionReference orders_completed = db.collection("completed_orders");
        int userID = account.getUserID();
        Query query = orders_completed.whereEqualTo(
                "user_id", userID
        );

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
                assert orders != null;

                Context context = getContext();
                if (context == null) { return; }

                OrdersAdapter ordersAdapter = new OrdersAdapter(context, orders);
                orderlist.setAdapter(ordersAdapter);
                orderlist.setClickable(true);

                orderlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        Order order = (Order) adapterView.getItemAtPosition(position);
                        String order_id = order.order_id;
                        Log.d("ORDER_ID SELECTED", order_id);
                        Log.d("ORDER_DATE", order.delivery_date);

                        Intent intent = new Intent(
                                getActivity(), GroceryList.class
                        );

                        intent.putExtra("order_id", order_id );
                        startActivity(intent);
                    }
                });
            }
        });

        return inflater.inflate(
                R.layout.fragment_delivery, container, false
        );
    }
}