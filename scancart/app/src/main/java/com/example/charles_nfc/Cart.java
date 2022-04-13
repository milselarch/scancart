package com.example.charles_nfc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Cart extends Fragment {
    ArrayList<ShoppingCartItemModel> shopping_cart = new ArrayList<>();
    RecyclerView mRecyclerView;
    Button checkoutButton;
    TextView cart_total;
    String cost_string;

    //TODO: Remove this eventually
    private final Integer userID = 1;
    private static final String TAG = "ShoppingCart";

    public void onCreate() {
        Log.e(TAG, "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        loadItems();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        Log.d("FRAGMENT_CREATE", "cart");

        return inflater.inflate(
            R.layout.shopping_cart_main, container, false
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void loadItems() {
        super.onStart();
        View currentView = getView();
        if (currentView == null) { return; }

        ImageView footerView = currentView.findViewById(R.id.cart_footer);
        footerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // prevent buttons below footer from being clicked
                // by not propagating click event downwards
                return true;
            }
        });

        mRecyclerView = currentView.findViewById(R.id.shopping_cart_items);
        checkoutButton = currentView.findViewById(R.id.checkoutbtn);
        cart_total = currentView.findViewById(R.id.cart_total);

        shopping_cart.clear();
        ShoppingCartAdapterClass adapter = new ShoppingCartAdapterClass(shopping_cart);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
            this.getContext(), RecyclerView.VERTICAL, false
        ));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Context context = getContext();

        db.collection("shopping_cart")
            .whereEqualTo("user_id", userID)
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(
                    @Nullable QuerySnapshot value,
                    @Nullable FirebaseFirestoreException e
                ) {
                    if (e != null) {
                        Log.w(TAG, "Failed to read shopping cart.", e);
                        Toast.makeText(
                            context,
                            "Unable to retrieve items in shopping cart.",
                            Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    shopping_cart = new ArrayList<>();
                    if (value.isEmpty()) {
                        ShoppingCartAdapterClass adapter = new ShoppingCartAdapterClass(shopping_cart);
                        mRecyclerView.setAdapter(adapter);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                            context, RecyclerView.VERTICAL, false
                        ));

                        cost_string = "$ " + String.format("%.2f", calculate_total_cost(shopping_cart));
                        cart_total.setText(cost_string);
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        Map<String, Object> docData = doc.getData();
                        String tag_id = (String) docData.get("tag_id");
                        long quantity = (long) docData.get("quantity");

                        db.collection("inventory")
                            .whereEqualTo("tag_id", tag_id)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());

                                            Map<String, Object> documentData = document.getData();
                                            String name = (String) documentData.get("display_name");
                                            double cost = (double) documentData.get("price");
                                            String image_url = (String) documentData.get("image_url");

                                            shopping_cart.add(new ShoppingCartItemModel(tag_id, name, quantity, cost, image_url));
                                        }
                                    } else {
                                        Log.w(TAG, "Failed to find item in Inventory", task.getException());
                                        Toast.makeText(
                                            context,
                                            "Item in cart not found in Inventory",
                                            Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    ShoppingCartAdapterClass adapter = new ShoppingCartAdapterClass(shopping_cart);
                                    adapter.sort();

                                    mRecyclerView.setAdapter(adapter);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(
                                        context, RecyclerView.VERTICAL, false
                                    ));

                                    cost_string = "$ " + String.format("%.2f",calculate_total_cost(shopping_cart));
                                    cart_total.setText(cost_string);
                                    Log.d("FIREBASE DEL", shopping_cart.toString());
                                }
                            }
                        );
                    }
                }
            }
        );

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (shopping_cart.isEmpty()) {
                    Toast.makeText(
                        context,
                        "There are no items in shopping cart!",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    // navigate to checkout
                    Intent intent = new Intent(getActivity(), Checkout.class);
                    intent.putParcelableArrayListExtra("shopping_cart", shopping_cart);
                    startActivity(intent);
                }
            }
        });

        Log.e(TAG, "onStart");
    }

    // Save the user's current shopping cart state
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("shopping_cart", shopping_cart);
        savedInstanceState.putString("cost_string", cost_string);
        super.onSaveInstanceState(savedInstanceState);
    }

    /*
    // Restore shopping cart items from saved instance (during onStart)
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Call the superclass so it can restore the view hierarchy
        Log.d("RESTORE", "test");
        super.onRestoreInstanceState(savedInstanceState);
        shopping_cart = (ArrayList<ShoppingCartItemModel>) savedInstanceState.<ShoppingCartItemModel>getParcelableArrayList("shopping_cart");
        cost_string = savedInstanceState.getString("cost_string");
    }
    */

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
}