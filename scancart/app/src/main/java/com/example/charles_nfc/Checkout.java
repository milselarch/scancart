package com.example.charles_nfc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Checkout extends AppCompatActivity {
    Button select_date_and_time, edit_address, return_shopping_cart,place_order;
    TextView text_address, text_date, text_time;
    String str_date, str_time, orderID;
    ArrayList<ShoppingCartItemModel> shopping_cart;
    List<Map> shopping_cart_array;
    double total_cost;

    final int REQUEST_CODE_DATETIME = 1000;
    private static final String TAG = "Checkout";

    //TODO: Remove this eventually
    private final Integer userID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);
        Intent intent = getIntent();

        text_address = findViewById(R.id.address);
        text_date = findViewById(R.id.date);
        text_time= findViewById(R.id.time);

        select_date_and_time = findViewById(R.id.select_date_and_time);
        edit_address = findViewById(R.id.editAddress);
        return_shopping_cart = findViewById(R.id.return_shopping_cart);
        place_order = findViewById(R.id.place_order);

        // Return to Shopping Cart
        return_shopping_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main_intent = new Intent(Checkout.this, ShoppingCartActivity.class);
                startActivity(main_intent);
            }
        });

        text_address.setText("Address:");
        text_date.setText("Delivery Date: -");
        text_time.setText("Delivery Time: -");

        // Edit Address
        edit_address.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(Checkout.this,EditAddress.class));
            }
        }));

        // Select Date and Time
        select_date_and_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent datetime_intent = new Intent(Checkout.this, SelectTiming.class);
                startActivityForResult(datetime_intent,1000);
            }
        });

        // Place Order
        place_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (str_date == null && str_time == null) {
                    Toast.makeText(
                        getApplicationContext(),
                        "Date and Time not selected",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    total_cost = calculate_total_cost(shopping_cart);
                    orderID = String.valueOf(
                        UUID.randomUUID().getLeastSignificantBits()
                    );

                    FirebaseHandler.FireCallback callback = new FirebaseHandler.FireCallback() {
                        @Override
                        public void callback(Object result) {
                            //Todo Intent to delivery page?
                            Intent main_intent = new Intent(Checkout.this, ShoppingCartActivity.class);
                            startActivity(main_intent);
                        }
                    };

                    FirebaseCheckOut(
                        str_date, str_time, shopping_cart_array, userID, orderID, total_cost,
                        callback
                    );
                }

            }
        });

        shopping_cart = intent.getParcelableArrayListExtra("shopping_cart");
        shopping_cart_array = shopping_cart_firebase(shopping_cart);
    }

    // Calculate total cost of items in Shopping Cart
    public Double calculate_total_cost(ArrayList<ShoppingCartItemModel> cart) {
        BigDecimal total_cost = new BigDecimal(0.0);
        for (ShoppingCartItemModel item: cart) {
            BigDecimal cost = new BigDecimal(item.cost);
            BigDecimal quantity = new BigDecimal(item.quantity);
            total_cost = total_cost.add(cost.multiply(quantity));
        }
        return total_cost.doubleValue();
    }

    public List<Map> shopping_cart_firebase(ArrayList<ShoppingCartItemModel> cart) {
        List<Map> shopping_cart_list = new ArrayList<>();
        for (ShoppingCartItemModel item: cart) {
            shopping_cart_list.add(item.toMap());
        }
        return shopping_cart_list;
    }

    // Save the user's current shopping cart state
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("shopping_cart", shopping_cart);
        super.onSaveInstanceState(savedInstanceState);
    }

    // Restore shopping cart items from saved instance (during onStart)
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        shopping_cart = (ArrayList<ShoppingCartItemModel>) savedInstanceState.<ShoppingCartItemModel>getParcelableArrayList("shopping_cart");
    }

    @Override
    protected void onActivityResult (
        int requestCode, int resultCode, Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DATETIME) {
            if (resultCode == Activity.RESULT_OK) {
                str_date = data.getStringExtra(SelectTiming.date_key);
                str_time = data.getStringExtra(SelectTiming.time_key);
                text_date.setText("Delivery Date: " + str_date);
                text_time.setText("Delivery Time: " + str_time);
            }
        }
    }

    protected void FirebaseCheckOut(
            String str_date,
            String str_time,
            List<Map> shopping_cart_array,
            int userID,
            String orderID,
            double total_cost,
            FirebaseHandler.FireCallback callback
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cart = db.collection("completed_orders");

        Map<String, Object> docData = new HashMap<>();
        docData.put("delivery_date",str_date);
        docData.put("delivery_time",str_time);
        docData.put("delivery_status","Delivery in Progress");
        docData.put("items", shopping_cart_array);
        docData.put("user_id", userID);
        docData.put("order_id", orderID);
        docData.put("total_cost", total_cost);

        cart.add(docData)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.e(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error writing document", e);
                }
            });

            Map<String,Object> checkOutList = new HashMap<>();

            db.collection("shopping_cart")
                .whereEqualTo("user_id", userID)
                .get()
                .addOnSuccessListener((querySnapshot) -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                        .addOnSuccessListener((result) -> {
                            Log.i(TAG, "All items have been removed.");
                            callback.callback(null);
                        })
                        .addOnFailureListener((error) -> {
                            Log.e(TAG, "Failed to remove all items.", error);
                        });
                })
                .addOnFailureListener((error) -> {
                    Log.e(TAG, "Failed to get your cart items.", error);
                });
    }
}

